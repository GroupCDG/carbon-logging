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
package com.groupcdg.carbon.logging.cglib;

import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.annotation.Level;
import com.groupcdg.carbon.logging.cglib.spi.CglibFactory;
import com.groupcdg.carbon.logging.interceptor.PerformanceMethodInterceptor;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class PerformanceCglibFactory {

	public static <V, W extends V> V getProxy(Level level, W inputObject) throws Throwable {

		return CglibFactory.getProxy(inputObject, PerformanceMethodInterceptor.of(level));
	}

	public static <V, W extends V> V getProxy(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis, W inputObject) throws Throwable {
		return CglibFactory.getProxy(inputObject, PerformanceMethodInterceptor.of(level, toStringStrategy, logArgs, includeStartAndEndMarkers, thresholdMillis));
	}

	public static <V, W extends V> V getProxy(Level level, ToStringStrategy toStringStrategy, boolean logArgs, boolean includeStartAndEndMarkers, long thresholdMillis, W inputObject, Method... matchMethods) throws Throwable {
		return CglibFactory.getProxy(inputObject, PerformanceMethodInterceptor.of(level, toStringStrategy, logArgs, includeStartAndEndMarkers, thresholdMillis, matchMethods));
	}
}
