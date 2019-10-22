/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.properties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * {@link EnumerablePropertySource} to adapt annotations marked with
 * {@link PropertyMapping @PropertyMapping}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public class AnnotationsPropertySource extends EnumerablePropertySource<Class<?>> {

	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([^A-Z-])([A-Z])");

	private final Map<String, Object> properties;

	public AnnotationsPropertySource(Class<?> source) {
		this("Annotations", source);
	}

	public AnnotationsPropertySource(String name, Class<?> source) {
		super(name, source);
		this.properties = getProperties(source);
	}

	private Map<String, Object> getProperties(Class<?> source) {
		Map<String, Object> properties = new LinkedHashMap<>();
		MergedAnnotations.from(source, SearchStrategy.SUPERCLASS).stream()
				.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getType)).forEach((annotation) -> {
					Class<Annotation> type = annotation.getType();
					MergedAnnotation<?> typeMapping = MergedAnnotations.from(type).get(PropertyMapping.class,
							MergedAnnotation::isDirectlyPresent);
					String prefix = typeMapping.getValue(MergedAnnotation.VALUE, String.class).orElse("");
					SkipPropertyMapping defaultSkip = typeMapping.getValue("skip", SkipPropertyMapping.class)
							.orElse(SkipPropertyMapping.YES);
					for (Method attribute : type.getDeclaredMethods()) {
						collectProperties(prefix, defaultSkip, annotation, attribute, properties);
					}
				});
		return properties;
	}

	private void collectProperties(String prefix, SkipPropertyMapping defaultSkip, MergedAnnotation<?> annotation,
			Method attribute, Map<String, Object> properties) {
		MergedAnnotation<?> attributeMapping = MergedAnnotations.from(attribute).get(PropertyMapping.class);
		SkipPropertyMapping skip = attributeMapping.getValue("skip", SkipPropertyMapping.class).orElse(defaultSkip);
		if (skip == SkipPropertyMapping.YES) {
			return;
		}
		Optional<Object> value = annotation.getValue(attribute.getName());
		if (!value.isPresent()) {
			return;
		}
		boolean condition = skip == SkipPropertyMapping.ON_DEFAULT_VALUE && ObjectUtils.nullSafeEquals(value.get(), annotation.getDefaultValue(attribute.getName()).orElse(null));
		if (condition) {
			return;
		}
		String name = getName(prefix, attributeMapping, attribute);
		putProperties(name, value.get(), properties);
	}

	private String getName(String prefix, MergedAnnotation<?> attributeMapping, Method attribute) {
		String name = attributeMapping.getValue(MergedAnnotation.VALUE, String.class).orElse("");
		if (!StringUtils.hasText(name)) {
			name = toKebabCase(attribute.getName());
		}
		return dotAppend(prefix, name);
	}

	private String toKebabCase(String name) {
		Matcher matcher = CAMEL_CASE_PATTERN.matcher(name);
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, new StringBuilder().append(matcher.group(1)).append('-').append(StringUtils.uncapitalize(matcher.group(2))).toString());
		}
		matcher.appendTail(result);
		return result.toString().toLowerCase(Locale.ENGLISH);
	}

	private String dotAppend(String prefix, String postfix) {
		if (StringUtils.hasText(prefix)) {
			return prefix.endsWith(".") ? prefix + postfix : new StringBuilder().append(prefix).append(".").append(postfix).toString();
		}
		return postfix;
	}

	private void putProperties(String name, Object value, Map<String, Object> properties) {
		if (ObjectUtils.isArray(value)) {
			Object[] array = ObjectUtils.toObjectArray(value);
			for (int i = 0; i < array.length; i++) {
				properties.put(new StringBuilder().append(name).append("[").append(i).append("]").toString(), array[i]);
			}
		}
		else {
			properties.put(name, value);
		}
	}

	@Override
	public boolean containsProperty(String name) {
		return this.properties.containsKey(name);
	}

	@Override
	public Object getProperty(String name) {
		return this.properties.get(name);
	}

	@Override
	public String[] getPropertyNames() {
		return StringUtils.toStringArray(this.properties.keySet());
	}

	public boolean isEmpty() {
		return this.properties.isEmpty();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return this.properties.equals(((AnnotationsPropertySource) obj).properties);
	}

	@Override
	public int hashCode() {
		return this.properties.hashCode();
	}

}
