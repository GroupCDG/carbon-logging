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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.groupcdg.carbon.logging.tostring.api.ToStringStrategy;

public class ReflectionToStringStrategy implements ToStringStrategy {

	public ReflectionToStringStrategy() {
	}

	private static final Map<String, ToStringStyle> STYLES = new HashMap<>();
	static {
		for (Field next : ToStringStyle.class.getDeclaredFields()) {
			int mods = next.getModifiers();
			if (Modifier.isStatic(mods) && Modifier.isFinal(mods)
					&& ToStringStyle.class.isAssignableFrom(next.getType())) {
				try {
					STYLES.put(next.getName(), (ToStringStyle)(next.get(null)));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new IllegalStateException("Could not initialise logging aspect for ToStringStyles: " + e.getMessage());
				}
			}
		}
	}

	private ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;

	public void setStyle(ToStringStyle style) {
		this.style = style;
	}

	public void setStyleName(String stringStyle) {

    	if ("SHORT_PREFIX_STYLE".equals(stringStyle)) {
    		this.style = ToStringStyle.SHORT_PREFIX_STYLE;
    	} else if ("DEFAULT_STYLE".equals(stringStyle)) {
    		this.style = ToStringStyle.DEFAULT_STYLE;
    	} else if ("MULTI_LINE_STYLE".equals(stringStyle)) {
    		this.style = ToStringStyle.MULTI_LINE_STYLE;
    	} else if ("NO_FIELD_NAMES_STYLE".equals(stringStyle)) {
    		this.style = ToStringStyle.NO_FIELD_NAMES_STYLE;
    	} else if ("SIMPLE_STYLE".equals(stringStyle)) {
    		this.style = ToStringStyle.SIMPLE_STYLE;
    	} else {

			ToStringStyle style = STYLES.get(stringStyle);
			if (style == null) {
				try {
					style = (ToStringStyle) Class.forName(stringStyle).newInstance();
					STYLES.put(stringStyle, style);
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					throw new IllegalStateException("Could not find or create class for ToStringStyle: " + stringStyle, e);
				}
			}
    	}
	}

    @Override
	public final String fieldsToString(boolean includeStartAndEndMarkers, String[] paramNames, Object[] obj) {

		String fieldSeparator;
		try {
			fieldSeparator = (String)(FIELDSEPARATORMETHOD.invoke(style));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Could not determine separator", e);
		}

        if (obj == null) {
            return null;
        }

        StringBuilder nextResult = new StringBuilder();

        if (includeStartAndEndMarkers) {
	        String startContent;
			try {
				startContent = (String)(STARTCONTENTMETHOD.invoke(style));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException("Could not determine start content", e);
			}
	        nextResult.append(startContent);
        }

        for (int i=0; i < obj.length; i++) {
        	if (paramNames != null && PARAM_NAMES_AVAILABLE) {
        		StringBuffer fieldStartBuffer = new StringBuffer();

        		try {
        			APPENDFIELDSTARTMETHOD.invoke(style, fieldStartBuffer, paramNames[i]);
					nextResult.append(fieldStartBuffer.toString());
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new IllegalStateException("Could not determine output param name: " + paramNames[i], e);
				}
        	}
        	nextResult.append(objectToString(obj[i]));
        	if (i < (obj.length - 1)) {
        		nextResult.append(fieldSeparator);
        	}
        }

        if (includeStartAndEndMarkers) {
		    String endContent;
			try {
				endContent = (String)(ENDCONTENTMETHOD.invoke(style));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException("Could not determine e", e);
			}
		    nextResult.append(endContent);
        }

        return nextResult.toString();
    }

    @Override
	public final String objectToString(Object obj) {

        if (obj == null) {
            return null;
        }

        try {
            Class<?> cls = obj.getClass().getMethod("toString").getDeclaringClass();

            StringBuffer buf = new StringBuffer();
            if (cls.equals(Object.class)) {
                // Doesn't override toString, lets construct our own
                buf.append(ReflectionToStringBuilder.reflectionToString(obj, style, true));
            } else {
            	APPENDDETAILMETHOD.invoke(style, buf, null, obj);
            }

            return buf.toString();
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        	// Use generic toString() method instead
        	StringBuffer buf = new StringBuffer();
            buf.append(obj.toString());

            return buf.toString();
        }
    }

	private static boolean PARAM_NAMES_AVAILABLE;
	static {
		try {
			Class.forName("java.lang.reflect.Executable");
			PARAM_NAMES_AVAILABLE = true;
		} catch (ClassNotFoundException e) {
			PARAM_NAMES_AVAILABLE = false;
		}
	}

	private static Method FIELDSEPARATORMETHOD;
	static {
		try {
			FIELDSEPARATORMETHOD = ToStringStyle.class.getDeclaredMethod("getFieldSeparator");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not obtain method: " + e.getMessage(), e);
		}
		FIELDSEPARATORMETHOD.setAccessible(true);
	}

	private static Method APPENDFIELDSTARTMETHOD;
	static {
		try {
			APPENDFIELDSTARTMETHOD = ToStringStyle.class.getDeclaredMethod("appendFieldStart", StringBuffer.class, String.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not obtain method: " + e.getMessage(), e);
		}
		APPENDFIELDSTARTMETHOD.setAccessible(true);
	}

	private static Method STARTCONTENTMETHOD;
	static {
		try {
			STARTCONTENTMETHOD = ToStringStyle.class.getDeclaredMethod("getContentStart");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not obtain method: " + e.getMessage(), e);
		}
		STARTCONTENTMETHOD.setAccessible(true);
	}

	private static Method ENDCONTENTMETHOD;
	static {
		try {
			ENDCONTENTMETHOD = ToStringStyle.class.getDeclaredMethod("getContentEnd");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not obtain method: " + e.getMessage(), e);
		}
		ENDCONTENTMETHOD.setAccessible(true);
	}

	private static Method APPENDDETAILMETHOD;
	static {
		try {
			APPENDDETAILMETHOD = ToStringStyle.class.getDeclaredMethod("appendDetail", StringBuffer.class, String.class, Object.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not obtain method: " + e.getMessage(), e);
		}
		APPENDDETAILMETHOD.setAccessible(true);
	}
}
