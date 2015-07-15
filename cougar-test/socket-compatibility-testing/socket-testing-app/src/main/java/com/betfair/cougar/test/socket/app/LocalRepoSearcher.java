/*
 * Copyright 2015, Simon Matić Langford
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

package com.betfair.cougar.test.socket.app;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class LocalRepoSearcher implements RepoSearcher {
    private File dir;

    public LocalRepoSearcher() {
        File repoDir = new File(System.getProperty("user.home")+"/.m2/repository");
        if (repoDir.exists() && repoDir.canRead()) {
            dir = repoDir;
        }
        else {
            throw new RuntimeException("Can't find local m2 repository");
        }
    }

    @Override
    public List<File> findAndCache(File tmpDir) {
        List<File> ret = new LinkedList<>();
        findAndCache(new File(dir,"com/betfair/cougar/socket-tester"), ret);
        return ret;
    }

    private void findAndCache(File groupDir, List<File> ret) {
        for (File f : groupDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        })) {
            File[] potentialJars = f.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("-jar-with-dependencies.jar");
                }
            });
            if (potentialJars.length > 1) {
                throw new RuntimeException("Too many jars in directory "+f);
            }
            if (potentialJars.length == 1) {
                ret.add(potentialJars[0]);
            }
        }
    }
}
