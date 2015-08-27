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

/**
 * Levels used for indicating the significance of a log event. Levels are organized from most specific to least:
 * <ul>
 * <li>{@link #ERROR}</li>
 * <li>{@link #WARN}</li>
 * <li>{@link #INFO}</li>
 * <li>{@link #DEBUG}</li>
 * <li>{@link #TRACE}</li>
 * </ul>
 * NONE indicates logging will not be performed, whilst DEFAULT indicates no preference.
 */
public enum Level {

    /**
     * An error in the application, possibly recoverable.
     */
    ERROR,
    /**
     * An event that might possible lead to an error.
     */
    WARN,
    /**
     * An event for informational purposes.
     */
    INFO,
    /**
     * ExampleObjectForLogging general debugging event.
     */
    DEBUG,
    /**
     * ExampleObjectForLogging fine-grained debug message, typically capturing the flow through the application.
     */
    TRACE,
    /**
     * Use the default level - typically TRACE. In the case of the exception level,
     * the standard level will be used
     */
    DEFAULT,
    /**
     * Indicates that no logging should be performed
     */
    NONE
}
