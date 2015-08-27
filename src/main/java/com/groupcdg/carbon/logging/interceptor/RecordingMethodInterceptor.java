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
import static com.groupcdg.carbon.logging.helper.spi.RecordingHelper.getLogger;
import static com.groupcdg.carbon.logging.helper.spi.RecordingHelper.logAfter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.annotation.Record;
import com.groupcdg.carbon.logging.helper.spi.LoggingUtils;
import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class RecordingMethodInterceptor implements MethodInterceptor {

	private static final RecordingMethodInterceptor ERROR = new RecordingMethodInterceptor(new StaticConfiguration(Level.ERROR));
	private static final RecordingMethodInterceptor WARN = new RecordingMethodInterceptor(new StaticConfiguration(Level.WARN));
	private static final RecordingMethodInterceptor INFO = new RecordingMethodInterceptor(new StaticConfiguration(Level.INFO));
	private static final RecordingMethodInterceptor DEBUG = new RecordingMethodInterceptor(new StaticConfiguration(Level.DEBUG));
	private static final RecordingMethodInterceptor TRACE = new RecordingMethodInterceptor(new StaticConfiguration(Level.TRACE));
	private static final RecordingMethodInterceptor DEFAULT = new RecordingMethodInterceptor(new StaticConfiguration(Level.DEFAULT));
	private static final RecordingMethodInterceptor NONE = new RecordingMethodInterceptor(new StaticConfiguration(Level.NONE));

	private Configuration configuration;

	private RecordingMethodInterceptor(RecordingMethodInterceptor.Configuration configuration) {
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

			logAfter(getLogger(), proxiedInterface, method.getName(), paramNames, args, configuration.getLevelToUse(method), configuration.isLogArgs(method), ((Long)interceptBefore).longValue(), finish, configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	@Override
	public void interceptAfterThrowing(Object proxy, Method method, Object[] args, Object realTarget, Throwable cause,
			Object interceptBefore) {

		boolean matchedMethod = configuration.isMatchedMethod(method);
		if (matchedMethod) {
			long finish = System.currentTimeMillis();
			Class<?> proxiedInterface = LoggingUtils.determineActualType(proxy);
			String[] paramNames = determineParameterNames(method.getParameters());

			logAfter(getLogger(), proxiedInterface, method.getName(), paramNames, args, configuration.getLevelToUse(method), configuration.isLogArgs(method), ((Long)interceptBefore).longValue(), finish, configuration.getToStringStrategy(method), configuration.isIncludeStartAndEndMarkers(method));
		}
	}

	public static RecordingMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers) {
		return new RecordingMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, includeStartAndEndMarkers));
	}

	public static RecordingMethodInterceptor of(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, Method... matchMethods) {
		return new RecordingMethodInterceptor(new StaticConfiguration(level, toStringStrategy, logArgs, includeStartAndEndMarkers, matchMethods));
	}

	public static RecordingMethodInterceptor of(Level level) {

		switch(level) {
		case ERROR:
			return RecordingMethodInterceptor.ERROR;
		case WARN:
			return RecordingMethodInterceptor.WARN;
		case INFO:
			return RecordingMethodInterceptor.INFO;
		case DEBUG:
			return RecordingMethodInterceptor.DEBUG;
		case TRACE:
			return RecordingMethodInterceptor.TRACE;
		case DEFAULT:
			return RecordingMethodInterceptor.DEFAULT;
		case NONE:
			return RecordingMethodInterceptor.NONE;
		default:
			throw new IllegalStateException("Level " + level + " was not found");
		}
	}

	public static RecordingMethodInterceptor of(Class<?> targetAnnotatedClass, Annotation annotation) {
		return new RecordingMethodInterceptor(new TargetAnnotationConfiguration(targetAnnotatedClass));
	}

	public static interface Configuration {

		public Level getLevelToUse(Method method);

		public boolean isLogArgs(Method method);

		public ToStringStrategy getToStringStrategy(Method method);

		public boolean isIncludeStartAndEndMarkers(Method method);

		public boolean isMatchedMethod(Method method);
	}

	private static class StaticConfiguration implements Configuration {

		private Level levelToUse;

		private boolean logArgs = true;

		private ToStringStrategy toStringStrategy = new ReflectionToStringStrategy();

		private boolean includeStartAndEndMarkers = true;

		private Method[] matchMethods = null;

		private StaticConfiguration(Level levelToUse) {
			this.levelToUse = levelToUse;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
		}

		private StaticConfiguration(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, Method... matchMethods) {
			this.levelToUse = level;
			this.toStringStrategy = toStringStrategy;
			this.logArgs = logArgs;
			this.includeStartAndEndMarkers = includeStartAndEndMarkers;
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

		private Record findRecord(Class<?> targetType, Method method) {
			Record record = method.getAnnotation(Record.class);
			if (record == null) {
				record = targetType.getAnnotation(Record.class);
			}
			return record;
		}

		@Override
		public Level getLevelToUse(Method method) {
			Record record = findRecord(targetType, method);
			return record.level();
		}

		@Override
		public boolean isLogArgs(Method method) {
			Record record = findRecord(targetType, method);
			return record.logArguments();		}

		@Override
		public ToStringStrategy getToStringStrategy(Method method) {
			Record record = findRecord(targetType, method);
			return obtainToStringStrategy(record.toStringStrategy(), record.toStringStrategyStyleName());
		}

		@Override
		public boolean isIncludeStartAndEndMarkers(Method method) {
			Record record = findRecord(targetType, method);
			return record.includeStartAndEndMarkers();
		}

		@Override
		public boolean isMatchedMethod(Method method) {
			if (method.getAnnotation(Record.class) != null) {
				return true;
			} if (targetType.getAnnotation(Record.class) != null) {
				return true;
			} else {
				return false;
			}
		}
	}
}