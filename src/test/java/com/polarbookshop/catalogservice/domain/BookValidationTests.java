package com.polarbookshop.catalogservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BookValidationTests {
	private static Validator validator;
	
	@BeforeAll
	static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}
	
	@Test
	void whenAllFieldsCorrectThenValidationSucceeds() {
		Book book = new Book("1234567890", "Title", "Author", 9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).isEmpty();
	}
	
	@Test
	void whenIsbnNotDefinedThenValidationFails() {
		Book book = new Book("", "Title", "Author", 9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(2);
		List<String> constraintViolationMessages = violations
			.stream()
			.map(ConstraintViolation::getMessage)
			.collect(Collectors.toList());
		assertThat(constraintViolationMessages)
			.contains("The book ISBN must be defined.")
			.contains("The ISBN format must be valid.");
	}
	
	@Test
	void whenIsbnDefinedButIncorrectThenValidationFails() {
		Book book = new Book("a234567890", "Title", "Author", 9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The ISBN format must be valid.");
	}
	
	@Test
	void whenTitleIsNotDefinedThenValidationFails() {
		Book book = new Book("1234567890", "", "Author", 9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The book title must be defined.");
	}
	
	@Test
	void whenAuthorIsNotDefinedThenValidationFails() {
		Book book = new Book("1234567890", "Title", "", 9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The book author must be defined.");
	}
	
	@Test
	void whenPriceIsNotDefinedThenValidationFails() {
		Book book = new Book("1234567890", "Title", "Author", null);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be defined.");
	}
	
	@Test
	void whenPriceDefinedButZeroThenValidationFails() {
		Book book = new Book("1234567890", "Title", "Author", 0.0);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be greater than zero.");
	}
	
	@Test
	void whenPriceDefinedButNegativeThenValidationFails() {
		Book book = new Book("1234567890", "Title", "Author", -9.90);
		Set<ConstraintViolation<Book>> violations = validator.validate(book);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getMessage()).isEqualTo("The book price must be greater than zero.");
	}
}