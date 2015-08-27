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
package com.groupcdg.carbon.logging;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.groupcdg.carbon.logging.annotation.LogExceptions;
import com.groupcdg.carbon.logging.annotation.LogPerformance;
import com.groupcdg.carbon.logging.annotation.Record;
import com.groupcdg.carbon.logging.annotation.Warn;
import com.groupcdg.carbon.logging.tostring.JacksonToStringStrategy;

@Component
@LogPerformance(thresholdMilliseconds=1000, logArguments=false)
public class ExampleServiceWithLogging { // implements ExampleService {

    @Record
    @Warn(toStringStrategy=JacksonToStringStrategy.class, includeStartAndEndMarkers=true)
    @LogExceptions
    public String serviceMethod(int a, String b, Object c, Object d) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            //
        }
         return Strings.repeat(b.toString(), a) + " " + d.toString();
    }
}
