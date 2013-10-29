Project Tasks
=============

Documentation Publishing
------------------------
* Ensure you have configured a [GitHub SSH Key](https://help.github.com/articles/generating-ssh-keys)
* Pull the latest [betfair/gh-pages-publishing](https://github.com/betfair/gh-pages-publishing)
* Run ```./publish betfair/cougar <github-user> <branch-name>```

Snapshot Deploy
---------------
* Ensure you have configured [servers in settings.xml](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide)
* Pull down latest code
* Run ```mvn:deploy```

Releasing
---------
* Branch cougar to create the release branch
* [Perform maven release](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) on the branch
* Branch the cougar-documentation to create the release documentation
* Publish the new cougar-documentation branch
* Change the master cougar-documentation to reference the new release
* Branch the archetypes to create the release branch
* Update the archetypes on the release branch to reference the release version
* [Perform maven release](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) on the archetype branch
* Update the archetypes on the master branch to reference the new SNAPSHOT
* Send notification email to betfair-cougar@googlegroups.com
