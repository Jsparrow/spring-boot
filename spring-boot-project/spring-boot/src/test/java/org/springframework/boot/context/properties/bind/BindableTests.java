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

package org.springframework.boot.context.properties.bind;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link Bindable}.
 *
 * @author Phillip Webb
 * @author Madhura Bhave
 */
class BindableTests {

	private static final Logger logger = LoggerFactory.getLogger(BindableTests.class);

	@Test
	void ofClassWhenTypeIsNullShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> Bindable.of((Class<?>) null))
				.withMessageContaining("Type must not be null");
	}

	@Test
	void ofTypeWhenTypeIsNullShouldThrowException() {
		assertThatIllegalArgumentException().isThrownBy(() -> Bindable.of((ResolvableType) null))
				.withMessageContaining("Type must not be null");
	}

	@Test
	void ofClassShouldSetType() {
		assertThat(Bindable.of(String.class).getType()).isEqualTo(ResolvableType.forClass(String.class));
	}

	@Test
	void ofTypeShouldSetType() {
		ResolvableType type = ResolvableType.forClass(String.class);
		assertThat(Bindable.of(type).getType()).isEqualTo(type);
	}

	@Test
	void ofInstanceShouldSetTypeAndExistingValue() {
		String instance = "foo";
		ResolvableType type = ResolvableType.forClass(String.class);
		assertThat(Bindable.ofInstance(instance).getType()).isEqualTo(type);
		assertThat(Bindable.ofInstance(instance).getValue().get()).isEqualTo("foo");
	}

	@Test
	void ofClassWithExistingValueShouldSetTypeAndExistingValue() {
		assertThat(Bindable.of(String.class).withExistingValue("foo").getValue().get()).isEqualTo("foo");
	}

	@Test
	void ofTypeWithExistingValueShouldSetTypeAndExistingValue() {
		assertThat(Bindable.of(ResolvableType.forClass(String.class)).withExistingValue("foo").getValue().get())
				.isEqualTo("foo");
	}

	@Test
	void ofTypeWhenExistingValueIsNotInstanceOfTypeShouldThrowException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> Bindable.of(ResolvableType.forClass(String.class)).withExistingValue(123))
				.withMessageContaining("ExistingValue must be an instance of " + String.class.getName());
	}

	@Test
	void ofTypeWhenPrimitiveWithExistingValueWrapperShouldNotThrowException() {
		Bindable<Integer> bindable = Bindable.<Integer>of(ResolvableType.forClass(int.class)).withExistingValue(123);
		assertThat(bindable.getType().resolve()).isEqualTo(int.class);
		assertThat(bindable.getValue().get()).isEqualTo(123);
	}

	@Test
	void getBoxedTypeWhenNotBoxedShouldReturnType() {
		Bindable<String> bindable = Bindable.of(String.class);
		assertThat(bindable.getBoxedType()).isEqualTo(ResolvableType.forClass(String.class));
	}

	@Test
	void getBoxedTypeWhenPrimitiveShouldReturnBoxedType() {
		Bindable<Integer> bindable = Bindable.of(int.class);
		assertThat(bindable.getType()).isEqualTo(ResolvableType.forClass(int.class));
		assertThat(bindable.getBoxedType()).isEqualTo(ResolvableType.forClass(Integer.class));
	}

	@Test
	void getBoxedTypeWhenPrimitiveArrayShouldReturnBoxedType() {
		Bindable<int[]> bindable = Bindable.of(int[].class);
		assertThat(bindable.getType().getComponentType()).isEqualTo(ResolvableType.forClass(int.class));
		assertThat(bindable.getBoxedType().isArray()).isTrue();
		assertThat(bindable.getBoxedType().getComponentType()).isEqualTo(ResolvableType.forClass(Integer.class));
	}

	@Test
	void getAnnotationsShouldReturnEmptyArray() {
		assertThat(Bindable.of(String.class).getAnnotations()).isEmpty();
	}

	@Test
	void withAnnotationsShouldSetAnnotations() {
		Annotation annotation = mock(Annotation.class);
		assertThat(Bindable.of(String.class).withAnnotations(annotation).getAnnotations()).containsExactly(annotation);
	}

	@Test
	void getAnnotationWhenMatchShouldReturnAnnotation() {
		Test annotation = AnnotationUtils.synthesizeAnnotation(Test.class);
		assertThat(Bindable.of(String.class).withAnnotations(annotation).getAnnotation(Test.class))
				.isSameAs(annotation);
	}

	@Test
	void getAnnotationWhenNoMatchShouldReturnNull() {
		Test annotation = AnnotationUtils.synthesizeAnnotation(Test.class);
		assertThat(Bindable.of(String.class).withAnnotations(annotation).getAnnotation(Bean.class)).isNull();
	}

	@Test
	void toStringShouldShowDetails() {
		Annotation annotation = AnnotationUtils.synthesizeAnnotation(TestAnnotation.class);
		Bindable<String> bindable = Bindable.of(String.class).withExistingValue("foo").withAnnotations(annotation);
		logger.info(bindable.toString());
		assertThat(bindable.toString())
				.contains("type = java.lang.String, value = 'provided', annotations = array<Annotation>["
						+ "@org.springframework.boot.context.properties.bind.BindableTests$TestAnnotation()]");
	}

	@Test
	void equalsAndHashCode() {
		Annotation annotation = AnnotationUtils.synthesizeAnnotation(TestAnnotation.class);
		Bindable<String> bindable1 = Bindable.of(String.class).withExistingValue("foo").withAnnotations(annotation);
		Bindable<String> bindable2 = Bindable.of(String.class).withExistingValue("foo").withAnnotations(annotation);
		Bindable<String> bindable3 = Bindable.of(String.class).withExistingValue("fof").withAnnotations(annotation);
		assertThat(bindable1.hashCode()).isEqualTo(bindable2.hashCode());
		assertThat(bindable1).isEqualTo(bindable1).isEqualTo(bindable2);
		assertThat(bindable1).isEqualTo(bindable3);
	}

	@Test // gh-18218
	void withExistingValueDoesNotForgetAnnotations() {
		Annotation annotation = AnnotationUtils.synthesizeAnnotation(TestAnnotation.class);
		Bindable<?> bindable = Bindable.of(String.class).withAnnotations(annotation).withExistingValue("");
		assertThat(bindable.getAnnotations()).containsExactly(annotation);
	}

	@Test // gh-18218
	void withSuppliedValueValueDoesNotForgetAnnotations() {
		Annotation annotation = AnnotationUtils.synthesizeAnnotation(TestAnnotation.class);
		Bindable<?> bindable = Bindable.of(String.class).withAnnotations(annotation).withSuppliedValue(() -> "");
		assertThat(bindable.getAnnotations()).containsExactly(annotation);
	}

	@Test
	void withConstructorFilterSetsConstructorFilter() {
		Predicate<Constructor<?>> constructorFilter = (constructor) -> false;
		Bindable<?> bindable = Bindable.of(TestNewInstance.class).withConstructorFilter(constructorFilter);
		assertThat(bindable.getConstructorFilter()).isSameAs(constructorFilter);
	}

	@Test
	void withConstructorFilterWhenFilterIsNullMatchesAll() {
		Bindable<?> bindable = Bindable.of(TestNewInstance.class).withConstructorFilter(null);
		assertThat(bindable.getConstructorFilter()).isSameAs(Bindable.of(TestNewInstance.class).getConstructorFilter());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface TestAnnotation {

	}

	static class TestNewInstance {

		private String foo = "hello world";

		String getFoo() {
			return this.foo;
		}

		void setFoo(String foo) {
			this.foo = foo;
		}

	}

	static class TestNewInstanceWithNoDefaultConstructor {

		private String foo = "hello world";

		TestNewInstanceWithNoDefaultConstructor(String foo) {
			this.foo = foo;
		}

		String getFoo() {
			return this.foo;
		}

		void setFoo(String foo) {
			this.foo = foo;
		}

	}

}
