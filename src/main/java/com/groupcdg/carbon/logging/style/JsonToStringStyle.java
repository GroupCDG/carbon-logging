/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Extracted by Chris Pheby from 
 * https://github.com/thiagoh/commons-lang/blob/
 * 609775c1acfae2da487fb762337a7d4d9c44cef5/
 * src/main/java/org/apache/commons/lang3/builder/ToStringStyle.java
 */
package com.groupcdg.carbon.logging.style;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * <p>
 * <code>ToStringStyle</code> that outputs with JSON format.
 * </p>
 */
public final class JsonToStringStyle extends ToStringStyle {

	public static final JsonToStringStyle JSON_STYLE = new JsonToStringStyle();
	
	private static final long serialVersionUID = 1L;

	/**
	 * The summary size text start <code>'&gt;'</code>.
	 */
	private String FIELD_NAME_PREFIX = "\"";

	/**
	 * <p>
	 * Constructor.
	 * </p>
	 *
	 * <p>
	 * Use the static constant rather than instantiating.
	 * </p>
	 */
	public JsonToStringStyle() {
		super();

		this.setUseClassName(false);
		this.setUseIdentityHashCode(false);

		this.setContentStart("{");
		this.setContentEnd("}");

		this.setArrayStart("[");
		this.setArrayEnd("]");

		this.setFieldSeparator(",");
		this.setFieldNameValueSeparator(":");

		this.setNullText("null");

		this.setSummaryObjectStartText("");
		this.setSummaryObjectEndText("");

		this.setSizeStartText("size=");
		this.setSizeEndText("");
	}

	@Override
	protected void appendFieldStart(StringBuffer buffer, String fieldName) {

		super.appendFieldStart(buffer, FIELD_NAME_PREFIX + fieldName + FIELD_NAME_PREFIX);
	}

	@Override
	protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {

		if (value == null) {

			appendNullText(buffer, fieldName);
			return;
		}

		if (value.getClass() == String.class) {

			appendValueAsString(buffer, (String) value);
			return;
		}

		buffer.append(value);
	}

	private void appendValueAsString(StringBuffer buffer, String value) {

		buffer.append("\"" + value + "\"");
	}

	/**
	 * <p>
	 * Ensure <code>Singleton</code> after serialization.
	 * </p>
	 *
	 * @return the singleton
	 */
	private Object readResolve() {
		return JSON_STYLE;
	}

}