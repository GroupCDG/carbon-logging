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
package com.groupcdg.carbon.logging.cglib;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.groupcdg.carbon.logging.annotation.Proxyable;
import com.groupcdg.carbon.logging.cglib.spi.CglibFactory;
import com.groupcdg.carbon.logging.helper.spi.LoggingUtils;
import com.groupcdg.carbon.logging.interceptor.api.MethodInterceptor;

public class AnnotationCglibFactory {

	public static <V, W extends V> V getProxy(W inputObject, Method... matchMethods) throws Throwable {

		Class<?> underlyingTarget = LoggingUtils.determineActualType(inputObject);

		@SuppressWarnings("unchecked")
		List<Class<? extends Annotation>> proxyableTypes = getClassAndMethodAnnotationTypesWithMetaAnnotation(underlyingTarget, new ElementType[] {ElementType.TYPE,  ElementType.METHOD}, true, Proxyable.class);

		MethodInterceptor[] interceptors = new MethodInterceptor[proxyableTypes.size()];
		for (int i =0; i < proxyableTypes.size(); i++) {
			Class<? extends Annotation> proxyAnnotationType = proxyableTypes.get(i);
			Class<? extends MethodInterceptor> methodInterceptor = proxyAnnotationType.getAnnotation(Proxyable.class).interceptor();
			Method ofMethod = methodInterceptor.getMethod("of", Class.class, Class.class);
			MethodInterceptor interceptor = (MethodInterceptor) ofMethod.invoke(null, underlyingTarget, proxyAnnotationType);
			interceptors[i] = interceptor;
		}

		return CglibFactory.getProxy(inputObject, interceptors);
	}

	public static final List<Class<? extends Annotation>> getClassAndMethodAnnotationTypesWithMetaAnnotation(Class<?> targetClass, ElementType[] searchLocations, boolean searchHierarchy, @SuppressWarnings("unchecked") Class<? extends Annotation>... metaAnnotations) {

		// TODO Should resolve bridge methods / synthetics like Spring's AnnotationUtils?
		List<Class<? extends Annotation>> result = new ArrayList<>();

		for (ElementType searchLocation : searchLocations) {

			if (ElementType.TYPE == searchLocation) {
				Annotation[] annotations = targetClass.getAnnotations();
				for (Annotation next : annotations) {
					if (metaAnnotations == null) {
						if (!result.contains(next.annotationType())) {
							result.add(next.annotationType());
						}
					} else if (hasMetaAnnotation(next.annotationType(), false, metaAnnotations)) {
						if (!result.contains(next.annotationType())) {
							result.add(next.annotationType());
						}
					}
				}
			}

			else if (ElementType.METHOD == searchLocation) {

				Method[] accessibleMethods = getAccessibleMethods(targetClass, searchHierarchy);
				for (Method nextMethod : accessibleMethods) {
					Annotation[] annotations = nextMethod.getAnnotations();
					for (Annotation next : annotations) {
						if (metaAnnotations == null) {
							if (!result.contains(next.annotationType())) {
								result.add(next.annotationType());
							}
						} else if (hasMetaAnnotation(next.annotationType(), false, metaAnnotations)) {
							if (!result.contains(next.annotationType())) {
								result.add(next.annotationType());
							}
						}
					}
				}
			}

			else {
				throw new IllegalArgumentException("Only Type and Method search locations are supported but encountered: " + searchLocation);
			}
		}

		return result;
	}

	public static Method[] getAccessibleMethods(Class<?> clazz, boolean searchHierarchy) {

		List<Method> result = new ArrayList<Method>();
	    while (clazz != null) {
	        for (Method method : clazz.getDeclaredMethods()) {
	        	// Limit matching to public and protected (accessible methods)
	            int modifiers = method.getModifiers();
	            if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
	                result.add(method);
	            }
	        }

	        if (!searchHierarchy) {
	        	clazz = null;
	        } else {
	        	clazz = clazz.getSuperclass();
	        }
	    }
	    return result.toArray(new Method[result.size()]);
	}

	public static final boolean hasMetaAnnotation(Class<? extends Annotation> target, boolean matchAll, @SuppressWarnings("unchecked") Class<? extends Annotation>... metaAnnotations) {

		if ((metaAnnotations == null) || (metaAnnotations.length ==0) ) {
			throw new IllegalArgumentException("No meta-annotation was supplied for matching");
		}

		for (Class<? extends Annotation> nextMeta : metaAnnotations) {
			Annotation foundMetaAnnotation = target.getAnnotation(nextMeta);
			if (foundMetaAnnotation != null) {
				if (!matchAll) {
					return true;
				}
			} else if (matchAll) {
				return false;
			}
		}
		return matchAll;
	}
}
