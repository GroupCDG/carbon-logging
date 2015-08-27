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
package com.groupcdg.carbon.logging.tostring;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class JacksonToStringStrategy implements ToStringStrategy {

	private static final JsonFactory JSON_FACTORY = new JsonFactory();

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static boolean PARAM_NAMES_AVAILABLE;
	static {
		try {
			Class.forName("java.lang.reflect.Executable");
			PARAM_NAMES_AVAILABLE = true;
		} catch (ClassNotFoundException e) {
			PARAM_NAMES_AVAILABLE = false;
		}
	}

    @Override
	public final String fieldsToString(boolean includeStartAndEndMarkers, String[] paramNames, Object[] obj) {

    	StringWriter writer = new StringWriter();

    	JsonGenerator generator = null;

        try {
        	generator = JSON_FACTORY.createGenerator(writer);
        	generator.setCodec(OBJECT_MAPPER);

	        if (includeStartAndEndMarkers) {
	        	generator.writeStartObject();
	        }

	        for (int i=0; i < obj.length; i++) {
	        	if (paramNames != null && PARAM_NAMES_AVAILABLE) {
	        		generator.writeObjectField(paramNames[i], obj[i]);
	        	} else {
	        		generator.writeObjectField("arg" + i, obj[i]);
	        	}
	        }

	        if (includeStartAndEndMarkers) {
	        	generator.writeEndObject();
	        }

	    	return writer.toString();
        } catch (IOException e) {
			throw new IllegalStateException("Could not generate JSON for Params: " + e.getMessage(), e);
        } finally {
        	try {
        		if (generator != null) {
        			generator.close();
        		}
			} catch (IOException e) {
			}
        	try {
				writer.close();
			} catch (IOException e) {
			}
        }
    }

    @Override
	public final String objectToString(Object obj) {

        if (obj == null) {
            return null;
        }

        try {
			return OBJECT_MAPPER.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Could not generate JSON for Object", e);
		}
    }
}
