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

package com.betfair.cougar.netutil.nio.connected;

public class InitialUpdate extends Update {

    // for transcription
    public InitialUpdate() {
    }

    public InitialUpdate(Update u) {
        this.setActions(u.getActions());
    }

    @Override
    public String toString() {
        return "InitialUpdate{" +
                "actions=" + getActions() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Update update = (Update) o;

        if (!getActions().equals(update.getActions())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getActions().hashCode();
    }
}
