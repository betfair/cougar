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
* Perform maven release - see below
* Branch the cougar-documentation to create the release documentation
* Publish the new cougar-documentation branch
* Change the master cougar-documentation to reference the new release
* Branch the archetypes to create the release branch
* Update the archetypes on the release branch to reference the release version
* [Perform maven release](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) on the archetype branch
* Update the archetypes on the master branch to reference the new SNAPSHOT
* Send notification email to betfair-cougar@googlegroups.com

Perform maven release
---------------------
```
# make sure up to date
git pull
# create branch
git checkout -b 3.2
mvn versions:set -DnewVersion=3.2.0-SNAPSHOT
find . -name "pom.xml.versionsBackup" -exec rm {} \;
git commit -m "Update versions on branch"
# fix master
git checkout master
mvn versions:set -DnewVersion=3.3-SNAPSHOT
find . -name "pom.xml.versionsBackup" -exec rm {} \;
git commit -m "Update versions post branch"
# prepare release
git checkout -b 3.2
mvn release:prepare
# enter gpg passphrase when prompted
# some fudgery required now, namely to install release binaries in local repo
cd target
git clone --branch 3.2.0 git@github.com:betfair/cougar checkout
cd checkout
git fetch git@github.com:betfair/cougar
git checkout 3.2.0
mvn install -Dmaven.test.skip=true
cd ..
rm -rf checkout
cd ..
# now do the actual release
mvn release:perform
```

If you have any errors at the release:perform stage, then reset using the following:
```
find . -name "*.releaseBackup" -exec rm {} \;
rm release.properties
mvn versions:set -DnewVersion=3.2.0-SNAPSHOT
find . -name "pom.xml.versionsBackup" -exec rm {} \;
git add .
git commit -m "Reset versions following failed release"
git push
mvn release:clean
```

If you have errors due to a pre-existing tag, then delete using:
```
# remote
git push origin :cougar-master-pom-3.2.0
# local
git tag -d cougar-master-pom-3.2.0
```
