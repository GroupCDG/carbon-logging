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

import com.groupcdg.carbon.logging.annotation.LogPerformance;
import com.groupcdg.carbon.logging.interceptor.PerformanceMethodInterceptor;

/**
 * Apply this logging aspect by using @LogPerformance at method or class level
 *
 * To use this aspect you must define Loggers named Perf1Logger, Perf5Logger, Perf10Logger, Perf30Logger, PerfMaxLogger.
 * These may have each their own appenders so that each logger can write to a different file. The loggers are used for
 * &lt;=1s, &lt;=5s, &lt;= 10 s, &lt;= 30 s and &gt; 30s greater than the configured threshold respectively.
 */
@Aspect
public class PerformanceAspect {

    @Around(value = "@within(log) && (!@annotation(com.groupcdg.carbon.logging.annotation.LogPerformance)) && execution(* *.*(..))")
    public Object handlePerformanceClassAnnotated(LogPerformance log, ProceedingJoinPoint jp) throws Throwable {

        return doAround(log, jp);
    }

    @Around(value = "@annotation(log) && execution(* *.*(..))")
    public Object handlePerformanceMethodAnnotated(LogPerformance log, ProceedingJoinPoint jp) throws Throwable {

        return doAround(log, jp);
    }

    private Object doAround(LogPerformance log, ProceedingJoinPoint jp) throws Throwable {

    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature)jp.getSignature()).getMethod();

    	PerformanceMethodInterceptor interceptor = PerformanceMethodInterceptor.of(targetClass, log);

    	Long start = interceptor.interceptBefore(targetClass, method, jp.getArgs(), actualClass);

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
