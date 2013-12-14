---
layout: default
---

Welcome to Cougar!
==================

Cougar is an open source framework for implementing well defined service interfaces with true transport independence, freeing you up to write your core logic. Originally written by [Betfair](http://www.betfair.com), and powering their core services, it is a high performance framework, easily supporting high concurrency requirements.

It is released under the [Apache Software Licence v2](http://www.apache.org/licenses/LICENSE-2.0).

Using Cougar
------------

If you're new to Cougar we suggest checking out the following 2 guides:

* [What is Cougar?](cougar-guide.html)
* [Cougar in under 5 minutes](getting-started.html)

Alternatively, checkout our [documentation hub](documentation.html) for other guides and references.

Releases
--------

Cougar has only recently been made an open source project after 4 years as a closed source project. Source code is available on GitHub, click [here](http://github.com/betfair/cougar) or checkout the "Fork Me" banner.

Whilst we're cleaning up Cougar (mostly in the documentation/archetype space), plus adding features we think are needed, Cougar is available as a SNAPSHOT release (3.0-SNAPSHOT to be precise) in the Sonatype OSS Repository:

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
