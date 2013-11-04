---
layout: default
---
# Interface Versioning and Upgrades

For all transports/wire formats except SOAP (see why we don't like SOAP?).

## Current Proposal

This proposal currently just talks about non-breaking changes (with the definition of non-breaking still to be agreed),
breaking changes will need to run concurrently for some period of time and we will deal with these when the first instance
crops up.

### Summary

We only allow hosting of one instance of each major version, hence the use of only major version on our endpoints.
Interfaces may use _x.y_ for versioning.

With regards to breaking vs non-breaking, each change will be considered on it's own merits. However, for clarity we have
defined interface shape changes as either definitely breaking, or potentially non-breaking, as per the table below
(this means that a change to the interface shape which is non-breaking may come with behavioural changes which are
distinctly breaking - there may even be no shape change at all):

|Change|Definitely Breaking?|
|:---- |:------------------ |
|Added operation|No|
|Added new mandatory request parameter to existing operation|Yes|
|Added new optional request parameter, or mandatory with default|No|
|Modified request parameter: made optional|No|
|Modified request parameter: made mandatory|Yes|
|Removed request parameter|No|
|Modified response: removed optional parameter|No|
|Modified response: removed mandatory parameter|Yes|
|Modified response: added parameter|No|
|Modified response parameter: made optional|Yes|
|Modified response parameter: made mandatory|No|
|Removed operation|Yes|
|Added event|No|
|Modified event: removed parameter|Yes|
|Modified event: added parameter|No|
|Modified event: made parameter optional|Yes|
|Modified event: made parameter mandatory|No|
|Removed event|Yes|
|Add new transport/messaging encoding|No|
|Remove transport/message encoding|Yes|
|Added exception to operation|Yes|
|Added value to list of valid values|No|