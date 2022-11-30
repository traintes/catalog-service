package com.polarbookshop.catalogservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.polarbookshop.catalogservice.config.DataConfig;

@DataJdbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
public class BookRepositoryJdbcTests {
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private JdbcAggregateTemplate jdbcAggregateTemplate;
	
	@Test
	void findAllBooks() {
		Book book1 = Book.of("1234561235", "Title", "Author", 12.90, "Polarsophia");
		Book book2 = Book.of("1234561236", "Another Title", "Author", 12.90, "Polarsophia");
		this.jdbcAggregateTemplate.insert(book1);
		this.jdbcAggregateTemplate.insert(book2);
		
		Iterable<Book> actualBooks = this.bookRepository.findAll();
		
		assertThat(StreamSupport.stream(actualBooks.spliterator(), true)
			.filter(book -> book.isbn().equals(book1.isbn()) || book.isbn().equals(book2.isbn()))
			.collect(Collectors.toList())
		).hasSize(2);
	}
	
	@Test
	void findByIsbnWhenExisting() {
		String bookIsbn = "1234561237";
		Book book = Book.of(bookIsbn, "Title", "Author", 12.90, "Polarsophia");
		this.jdbcAggregateTemplate.insert(book);
		
		Optional<Book> actualBook = this.bookRepository.findByIsbn(bookIsbn);
		
		assertThat(actualBook).isPresent();
		assertThat(actualBook.get().isbn()).isEqualTo(book.isbn());
	}
	
	@Test
	void findBookByIsbnWhenNotExisting() {	
		Optional<Book> actualBook = this.bookRepository.findByIsbn("1234561238");
		assertThat(actualBook).isEmpty();
	}
	
	@Test
	void existsByIsbnWhenExisting() {
		String bookIsbn = "1234561239";
		Book bookToCreate = Book.of(bookIsbn, "Title", "Author", 12.90, "Polarsophia");
		this.jdbcAggregateTemplate.insert(bookToCreate);
		
		boolean existing = this.bookRepository.existsByIsbn(bookIsbn);
		
		assertThat(existing).isTrue();
	}
	
	@Test
	void existsByIsbnWhenNotExisting() {		
		boolean existing = this.bookRepository.existsByIsbn("1234561240");
		assertThat(existing).isFalse();
	}
	
	@Test
	void deleteByIsbn() {
		String bookIsbn = "1234561241";
		Book bookToCreate = Book.of(bookIsbn, "Title", "Author", 12.90, "Polarsophia");
		Book persistedBook = this.jdbcAggregateTemplate.insert(bookToCreate);
		
		this.bookRepository.deleteByIsbn(bookIsbn);
		
		assertThat(this.jdbcAggregateTemplate.findById(persistedBook.id(), Book.class)).isNull();
	}
}
