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
 *
 */
public class ExecutableWrapperUtils {

    public static <T extends Executable> T findChild(Class<T> clazz, ExecutableWrapper wrapper) {
        Executable child = wrapper.getWrappedExecutable();
        return findChild(clazz, child);
    }

    public static <T extends Executable> T findChild(Class<T> clazz, Executable executable) {
        if (clazz.isAssignableFrom(executable.getClass())) {
            return (T) executable;
        }

        if (executable instanceof ExecutableWrapper) {
            return ((ExecutableWrapper)executable).findChild(clazz);
        }

        return null;
    }
}
