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
package com.groupcdg.carbon.logging.proxy.spi;

import java.lang.reflect.Proxy;

import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;

public class ProxyFactory {

	private ProxyFactory() {
	}

	public static <V, W extends V> V getProxy(W inputObject, MethodInterceptor... interceptors) {

		if (interceptors != null && interceptors.length > 0) {

			V inputProxiedObject = inputObject;
			for (int i = 0; i < interceptors.length; i++) {
				try {
					inputProxiedObject = getProxy(inputObject, interceptors[i], inputProxiedObject);
				} catch (Throwable e) {
					throw new RuntimeException(e); // TODO Refactor
				}
			}
			return inputProxiedObject;
		} else {
			return inputObject;
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, W extends V> V  getProxy(W inObject, MethodInterceptor interceptor, V inProxiedObject) throws Throwable {

		GenericInvocationHandler invocationHandler = new GenericInvocationHandler();
		if (interceptor == null) {
			return inProxiedObject;
		}
		invocationHandler.setTarget(inProxiedObject);
		invocationHandler.setRealTarget(inObject);
		invocationHandler.setMethodInterceptor(interceptor);

		return (V) Proxy.newProxyInstance(inObject.getClass().getClassLoader(), inObject.getClass().getInterfaces(),
				invocationHandler);
	}
}