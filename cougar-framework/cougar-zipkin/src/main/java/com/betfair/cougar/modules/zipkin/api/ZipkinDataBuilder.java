/*
 * Copyright 2015, The Sporting Exchange Limited
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

package com.betfair.cougar.modules.zipkin.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builder interface for ZipkinData (@see com.betfair.cougar.modules.zipkin.api.ZipkinData).
 */
public interface ZipkinDataBuilder {

    @Nonnull
    ZipkinDataBuilder traceId(long traceId);

    @Nonnull
    ZipkinDataBuilder spanId(long spanId);

    @Nonnull
    ZipkinDataBuilder parentSpanId(@Nullable Long parentSpanId);

    @Nonnull
    ZipkinDataBuilder spanName(@Nonnull String spanName);

    @Nonnull
    ZipkinDataBuilder port(short port);

    @Nonnull
    ZipkinDataBuilder flags(Long flags);

    @Nonnull
    ZipkinData build();
}
