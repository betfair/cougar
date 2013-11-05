---
layout: default
---
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
