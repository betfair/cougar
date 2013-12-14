#!/bin/bash

TMP_DIR=/tmp/publish_$$
mkdir -p $TMP_DIR
cd $TMP_DIR

REPO=$1
VERSION=$2
USER=$GITHUB_USER
PASS=$GITHUB_PASSWORD
EMAIL=$GIT_EMAIL
NAME=$GIT_NAME

if [ -z $USER ]; then
  if [ ! -z "$3" ]; then
    USER=$3
  fi
fi
if [ -z $PASS ]; then
  if [ ! -z "$4" ]; then
    PASS=$4
  fi
fi

if [ -z "$USER" ]; then
  echo "Usage: publish.sh <repo> <version> [<gh-user> [gh-password]]" >&2
  echo "" >&2
  echo "       Username/password may also be passed by setting environment variables:" >&2
  echo "         GITHUB_USER" >&2
  echo "         GITHUB_PASSWORD" >&2
  echo "       If no password set then will try to use private key" >&2
  echo "" >&2
  echo "       Git name/email - if not set locally then need to be provided via environment variables:" >&2
  echo "         GIT_EMAIL" >&2
  echo "         GIT_NAME" >&2
  exit 1
fi

echo "Repository: $REPO"
echo "Version: $VERSION"

echo "Cloning git repos"
USER_PASS=$USER
if [ ! -z $PASS ]; then
  USER_PASS=$USER_PASS:$PASS
fi

git clone -b gh-pages https://$USER_PASS@github.com/$REPO.git gh-pages
git clone -b $VERSION https://$USER_PASS@github.com/$REPO.git source
git clone -b $VERSION https://$USER_PASS@github.com/$REPO-documentation.git doco-source

if [ $VERSION == "master" ]; then
  echo "Need to parse pom.xml"
#  exit 1
fi

# todo: removing old master content? Manual for now
if [ -d gh-pages/$VERSION ]; then
  echo "Cleaning out old site content for branch $VERSION"
  cd gh-pages
  git rm -rf $VERSION
  cd ..
fi

PAGES_DIR=gh-pages/$VERSION
if [ $VERSION == "master" ]; then
  PAGES_DIR=gh-pages
fi

echo "Generating maven site"
mkdir -p $PAGES_DIR/maven
cd source
mvn site:site site:deploy -Dsite.deploy.dir=$TMP_DIR/$PAGES_DIR/maven
cd ..

echo "Copying maven site into place"
rm -rf doco-source/.git
cp -R doco-source/* $PAGES_DIR

echo "Telling git about our changes"
cd gh-pages
if [ $VERSION == "master" ]; then
  find . -exec git add {} \;
else
  find $VERSION -exec git add {} \;
fi
cd ..

echo "Pushing to live"
cd gh-pages
# todo: move these to env variables
if [ ! -z $EMAIL ]; then
  git config user.email "simon@exemel.co.uk"
fi
if [ ! -z $NAME ]; then
  git config user.name "Simon Matic Langford"
fi
git commit -a -m "Pushing latest site updates"
# the sed is so we don't show the password in the log
git push https://$USER_PASS@github.com/$REPO.git gh-pages | sed -e 's/\/\/.*\:.*@github\.com/\/\/github\.com/'
cd ..

echo "Cleanup.."
rm -rf $TMP_DIR
