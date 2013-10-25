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

TIME_FOR_EACH_TEST=60000

NUM_CLIENT_VARIATIONS="1 3 5 10"
# this is used for socket thread pool size and ev size
SERVER_THREAD_VARIATIONS="10 25 50 100"
# this is used for socket thread pool size and client calling thread pool size
CLIENT_THREAD_VARIATIONS="10 25 50 100"

stopClients() {
    echo "Stopping clients"
    # Now stop those clients (to reconfigure threads)
    for pid in `jps -m | grep "RpcMain" | awk '{ print $1 }'`; do
      kill $pid
    done
}

rm -f $RUN_DIR/rpcTesting.log

# First deploy the latest code
rm -rf /tmp/socket-perf-testing-*
cp socket-perf-testing-service/socket-perf-testing-launcher/target/socket-perf-testing-launcher-2.6-push-SNAPSHOT-deploy.tar.gz /tmp
cp socket-perf-testing-client/target/socket-perf-testing-client-2.6-push-SNAPSHOT-deploy.tar.gz /tmp
cd /tmp
tar xfz socket-perf-testing-client-2.6-push-SNAPSHOT-deploy.tar.gz
tar xfz socket-perf-testing-launcher-2.6-push-SNAPSHOT-deploy.tar.gz

for SERVER_THREADS in `echo $SERVER_THREAD_VARIATIONS`; do

    echo "Starting server with $SERVER_THREADS threads"
    # Now start the server
    mkdir -p /tmp/socket-perf-testing-server/bin/logs
    # first 4 params are for push tests only, so ignore for now
    SERVER_PARAMS="1 1 1 1 $SERVER_THREADS"
    cd /tmp/socket-perf-testing-server/bin
    ./socketTestServer.sh $SERVER_PARAMS > logs/stdout.log &
    while ( ! grep -q "COUGAR HAS STARTED" logs/stdout.log ); do
      echo "Waiting for server to start"
      sleep 1
    done
    SERVER_PID=`jps -m | grep "Main $SERVER_PARAMS" | awk '{print $1}'`

    for NUM_CLIENTS in `echo $NUM_CLIENT_VARIATIONS`; do

        for CLIENT_THREADS in `echo $CLIENT_THREAD_VARIATIONS`; do

            WORKED_THIS_RUN="false"
            while [ $WORKED_THIS_RUN == "false" ]; do
                # assume it's going to work until it fails
                WORKED_THIS_RUN="true"
                stopClients

                echo "Starting $NUM_CLIENTS each with $CLIENT_THREADS threads"
                # So for starting many clients
                for ((c=1; c<=$NUM_CLIENTS; c++)); do
                  CLIENT_NAME="client$c"
                  /tmp/socket-perf-testing-client/bin/rpcTestClient.sh $CLIENT_NAME localhost:9003 $CLIENT_THREADS > /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log 2>&1 &
                  while ( ! grep -q "READY" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log ); do
                    grep -q "This Client is not connected to a server so this call" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                    if [ $? -eq 0 ]; then
                      echo "$CLIENT_NAME failed to start properly, restarting.."
                      PID=`jps -m | grep "RpcMain $CLIENT_NAME" | awk '{ print $1 }'`
                      kill -9 $PID
                      /tmp/socket-perf-testing-client/bin/rpcTestClient.sh $CLIENT_NAME localhost:9003 $CLIENT_THREADS > /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log 2>&1 &
                    else
                        echo "Waiting for $CLIENT_NAME to start"
                        sleep 1
                    fi
                  done
                done

                echo -n "Running test ($NUM_CLIENTS/$CLIENT_THREADS/$SERVER_THREADS).."
                # Now, logic for running a test
                /tmp/socket-perf-testing-server/bin/startRpcTest.sh $TIME_FOR_EACH_TEST $CLIENT_THREADS
                echo "done"

                # Now, report on results
                TOTAL_TPS=0
                TOTAL_LATENCY=0
                for ((c=1; c<=$NUM_CLIENTS; c++)); do
                    CLIENT_NAME="client$c"
                    while ( ! grep -q "COMPLETED" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log ); do
                        grep -q "Connectivity to remote server lost!" /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                        if [ $? -eq 0 ]; then
                            echo "[ERROR]: $CLIENT_NAME failed, result will be invalid for this test"
                            echo "[$CLIENT_NAME]: COMPLETED AT 0 TOOK 0ms @ 0 tps and 0 ms/call" >> /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log
                            $WORKED_THIS_RUN = "false"
                            break
                        else
                            echo "Waiting for $CLIENT_NAME to complete"
                            sleep 1
                        fi
                    done
                    if [ $WORKED_THIS_RUN == "true" ]; then
                        END_LINE=`cat /tmp/socket-perf-testing-client/bin/$CLIENT_NAME.log | grep "COMPLETED" | tail -1`
                        echo "$CLIENT_NAME result: $END_LINE"
                        TPS=`echo "$END_LINE" | awk '{print $8}'`
                        LATENCY=`echo "$END_LINE" | awk '{print $11}'`
                        TOTAL_TPS=`echo "scale=2; $TOTAL_TPS+$TPS" | bc`
                        TOTAL_LATENCY=`echo "scale=2; $TOTAL_LATENCY+($LATENCY*$TPS)" | bc`
                    fi
                done
                if [ $WORKED_THIS_RUN == "true" ]; then
                    TOTAL_LATENCY=`echo "scale=2; $TOTAL_LATENCY/$TOTAL_TPS" | bc`
                    echo "$NUM_CLIENTS $CLIENT_THREADS $SERVER_THREADS $TOTAL_TPS $TOTAL_LATENCY" >> $RUN_DIR/rpcTesting.log
                fi
            done

        done

    done

    echo "Stopping server"
    # Now stop the server
    kill $SERVER_PID

done

stopClients