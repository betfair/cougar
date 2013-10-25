/*
 * Copyright 2013, The Sporting Exchange Limited
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

package com.betfair.cougar.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public interface CougarLogger {

	public boolean isLoggable(Level level);

	public void log(Level level, String msg, Object... args);

	public void log(Throwable exception);

	public void log(Level level, String msg, Throwable exception, Object... args);

	public void entering(String sourceClass, String sourceMethod, Object... args);

	public void exiting(String sourceClass, String sourceMethod, Object result);

	public void throwing(String sourceClass, String sourceMethod, Throwable thrown);

	public void forceTrace(String traceId, String msg, Object... args);

	public void log(LogRecord record);

	public Level getLevel();
	
	public void setEventForwarding(boolean forward);
	
    public int removeHandlers();
    
    public Handler[] getHandlers();
    
    public void addHandler(Handler handler);
    
    public String getLogName();
    
    public void flush();

}
