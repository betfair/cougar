---
layout: default
---
# Does BSIDL Support&nbsp;Inheritance?

*Q: I was hoping to have a type say “Shape” with some attributes and extend that type with some sub types say “Triangle” etc ?*

*Is inheritance of “types” possible in BSIDL ? (or anywhere in the roadmap?)&nbsp;*

A: TL;DR No and by design

No. I’m not sure exposing services with inherited data types is that useful to the consumer and the interfaces are optimised
for consumer ease of use. It also opens up other complexities around polymorphism in generic collections, method overloading
and so forth that really don’t help anyone and add significant complexity. If it is just to save you typing the base parameters
each time I would suggest a pre-processor of some description.