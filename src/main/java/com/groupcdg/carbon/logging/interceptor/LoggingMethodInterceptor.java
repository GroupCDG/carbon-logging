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

import static com.groupcdg.carbon.logging.helper.spi.LoggingHelper.logAfter;
import static com.groupcdg.carbon.logging.helper.spi.LoggingHelper.logAfterThrowing;
import static com.groupcdg.carbon.logging.helper.spi.LoggingHelper.logBefore;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.determineParameterNames;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.getLogger;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.inferEffectiveLevel;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.matchMethod;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.obtainToStringStrategy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.annotation.Debug;
import com.groupcdg.carbon.logging.annotation.Info;
import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.annotation.Log;
import com.groupcdg.carbon.logging.annotation.None;
import com.groupcdg.carbon.logging.annotation.Trace;
import com.groupcdg.carbon.logging.annotation.Warn;
import com.groupcdg.carbon.logging.helper.spi.LoggingUtils;
import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class LoggingMethodInterceptor implements MethodInterceptor {

	private static final LoggingMethodInterceptor ERROR = new LoggingMethodInterceptor(new StaticConfiguration(Level.ERROR));
	private static final LoggingMethodInterceptor WARN = new LoggingMethodInterceptor(new StaticConfiguration(Level.WARN));
	private static final LoggingMethodInterceptor INFO = new LoggingMethodInterceptor(new StaticConfiguration(Level.INFO));
	private static final LoggingMethodInterceptor DEBUG = new LoggingMethodInterceptor(new StaticConfiguration(Level.DEBUG));
	private static final LoggingMethodInterceptor TRACE = new LoggingMethodInterceptor(new StaticConfiguration(Level.TRACE));
	private static final LoggingMethodInterceptor DEFAULT = new LoggingMethodInterceptor(new StaticConfiguration(Level.DEFAULT));
	private static final LoggingMethodInterceptor NONE = new LoggingMethodInterceptor(new StaticConfiguration(Level.NONE));

	private Configuration configuration;

	@SuppressWarnings("unchecked")
	private Class<Throwable>[] exceptionClasses = new Class[0];

	@SuppressWarnings("unchecked")
	private Class<Throwable>[] ignoredExceptionClasses = new Class[0];

	private LoggingMethodInterceptor(LoggingMethodInterceptor.Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Object interceptBefore(Object proxy, Method method, Object[] args, Object realTarget) {

		boolean matchedMethod = configuration.isMatchedMethod(method);
		if (matchedMethod) {
			Class<?> actualType = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());
			logBefore(getLogger(actualType), actualType, method.getName(), paramNames, args, configuration.getLevelToUseBefore(method), configuration.isLogArgs(method), configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
		return null;
	}

	@Override
	public void interceptAfter(Object proxy, Method method, Object[] args, Object realTarget, Object retObject,
			Object interceptBefore) {

		boolean matchedMethod = configuration.isMatchedMethod(method);
		if (matchedMethod) {
			Class<?> actualType = LoggingUtils.determineActualType(proxy);
			logAfter(getLogger(actualType), actualType, method.getName(), method.getReturnType(), configuration.getLevelToUseBefore(method), configuration.isLogReturning(method), configuration.isLogReturnValue(method), retObject, configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	@Override
	public void interceptAfterThrowing(Object proxy, Method method, Object[] args, Object realTarget, Throwable cause,
			Object interceptBeforeReturnObject) {

		boolean matchedMethod = configuration.isMatchedMethod(method);
		if (matchedMethod) {
			Class<?> actualType = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());
			logAfterThrowing(getLogger(actualType), actualType, method.getName(), paramNames, args, configuration.getLevelToUseAfterThrowing(method), configuration.isLogExceptions(method), exceptionClasses, ignoredExceptionClasses, cause, configuration.isPrintStackTrace(method), configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	public static LoggingMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean logReturning, boolean logReturnValue, boolean logExceptions, boolean includeStartAndEndMarkers) {
		return new LoggingMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, logReturning, logReturnValue, logExceptions, includeStartAndEndMarkers));
	}

	public static LoggingMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean logReturning, boolean logReturnValue, boolean logExceptions, boolean includeStartAndEndMarkers, boolean printStackTrace, Method... matchMethods) {
		return new LoggingMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, logReturning, logReturnValue, logExceptions, includeStartAndEndMarkers, printStackTrace, matchMethods));
	}

	public static LoggingMethodInterceptor of(Level level) {

		switch(level) {
		case ERROR:
			return LoggingMethodInterceptor.ERROR;
		case WARN:
			return LoggingMethodInterceptor.WARN;
		case INFO:
			return LoggingMethodInterceptor.INFO;
		case DEBUG:
			return LoggingMethodInterceptor.DEBUG;
		case TRACE:
			return LoggingMethodInterceptor.TRACE;
		case DEFAULT:
			return LoggingMethodInterceptor.DEFAULT;
		case NONE:
			return LoggingMethodInterceptor.NONE;
		default:
			throw new IllegalStateException("Level " + level + " was not found");
		}
	}

	public static LoggingMethodInterceptor of(Class<?> targetAnnotatedClass, Annotation annotation) {
		return new LoggingMethodInterceptor(new TargetAnnotationConfiguration(targetAnnotatedClass, annotation));
	}

	public static interface Configuration {

		public Level getLevelToUseBefore(Method method);

		public Level getLevelToUseAfter(Method method);

		public Level getLevelToUseAfterThrowing(Method method);

		public ToStringStrategy getToStringStrategy(Method method);

		public boolean isLogArgs(Method method);

		public boolean isLogReturning(Method method);

		public boolean isLogReturnValue(Method method);

		public boolean isLogExceptions(Method method);

		public boolean isIncludeStartAndEndMarkers(Method method);

		public boolean isPrintStackTrace(Method method);

		public boolean isMatchedMethod(Method method);
	}

	private static class StaticConfiguration implements Configuration {

		private Level levelToUse;

		private ToStringStrategy toStringStrategy = new ReflectionToStringStrategy();

		private boolean logArgs = true;

		private boolean logReturning = true;

		private boolean logReturnValue = true;

		private boolean logExceptions = true;

		private boolean includeStartAndEndMarkers = true;

		private boolean printStackTrace = false;

		private Method[] matchMethods = null;

		private StaticConfiguration(Level levelToUse) {
			this.levelToUse = levelToUse;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean logReturning, boolean logReturnValue, boolean logExceptions, boolean includeStartAndEndMarkers) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.logReturning = logReturning;
			this.logReturnValue = logReturnValue;
			this.logExceptions = logExceptions;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean logReturning, boolean logReturnValue, boolean logExceptions, boolean includeStartAndEndMarkers, boolean printStackTrace, Method... matchMethods) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.logReturning = logReturning;
			this.logReturnValue = logReturnValue;
			this.logExceptions = logExceptions;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
			this.printStackTrace = printStackTrace;
			this.matchMethods = matchMethods;
		}

		@Override
		public Level getLevelToUseBefore(Method method) {
			return levelToUse;
		}

		@Override
		public Level getLevelToUseAfter(Method method) {
			return levelToUse;
		}

		@Override
		public Level getLevelToUseAfterThrowing(Method method) {
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
		public boolean isLogArgs(Method method) {
			return logArgs;
		}

		@Override
		public boolean isLogReturning(Method method) {
			return logReturning;
		}

		@Override
		public boolean isLogReturnValue(Method method) {
			return logReturnValue;
		}

		@Override
		public boolean isLogExceptions(Method method) {
			return logExceptions;
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
		private Annotation annotation;

		private TargetAnnotationConfiguration(Class<?> targetType, Annotation annotation) {
			this.targetType = targetType;
			this.annotation = annotation;
		}

		private Log findLog(Class<?> targetType, Method method) {
			Log annotation = method.getAnnotation(Log.class);
			return annotation;
		}

		private com.groupcdg.carbon.logging.annotation.Error findError(Class<?> targetType, Method method) {
			com.groupcdg.carbon.logging.annotation.Error annotation = method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class);
			return annotation;
		}

		private Warn findWarn(Class<?> targetType, Method method) {
			Warn annotation = method.getAnnotation(Warn.class);
			return annotation;
		}

		private Info findInfo(Class<?> targetType, Method method) {
			Info annotation = method.getAnnotation(Info.class);
			return annotation;
		}

		private Debug findDebug(Class<?> targetType, Method method) {
			Debug annotation = method.getAnnotation(Debug.class);
			return annotation;
		}

		private Trace findTrace(Class<?> targetType, Method method) {
			Trace annotation = method.getAnnotation(Trace.class);
			return annotation;
		}

		private None findNone(Class<?> targetType, Method method) {
			None annotation = method.getAnnotation(None.class);
			return annotation;
		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return obtainToStringStrategy(ann.toStringStrategy(), ann.toStringStrategyStyleName());
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.includeStartAndEndMarkers();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isLogArgs(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.logArguments();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.logArguments();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isLogReturning(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.logAfter();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.logAfter();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isLogReturnValue(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.logReturnValue();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.logReturnValue();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isLogExceptions(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.logExceptions();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.logExceptions();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isPrintStackTrace(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return ann.printStackTrace();
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return ann.printStackTrace();
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public boolean isMatchedMethod(Method method) {

			if ((annotation.annotationType() == Log.class) && (method.getAnnotation(Log.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				return true;
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public Level getLevelToUseBefore(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return inferEffectiveLevel(ann, ann.level(), ann.beforeLevel());
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return inferEffectiveLevel(ann, Level.ERROR, ann.beforeLevel());
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return inferEffectiveLevel(ann, Level.WARN, ann.beforeLevel());
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return inferEffectiveLevel(ann, Level.INFO, ann.beforeLevel());
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return inferEffectiveLevel(ann, Level.DEBUG, ann.beforeLevel());
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return inferEffectiveLevel(ann, Level.TRACE, ann.beforeLevel());
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return inferEffectiveLevel(ann, Level.NONE, ann.beforeLevel());
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public Level getLevelToUseAfter(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return inferEffectiveLevel(ann, ann.level(), ann.afterLevel());
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return inferEffectiveLevel(ann, Level.ERROR, ann.afterLevel());
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return inferEffectiveLevel(ann, Level.WARN, ann.afterLevel());
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return inferEffectiveLevel(ann, Level.INFO, ann.afterLevel());
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return inferEffectiveLevel(ann, Level.DEBUG, ann.afterLevel());
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return inferEffectiveLevel(ann, Level.TRACE, ann.afterLevel());
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return inferEffectiveLevel(ann, Level.NONE, ann.afterLevel());
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}

		@Override
		public Level getLevelToUseAfterThrowing(Method method) {

			if ((annotation.annotationType() == Log.class)) {
				Log ann = findLog(targetType, method);
				return inferEffectiveLevel(ann, ann.level(), ann.exceptionLevel());
			} else if ((annotation.annotationType() == com.groupcdg.carbon.logging.annotation.Error.class) && (method.getAnnotation(com.groupcdg.carbon.logging.annotation.Error.class) != null)) {
				com.groupcdg.carbon.logging.annotation.Error ann = findError(targetType, method);
				return inferEffectiveLevel(ann, Level.ERROR, ann.exceptionLevel());
			} else if ((annotation.annotationType() == Warn.class) && (method.getAnnotation(Warn.class) != null)) {
				Warn ann = findWarn(targetType, method);
				return inferEffectiveLevel(ann, Level.WARN, ann.exceptionLevel());
			} else if ((annotation.annotationType() == Info.class) && (method.getAnnotation(Info.class) != null)) {
				Info ann = findInfo(targetType, method);
				return inferEffectiveLevel(ann, Level.INFO, ann.exceptionLevel());
			} else if ((annotation.annotationType() == Debug.class) && (method.getAnnotation(Debug.class) != null)) {
				Debug ann = findDebug(targetType, method);
				return inferEffectiveLevel(ann, Level.DEBUG, ann.exceptionLevel());
			} else if ((annotation.annotationType() == Trace.class) && (method.getAnnotation(Trace.class) != null)) {
				Trace ann = findTrace(targetType, method);
				return inferEffectiveLevel(ann, Level.TRACE, ann.exceptionLevel());
			} else if ((annotation.annotationType() == None.class) && (method.getAnnotation(None.class) != null)) {
				None ann = findNone(targetType, method);
				return inferEffectiveLevel(ann, Level.NONE, ann.exceptionLevel());
			} else {
				throw new IllegalStateException("Invalid annotation type matched: " + annotation.annotationType());
			}
		}
	}
}