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

import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.determineParameterNames;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.matchMethod;
import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.obtainToStringStrategy;
import static com.groupcdg.carbon.logging.helper.spi.PerformanceHelper.logAfter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.annotation.LogPerformance;
import com.groupcdg.carbon.logging.helper.spi.LoggingUtils;
import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class PerformanceMethodInterceptor implements MethodInterceptor {

	private static final PerformanceMethodInterceptor ERROR = new PerformanceMethodInterceptor(new StaticConfiguration(Level.ERROR));
	private static final PerformanceMethodInterceptor WARN = new PerformanceMethodInterceptor(new StaticConfiguration(Level.WARN));
	private static final PerformanceMethodInterceptor INFO = new PerformanceMethodInterceptor(new StaticConfiguration(Level.INFO));
	private static final PerformanceMethodInterceptor DEBUG = new PerformanceMethodInterceptor(new StaticConfiguration(Level.DEBUG));
	private static final PerformanceMethodInterceptor TRACE = new PerformanceMethodInterceptor(new StaticConfiguration(Level.TRACE));
	private static final PerformanceMethodInterceptor DEFAULT = new PerformanceMethodInterceptor(new StaticConfiguration(Level.DEFAULT));
	private static final PerformanceMethodInterceptor NONE = new PerformanceMethodInterceptor(new StaticConfiguration(Level.NONE));

	private Configuration configuration;

	private PerformanceMethodInterceptor(PerformanceMethodInterceptor.Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Long interceptBefore(Object proxy, Method method, Object[] args, Object realTarget) {

		if (configuration.isMatchedMethod(method)) {
			return Long.valueOf(System.currentTimeMillis());
		}
		return null;
	}

	@Override
	public void interceptAfter(Object proxy, Method method, Object[] args, Object realTarget, Object retObject,
			Object interceptBefore) {

		if (configuration.isMatchedMethod(method)) {
			long finish = System.currentTimeMillis();
			Class<?> proxiedInterface = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());

			logAfter(proxiedInterface, method.getName(), paramNames, args, configuration.getLevelToUse(method), configuration.isLogArgs(method), ((Long)interceptBefore).longValue(), finish, configuration.getThresholdMillis(method), configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	@Override
	public void interceptAfterThrowing(Object proxy, Method method, Object[] args, Object realTarget, Throwable cause,
			Object interceptBefore) {

		if (configuration.isMatchedMethod(method)) {
			long finish = System.currentTimeMillis();
			Class<?> proxiedInterface = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());

			logAfter(proxiedInterface, method.getName(), paramNames, args, configuration.getLevelToUse(method), configuration.isLogArgs(method), ((Long)interceptBefore).longValue(), finish, configuration.getThresholdMillis(method), configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	public static PerformanceMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis) {
		return new PerformanceMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, includeStartAndEndMarkers, thresholdMillis));
	}

	public static PerformanceMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis, Method... matchMethods) {
		return new PerformanceMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, includeStartAndEndMarkers, thresholdMillis, matchMethods));
	}

	public static PerformanceMethodInterceptor of(Level level) {

		switch(level) {
		case ERROR:
			return PerformanceMethodInterceptor.ERROR;
		case WARN:
			return PerformanceMethodInterceptor.WARN;
		case INFO:
			return PerformanceMethodInterceptor.INFO;
		case DEBUG:
			return PerformanceMethodInterceptor.DEBUG;
		case TRACE:
			return PerformanceMethodInterceptor.TRACE;
		case DEFAULT:
			return PerformanceMethodInterceptor.DEFAULT;
		case NONE:
			return PerformanceMethodInterceptor.NONE;
		default:
			throw new IllegalStateException("Level " + level + " was not found");
		}
	}

	public static PerformanceMethodInterceptor of(Class<?> targetAnnotatedClass, Annotation annotation) {
		return new PerformanceMethodInterceptor(new TargetAnnotationConfiguration(targetAnnotatedClass));
	}

	public static interface Configuration {

		public Level getLevelToUse(Method method);

		public boolean isLogArgs(Method method);

		public ToStringStrategy getToStringStrategy(Method method);

		public boolean isIncludeStartAndEndMarkers(Method method);

		public long getThresholdMillis(Method method);

		public boolean isMatchedMethod(Method method);
	}

	private static class StaticConfiguration implements Configuration {

		private Level levelToUse;

		private boolean logArgs = true;

		private ToStringStrategy toStringStrategy = new ReflectionToStringStrategy();

		private boolean includeStartAndEndMarkers = true;

		private long thresholdMillis;

		private Method[] matchMethods = null;

		private StaticConfiguration(Level levelToUse) {
			this.levelToUse = levelToUse;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
			this.thresholdMillis = thresholdMillis;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis, Method... matchMethods) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
			this.thresholdMillis = thresholdMillis;
			this.matchMethods = matchMethods;
		}

		@Override
		public Level getLevelToUse(Method method) {
			return levelToUse;
		}

		@Override
		public boolean isLogArgs(Method method) {
			return logArgs;
		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {
			return toStringStrategy;
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {
			return includeStartAndEndMarkers;
		}

		public long getThresholdMillis(Method method) {
			return thresholdMillis;
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

		private LogPerformance findLogPerformance(Class<?> targetType, Method method) {
			LogPerformance performance = method.getAnnotation(LogPerformance.class);
			if (performance == null) {
				performance = targetType.getAnnotation(LogPerformance.class);
			}
			return performance;
		}

		@Override
		public Level getLevelToUse(Method method) {
			LogPerformance performance = findLogPerformance(targetType, method);
			return performance.level();
		}

		@Override
		public boolean isLogArgs(Method method) {
			LogPerformance performance = findLogPerformance(targetType, method);
			return performance.logArguments();		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {
			LogPerformance performance = findLogPerformance(targetType, method);
			return obtainToStringStrategy(performance.toStringStrategy(), performance.toStringStrategyStyleName());
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {
			LogPerformance performance = findLogPerformance(targetType, method);
			return performance.includeStartAndEndMarkers();
		}

		public long getThresholdMillis(Method method) {
			LogPerformance performance = findLogPerformance(targetType, method);
			return performance.thresholdMilliseconds();
		}

		@Override
		public boolean isMatchedMethod(Method method) {
			if (method.getAnnotation(LogPerformance.class) != null) {
				return true;
			} if (targetType.getAnnotation(LogPerformance.class) != null) {
				return true;
			} else {
				return false;
			}
		}
	}

}