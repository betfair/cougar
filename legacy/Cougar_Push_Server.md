---
layout: default
---
# Implementing push in your service

{toc:minLevel=2|maxLevel=3}

## Implementing the initial subscription call

As mentioned above, the operation that is defined in your generated service interface is the one called by Cougar when a
client initially connects to your connected object, allowing the client to pass in information that may affect which
instance is returned, and also allowing the server to decide whether and which instance to return.

Taking you through this line by line...

Within your service, before you can send updates to a connected object back to a client, you need to create an instance
of the Heap which you will later mutate. You can create this instance whenever you like, at the very latest before returning
a reference to the first client that needs it.

The constructor for the heap takes a logical uri that must be unique within the context of this cougar instance.

```
  MutableHeap heap = new MutableHeap(marketId+"/bestPrices/live");
```

{info}
On the server side we always construct a ```MutableHeap```, there is a conversely named ```ImmutableHeap``` which is used
on the client to ensure we don't end up mutating at both ends of the connection.
{info}

Now you will need to construct your domain projection of that heap, and link it to the heap.

```
heap.beginUpdate();
BestPricesCO connectedObect = BestPricesServerCO.rootFrom(heap);
heap.endUpdate();
```

or for a list:

```
    ListProjection<BestPricesCO> = BestPricesServerCO.rootFromAsList(heap);
```

or for a set:

```
    SetProjection<BestPricesCO> = BestPricesServerCO.rootFromAsSet(heap);
```

or for a map:

```
    MapProjection<BestPricesCO> = BestPricesServerCO.rootFromAsMap(heap);
```


<table style='background-color: #FFFFCE;'>
    <tr>
        <td valign='top'><img src='warning.gif' width='16' height='16' align='center' valign='middle' border='0'></td>
        <td><p>Any attempt to use the server side projections on a client-side Heap will result in an ImmutableHeapException being thrown.</p></td>
    </tr>
</table>


{info}
All mutations of a heap must be done within transactional boundaries - indicated by calls to ```beginUpdate()``` and
```endUpdate()``` on the Heap instance. Failure to do so will result in an ```IllegalStateException``` being thrown. We
strongly recommend you wrap this transactional code in a ```try...finally...``` block.
{info}

Next we need to return this heap back to the client, by returning an instance of ConnectedResponse (we'll cover the
subscription in more detail later):

```
ConnectedResponse response = new ConnectedResponseImpl(heap, new DefaultSubscription());
```

Whenever we want to mutate the connected object we do so by interacting with the domain projection within our heap
transaction, and the changes will automatically be replicated on completion of that transaction:

```
heap.beginUpdate();
connectedObject.back1().set(1.5);
connectedObject.back2().set(1.4);
connectedObject.back3().set(1.3);
connectedObject.lay1().set(1.6);
connectedObject.lay2().set(1.7);
connectedObject.lay3().set(1.8);
heap.endUpdate();
```

And lastly when no further updates will ever be emitted for this connected object, you can notify clients (and also
cause all resources associated with the connection):

```
heap.beginUpdate();
heap.terminateHeap();
heap.endUpdate();
```

Putting it all together:

```
private Heap bestPricesHeap;
private BestPricesCO bestPrices;
public ConnectedResponse bestPrices(ExecutionContext ctx, String marketId) {
  if (bestPricesHeap == null) {
    bestPricesHeap = new MutableHeap(marketId+"/bestPrices/live");
    bestPricesHeap.beginUpdate();
    BestPricesCO bestPrices = BestPricesServerCO.rootFrom(bestPricesHeap);
    bestPrices.back1().set(1.5);
    bestPrices.back2().set(1.4);
    bestPrices.back3().set(1.3);
    bestPrices.lay1().set(1.6);
    bestPrices.lay2().set(1.7);
    bestPrices.lay3().set(1.8);
    bestPricesHeap.endUpdate();
    Thread t = new Thread(new Runnable() {
      public void run() {
        try {
          for (int i=0; i<10; i<u></u>) {
            Thread.sleep(2000);
            bestPricesHeap.beginUpdate();
            bestPrices.back1().set(bestPrices.back1().get() + 0.01);
            bestPrices.back2().set(bestPrices.back2().get() + 0.01);
            bestPrices.back3().set(bestPrices.back3().get() + 0.01);
            bestPrices.lay1().set(bestPrices.lay1().get() + 0.01);
            bestPrices.lay2().set(bestPrices.lay1().get() + 0.01);
            bestPrices.lay3().set(bestPrices.lay1().get() + 0.01);
            bestPricesHeap.endUpdate();
          }
          bestPricesHeap.beginUpdate();
          bestPricesHeap.terminateHeap();
          bestPricesHeap.endUpdate();
        } catch (InterruptedException ie) {}
      }
    });
    t.start();
  }
  ConnectedResponse response = new ConnectedResponseImpl(bestPricesHeap, new DefaultSubscription());
  return response;
}
```

Obviously the example is simplistic, it assumes only one market is active at any point in time and that all customers
should receive live markets. It also ignores the fact that all the listeners may have been shutdown and it's emitting
updates into the ether.

## Subscriptions

When returning a ```ConnectedResponse``` from your service implementation, you must return an instance of the
```Subscription``` interface. A default implementation is provided for your convenience (```DefaultSubscription```) as
used in the examples above. We recommend that if you wish to provide a custom implementation of this interface that you
extend this rather than roll your own.

The subscription interface allows 2 main types of interaction:

* Termination of a subscription, giving a reason why (terminating a heap automatically terminates all subscriptions with
the reason REQUESTED_BY_PUBLISHER).
* Registering a listener to get notification of subscription termination, to enable triggering of other functionality.

The subscription returned is a link between the server and a particular client that is receiving updates for this connected
object. Whilst you can return the same instance to 2 different callers, the behaviour is undefined when doing so. If you
want to be able to disconnect particular clients or are interested in clients disconnecting, then make sure you hang on
to the subscription reference or register a listener.

### Subscription logging

Cougar logs subscription start and termination to a push-subscription log (found at ```dw/<host>-push-subscription.log```).

As with request logs, you may add custom fields to the standard set in the init method on your service implementation:

```
    @Override
    public void init(ContainerContext cc) {
        cc.registerExtensionLoggerClass(BaselineLogExtension.class, 3);
        cc.registerConnectedObjectExtensionLoggerClass(BaselineLogExtension.class, 3);
    }
```

Then you need to ensure you set the log extension on the ```RequestContext``` when your connected operation is invoked:

```
    @Override
    public ConnectedResponse simpleConnectedObject(RequestContext ctx) {
        ctx.setConnectedObjectLogExtension(new BaselineLogExtension("a","b","c"));
        Subscription sub = createSub(simpleConnectedObjectHeap);
        return new ConnectedResponseImpl(simpleConnectedObjectHeap, sub);
    }
```

The log format for the core field set is as follows:

1. yyyy-MM-dd HH:mm:ss.SSS
2. Subscription ID
3. Heap URI
4. A value from ```Subscription.CloseReason``` or SUBSCRIPTION_START if this is the start of a subscription

On clean shutdown, Cougar will attempt to write subscription closed lines to the subscription log.

