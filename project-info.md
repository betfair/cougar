---
layout: default
---
# Mailing lists

We maintain 2 mailing lists for very different purposes. The main user list is for user queries, release announcements, discussion, etc. We will normally aim to respond to mails on the list within a UK working day, but we offer no guarantees:

* Address: betfair-cougar@googlegroups.com
* Archives/Search: https://groups.google.com/forum/#!forum/betfair-cougar

The developer list is intended for use for build notifications and other automated messages. We're happy for anyone to subscribe to this list, but please don't post there if you want an answer:

* Address: betfair-cougar-dev@googlegroups.com
* Archives/Search: https://groups.google.com/forum/#!forum/betfair-cougar-dev

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
