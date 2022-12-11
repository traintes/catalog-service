package com.polarbookshop.catalogservice.domain;

import org.springframework.stereotype.Service;

@Service
public class BookService {
	private final BookRepository bookRepository;
	
	public BookService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}
	
	public Iterable<Book> viewBookList() {
		return this.bookRepository.findAll();
	}
	
	public Book viewBookDetails(String isbn) {
		return this.bookRepository.findByIsbn(isbn).orElseThrow(() -> new BookNotFoundException(isbn));
	}
	
	public Book addBookToCatalog(Book book) {
		if (this.bookRepository.existsByIsbn(book.isbn())) {
			throw new BookAlreadyExistsException(book.isbn());
		}
		return this.bookRepository.save(book);
	}
	
	public void removeBookFromCatalog(String isbn) {
		this.bookRepository.deleteByIsbn(isbn);
	}
	
	public Book editBookDetails(String isbn, Book book) {
		return this.bookRepository.findByIsbn(isbn)
			.map(existingBook -> {
				Book bookToUpdate = new Book(
					existingBook.id(),
					existingBook.isbn(),
					book.title(),
					book.author(),
					book.price(),
					book.publisher(),
					existingBook.createdDate(),
					existingBook.lastModifiedDate(),
					existingBook.createdBy(),
					existingBook.lastModifiedBy(),
					existingBook.version()
				);
				return this.bookRepository.save(bookToUpdate);
			})
			.orElseGet(() -> this.addBookToCatalog(book));
	}
}
