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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.MockCsvAnnotationBuilder.csvFileSource;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.CsvFileArgumentsProvider.InputStreamProvider;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class CsvFileArgumentsProviderTests {

	@Test
	void providesArgumentsForNewlineAndComma() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.lineSeparator("\n")//
				.delimiter(',')//
				.build();

		var arguments = provideArguments(annotation, "foo, bar \n baz, qux \n");

		assertThat(arguments).containsExactly(array("foo", "bar"), array("baz", "qux"));
	}

	@Test
	void providesArgumentsForCarriageReturnAndSemicolon() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.lineSeparator("\r")//
				.delimiter(';')//
				.build();

		var arguments = provideArguments(annotation, "foo; bar \r baz; qux");

		assertThat(arguments).containsExactly(array("foo", "bar"), array("baz", "qux"));
	}

	@Test
	void providesArgumentsWithStringDelimiter() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.delimiterString(",")//
				.build();

		var arguments = provideArguments(annotation, "foo, bar \n baz, qux \n");

		assertThat(arguments).containsExactly(array("foo", "bar"), array("baz", "qux"));
	}

	@Test
	void throwsExceptionIfBothDelimitersAreSimultaneouslySet() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.delimiter(',')//
				.delimiterString(";")//
				.build();

		var exception = assertThrows(PreconditionViolationException.class, () -> provideArguments(annotation, "foo"));

		assertThat(exception)//
				.hasMessageStartingWith("The delimiter and delimiterString attributes cannot be set simultaneously in")//
				.hasMessageContaining("CsvFileSource");
	}

	@Test
	void ignoresCommentedOutEntries() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.delimiter(',')//
				.build();

		var arguments = provideArguments(annotation, "foo, bar \n#baz, qux");

		assertThat(arguments).containsExactly(array("foo", "bar"));
	}

	@Test
	void closesInputStreamForClasspathResource() {
		var closed = new AtomicBoolean(false);
		InputStream inputStream = new ByteArrayInputStream("foo".getBytes()) {

			@Override
			public void close() {
				closed.set(true);
			}
		};
		var annotation = csvFileSource().resources("test.csv").build();

		var arguments = provideArguments(inputStream, annotation);

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void closesInputStreamForFile(@TempDir Path tempDir) {
		var closed = new AtomicBoolean(false);
		InputStream inputStream = new ByteArrayInputStream("foo".getBytes()) {

			@Override
			public void close() {
				closed.set(true);
			}
		};
		var annotation = csvFileSource().files(tempDir.resolve("test.csv").toAbsolutePath().toString()).build();

		var arguments = provideArguments(inputStream, annotation);

		assertThat(arguments.count()).isEqualTo(1);
		assertThat(closed.get()).describedAs("closed").isTrue();
	}

	@Test
	void readsFromSingleClasspathResource() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv")//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("baz"), array("qux"), array(""));
	}

	@Test
	void readsFromSingleFileWithAbsolutePath(@TempDir Path tempDir) throws Exception {
		var csvFile = writeClasspathResourceToFile("/single-column.csv", tempDir.resolve("single-column.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.files(csvFile.toAbsolutePath().toString())//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("baz"), array("qux"), array(""));
	}

	@Test
	void readsFromClasspathResourcesAndFiles(@TempDir Path tempDir) throws Exception {
		var csvFile = writeClasspathResourceToFile("/single-column.csv", tempDir.resolve("single-column.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv")//
				.files(csvFile.toAbsolutePath().toString())//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(2 * 5);
	}

	@Test
	void readsFromSingleFileWithRelativePath() throws Exception {
		var csvFile = writeClasspathResourceToFile("/single-column.csv", Path.of("single-column.csv"));
		try {
			var annotation = csvFileSource()//
					.encoding("ISO-8859-1")//
					.files(csvFile.getFileName().toString())//
					.build();

			var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

			assertThat(arguments).containsExactly(array("foo"), array("bar"), array("baz"), array("qux"), array(""));
		}
		finally {
			Files.delete(csvFile);
		}
	}

	@Test
	void readsFromSingleClasspathResourceWithCustomEmptyValue() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv")//
				.emptyValue("EMPTY")//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(array("foo"), array("bar"), array("baz"), array("qux"), array("EMPTY"));
	}

	@Test
	void readsFromMultipleClasspathResources() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv", "/single-column.csv")//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(10);
	}

	@Test
	void readsFromSingleClasspathResourceWithHeaders() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv")//
				.numLinesToSkip(1)//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(array("bar"), array("baz"), array("qux"), array(""));
	}

	@Test
	void readsFromSingleClasspathResourceWithMoreHeadersThanLines() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv")//
				.numLinesToSkip(10)//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).isEmpty();
	}

	@Test
	void readsFromMultipleClasspathResourcesWithHeaders() {
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/single-column.csv", "/single-column.csv")//
				.numLinesToSkip(1)//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).containsExactly(array("bar"), array("baz"), array("qux"), array(""), array("bar"),
			array("baz"), array("qux"), array(""));
	}

	@Test
	void throwsExceptionForMissingClasspathResource() {
		var annotation = csvFileSource()//
				.resources("/does-not-exist.csv")//
				.build();

		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [/does-not-exist.csv] does not exist");
	}

	@Test
	void throwsExceptionForBlankClasspathResource() {
		var annotation = csvFileSource()//
				.resources("    ")//
				.build();

		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Classpath resource [    ] must not be null or blank");
	}

	@Test
	void throwsExceptionForMissingFile() {
		var annotation = csvFileSource()//
				.files("does-not-exist.csv")//
				.build();

		var exception = assertThrows(JUnitException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("File [does-not-exist.csv] could not be read");
	}

	@Test
	void throwsExceptionForBlankFile() {
		var annotation = csvFileSource()//
				.files("    ")//
				.build();

		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("File [    ] must not be null or blank");
	}

	@Test
	void throwsExceptionIfResourcesAndFilesAreEmpty() {
		var annotation = csvFileSource()//
				.resources()//
				.files()//
				.build();

		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception).hasMessageContaining("Resources or files must not be empty");
	}

	@Test
	void throwsExceptionForInvalidCharset() {
		var annotation = csvFileSource()//
				.encoding("Bogus-Charset")//
				.resources("/bogus-charset.csv")//
				.build();

		var exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageContaining("The charset supplied in Mock for CsvFileSource")//
				.hasMessageEndingWith("is invalid");
	}

	@Test
	void throwsExceptionForInvalidCsvFormat() {
		var annotation = csvFileSource()//
				.resources("/broken.csv")//
				.build();

		var exception = assertThrows(CsvParsingException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageStartingWith("Failed to parse CSV input configured via Mock for CsvFileSource")//
				.hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void emptyValueIsAnEmptyWithCustomNullValueString() {
		var annotation = csvFileSource()//
				.resources("test.csv")//
				.lineSeparator("\n")//
				.delimiter(',')//
				.nullValues("NIL")//
				.build();

		var arguments = provideArguments(annotation, "apple, , NIL, ''\nNIL, NIL, foo, bar");

		assertThat(arguments).containsExactly(array("apple", null, null, "''"), array(null, null, "foo", "bar"));
	}

	@Test
	void readsLineFromDefaultMaxCharsFileWithDefaultConfig(@TempDir Path tempDir) throws Exception {
		var csvFile = writeClasspathResourceToFile("/default-max-chars.csv", tempDir.resolve("default-max-chars.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/default-max-chars.csv")//
				.files(csvFile.toAbsolutePath().toString())//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(2);
	}

	@Test
	void readsLineFromExceedsMaxCharsFileWithCustomConfig(@TempDir Path tempDir) throws IOException {
		var csvFile = writeClasspathResourceToFile("/exceeds-default-max-chars.csv",
			tempDir.resolve("exceeds-default-max-chars.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/exceeds-default-max-chars.csv")//
				.maxCharsPerColumn(4097)//
				.files(csvFile.toAbsolutePath().toString())//
				.build();

		var arguments = provideArguments(new CsvFileArgumentsProvider(), annotation);

		assertThat(arguments).hasSize(2);
	}

	@Test
	void throwsExceptionWhenMaxCharsPerColumnIsNotPositiveNumber(@TempDir Path tempDir) throws IOException {
		var csvFile = writeClasspathResourceToFile("/exceeds-default-max-chars.csv",
			tempDir.resolve("exceeds-default-max-chars.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/exceeds-default-max-chars.csv")//
				.maxCharsPerColumn(-1).files(csvFile.toAbsolutePath().toString())//
				.build();

		var exception = assertThrows(PreconditionViolationException.class, //
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation));

		assertThat(exception)//
				.hasMessageStartingWith("maxCharsPerColumn must be a positive number: -1");
	}

	@Test
	void throwsExceptionForExceedsMaxCharsFileWithDefaultConfig(@TempDir Path tempDir) throws IOException {
		var csvFile = writeClasspathResourceToFile("/exceeds-default-max-chars.csv",
			tempDir.resolve("exceeds-default-max-chars.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/exceeds-default-max-chars.csv")//
				.files(csvFile.toAbsolutePath().toString())//
				.build();

		var exception = assertThrows(CsvParsingException.class,
			() -> provideArguments(new CsvFileArgumentsProvider(), annotation).toArray());

		assertThat(exception)//
				.hasMessageStartingWith("Failed to parse CSV input configured via Mock for CsvFileSource")//
				.hasRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void ignoresLeadingAndTrailingSpaces(@TempDir Path tempDir) throws IOException {
		var csvFile = writeClasspathResourceToFile("/leading-trailing-spaces.csv",
			tempDir.resolve("leading-trailing-spaces.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/leading-trailing-spaces.csv")//
				.files(csvFile.toAbsolutePath().toString())//
				.ignoreLeadingAndTrailingWhitespace(true)//
				.build();

		var arguments = provideArguments(new ByteArrayInputStream(Files.readAllBytes(csvFile)), annotation);

		assertThat(arguments).containsExactly(array("ab", "cd"), array("ef", "gh"));
	}

	@Test
	void trimsLeadingAndTrailingSpaces(@TempDir Path tempDir) throws IOException {
		var csvFile = writeClasspathResourceToFile("/leading-trailing-spaces.csv",
			tempDir.resolve("leading-trailing-spaces.csv"));
		var annotation = csvFileSource()//
				.encoding("ISO-8859-1")//
				.resources("/leading-trailing-spaces.csv")//
				.files(csvFile.toAbsolutePath().toString())//
				.delimiter(',')//
				.ignoreLeadingAndTrailingWhitespace(false)//
				.build();

		var arguments = provideArguments(new ByteArrayInputStream(Files.readAllBytes(csvFile)), annotation);

		assertThat(arguments).containsExactly(array(" ab ", " cd"), array("ef ", "gh"));
	}

	private Stream<Object[]> provideArguments(CsvFileSource annotation, String content) {
		return provideArguments(new ByteArrayInputStream(content.getBytes(UTF_8)), annotation);
	}

	private Stream<Object[]> provideArguments(InputStream inputStream, CsvFileSource annotation) {
		var provider = new CsvFileArgumentsProvider(new InputStreamProvider() {
			@Override
			public InputStream openClasspathResource(Class<?> baseClass, String path) {
				assertThat(path).isEqualTo(annotation.resources()[0]);
				return inputStream;
			}

			@Override
			public InputStream openFile(String path) {
				assertThat(path).isEqualTo(annotation.files()[0]);
				return inputStream;
			}
		});
		return provideArguments(provider, annotation);
	}

	private Stream<Object[]> provideArguments(CsvFileArgumentsProvider provider, CsvFileSource annotation) {
		provider.accept(annotation);
		var context = mock(ExtensionContext.class);
		when(context.getTestClass()).thenReturn(Optional.of(CsvFileArgumentsProviderTests.class));
		doCallRealMethod().when(context).getRequiredTestClass();
		return provider.provideArguments(context).map(Arguments::get);
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] array(T... elements) {
		return elements;
	}

	private static Path writeClasspathResourceToFile(String name, Path target) throws IOException {
		try (var in = CsvFileArgumentsProviderTests.class.getResourceAsStream(name)) {
			Files.copy(in, target);
		}
		return target;
	}

}
