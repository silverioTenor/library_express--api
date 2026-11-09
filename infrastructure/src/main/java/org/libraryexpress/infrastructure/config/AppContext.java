package org.libraryexpress.infrastructure.config;

import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.application.book.usecase.FindBook;
import org.libraryexpress.application.book.usecase.SearchBooks;
import org.libraryexpress.application.book.usecase.RegisterBook;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.application.customer.usecase.CreateCustomer;
import org.libraryexpress.application.customer.usecase.FindCustomer;
import org.libraryexpress.application.customer.usecase.SearchCustomers;
import org.libraryexpress.application.customer.usecase.UpdateCustomerEmail;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.libraryexpress.application.loan.mapper.LoanMapper;
import org.libraryexpress.application.loan.usecase.*;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;
import org.libraryexpress.infrastructure.api.controller.BookController;
import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.api.controller.LoanController;
import org.libraryexpress.infrastructure.api.routing.openApi.OpenApiContractHandler;
import org.libraryexpress.infrastructure.api.routing.openApi.SwaggerUiHandler;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.gateway.adapter.NativeHttpClient;
import org.libraryexpress.infrastructure.gateway.googlebooks.GoogleBooksGateway;
import org.libraryexpress.infrastructure.repository.jdbc.BookDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.CustomerDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.LoanDbRepository;

import javax.sql.DataSource;
import java.time.Clock;

public class AppContext {

    // CUSTOMER
    private final CreateCustomer createCustomer;
    private final FindCustomer findCustomer;
    private final SearchCustomers searchCustomers;
    private final UpdateCustomerEmail updateCustomerEmail;
    private final CustomerController customerController;

    // BOOK
    private final RegisterBook registerBook;
    private final FindBook findBook;
    private final SearchBooks serchBooks;
    private final BookController bookController;

    // LOAN
    private final CreateLoan createLoan;
    private final SearchLoans searchLoans;
    private final LoanReturn loanReturn;
    private final CloseOverdueLoan closeOverdueLoan;
    private final LoanController loanController;

    // DOCS
    private final OpenApiContractHandler  openApiContractHandler;
    private final SwaggerUiHandler swaggerUiHandler;

    // GATEWAYS
    private final NativeHttpClient nativeHttpClient;
    private final GoogleBooksGateway googleBooksGateway;

    public AppContext(ConnectionProvider connectionProvider) {
        DataSource dataSource = connectionProvider.getDataSource();

        // GATEWAYS
        this.nativeHttpClient = new NativeHttpClient();
        this.googleBooksGateway = new GoogleBooksGateway(this.nativeHttpClient);

        // CUSTOMER
        CustomerDbRepository customerRepository = new CustomerDbRepository(dataSource);

        this.createCustomer = new CreateCustomer(customerRepository, CustomerMapper.INSTANCE);
        this.findCustomer = new FindCustomer(customerRepository, CustomerMapper.INSTANCE);
        this.searchCustomers = new SearchCustomers(customerRepository, CustomerMapper.INSTANCE);
        this.updateCustomerEmail = new UpdateCustomerEmail(customerRepository, CustomerMapper.INSTANCE);
        this.customerController = new CustomerController(this);

        // BOOK
        BookDbRepository bookRepository = new BookDbRepository(dataSource);
        BookAvailabilityValidator bookAvailabilityValidator = new BookAvailabilityValidator(bookRepository);

        this.registerBook = new RegisterBook(bookRepository, googleBooksGateway, BookMapper.INSTANCE);
        this.findBook = new FindBook(bookRepository, BookMapper.INSTANCE);
        this.serchBooks = new SearchBooks(bookRepository, BookMapper.INSTANCE);
        this.bookController = new BookController(this);

        // LOAN
        LoanDbRepository loanRepository = new LoanDbRepository(dataSource);
        LoanEligibilityValidator loanEligibilityValidator = new LoanEligibilityValidator(loanRepository);
        LoanPeriodValidator loanPeriodValidator = new LoanPeriodValidator();

        this.createLoan = new CreateLoan(
                loanRepository,
                bookRepository,
                customerRepository,
                loanEligibilityValidator,
                bookAvailabilityValidator,
                loanPeriodValidator
        );
        this.searchLoans = new SearchLoans(loanRepository, LoanMapper.INSTANCE);
        this.loanReturn = new LoanReturn(loanRepository, bookRepository, Clock.systemDefaultZone());
        this.closeOverdueLoan = new CloseOverdueLoan(loanRepository, Clock.systemDefaultZone());
        this.loanController = new LoanController(this);

        // DOCS
        this.openApiContractHandler = new OpenApiContractHandler();
        this.swaggerUiHandler = new SwaggerUiHandler();
    }

    public CreateCustomer getCreateCustomer() {
        return createCustomer;
    }

    public FindCustomer getFindCustomer() {
        return findCustomer;
    }

    public SearchCustomers getListCustomers() {
        return searchCustomers;
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

    public SearchBooks getListBooks() {
        return serchBooks;
    }

    public CreateLoan getCreateLoan() {
        return createLoan;
    }

    public SearchLoans getSearchLoans() {
        return searchLoans;
    }

    public LoanReturn getLoanReturn() {
        return loanReturn;
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

    public LoanController getLoanController() {
        return loanController;
    }

    public OpenApiContractHandler getOpenApiContractHandler() {
        return openApiContractHandler;
    }

    public SwaggerUiHandler getSwaggerUiHandler() {
        return swaggerUiHandler;
    }

    public GoogleBooksGateway getGoogleBooksGateway() {
        return googleBooksGateway;
    }
}
