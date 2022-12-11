package com.polarbookshop.catalogservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.catalogservice.config.SecurityConfig;
import com.polarbookshop.catalogservice.domain.Book;
import com.polarbookshop.catalogservice.domain.BookNotFoundException;
import com.polarbookshop.catalogservice.domain.BookService;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
public class BookControllerMvcTests {
	private static final String ROLE_EMPLOYEE = "ROLE_employee";
	private static final String ROLE_CUSTOMER = "ROLE_customer";
	
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	BookService bookService;
	
	@MockBean
	JwtDecoder jwtDecoder;
	
	@Test
	void whenGetBookExistingAndAuthenticatedThenShouldReturn200() throws Exception {
		String isbn = "7373731394";
		Book expectedBook = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.viewBookDetails(isbn)).willReturn(expectedBook);
		this.mockMvc
			.perform(get("/books/" + isbn)
				.with(jwt()))
			.andExpect(status().isOk());
	}
	
	@Test
	void whenGetBookExistingAndNotAuthenticatedThenShouldReturn200() throws Exception {
		String isbn = "7373731394";
		Book expectedBook = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.viewBookDetails(isbn)).willReturn(expectedBook);
		this.mockMvc
			.perform(get("/books/" + isbn))
			.andExpect(status().isOk());
	}
	
	@Test
	void whenGetBookNotExistingAndAuthenticatedThenShouldReturn404() throws Exception {
		String isbn = "7373731394";
		given(this.bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);
		this.mockMvc
			.perform(get("/books/" + isbn)
				.with(jwt()))
			.andExpect(status().isNotFound());
	}
	
	@Test
	void whenGetBookNotExistingAndNotAuthenticatedThenShouldReturn404() throws Exception {
		String isbn = "7373731394";
		given(this.bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException.class);
		this.mockMvc
			.perform(get("/books/" + isbn))
			.andExpect(status().isNotFound());
	}
	
	@Test
	void whenDeleteBookWithEmployeeRoleThenShouldReturn204() throws Exception {
		String isbn = "7373731394";
		this.mockMvc
			.perform(delete("/books/" + isbn)
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE))))
			.andExpect(status().isNoContent());
	}
	
	@Test
	void whenDeleteBookWithCustomerRoleThenShouldReturn403() throws Exception {
		String isbn = "7373731394";
		this.mockMvc
			.perform(delete("/books/" + isbn)
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER))))
			.andExpect(status().isForbidden());
	}
	
	@Test
	void whenDeleteBookNotAuthenticatedThenShouldReturn401() throws Exception {
		String isbn = "7373731394";
		this.mockMvc
			.perform(delete("/books/" + isbn))
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void whenPostBookWithEmployeeRoleThenShouldReturn201() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
		this.mockMvc
			.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE)))
			)
			.andExpect(status().isCreated());
	}
	
	@Test
	void whenPostBookWithCustomerRoleThenShouldReturn403() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
		this.mockMvc
			.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER)))
			)
			.andExpect(status().isForbidden());
	}
	
	@Test
	void whenPostBookAndNotAuthenticatedThenShouldReturn403() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		this.mockMvc
			.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
			)
			.andExpect(status().isUnauthorized());
	}
	
	@Test
	void whenPutBookWithEmployeeRoleThenShouldReturn200() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
		this.mockMvc
			.perform(put("/books/" + isbn)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_EMPLOYEE)))
			)
			.andExpect(status().isOk());
	}
	
	@Test
	void whenPutBookWithCustomerRoleThenShouldReturn403() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		given(this.bookService.addBookToCatalog(bookToCreate)).willReturn(bookToCreate);
		this.mockMvc
			.perform(put("/books/" + isbn)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
				.with(jwt().authorities(new SimpleGrantedAuthority(ROLE_CUSTOMER)))
			)
			.andExpect(status().isForbidden());
	}
	
	@Test
	void whenPutBookAndNotAuthenticatedThenShouldReturn401() throws Exception {
		String isbn = "7373731394";
		Book bookToCreate = Book.of(isbn, "Title", "Author", 9.90, "Polarsophia");
		this.mockMvc
			.perform(post("/books/" + isbn)
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(bookToCreate))
			)
			.andExpect(status().isUnauthorized());
	}
}
