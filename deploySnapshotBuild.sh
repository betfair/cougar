#!/bin/bash

if [ "$TRAVIS_REPO_SLUG" != "betfair/cougar" ]; then
  echo "WARNING: Not publishing snapshots as this is a branch" >&2
  exit 1
fi

if [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "WARNING: Not publishing snapshots as this is a pull request" >&2
  exit 2
fi

mvn deploy -DskipTests=true -s upload_settings.xml -B