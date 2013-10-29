Project Tasks
=============

Documentation Publishing
------------------------


Snapshot Deploy
---------------


Releasing
---------
* Branch cougar to create the release branch
* [Perform maven release]() on the branch
* Branch the cougar-documentation to create the release documentation
* Publish the new cougar-documentation branch
* Change the master cougar-documentation to reference the new release
* Branch the archetypes to create the release branch
* Update the archetypes on the release branch to reference the release version
* [Perform maven release]() on the archetype branch
* Update the archetypes on the master branch to reference the new SNAPSHOT
* Send notification email to betfair-cougar@googlegroups.com
