package org.libraryexpress.infrastructure.config;

import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.application.book.usecase.FindBook;
import org.libraryexpress.application.book.usecase.ListBooks;
import org.libraryexpress.application.book.usecase.RegisterBook;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.application.customer.usecase.CreateCustomer;
import org.libraryexpress.application.customer.usecase.FindCustomer;
import org.libraryexpress.application.customer.usecase.ListCustomers;
import org.libraryexpress.application.customer.usecase.UpdateCustomerEmail;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.libraryexpress.application.loan.mapper.LoanMapper;
import org.libraryexpress.application.loan.usecase.*;
import org.libraryexpress.application.loan.validator.SearchLoanValidator;
import org.libraryexpress.infrastructure.api.controller.BookController;
import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.repository.jdbc.BookDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.CustomerDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.LoanDbRepository;

import javax.sql.DataSource;
import java.time.Clock;

public class AppContext {

    // CUSTOMER
    private final CreateCustomer createCustomer;
    private final FindCustomer findCustomer;
    private final ListCustomers listCustomers;
    private final UpdateCustomerEmail updateCustomerEmail;
    private final CustomerController customerController;

    // BOOK
    private final RegisterBook registerBook;
    private final FindBook findBook;
    private final ListBooks listBooks;
    private final BookController bookController;

    // LOAN
    private final CreateLoan createLoan;
    private final SearchLoans searchLoans;
    private final ListLoans listLoans;
    private final ReturnLoan returnLoan;
    private final CloseOverdueLoan closeOverdueLoan;

    public AppContext(ConnectionProvider connectionProvider) {
        DataSource dataSource = connectionProvider.getDataSource();

        // CUSTOMER
        CustomerDbRepository customerRepository = new CustomerDbRepository(dataSource);

        this.createCustomer = new CreateCustomer(customerRepository, CustomerMapper.INSTANCE);
        this.findCustomer = new FindCustomer(customerRepository, CustomerMapper.INSTANCE);
        this.listCustomers = new ListCustomers(customerRepository, CustomerMapper.INSTANCE);
        this.updateCustomerEmail = new UpdateCustomerEmail(customerRepository, CustomerMapper.INSTANCE);
        this.customerController = new CustomerController(this);

        // BOOK
        BookDbRepository bookRepository = new BookDbRepository(dataSource);
        BookAvailabilityValidator bookAvailabilityValidator = new BookAvailabilityValidator(bookRepository);

        this.registerBook = new RegisterBook(bookRepository, BookMapper.INSTANCE);
        this.findBook = new FindBook(bookRepository, BookMapper.INSTANCE);
        this.listBooks = new ListBooks(bookRepository, BookMapper.INSTANCE);
        this.bookController = new BookController(this);

        // LOAN
        LoanDbRepository loanRepository = new LoanDbRepository(dataSource);
        LoanEligibilityValidator loanEligibilityValidator = new LoanEligibilityValidator(loanRepository);
        SearchLoanValidator searchLoanValidator = new SearchLoanValidator();

        this.createLoan = new CreateLoan(loanRepository, bookRepository, loanEligibilityValidator, bookAvailabilityValidator);
        this.searchLoans = new SearchLoans(loanRepository, LoanMapper.INSTANCE, searchLoanValidator);
        this.listLoans = new ListLoans(loanRepository, LoanMapper.INSTANCE);
        this.returnLoan = new ReturnLoan(loanRepository, bookRepository, Clock.systemDefaultZone());
        this.closeOverdueLoan = new CloseOverdueLoan(loanRepository);
    }

    public CreateCustomer getCreateCustomer() {
        return createCustomer;
    }

    public FindCustomer getFindCustomer() {
        return findCustomer;
    }

    public ListCustomers getListCustomers() {
        return listCustomers;
    }

    public UpdateCustomerEmail getUpdateCustomerEmail() {
        return updateCustomerEmail;
    }

    public RegisterBook getRegisterBook() {
        return registerBook;
    }

    public FindBook getFindBook() {
        return findBook;
    }

    public ListBooks getListBooks() {
        return listBooks;
    }

    public CreateLoan getCreateLoan() {
        return createLoan;
    }

    public SearchLoans getSearchLoans() {
        return searchLoans;
    }

    public ListLoans getListLoans() {
        return listLoans;
    }

    public ReturnLoan getReturnLoan() {
        return returnLoan;
    }

    public CloseOverdueLoan getCloseOverdueLoan() {
        return closeOverdueLoan;
    }

    public BookController getBookController() {
        return bookController;
    }

    public CustomerController getCustomerController() {
        return customerController;
    }
}
