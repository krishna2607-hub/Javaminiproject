
package com.example.library_book_management.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.library_book_management.model.Book;

@Service
public class BookService {

    private List<Book> books = new ArrayList<>();

    public Book addBook(Book book) {
        books.add(book);
        return book;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public Book getBookById(int bookId) {
        for (Book book : books) {
            if (book.getBookId() == bookId) {
                return book;
            }
        }
        return null;
    }

    public Book updateAvailability(int bookId, boolean available) {
        Book book = getBookById(bookId);

        if (book != null) {
            book.setAvailable(available);
        }

        return book;
    }

    public boolean deleteBook(int bookId) {
        Book book = getBookById(bookId);

        if (book != null) {
            books.remove(book);
            return true;
        }

        return false;
    }
}
