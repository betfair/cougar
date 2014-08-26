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

package com.betfair.cougar.core.impl.logging;

import java.io.IOException;
import java.util.Properties;

/**
 * This interface describes a class that is used to bootstrap the logging system
 *
 *
 * if you don't want an initialisation class, then ensure the System variable
 * is null - you would want to do this if you have your own appender config for log4j
 *
 * If you want to initialise a logging system that is not log4j, then point the
 * System variable to your own implementation of this interface.
 *
 *  <code>cougar.core.log.bootstrap.class</code> System variable to point to your implementation
 *
 * See @CougarLog4JBootstrap
 */

public interface LogBootstrap {
    /**
     * Launch the logging system
     * @param properties - the full set of overlayed properties to bootstrap the logger
     * @throws IOException
     */
    public void init(Properties properties) throws IOException;
}
