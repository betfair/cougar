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

package com.betfair.cougar.netutil.nio;

import com.betfair.cougar.core.api.ServiceVersion;
import com.betfair.cougar.core.api.transcription.Transcribable;

/**
 *
 */
public abstract class AbstractHeapTranscribable implements Transcribable {

    // Fixed service version, it's used for package renaming, which isn't used for non-generated classes.
    public static final ServiceVersion FIXED_SERVICE_VERSION = new ServiceVersion(1,0);

    @Override
    public ServiceVersion getServiceVersion() {
        return FIXED_SERVICE_VERSION;
    }
}
