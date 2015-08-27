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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import com.groupcdg.carbon.logging.annotation.Debug;
import com.groupcdg.carbon.logging.annotation.Error;
import com.groupcdg.carbon.logging.annotation.Info;
import com.groupcdg.carbon.logging.annotation.Log;
import com.groupcdg.carbon.logging.annotation.None;
import com.groupcdg.carbon.logging.annotation.Trace;
import com.groupcdg.carbon.logging.annotation.Warn;
import com.groupcdg.carbon.logging.interceptor.LoggingMethodInterceptor;

/**
 * This aspect can be used with @Error, @Warn, @Info, @Debug and @Trace annotations
 * to log specific methods. Alternatively the @Log annotation may also be used.
 */
@Aspect
public class LoggingAspect {

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Log * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Log log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Log * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Log log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Log * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Log log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.None * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, None log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.None * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, None log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.None * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, None log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Debug * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Debug log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Debug * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Debug log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Debug * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Debug log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Error * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Error log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Error * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Error log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Error * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Error log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Info * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Info log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Info * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Info log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Info * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Info log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Trace * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Trace log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Trace * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Trace log) {
    	doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Trace * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Trace log, Object returnValue) {
    	doAfter(log, returnValue, joinPoint);
    }

    @Before(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Warn * *(..))", argNames = "joinPoint, log")
    public void beforeAnnotatedMethod(JoinPoint joinPoint, Warn log) {
    	doBefore(log, joinPoint);
    }

    @AfterThrowing(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Warn * *(..))", throwing = "throwable", argNames = "joinPoint, throwable, log")
    public void afterAnnotatedMethodThrowing(JoinPoint joinPoint, Throwable throwable, Warn log) throws Throwable {
        doAfterThrowing(log, throwable, joinPoint);
    }

    @AfterReturning(value = "@annotation(log) && execution(@com.groupcdg.carbon.logging.annotation.Warn * *(..))", returning = "returnValue", argNames = "joinPoint, log, returnValue")
    public void afterAnnotatedMethod(JoinPoint joinPoint, Warn log, Object returnValue) {
        doAfter(log, returnValue, joinPoint);
    }


    private void doBefore(Annotation log, JoinPoint jp) {

    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature) jp.getSignature()).getMethod();

    	LoggingMethodInterceptor interceptor = LoggingMethodInterceptor.of(targetClass, log);

    	interceptor.interceptBefore(targetClass, method, jp.getArgs(), actualClass);
    }

    private void doAfterThrowing(Annotation log, Throwable throwable, JoinPoint jp) {

    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature)jp.getSignature()).getMethod();

    	LoggingMethodInterceptor interceptor = LoggingMethodInterceptor.of(targetClass, log);

    	interceptor.interceptAfterThrowing(targetClass, method, jp.getArgs(), actualClass, throwable, null);
    }


    private void doAfter(Annotation log, Object returnValue, JoinPoint jp) {

    	Class<?> targetClass = jp.getTarget().getClass();
    	Class<?> actualClass = determineActualType(jp.getTarget());

    	Method method = ((MethodSignature)jp.getSignature()).getMethod();

    	LoggingMethodInterceptor interceptor = LoggingMethodInterceptor.of(targetClass, log);

    	interceptor.interceptAfter(targetClass, method, jp.getArgs(), actualClass, returnValue, null);

    }
}
