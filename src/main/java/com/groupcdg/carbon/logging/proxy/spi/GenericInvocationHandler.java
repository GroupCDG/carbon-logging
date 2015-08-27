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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;

public class GenericInvocationHandler implements java.lang.reflect.InvocationHandler {

	private Object target = null;

	public void setTarget(Object target) {
		this.target = target;
	}

	private Object realtarget = null;

	public void setRealTarget(Object realtarget) {
		this.realtarget = realtarget;
	}

	MethodInterceptor methodInterceptor = null;

	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor = methodInterceptor;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Object interceptBeforeReturnObject = null;
		try {
			if (methodInterceptor != null) {
				interceptBeforeReturnObject = methodInterceptor.interceptBefore(proxy, method, args, realtarget);
			}
			Object retObject = method.invoke(target, args);
			if (methodInterceptor != null) {
				methodInterceptor.interceptAfter(proxy, method, args, realtarget, retObject, interceptBeforeReturnObject);
			}
			return retObject;
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException();
			methodInterceptor.interceptAfterThrowing(proxy, method, args, realtarget, cause, interceptBeforeReturnObject);
			throw e.getTargetException();
		}

		catch (Exception e) {
			throw e;
		}
	}
}
