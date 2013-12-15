---
layout: default
---
# Mailing lists

We maintain 2 mailing lists for very different purposes. The main user list is for user queries, release announcements, discussion, etc. We will normally aim to respond to mails on the list within a UK working day, but we offer no guarantees:

* Address: [betfair-cougar@googlegroups.com](mailto:betfair-cougar@googlegroups.com)
* Archives/Search: [https://groups.google.com/forum/#!forum/betfair-cougar](https://groups.google.com/forum/#!forum/betfair-cougar)

The developer list is intended for use for build notifications and other automated messages. We're happy for anyone to subscribe to this list, but please don't post there if you want an answer:

* Address: [betfair-cougar-dev@googlegroups.com](mailto:betfair-cougar-dev@googlegroups.com)
* Archives/Search: [https://groups.google.com/forum/#!forum/betfair-cougar-dev](https://groups.google.com/forum/#!forum/betfair-cougar-dev)

# Contributing

If you want to contribute to Cougar you can do so in many ways, and we'll gladly accept all the help we can get:
* Raising new issues: if you want to discuss your requirements before raising then feel free to use the user list, alternatively we can use github discussion on the actual issue once raised.
* Documentation: we hold our documentation in a seperate [repo](http://github.com/betfair/cougar-documentation) so we can version it independently but broadly aligned to the binary builds.
* Archetypes: we hold our archetypes in a seperate [repo](http://github.com/betfair/cougar-archetypes) so we can version it independently but broadly aligned to the binary builds.

# Maven repositories

## Snapshot builds

Snapshot builds are published in the OSS Sonatype Repository, look below for snippets (for dependencies and plugins - both are required):

```
<repository>
  <id>sonatype-nexus-snapshots</id>
  <name>Sonatype Nexus Snapshots</name>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

```
<pluginRepository>
  <id>sonatype-nexus-snapshots</id>
  <name>Sonatype Nexus Snapshots</name>
  <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  <releases>
    <enabled>false</enabled>
  </releases>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</pluginRepository>
```

## Release builds

Release builds are pushed to Maven Central, so you don't need to add anything to your POM to use them.
