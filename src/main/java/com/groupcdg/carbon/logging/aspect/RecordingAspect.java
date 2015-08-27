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
package com.groupcdg.carbon.logging.aspect;

import static com.groupcdg.carbon.logging.helper.spi.LoggingUtils.determineActualType;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.groupcdg.carbon.logging.annotation.Record;
import com.groupcdg.carbon.logging.interceptor.RecordingMethodInterceptor;

/**
 * Apply this logging aspect by using @Record at method or class level
 */
@Aspect
public class RecordingAspect {

    @Around(value = "@within(log) && (!@annotation(com.groupcdg.carbon.logging.annotation.Record)) && execution(* *.*(..))")
    public Object handlePerformanceClassAnnotated(Record log, ProceedingJoinPoint jp) throws Throwable {

        return doAround(log, jp);
    }

    @Around(value = "@annotation(log) && execution(* *.*(..))")
    public Object handlePerformanceMethodAnnotated(Record log, ProceedingJoinPoint jp) throws Throwable {

    	return doAround(log, jp);
    }

    private Object doAround(Record log, ProceedingJoinPoint jp) throws Throwable {

    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature)jp.getSignature()).getMethod();

    	RecordingMethodInterceptor interceptor = RecordingMethodInterceptor.of(targetClass, log);

    	Long start = interceptor.interceptBefore(jp.getTarget(), method, jp.getArgs(), actualClass);

        Object result = null;
		try {
			result = jp.proceed();
	        interceptor.interceptAfter(targetClass, method, jp.getArgs(), actualClass, result, start);
		} catch (Throwable e) {
			interceptor.interceptAfterThrowing(targetClass, method, jp.getArgs(), actualClass, e, start);
			throw e;
		}

		return result;
    }
}
