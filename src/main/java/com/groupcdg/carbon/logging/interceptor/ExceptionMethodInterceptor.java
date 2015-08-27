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
package com.groupcdg.carbon.logging.interceptor;

import static com.groupcdg.carbon.logging.helper.spi.ExceptionHelper.getExceptionLogger;
import static com.groupcdg.carbon.logging.helper.spi.ExceptionHelper.logAfterThrowing;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.determineParameterNames;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.matchMethod;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.obtainToStringStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.annotation.LogExceptions;
import com.groupcdg.carbon.logging.helper.spi.LoggingUtils;
import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class ExceptionMethodInterceptor implements MethodInterceptor {

	private static final ExceptionMethodInterceptor ERROR = new ExceptionMethodInterceptor(new StaticConfiguration(Level.ERROR));
	private static final ExceptionMethodInterceptor WARN = new ExceptionMethodInterceptor(new StaticConfiguration(Level.WARN));
	private static final ExceptionMethodInterceptor INFO = new ExceptionMethodInterceptor(new StaticConfiguration(Level.INFO));
	private static final ExceptionMethodInterceptor DEBUG = new ExceptionMethodInterceptor(new StaticConfiguration(Level.DEBUG));
	private static final ExceptionMethodInterceptor TRACE = new ExceptionMethodInterceptor(new StaticConfiguration(Level.TRACE));
	private static final ExceptionMethodInterceptor DEFAULT = new ExceptionMethodInterceptor(new StaticConfiguration(Level.DEFAULT));
	private static final ExceptionMethodInterceptor NONE = new ExceptionMethodInterceptor(new StaticConfiguration(Level.NONE));

	private Configuration configuration;

	private ExceptionMethodInterceptor(ExceptionMethodInterceptor.Configuration configuration) {
		this.configuration = configuration;
	}

	public Object interceptBefore(Object proxy, Method method, Object[] args, Object realTarget) {
		return null;
	}

	public void interceptAfter(Object proxy, Method method, Object[] args, Object realTarget, Object retObject,
			Object interceptBefore) {
    }

	@Override
	public void interceptAfterThrowing(Object proxy, Method method, Object[] args, Object realTarget, Throwable cause,
			Object interceptBeforeReturnObject) {

		boolean matchedMethod = configuration.isMatchedMethod(method);
		if (matchedMethod) {
			Class<?> proxiedInterface = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());
			logAfterThrowing(getExceptionLogger(), proxiedInterface, method.getName(), paramNames, args, configuration.getLevelToUse(method), configuration.getLogExceptionTypes(method), configuration.getIgnoredExceptionTypes(method), cause, configuration.isPrintStackTrace(method), configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	public static ExceptionMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {
		return new ExceptionMethodInterceptor(new StaticConfiguration(level, toStringStrategy, includeStartAndEndMarkers));
	}

	public static ExceptionMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers, boolean printStackTrace, Method... matchMethods) {
		return new ExceptionMethodInterceptor(new StaticConfiguration(level, toStringStrategy, includeStartAndEndMarkers, printStackTrace, matchMethods));
	}

	public static ExceptionMethodInterceptor of(Level level, ToStringStrategy toStringStrategy) {
		return new ExceptionMethodInterceptor(new StaticConfiguration(level, toStringStrategy));
	}

	public static ExceptionMethodInterceptor of(Class<?> targetAnnotatedClass, Annotation annotation) {
		return new ExceptionMethodInterceptor(new TargetAnnotationConfiguration(targetAnnotatedClass));
	}


	public static ExceptionMethodInterceptor of(Level level) {

		switch(level) {
		case ERROR:
			return ExceptionMethodInterceptor.ERROR;
		case WARN:
			return ExceptionMethodInterceptor.WARN;
		case INFO:
			return ExceptionMethodInterceptor.INFO;
		case DEBUG:
			return ExceptionMethodInterceptor.DEBUG;
		case TRACE:
			return ExceptionMethodInterceptor.TRACE;
		case DEFAULT:
			return ExceptionMethodInterceptor.DEFAULT;
		case NONE:
			return ExceptionMethodInterceptor.NONE;
		default:
			throw new IllegalStateException("Level " + level + " was not found");
		}
	}

	public static interface Configuration {

		public Level getLevelToUse(Method method);

		public ToStringStrategy getToStringStrategy(Method method);

		public boolean isIncludeStartAndEndMarkers(Method method);

		public Class<? extends Throwable>[] getLogExceptionTypes(Method method);

		public Class<? extends Throwable>[] getIgnoredExceptionTypes(Method method);

		public boolean isPrintStackTrace(Method method);

		public boolean isMatchedMethod(Method method);
	}

	private static class StaticConfiguration implements Configuration {

		private Level levelToUse;

		private ToStringStrategy toStringStrategy = new ReflectionToStringStrategy();

		private boolean includeStartAndEndMarkers = true;

		@SuppressWarnings("unchecked")
		private Class<? extends Throwable>[] logExceptionTypes = new Class[] {};

		@SuppressWarnings("unchecked")
		private Class<? extends Throwable>[] ignoredExceptionTypes = new Class[] {};

		private boolean printStackTrace = true;

		private Method[] matchMethods = null;

		private StaticConfiguration(Level levelToUse) {
			this.levelToUse = levelToUse;
		}

		private StaticConfiguration(Level levelToUse, ToStringStrategy toStringStrategy) {
			this.levelToUse = levelToUse;
			this.toStringStrategy = toStringStrategy;
		}

		private StaticConfiguration(Level levelToUse, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers) {
			this.levelToUse = levelToUse;
			this.toStringStrategy = toStringStrategy;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
		}

		private StaticConfiguration(Level levelToUse, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers, boolean printStackTrace, Method... matchMethods) {
			this.levelToUse = levelToUse;
			this.toStringStrategy = toStringStrategy;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
			this.printStackTrace = printStackTrace;
			this.matchMethods = matchMethods;
		}

		@Override
		public Level getLevelToUse(Method method) {
			return levelToUse;
		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {
			return toStringStrategy;
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {
			return includeStartAndEndMarkers;
		}

		@Override
		public Class<? extends Throwable>[] getLogExceptionTypes(Method method) {
			return logExceptionTypes;
		}

		@Override
		public Class<? extends Throwable>[] getIgnoredExceptionTypes(Method method) {
			return ignoredExceptionTypes;
		}

		@Override
		public boolean isPrintStackTrace(Method method) {
			return printStackTrace;
		}

		@Override
		public boolean isMatchedMethod(Method method) {
			return matchMethod(method, matchMethods);
		}
	}

	private static class TargetAnnotationConfiguration implements Configuration {

		private Class<?> targetType;

		private TargetAnnotationConfiguration(Class<?> targetType) {
			this.targetType = targetType;
		}

		private LogExceptions findLogExceptions(Class<?> targetType, Method method) {
			LogExceptions le = method.getAnnotation(LogExceptions.class);
			if (le == null) {
				le = targetType.getAnnotation(LogExceptions.class);
			}
			return le;
		}

		@Override
		public Level getLevelToUse(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return logExceptions.level();
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return logExceptions.includeStartAndEndMarkers();
		}

		@Override
		public Class<? extends Throwable>[] getLogExceptionTypes(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return logExceptions.logExceptionTypes();
		}

		@Override
		public Class<? extends Throwable>[] getIgnoredExceptionTypes(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return logExceptions.ignoredExceptionTypes();
		}

		@Override
		public boolean isPrintStackTrace(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return logExceptions.printStackTrace();
		}

		@Override
		public boolean isMatchedMethod(Method method) {
			if (method.getAnnotation(LogExceptions.class) != null) {
				return true;
			} if (targetType.getAnnotation(LogExceptions.class) != null) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {
			LogExceptions logExceptions = findLogExceptions(targetType, method);
			return obtainToStringStrategy(logExceptions.toStringStrategy(), logExceptions.toStringStrategyStyleName());
		}
	}
}