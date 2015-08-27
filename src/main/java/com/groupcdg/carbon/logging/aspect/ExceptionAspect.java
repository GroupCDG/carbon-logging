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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.groupcdg.carbon.logging.annotation.LogExceptions;
import com.groupcdg.carbon.logging.interceptor.ExceptionMethodInterceptor;

/**
 * Apply this logging aspect by using @LogExceptions at method or class level
 * To use this aspect you must define a Logger named ExceptionLogger.
 * This may have it's own appenders so that it writes to a different file.
 */
@Aspect
public class ExceptionAspect {

    @AfterThrowing(value="@within(log) && (!@annotation(com.groupcdg.carbon.logging.annotation.LogExceptions)) && execution(* *.*(..))", throwing="e")
    public void handleExceptionClassAnnotated(LogExceptions log, Throwable e, JoinPoint jp) {
    	doAfterThrowing(log, e, jp);
    }

    @AfterThrowing(value="@annotation(log) && execution(* *.*(..))", throwing="e")
    public void handleExceptionMethodAnnotated(LogExceptions log, Throwable e, JoinPoint jp) {
        doAfterThrowing(log, e, jp);
    }

    private void doAfterThrowing(LogExceptions log, Throwable throwable, JoinPoint jp) {


    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature)jp.getSignature()).getMethod();

    	ExceptionMethodInterceptor interceptor = ExceptionMethodInterceptor.of(targetClass, log);

    	interceptor.interceptAfterThrowing(targetClass, method, jp.getArgs(), actualClass, throwable, null);
    }
}
