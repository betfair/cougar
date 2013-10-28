---
layout: default
---
# Consuming push in your client

{toc:minLevel=2}

## Subscribing to the connected object

Initial subscription to a connected object takes the form of an RPC call (which is available on the generated client
interface). The response from that call ```ConnectedResponse``` provides you with a heap which is the physical
representation of your connected domain object and a subscription object:

```
...
ConnectedResponse response = client.bestPrices(ctx, "1.1234");
Heap heap = response.getHeap();
Subscription sub = response.getSubscription();
```

As with the server side you can construct a domain projection from this heap, although you will only be able to do so
once a root node has actually been installed on the heap (not required when you construct a heap):

```
if (heap.isRootInstalled()) {
    BestPricesCO bestPrices = BestPricesClientCO.rootFrom(heap);
}
```

or for a list:
```
    ListProjection<BestPricesCO> = BestPricesClientCO.rootFromAsList(heap);
```

or for a set:
```
    SetProjection<BestPricesCO> = BestPricesClientCO.rootFromAsSet(heap);
```

or for a map:
```
    MapProjection<BestPricesCO> = BestPricesClientCO.rootFromAsMap(heap);
```


<table style='background-color: #FFFFCE;'>
    <tr>
        <td valign='top'><img src='warning.gif' width='16' height='16' align='center' valign='middle' border='0'></td>
        <td><p>Any attempt to use the server side projections on a client-side Heap will result in an ImmutableHeapException being thrown.</p></td>
    </tr>
</table>


## Processing updates

2 paradigms are available for receiving and processing updates on the connected object. Consumers may register for
notification that changes have occurred on the underlying heap, or poll the heap via the domain projection. It's generally
recommended that regardless of the method used for finding updates, that the domain projection is always used to interact
with the heap.

### Event driven

It's possible to register a low level listener to a heap which is notified of each transactional update that is applied
to that heap. The listener must implement a single method which receives the contents of that update as a single block.
This method call can be used as a trigger to re-read the heap via the domain projection to identify the changes in it and
post-process them (it's also the mechanism by which heap updates are propagated from server to client):

```
heap.addListener(new HeapListener() {
  public void applyUpdate(UpdateBlock update) {
    // do some processing
  }
});
```

{info}
The applyUpdate is called whilst the transaction is still in progress, so any blocking logic here will prevent other
updates coming through at the same time - this can be a good or a bad thing depending on your use case.
{info}

### Polling

Once you have constructed your domain projection from the heap, it's then possible for you to query the heap in terms of
the domain projection:

```
double back1 = bestPrices.back1().get();
```

If a field has yet to be installed then the accessor will be null:

```
Double back1 = bestPrices.back() != null ? bestPrices.back().get() : null;
```

{info}
Note that if you want reads of your domain projection to be transactionally consistent, then you must make these reads
within a listener's ```applyUpdate()``` method call, otherwise the heap could be mutating whilst or in-between calls to
query the projection.
{info}

## Unsubscribing

If for some reason you no longer are interested in updates for a connected object it's considered polite (especially if
you want to avoid charging) to notify the server that you're no long interested <u>before</u> discarding your references
to the heap and subscription. This is performed by calling the ```close()``` method on the returned ```Subscription```
instance:

```
sub.close();
```

## Subscription termination

There are many reasons that your subscription could be terminated, preventing you receiving any further updates. In order
to find out that this has happened, you can add a listener to the subscription that is returned to you, which will be
notified in the event of the subscription being closed:

```
sub.addListener(new Subscription.SubscriptionListener() {
  public void subscriptionClosed(Subscription subscription, CloseReason reason) {
    // do something
  }
});
```

It's also possible to query a subscription for it's closure reason (which will be ```null``` if it hasn't been terminated)
via the ```getCloseReason()``` method.

## End of the stream

As covered in the [server guide](Cougar_Push_Server.html) there may come a time when no further updates will ever be
issued for this logical heap, in which case a ```terminateHeap()``` command will be issued by the server. As a client
you will see the effects of this in 3 ways:

* Your subscription will be closed with a reason of ```REQUESTED_BY_PUBLISHER```
* The UpdateBlock received within your HeapListener will contain a final update of type ```TERMINATE_HEAP```
* The ```isTerminated()``` method on your Heap instance will return ```true```