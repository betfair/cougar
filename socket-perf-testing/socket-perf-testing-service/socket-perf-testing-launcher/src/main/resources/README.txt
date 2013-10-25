To run server:
==============

1. bash$ mkdir -p logs; bin/socketTestServer.sh <concurrencyOfUpdates> <numHeaps> <updatesPerHeap> <numPushProcessingThreads> <socketServerPoolSize>

Server will startup on port 9003 and initialise the heaps. Once cougar has started:

**** COUGAR HAS STARTED ****

then it is ready to accept client connections.

2. Once all clients are started and ready, start the appropriate of test:

RPC
---

bash$ bin/startRpcTest.sh <timeToRunInMs> [<numClientThreads>]

Once a test has been run the server and clients do NOT need restarting before running a new test

Push
----

bash$ bin/startPushTest.sh

Once a test has been run the server and clients MUST be restarted before running a new test