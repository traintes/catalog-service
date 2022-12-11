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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.polarbookshop.catalogservice.config.DataConfig;

@DataJdbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@Testcontainers
public class BookRepositoryJdbcTests {
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private JdbcAggregateTemplate jdbcAggregateTemplate;
	
	@Container
	static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.4"));
	
	@DynamicPropertySource
	static void dynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgresql::getJdbcUrl);
		registry.add("spring.datasource.username", postgresql::getUsername);
		registry.add("spring.datasource.password", postgresql::getPassword);
	}
	
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
	void whenCreateBookNotAuthenticatedThenNoAuditMetadata() {
		Book bookToCreate = Book.of("1232343456", "Title", "Author", 12.90, "Polarsophia");
		Book createdBook = this.bookRepository.save(bookToCreate);
		
		assertThat(createdBook.createdBy()).isNull();
		assertThat(createdBook.lastModifiedBy()).isNull();
	}
	
	@Test
	@WithMockUser("john")
	void whenCreateBookAuthenticatedThenAuditMetadata() {
		Book bookToCreate = Book.of("1232343457", "Title", "Author", 12.90, "Polarsophia");
		Book createdBook = this.bookRepository.save(bookToCreate);
		
		assertThat(createdBook.createdBy()).isEqualTo("john");
		assertThat(createdBook.lastModifiedBy()).isEqualTo("john");
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
