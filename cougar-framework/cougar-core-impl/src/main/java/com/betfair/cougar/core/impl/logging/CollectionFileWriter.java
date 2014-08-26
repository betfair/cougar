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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;

/**
 * Writes a Collection of Objects to a file using their toString representations.
 * Each member of the collection's representation will be written to a separate line.
 */
public class CollectionFileWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(CollectionFileWriter.class);

    /**
     * Constructor.
     * @param fileName the name of the file to write to
     * @param entries the strings to write to the file (one string per line)
     * @throws IllegalArgumentException if enabled but no file name defined
     */
    public static void write(String fileName, Collection<? extends Object> entries) {
        if (fileName == null) {
            throw new IllegalArgumentException("Must specify a file name to write to");
        }
        PrintWriter writer = null;
        File file = new File(fileName);
        try {
            file.mkdirs();
            if (file.exists()) {
                file.delete();
            }
            writer = new PrintWriter(new FileWriter(file), true);
            for (Object entry : entries) {
                writer.println(String.valueOf(entry));
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Problem writing to " + file.getAbsolutePath(), e);
        }
        finally {
            try {
            	if(writer != null) {
            		writer.close();
                }
            }
            catch (Exception e) {
                //
            }
        }
    }
}
