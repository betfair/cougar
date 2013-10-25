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

curl --header "Accept: application/xml" "http://localhost:8080/cougarBaseline/v2.0/testConnectedObjects?protocol=$1" 2>/dev/null | sed -e 's/<TestResult>/\n <TestResult>\n  /g' -e 's/\/description>/\/description>\n  /g' -e 's/Response>/Response>\n/' -e 's/<\/TestResult>/\n<\/TestResult>/g' -e 's/<\/results>/\n<\/results>\n\n/' -e 's/<\/TestResults>/\n\n<\/TestResults>/'
