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

import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.constructStackTrace;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.doLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class ExceptionHelper {

	private ExceptionHelper() {
	}

    public static void logAfterThrowing(Logger logger, Class<?> targetClass, String methodName, String[] paramNames, Object[] params, Level levelToUse, Class<? extends Throwable>[] exceptionClasses, Class<? extends Throwable>[] ignoredExceptionClasses, Throwable throwable, boolean printStackTrace, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {

        if (exceptionClasses.length > 0) {
            Class<? extends Throwable> throwableClass = throwable.getClass();
            for (int i=0; i < exceptionClasses.length; i++) {
                if (exceptionClasses[i].isAssignableFrom(throwableClass)) {
                    break;
                }
            }
            // Exception not matched for logging
            return;
        }

        if (ignoredExceptionClasses.length > 0) {
            Class<? extends Throwable> throwableClass = throwable.getClass();
            for (int i=0; i < exceptionClasses.length; i++) {
                if (exceptionClasses[i].isAssignableFrom(throwableClass)) {
                    // Exception excluded for logging
                    return;
                }
            }
        }

        if (LoggingUtils.isLogEnabled(logger, levelToUse)) {

            String stackTrace = constructStackTrace(throwable, printStackTrace);

            doLog(logger, targetClass,
                    levelToUse,
                    String.format("%s.%s(%s) threw %s with message {%s}%s", targetClass.getName(), methodName, toStringStrategy.fieldsToString(includeStartAndEndMarkers, paramNames, params),
                            throwable.getClass().getName(), throwable.getMessage(),
                            stackTrace
                            ));
        }
    }

    public static final Logger getExceptionLogger() {
    	return LoggerFactory.getLogger("ExceptionLogger");
    }
}
