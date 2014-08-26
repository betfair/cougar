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

package com.betfair.cougar.codegen;

import org.w3c.dom.Node;

import javax.xml.xpath.XPath;

/**
 * Use an instance of this interface to describe how you'd
 * omit a particular transformation from being run.  The @see
 * Transformation class does not provide any functionality
 * to prevent a class from being generated.
 */
public interface NodeExcluder {
    /**
     * Implement this method to return true if a node SHOULD be omitted
     * from having a transformation run against it.
     * @param xp an instanciated XPath instance
     * @param node to use to determine whether this transformation should be run or not
     * @return returns true if the transformation should be omitted
     */
    public boolean exclude(XPath xp, Node node);
}
