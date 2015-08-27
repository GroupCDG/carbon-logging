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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupcdg.carbon.logging.annotation.Debug;
import com.groupcdg.carbon.logging.annotation.Info;
import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.annotation.Log;
import com.groupcdg.carbon.logging.annotation.None;
import com.groupcdg.carbon.logging.annotation.Trace;
import com.groupcdg.carbon.logging.annotation.Warn;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

/**
 * Utilities to assist with producing useful log messages from aspects
 */
public class LoggingUtils {

	private static final Map<String, ToStringStrategy> STRATEGIES = new HashMap<>();

	static {
		for (Field next : ToStringStyle.class.getDeclaredFields()) {
			int mods = next.getModifiers();
			if (Modifier.isStatic(mods) && Modifier.isFinal(mods)
					&& ToStringStyle.class.isAssignableFrom(next.getType())) {
				ReflectionToStringStrategy nextStrategy = new ReflectionToStringStrategy();
				nextStrategy.setStyleName(next.getName());
				STRATEGIES.put(nextStrategy.getClass().getName() + ":" + next.getName(), nextStrategy);
			}
		}
	}

    private LoggingUtils() {
    }

    public static final Logger getLogger(Class<? extends Object> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public static final String constructStackTrace(Throwable throwable, boolean printStackTrace) {

    	String stackTrace = "";
        if (printStackTrace == true) {
            StringWriter sw = new StringWriter();
            PrintWriter pWriter = new PrintWriter(sw);
            throwable.printStackTrace(pWriter);
            stackTrace = System.getProperty("line.separator") + sw.toString();
        }
        return stackTrace;
    }

    public static final void doLog(Logger logger, Class<?> clazz, Level logLevel, String pattern, Object... args) {
        switch (logLevel) {
        case ERROR:
            logger.error(pattern, args);
            break;
        case WARN:
            logger.warn(pattern, args);
            break;
        case INFO:
            logger.info(pattern, args);
            break;
        case DEBUG:
            logger.debug(pattern, args);
            break;
        case NONE:
        	break;
        case TRACE:
        case DEFAULT:
            logger.trace(pattern, args);
            break;
        }
    }

    public static final boolean isLogEnabled(Logger logger, Level logLevel) {
        switch (logLevel) {
            case ERROR:
                return logger.isErrorEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case NONE:
            	return false;
            case TRACE:
            case DEFAULT:
                return logger.isTraceEnabled();
        }
        throw new IllegalStateException("Log Level could not be determined: " + logLevel);
    }

    public static final Level inferEffectiveLevel(Annotation logAnnotation, Level level) {
        return inferEffectiveLevel(logAnnotation, level, Level.DEFAULT);
    }

    /**
     * Works out the correct Level to use based on the following logic:
     * If a Level is defined for the current context, this is used
     * Otherwise if a specific default Level is defined, this is used instead
     * Otherwise, the Log Annotation is inspected to determine the level to be used
     * Otherwise, Trace is used.
     * @param logAnnotation The log annotation to use
     * @param baseLevel The base log Level
     * @param contextLevel The log Level defined for the current context (or null if none)
     * @return The inferred log Level
     */
    public static final Level inferEffectiveLevel(Annotation logAnnotation, Level baseLevel, Level contextLevel) {

        final Level levelToUse;

        if (contextLevel == Level.DEFAULT || contextLevel == null) {
            if (baseLevel == Level.DEFAULT || baseLevel == null) {
                if (Log.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.TRACE;
                } else if (com.groupcdg.carbon.logging.annotation.Error.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.ERROR;
                } else if (Warn.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.WARN;
                } else if (Info.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.INFO;
                } else if (Debug.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.DEBUG;
                } else if (Trace.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.TRACE;
                } else if (None.class.isAssignableFrom(logAnnotation.getClass())) {
                    levelToUse = Level.NONE;
                } else {
                    levelToUse = Level.TRACE;
                }
            } else {
                levelToUse = baseLevel;
            }
        } else {
            levelToUse = contextLevel;
        }

        return levelToUse;
    }

	public static String[] determineParameterNames(Parameter[] parameters) {

		String[] result = new String[parameters.length];
		for (int i=0; i<parameters.length; i++) {
			result[i] = parameters[i].getName();
		}
		return result;
	}

	public static Class<?> determineActualType(Object proxy) {

		Class<?> actualType;
		if (Proxy.isProxyClass(proxy.getClass())) {
			actualType = proxy.getClass().getInterfaces()[0];
		}
		else {
			actualType = proxy.getClass();
			while (actualType.getSimpleName().contains("$$EnhancerByCGLIB$$")) {
				actualType = actualType.getSuperclass();
			}
		}
		return actualType;
	}

	public static boolean matchMethod(Method method, Method[] matchMethods) {
		boolean matchedMethod = matchMethods == null;

		if (!matchedMethod) {
			for (int i = 0; i < matchMethods.length; i++) {
				if (method == matchMethods[i]) {
					matchedMethod = true;
					break;
				}
			}
		}
		return matchedMethod;
	}

    public static final ToStringStrategy obtainToStringStrategy(Class<? extends ToStringStrategy> strategyClass, String stringStyle) {

		ToStringStrategy strategy;
		if (ReflectionToStringStrategy.class.isAssignableFrom(strategyClass)) {
			strategy = STRATEGIES.get(strategyClass.getName() + ":" + stringStyle);
		} else {
			strategy = STRATEGIES.get(strategyClass.getName());
		}
		if (strategy == null) {
			try {
				strategy = (ToStringStrategy) strategyClass.newInstance();
				if (strategy instanceof ReflectionToStringStrategy) {
					((ReflectionToStringStrategy)strategy).setStyleName(stringStyle);
					STRATEGIES.put(strategyClass.getName() + ":" + stringStyle, strategy);
				} else {
					STRATEGIES.put(strategyClass.getName(), strategy);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalStateException("Could not find or create class for ToStringStrategy: " + strategyClass, e);
			}
		}
		return strategy;
	}
}
