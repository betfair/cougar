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

package com.betfair.cougar.core.api.ev;

/**
 * Hints as to when an execution processor should be run.
 */
public enum ExecutionRequirement {
    /**
     * Execution should be optimised so that this processor should be run exactly once, but at any execution point.
     */
    EXACTLY_ONCE,

    /**
     * Execution should only performed prior to pushing an execution request into the Executor pool.
     */
    PRE_QUEUE,

    /**
     * Execution should only performed prior to executing the target executable.
     */
    PRE_EXECUTE,

    /**
     * Execution should be performed at every location it is possible to do so.
     */
    EVERY_OPPORTUNITY
}
