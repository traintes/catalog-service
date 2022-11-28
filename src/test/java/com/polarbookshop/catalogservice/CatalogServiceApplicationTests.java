package com.polarbookshop.catalogservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.polarbookshop.catalogservice.domain.Book;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {
	@Autowired
	private WebTestClient webTestClient;
	
	@Test
	void whenGetRequestWithIdThenBookReturned() {
		String bookIsbn = "1231231230";
		Book bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);
		Book expectedBook = this.webTestClient
			.post()
			.uri("/books")
			.bodyValue(bookToCreate)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(Book.class).value(book -> assertThat(book).isNotNull())
			.returnResult().getResponseBody();
		
		this.webTestClient
			.get()
			.uri("/books/" + bookIsbn)
			.exchange()
			.expectStatus().is2xxSuccessful()
			.expectBody(Book.class).value(actualBook -> {
				assertThat(actualBook).isNotNull();
				assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
			});
	}
	
	@Test
	void whenPostRequestThenBookCreated() {
		Book expectedBook = new Book("1231231231", "Title", "Author", 9.90);
		
		this.webTestClient
			.post()
			.uri("/books")
			.bodyValue(expectedBook)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(Book.class).value(actualBook -> {
				assertThat(actualBook).isNotNull();
				assertThat(actualBook.isbn()).isEqualTo(expectedBook.isbn());
			});
	}
	
	@Test
	void whenPutRequestThenBookUpdated() {
		String bookIsbn = "1231231232";
		Book bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);
		Book createdBook = this.webTestClient
			.post()
			.uri("/books")
			.bodyValue(bookToCreate)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(Book.class).value(book -> assertThat(book).isNotNull())
			.returnResult().getResponseBody();
		Book bookToUpdate = new Book(createdBook.isbn(), createdBook.title(), createdBook.author(), 7.95);
		
		this.webTestClient
			.put()
			.uri("/books/" + bookIsbn)
			.bodyValue(bookToUpdate)
			.exchange()
			.expectStatus().isOk()
			.expectBody(Book.class).value(actualBook -> {
				assertThat(actualBook).isNotNull();
				assertThat(actualBook.price()).isEqualTo(bookToUpdate.price());
			});
	}
	
	@Test
	void whenDeleteRequestThenBookDeleted() {
		String bookIsbn = "1231231233";
		Book bookToCreate = new Book(bookIsbn, "Title", "Author", 9.90);
		this.webTestClient
			.post()
			.uri("/books")
			.bodyValue(bookToCreate)
			.exchange()
			.expectStatus().isCreated();
		
		this.webTestClient
			.delete()
			.uri("/books/" + bookIsbn)
			.exchange()
			.expectStatus().isNoContent();
		
		this.webTestClient
			.get()
			.uri("/books/" + bookIsbn)
			.exchange()
			.expectStatus().isNotFound()
			.expectBody(String.class).value(errorMessage -> {
				assertThat(errorMessage).isEqualTo("The book with ISBN " + bookIsbn + " was not found.");
			});
	}
}
