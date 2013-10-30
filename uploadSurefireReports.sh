#!/bin/bash

if [ "$ARTIFACTS_S3_BUCKET" == "" ]; then
  echo "WARNING: Not uploading as no S3 Bucket specified" >&2
  exit 1
fi

#if [ "$TRAVIS_PULL_REQUEST" == "true"]; then
#  echo "WARNING: Not uploading results as this is a pull request" >&2
#  exit 2
#fi

ARTIFACTS_S3_BUCKET_URL=http://$ARTIFACTS_S3_BUCKET.s3-website-$ARTIFACTS_AWS_REGION.amazonaws.com

# Create our tarballs for upload
find . -name "*.log" -exec tar rvf logs.tar {} \;
find . -name "TEST-*.xml" -exec tar rvf TEST-xml.tar {} \;

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
echo "<a href=\"$$TRAVIS_BUILD_ID/index.html\">$TRAVIS_BUILD_ID</a><br/>" >> index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH --path index.html

# Create and upload the build index and tarballs
echo "<a href=\"logs.tar\">logs.tar</a><br/>" > index.html
echo "<a href=\"TEST-xml.tar\">TEST-xml.tar</a><br/>" >> index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID --path index.html
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID --path logs.tar
travis-artifacts upload --target-path $TRAVIS_REPO_SLUG/$TRAVIS_BRANCH/$TRAVIS_BUILD_ID --path TEST-xml.tar

#for i in `find . -name surefire-reports`; do
#  PATH1=`echo "$i" | cut -c2-1000`
#  PATH_SO_FAR=""
#  SEP=""
#  for dir in `echo "$PATH1" | sed -e 's/\// /g'`; do
#    NEW_PATH_SO_FAR="$PATH_SO_FAR$SEP$dir"
#    SEP="/"
#    OUTPUT=$PATH_SO_FAR/index.html
#    if [ -z "$PATH_SO_FAR" ]; then
#      OUTPUT=index.html
#    fi
#    echo "<a href='$dir/index.html'>$dir</a><br/>" >> $OUTPUT
#    travis-artifacts upload --target-path $TRAVIS_REPO_SLUG --path $OUTPUT
#    if [ $dir == "surefire-reports" ]; then
#      for file in `ls $NEW_PATH_SO_FAR`; do
#        echo "<a href='$file'>$file</a><br/>" >> $NEW_PATH_SO_FAR/index.html
#      done
#      travis-artifacts upload --target-path $TRAVIS_REPO_SLUG --path $NEW_PATH_SO_FAR
#    fi
#    PATH_SO_FAR=$NEW_PATH_SO_FAR
#  done
#done
