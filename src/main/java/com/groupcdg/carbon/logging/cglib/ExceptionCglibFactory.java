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
import com.groupcdg.carbon.logging.interceptor.ExceptionMethodInterceptor;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class ExceptionCglibFactory {

	public static <V, W extends V> V getProxy(Level level, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers, boolean printStackTrace, W inputObject, Method... matchMethods) throws Throwable {

		return CglibFactory.getProxy(inputObject, ExceptionMethodInterceptor.of(level, toStringStrategy, includeStartAndEndMarkers, printStackTrace, matchMethods));
	}

	public static <V, W extends V> V getProxy(Level level, ToStringStrategy toStringStrategy, boolean includeStartAndEndMarkers, W inputObject) throws Throwable {

		return CglibFactory.getProxy(inputObject, ExceptionMethodInterceptor.of(level, toStringStrategy, includeStartAndEndMarkers));
	}

	public static <V, W extends V> V getProxy(Level level, ToStringStrategy toStringStrategy, W inputObject) throws Throwable {

		return CglibFactory.getProxy(inputObject, ExceptionMethodInterceptor.of(level, toStringStrategy));
	}

	public static <V, W extends V> V getProxy(Level level, W inputObject) throws Throwable {

		return CglibFactory.getProxy(inputObject, ExceptionMethodInterceptor.of(level));
	}
}
