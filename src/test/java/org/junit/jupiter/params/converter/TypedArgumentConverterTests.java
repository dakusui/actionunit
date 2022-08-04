/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;

/**
 * Tests for {@link TypedArgumentConverter}.
 *
 * @since 5.7
 */
class TypedArgumentConverterTests {

	@Nested
	class UnitTests {

		private final StringLengthArgumentConverter converter = new StringLengthArgumentConverter();

		/**
		 * @since 5.8
		 */
		@Test
		void preconditions() {
			assertThatExceptionOfType(PreconditionViolationException.class)//
					.isThrownBy(() -> new StringLengthArgumentConverter(null, Integer.class))//
					.withMessage("sourceType must not be null");

			assertThatExceptionOfType(PreconditionViolationException.class)//
					.isThrownBy(() -> new StringLengthArgumentConverter(String.class, null))//
					.withMessage("targetType must not be null");
		}

		@Test
		void convertsSourceToTarget() {
			assertAll(//
				() -> assertConverts("abcd", 4), //
				() -> assertConverts("", 0), //
				() -> assertConverts(null, 0)//
			);
		}

		private void assertConverts(String input, int expected) {
			assertThat(this.converter.convert(input)).isEqualTo(expected);
		}

		@Test
		void sourceTypeMismatch() {
			Parameter parameter = findParameterOfMethod("stringToBoolean", Boolean.class);
			ParameterContext parameterContext = parameterContext(parameter);
			assertThatExceptionOfType(ArgumentConversionException.class)//
					.isThrownBy(() -> this.converter.convert(Boolean.TRUE, parameterContext))//
					.withMessage("StringLengthArgumentConverter cannot convert objects of type [java.lang.Boolean]. "
							+ "Only source objects of type [java.lang.String] are supported.");
		}

		@Test
		void targetTypeMismatch() {
			Parameter parameter = findParameterOfMethod("stringToBoolean", Boolean.class);
			ParameterContext parameterContext = parameterContext(parameter);
			assertThatExceptionOfType(ArgumentConversionException.class)//
					.isThrownBy(() -> this.converter.convert("enigma", parameterContext))//
					.withMessage("StringLengthArgumentConverter cannot convert to type [java.lang.Boolean]. "
							+ "Only target type [java.lang.Integer] is supported.");
		}

		private ParameterContext parameterContext(Parameter parameter) {
			ParameterContext parameterContext = Mockito.mock(ParameterContext.class);
			when(parameterContext.getParameter()).thenReturn(parameter);
			return parameterContext;
		}

		private Parameter findParameterOfMethod(String methodName, Class<?>... parameterTypes) {
			Method method = ReflectionUtils.findMethod(getClass(), methodName, parameterTypes).get();
			return method.getParameters()[0];
		}

		void stringToBoolean(Boolean b) {
		}

	}

	/**
	 * @since 5.8
	 */
	@Nested
	class IntegrationTests {

		@ParameterizedTest
		@NullSource
		void nullStringToInteger(@StringLength Integer length) {
			assertThat(length).isEqualTo(0);
		}

		@ParameterizedTest
		@NullSource
		void nullStringToPrimitiveInt(@StringLength int length) {
			assertThat(length).isEqualTo(0);
		}

		@ParameterizedTest
		@NullSource
		void nullStringToPrimitiveLong(@StringLength long length) {
			assertThat(length).isEqualTo(0);
		}

		@ParameterizedTest
		@ValueSource(strings = "enigma")
		void stringToInteger(@StringLength Integer length) {
			assertThat(length).isEqualTo(6);
		}

		@ParameterizedTest
		@ValueSource(strings = "enigma")
		void stringToPrimitiveInt(@StringLength int length) {
			assertThat(length).isEqualTo(6);
		}

		@ParameterizedTest
		@ValueSource(strings = "enigma")
		void stringToPrimitiveLong(@StringLength long length) {
			assertThat(length).isEqualTo(6);
		}

	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@ConvertWith(StringLengthArgumentConverter.class)
	private @interface StringLength {
	}

	private static class StringLengthArgumentConverter extends TypedArgumentConverter<String, Integer> {

		StringLengthArgumentConverter() {
			this(String.class, Integer.class);
		}

		StringLengthArgumentConverter(Class<String> sourceType, Class<Integer> targetType) {
			super(sourceType, targetType);
		}

		@Override
		protected Integer convert(String source) throws ArgumentConversionException {
			return (source != null ? source.length() : 0);
		}
	}

}
