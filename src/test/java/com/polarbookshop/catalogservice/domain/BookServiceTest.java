package com.polarbookshop.catalogservice.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
	@Mock
	private BookRepository bookRepository;
	
	@InjectMocks
	private BookService bookService;
	
	@Test
	void whenBookToCreateAlreadyExistsThenThrows() {
		String bookIsbn = "1234561232";
		Book bookToCreate = Book.of(bookIsbn, "Title", "Author", 9.90);
		when(this.bookRepository.existsByIsbn(bookIsbn)).thenReturn(true);
		assertThatThrownBy(() -> this.bookService.addBookToCatalog(bookToCreate))
			.isInstanceOf(BookAlreadyExistsException.class)
			.hasMessage("A book with ISBN " + bookIsbn + " already exists.");
	}
	
	@Test
	void whenBookToReadDoesNotExistThenThrows() {
		String bookIsbn = "1234561232";
		when(this.bookRepository.findByIsbn(bookIsbn)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> this.bookService.viewBookDetails(bookIsbn))
			.isInstanceOf(BookNotFoundException.class)
			.hasMessage("The book with ISBN " + bookIsbn + " was not found.");
	}
}
