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

import static org.junit.jupiter.params.provider.CsvParserFactory.createParserFor;
import static org.junit.platform.commons.util.CollectionUtils.toSet;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.univocity.parsers.csv.CsvParser;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.0
 */
class CsvArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<CsvSource> {

	private static final Pattern NEW_LINE_REGEX = Pattern.compile("\\n");

	private static final String LINE_SEPARATOR = "\n";

	private CsvSource annotation;
	private Set<String> nullValues;
	private CsvParser csvParser;

	@Override
	public void accept(CsvSource annotation) {
		this.annotation = annotation;
		this.nullValues = toSet(annotation.nullValues());
		this.csvParser = createParserFor(annotation);
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Preconditions.condition(this.annotation.value().length > 0 ^ !this.annotation.textBlock().isEmpty(),
			() -> "@CsvSource must be declared with either `value` or `textBlock` but not both");

		String[] lines;
		if (!this.annotation.textBlock().isEmpty()) {
			lines = NEW_LINE_REGEX.split(this.annotation.textBlock(), 0);
		}
		else {
			lines = this.annotation.value();
		}

		AtomicLong index = new AtomicLong(0);
		// @formatter:off
		return Arrays.stream(lines)
				.map(line -> parseLine(index.getAndIncrement(), line))
				.map(Arguments::of);
		// @formatter:on
	}

	private String[] parseLine(long index, String line) {
		String[] parsedLine = null;
		try {
			parsedLine = this.csvParser.parseLine(line + LINE_SEPARATOR);
			if (parsedLine != null && !this.nullValues.isEmpty()) {
				for (int i = 0; i < parsedLine.length; i++) {
					if (this.nullValues.contains(parsedLine[i])) {
						parsedLine[i] = null;
					}
				}
			}
		}
		catch (Throwable throwable) {
			handleCsvException(throwable, this.annotation);
		}
		Preconditions.notNull(parsedLine, () -> "Line at index " + index + " contains invalid CSV: \"" + line + "\"");
		return parsedLine;
	}

	static void handleCsvException(Throwable throwable, Annotation annotation) {
		UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
		if (throwable instanceof PreconditionViolationException) {
			throw (PreconditionViolationException) throwable;
		}
		throw new CsvParsingException("Failed to parse CSV input configured via " + annotation, throwable);
	}

}
