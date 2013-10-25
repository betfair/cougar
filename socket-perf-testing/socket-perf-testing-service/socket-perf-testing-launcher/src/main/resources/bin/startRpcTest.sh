#!/bin/bash
# Copyright 2013, The Sporting Exchange Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

if [ "$1" = "" ]; then
  echo "Usage: startRpcTest.sh <lengthMs> [<numThreads>]"
  exit 1
fi

THREADS=$2
if [ "$THREADS" = "" ]; then
  THREADS=1
fi

curl http://localhost:8080/SocketPerfTesting/v1.0/startRpcTest?length=$1\&numClientThreads=$THREADS