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

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class LoggingHelper {

	private LoggingHelper() {
	}

    public static void logBefore(Logger logger, Class<?> targetClass, String methodName, String[] paramNames, Object[] params, Level levelToUse, boolean logArgs, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {

        if (LoggingUtils.isLogEnabled(logger, levelToUse)) {

            if ((params != null) && (params.length != 0) && (logArgs)) {
                doLog(logger, targetClass, levelToUse, "Before {}({})", methodName, toStringStrategy.fieldsToString(includeStartAndEndMarkers, paramNames, params));
            } else {
                doLog(logger, targetClass, levelToUse, "Before {}()", methodName);
            }
        }
    }

    public static void logAfterThrowing(Logger logger, Class<?> targetClass, String methodName, String[] paramNames, Object[] params, Level levelToUse, boolean logExceptions, Class<? extends Throwable>[] exceptionClasses, Class<? extends Throwable>[] ignoredExceptionClasses, Throwable throwable, boolean printStackTrace, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {

        if (!logExceptions) {
            return;
        }

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
                    String.format("%s(%s) threw %s with message {%s}%s", methodName, toStringStrategy.fieldsToString(includeStartAndEndMarkers, paramNames, params),
                            throwable.getClass().getName(), throwable.getMessage(), stackTrace));
        }
    }

    public static void logAfter(Logger logger, Class<?> targetClass, String methodName, Class<?> returnType, Level levelToUse, boolean logReturning, boolean logReturnValue, Object returnValue, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {

        if (!logReturning && !logReturnValue) {
            return;
        }

        if (LoggingUtils.isLogEnabled(logger, levelToUse)) {

            if (returnType.getName().compareTo("void") == 0 || !logReturnValue) {
                doLog(logger, targetClass, levelToUse, "After  {}() =>", methodName);
                return;
            }
        }

        doLog(logger, targetClass, levelToUse, "After  {}() => {}", methodName, toStringStrategy.fieldsToString(includeStartAndEndMarkers, new String[]{"returnValue"}, new Object[] {returnValue}));
    }
}
