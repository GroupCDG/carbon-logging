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
package com.groupcdg.carbon.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.builder.ToStringStyle;

import com.groupcdg.carbon.logging.interceptor.ExceptionMethodInterceptor;
import com.groupcdg.carbon.logging.tostring.ReflectionToStringStrategy;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

/**
 * Used to mark where dedicated exception logging should be applied to methods
 * @author Chris Pheby
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Proxyable(interceptor = ExceptionMethodInterceptor.class)
public @interface LogExceptions {

	/**
	 * The default level to log at
	 * @return The default log level
	 */
    Level level() default Level.WARN;

    /**
     * Indicates whether the value of arguments passed to the given method should be logged
     * @return True if method arguments should be logged
     */
    boolean logArguments() default true;

    /**
     * Indicates which types of exception should be logged. If no types are specified,
     * then all exception types would be logged.
     * @return The exception types to log.
     */
    Class<? extends Throwable>[] logExceptionTypes() default {};

    /**
     * Indicates which types of exception should be ignored and not logged logged.
     * @return The exception types to ignore.
     */
    Class<? extends Throwable>[] ignoredExceptionTypes() default {};

    /**
     * Indicates whether the stack trace should be printed when logging exceptions
     * @return True if the stack trace should be printed
     */
    boolean printStackTrace() default false;

    /**
     * A strategy for outputting objects and method parameters as Strings
     * @return The Strategy
     */
    Class<? extends ToStringStrategy> toStringStrategy() default ReflectionToStringStrategy.class;

    /**
     * Optional. This configuration open is only used with the default ReflectionToStringStrategy.
     * Provides either one of the {@link ToStringStyle} listed types or a fully qualified class name for a ToStringStyle implementation
     * @return The name of the style
     */
    String toStringStrategyStyleName() default "SHORT_PREFIX_STYLE";

    /**
     * Print start and end markers. This is useful when you are using a Style where it is expected that there will be a
     * start and end block around the output.
     */
    boolean includeStartAndEndMarkers() default false;
}
