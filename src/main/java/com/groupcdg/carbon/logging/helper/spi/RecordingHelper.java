/*
 *  Copyright 2015 Computing Distribution Group Ltd
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.groupcdg.carbon.logging.helper.spi;

import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.doLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class RecordingHelper {

	private RecordingHelper() {
	}

    public static void logAfter(Logger logger, Class<?> targetClass, String methodName, String[] paramNames, Object[] params, Level levelToUse, boolean logArgs, long start, long end, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {

        long duration = end - start;

        if (LoggingUtils.isLogEnabled(logger, levelToUse)) {

            if (params.length != 0 && logArgs) {
                doLog(logger, targetClass, levelToUse, "Execution of {}.{}({}) took {} milliseconds", targetClass.getName(), methodName,
                		toStringStrategy.fieldsToString(includeStartAndEndMarkers, paramNames, params), duration);
            } else {
                doLog(logger, targetClass, levelToUse, "Execution of {}.{}() took {} milliseconds", targetClass.getName(), methodName, duration);
            }
        }
    }

    public static final Logger getLogger() {

        return LoggerFactory.getLogger("RecordLogger");
    }
}
