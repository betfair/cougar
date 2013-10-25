To run push client:
===================

1. Make sure your server is already running
2. bash$ bin/pushTestClient.sh <clientname> <server host:port>

Client will connect and subscribe to all the available heaps, printing:

[<clientname>]: SUBSCRIPTIONS TOOK <timeToSubscribe>ms
[<clientname>]: READY (<numHeaps>)

It will then wait until the test is started on the server, at which point it will print:

[<clientname>]: STARTED AT <msStartTime>

When finished it will print:

[<clientname>]: COMPLETED AT <msEndTime> TOOK <totalTime> ms

If there are any issues you will see more output, either because the heaps terminated early or the resulting value in the heaps was not as expected

To run rpc client:
==================

1. Make sure your server is already running
2. bash$ bin/rpcTestClient.sh <clientname> <server host:port>

Client will connect and subscribe to the control heap, printing:

[<clientname>]: SUBSCRIPTIONS TOOK <timeToSubscribe>ms
[<clientname>]: READY

It will then wait until the test is started on the server, at which point it will print:

[<clientname>]: STARTED AT <msStartTime>

When finished it will print:

[<clientname>]: COMPLETED AT <msEndTime> TOOK <totalTime>ms @ <avgTps> tps and <avgLatency> ms/call

If there are any issues you will see more output, either because the heaps terminated early or the resulting value in the heaps was not as expected
