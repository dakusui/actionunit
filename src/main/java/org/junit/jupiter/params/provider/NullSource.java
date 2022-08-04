/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @NullSource} is an {@link ArgumentsSource} which provides a single
 * {@code null} argument to the annotated {@code @ParameterizedTest} method.
 *
 * <p>Note that {@code @NullSource} cannot be used for an argument that has
 * a primitive type, unless the argument is converted to a corresponding wrapper
 * type with an {@link org.junit.jupiter.params.converter.ArgumentConverter}.
 *
 * @since 5.4
 * @see ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 * @see EmptySource
 * @see NullAndEmptySource
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.7")
@ArgumentsSource(NullArgumentsProvider.class)
public @interface NullSource {
}
