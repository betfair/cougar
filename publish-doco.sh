#!/bin/bash

#git clone https://github.com/betfair/gh-pages-publishing.git
#cd gh-pages-publishing

# NOTE: Never run this -x otherwise it will expose our github password, which is bad!
chmod u+x publish.sh
./publish.sh betfair/cougar $TRAVIS_BRANCH
