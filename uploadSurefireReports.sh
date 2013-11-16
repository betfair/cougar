#!/bin/bash

if [ "$ARTIFACTS_S3_BUCKET" == "" ]; then
  echo "WARNING: Not uploading as no S3 Bucket specified" >&2
  exit 1
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "WARNING: Not uploading results as this is a pull request" >&2
  exit 2
fi

#
# NOTE: This code isn't exactly thread safe, if 2 builds are publishing at the same time then this could get messy
#       Kinda assuming that's extremely unlikely, and if it were to happen, we could just re-run the build
#

ARTIFACTS_S3_BUCKET_URL=http://$ARTIFACTS_S3_BUCKET.s3-website-$ARTIFACTS_AWS_REGION.amazonaws.com

# Create our tarballs for upload
find . -name "*.log" -exec tar rvf logs.tar {} \; >/dev/null

mkdir testxml
find . -name "TEST-*.xml" > 1; for i in `cat 1`; do cp $i testxml; done; rm 1
cd testxml
for i in `egrep -H "failures=\"[1-9]|errors=\"[1-9]" * | cut -d: -f1`; do
  echo "<a href=\"$i\">$i</a><br/>" >> index.html
done
cd ..
tar cf TEST-xml.tar testxml

# Make sure the branch exists in the s3 bucket
wget -O index.html $UPLOAD_S3_BUCKET_URL/$TRAVIS_REPO_SLUG/index.html
grep -q "$TRAVIS_BRANCH" index.html
RESULT=$?
if [ $RESULT -eq 1 ]; then
  echo "<a href=\"$TRAVIS_BRANCH/index.html\">$TRAVIS_BRANCH</a><br/>" >> index.html
  travis-artifacts upload --target-path $TRAVIS_REPO_SLUG --path index.html
  rm index.html
  touch index.html
else
  wget -O index.html $UPLOAD_S3_BUCKET_URL/$TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/index.html
fi

# Add this build to the branch index
grep -q "$TRAVIS_BUILD_ID" index.html
RESULT=$?
if [ $RESULT -eq 1 ]; then
  echo "<a href=\"$TRAVIS_BUILD_ID/index.html\">$TRAVIS_BUILD_ID</a><br/>" >> index.html
  travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID --path index.html
  rm index.html
  touch index.html
else
  wget -O index.html $UPLOAD_S3_BUCKET_URL/$TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID/index.html
fi

# Add this job to the build index
echo "<a href=\"$TRAVIS_JOB_ID/index.html\">$TRAVIS_JOB_ID</a><br/>" >> index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID --path index.html

# Create and upload the build index and tarballs
echo "<a href=\"logs.tar\">logs.tar</a><br/>" > index.html

echo "<a href=\"TEST-xml.tar\">TEST-xml.tar</a><br/>" >> index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID/$TRAVIS_JOB_ID --path index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID/$TRAVIS_JOB_ID --path logs.tar
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID/$TRAVIS_JOB_ID --path TEST-xml.tar

echo "Artifacts uploaded to $ARTIFACTS_S3_BUCKET_URL/$TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID/$TRAVIS_JOB_ID"
