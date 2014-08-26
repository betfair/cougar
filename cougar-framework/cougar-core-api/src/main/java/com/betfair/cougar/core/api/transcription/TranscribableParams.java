/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.core.api.transcription;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Parameters that affect transcription, these should be positive values, ie the default behaviour (of older protocol versions) should be indicated
 * by the absence of one of these parameters in the set of parameters passed into any transcribe method.
 */
public enum TranscribableParams {

    EnumsWrittenAsStrings, MajorOnlyPackageNaming;

    private static Set<TranscribableParams> ALL_SET = Collections.unmodifiableSet(EnumSet.allOf(TranscribableParams.class));
    private static Set<TranscribableParams> NONE_SET = Collections.unmodifiableSet(EnumSet.noneOf(TranscribableParams.class));

    public static Set<TranscribableParams> getAll() {
        return ALL_SET;
    }

    public static Set<TranscribableParams> getNone() {
        return NONE_SET;
    }


}
