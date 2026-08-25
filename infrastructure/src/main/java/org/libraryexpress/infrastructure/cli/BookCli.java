package org.libraryexpress.infrastructure.cli;

import org.libraryexpress.application.book.dto.request.RegisterBookDto;
import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.book.usecase.FindBook;
import org.libraryexpress.application.book.usecase.ListBooks;
import org.libraryexpress.application.book.usecase.RegisterBook;
import org.libraryexpress.domain.book.enums.BookStatus;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.UniqueIsbnViolationException;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.util.RandomGenerator;
import org.libraryexpress.infrastructure.config.AppContext;
import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;
import org.libraryexpress.infrastructure.util.JsonPrinter;

import java.util.Scanner;

public class BookCli {

    private final RegisterBook registerBook;
    private final FindBook findBook;
    private final ListBooks listBooks;

    public BookCli(AppContext context) {
        this.registerBook = context.getRegisterBook();
        this.findBook = context.getFindBook();
        this.listBooks = context.getListBooks();
    }

    public void init(Scanner scan) {

        boolean loop = true;

        do {
            System.out.println(" ");
            System.out.println("[1] - Register");
            System.out.println("[2] - Show");
            System.out.println("[3] - List");
            System.out.println("[6] - Back");
            System.out.println(" ");

            int option = scan.nextInt();

            switch (option) {
                case 1 -> this.register(scan);
                case 2 -> this.show(scan);
                case 3 -> this.list(scan);
                case 6 -> loop = false;
                default -> System.out.println("Invalid option!");
            }

        } while (loop);

    }

    private void register(Scanner scan) {
        CorrelationIdSupport.start();

        String ISBN = Isbn.generate().value();

        scan.nextLine();

        System.out.println("Enter the title:");
        String title = scan.nextLine();
        System.out.println("  ");

        System.out.println("Enter the author:");
        String author = scan.nextLine();
        System.out.println("  ");

        System.out.println("Enter the year:");
        int year;

        try {
            year = Integer.parseInt(scan.nextLine().trim());

            RegisterBookDto registerBookDto = new RegisterBookDto(ISBN, title, author, year, BookStatus.AVAILABLE);

            this.registerBook.execute(registerBookDto);

            System.out.println("Book registered successfully");

        } catch (NumberFormatException e) {
            System.out.println("Invalid year. Registration cancelled.");
            return;
        } catch (UniqueIsbnViolationException e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }

    private void show(Scanner scan) {
        CorrelationIdSupport.start();

        System.out.println("  ");
        System.out.println("Enter the ISBN:");
        String ISBN = scan.next();

        try {
            BookDto bookDto = this.findBook.execute(ISBN);

            System.out.println(JsonPrinter.print(bookDto));
        } catch (BookNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }

    private void list(Scanner scan) {
        CorrelationIdSupport.start();
        var books = this.listBooks.execute();

        if (books.isEmpty()) {
            System.out.println("No books found.");
        } else {
            System.out.println(JsonPrinter.print(books));
        }
        CorrelationIdSupport.clear();
    }
}
