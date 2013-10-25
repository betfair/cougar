#!/bin/bash
# Copyright 2013, The Sporting Exchange Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

RUN_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

HEAP_UPDATES_PER_TEST=100000
SERVER_THREADS=10
EV_POOL_SIZE=10
SUBSCRIPTION_POOL_SIZE=10

# how many heaps to have on the server
# these run out memory due to the way the tests work - changing the tests to drip feed in updates (rather than sticking
# 5000000 into memory to be processed) would be more representative
#NUM_HEAP_VARIATIONS="50 66 100 1000 10000"
NUM_HEAP_VARIATIONS="5 10 20 30 40"
NUM_CLIENT_VARIATIONS="1 3 5"
PUSHER_PULLER_VARIATIONS="1 5 10 25 50 100"


stopClients() {
    echo "Stopping clients"
    # Now stop those clients (to reconfigure threads)
    for pid in `jps -m | grep "PushMain" | awk '{ print $1 }'`; do
      kill $pid
    done
}

rm -f $RUN_DIR/pushTesting.log

# First deploy the latest code
rm -rf /tmp/socket-perf-testing-*
cp socket-perf-testing-service/socket-perf-testing-launcher/target/socket-perf-testing-launcher-2.6-push-SNAPSHOT-deploy.tar.gz /tmp
cp socket-perf-testing-client/target/socket-perf-testing-client-2.6-push-SNAPSHOT-deploy.tar.gz /tmp
cd /tmp
tar xfz socket-perf-testing-client-2.6-push-SNAPSHOT-deploy.tar.gz
tar xfz socket-perf-testing-launcher-2.6-push-SNAPSHOT-deploy.tar.gz

for NUM_HEAPS in `echo $NUM_HEAP_VARIATIONS`; do

    for NUM_CLIENTS in `echo $NUM_CLIENT_VARIATIONS`; do

        for PUSHER_PULLER_THREADS in `echo $PUSHER_PULLER_VARIATIONS`; do

            echo "Starting server with $SERVER_THREADS threads"
            # Now start the server
            mkdir -p /tmp/socket-perf-testing-server/bin/logs
            # first 4 params are for push tests only, so ignore for now
            SERVER_PARAMS="$SERVER_THREADS $NUM_HEAPS $HEAP_UPDATES_PER_TEST $PUSHER_PULLER_THREADS $EV_POOL_SIZE"
            cd /tmp/socket-perf-testing-server/bin
            ./socketTestServer.sh $SERVER_PARAMS > logs/stdout.log &
            while ( ! grep -q "COUGAR HAS STARTED" logs/stdout.log ); do
              echo "Waiting for server to start"
              sleep 1
            done
            SERVER_PID=`jps -m | grep "Main $SERVER_PARAMS" | awk '{print $1}'`

            WORKED_THIS_RUN="false"
            while [ $WORKED_THIS_RUN == "false" ]; do
                # assume it's going to work until it fails
                WORKED_THIS_RUN="true"

                echo "Starting $NUM_CLIENTS clients"
                # So for starting many clients
                for ((c=1; c<=$NUM_CLIENTS; c++)); do
                  CLIENT_NAME="client$c"
                  cd /tmp/socket-perf-testing-client/bin
                  ./pushTestClient.sh $CLIENT_NAME localhost:9003 > /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log 2>&1 &
                  echo "Waiting for $CLIENT_NAME to start"
                  while ( ! grep -q "READY" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log ); do
                    grep -q "This Client is not connected to a server so this call" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                    if [ $? -eq 0 ]; then
                      echo "$CLIENT_NAME failed to start properly, restarting.."
                      PID=`jps -m | grep "PushMain $CLIENT_NAME" | awk '{ print $1 }'`
                      kill -9 $PID
                      ./pushTestClient.sh $CLIENT_NAME localhost:9003 > /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log 2>&1 &
                    else
                        echo "Waiting for $CLIENT_NAME to start"
                        sleep 1
                    fi
                  done
                done

                echo -n "Running test ($NUM_HEAPS/$NUM_CLIENTS).."
                # Now, logic for running a test
                cd /tmp/socket-perf-testing-server/bin
                ./startPushTest.sh
                echo "done"

                # Now, report on results
                MAX_TIME=0
                MAX_SUB_TIME=0
                for ((c=1; c<=$NUM_CLIENTS; c++)); do
                    CLIENT_NAME="client$c"
                    while ( ! grep -q "COMPLETED" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log ); do
                        grep -q "Connectivity to remote server lost!" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                        if [ $? -eq 0 ]; then
                            echo "[ERROR]: $CLIENT_NAME failed, result will be invalid for this test"
                            echo "[$CLIENT_NAME]: COMPLETED AT 0 TOOK 0ms @ 0 tps and 0 ms/call" >> /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                            WORKED_THIS_RUN="false"
                            break
                        else
                            echo "Waiting for $CLIENT_NAME to complete"
                            sleep 1
                        fi
                    done
                    grep -q "Expected" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                    if [ $? -eq 0 ]; then
                        echo "[ERROR]: $CLIENT_NAME failed, result will be invalid for this test"
                        WORKED_THIS_RUN="false"
                    fi
                    if [ $WORKED_THIS_RUN == "true" ]; then
                        # System.out.println("["+name+"]: COMPLETED AT "+endMillis+" TOOK "+(endMillis-startMillis)+" ms");
                        END_LINE=`cat /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log | grep "COMPLETED" | tail -1`
                        echo "$CLIENT_NAME result: $END_LINE"
                        TOTAL_TIME=`echo "$END_LINE" | awk '{print $6}'`
                        if [ $TOTAL_TIME -gt $MAX_TIME ]; then
                            MAX_TIME=$TOTAL_TIME
                        fi


                        # System.out.println("["+name+"]: SUBSCRIPTIONS TOOK "+(subEnd-subStart)+" ms");
                        END_LINE=`cat /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log | grep "SUBSCRIPTIONS TOOK" | tail -1`
                        echo "$CLIENT_NAME sub result: $END_LINE"
                        SUB_TIME=`echo "$END_LINE" | awk '{print $4}'`
                        if [[ $SUB_TIME -gt $MAX_SUB_TIME ]]; then
                            MAX_SUB_TIME=$SUB_TIME
                        fi
                    fi
                done
                if [ $WORKED_THIS_RUN == "true" ]; then
                    LATENCY=`echo "scale=4; $MAX_TIME/$HEAP_UPDATES_PER_TEST" | bc`
                    SUB_LATENCY=`echo "scale=2; $MAX_SUB_TIME/($SUBSCRIPTION_POOL_SIZE*$NUM_HEAPS)" | bc`
                    echo "$NUM_HEAPS $NUM_CLIENTS $PUSHER_PULLER_THREADS $MAX_SUB_TIME $SUB_LATENCY $MAX_TIME $LATENCY" >> $RUN_DIR/pushTesting.log
                fi
            done

            echo "Stopping server $SERVER_PID"
            # Now stop the server
            kill $SERVER_PID

            stopClients

        done

    done

done

stopClients