package org.libraryexpress.infrastructure.config;

import java.time.Clock;
import javax.sql.DataSource;
import org.libraryexpress.application.book.mapper.BookMapper;
import org.libraryexpress.application.book.usecase.FindBook;
import org.libraryexpress.application.book.usecase.RegisterBook;
import org.libraryexpress.application.book.usecase.SearchBooks;
import org.libraryexpress.application.customer.mapper.CustomerMapper;
import org.libraryexpress.application.customer.usecase.CreateCustomer;
import org.libraryexpress.application.customer.usecase.FindCustomer;
import org.libraryexpress.application.customer.usecase.SearchCustomers;
import org.libraryexpress.application.customer.usecase.UpdateCustomerEmail;
import org.libraryexpress.application.loan.mapper.LoanMapper;
import org.libraryexpress.application.loan.usecase.CloseOverdueLoan;
import org.libraryexpress.application.loan.usecase.CreateLoan;
import org.libraryexpress.application.loan.usecase.LoanReturn;
import org.libraryexpress.application.loan.usecase.SearchLoans;
import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;
import org.libraryexpress.infrastructure.api.controller.BookController;
import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.api.controller.LoanController;
import org.libraryexpress.infrastructure.api.routing.openApi.OpenApiContractHandler;
import org.libraryexpress.infrastructure.api.routing.openApi.SwaggerUiHandler;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.gateway.BookMetadataGatewayFactory;
import org.libraryexpress.infrastructure.gateway.adapter.NativeHttpClient;
import org.libraryexpress.infrastructure.repository.jdbc.BookDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.CustomerDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.LoanDbRepository;
import org.libraryexpress.infrastructure.repository.jdbc.TransactionManagerDb;

public class AppContext {

    private final Database database;
    private final Repositories repositories;
    private final Validators validators;
    private final Gateways gateways;
    private final UseCases useCases;
    private final Controllers controllers;
    private final Documentation documentation;

    public AppContext(ConnectionProvider connectionProvider) {
        DataSource dataSource = connectionProvider.getDataSource();

        this.database = new Database(dataSource);
        this.repositories = new Repositories(database.transactionAwareDataSource);
        this.validators = new Validators(repositories);
        this.gateways = new Gateways();
        this.useCases = new UseCases(database.transactionManager, repositories, validators, gateways);
        this.controllers = new Controllers(this);
        this.documentation = new Documentation();
    }

    public CreateCustomer getCreateCustomer() {
        return useCases.createCustomer;
    }

    public FindCustomer getFindCustomer() {
        return useCases.findCustomer;
    }

    public SearchCustomers getListCustomers() {
        return useCases.searchCustomers;
    }

    public UpdateCustomerEmail getUpdateCustomerEmail() {
        return useCases.updateCustomerEmail;
    }

    public RegisterBook getRegisterBook() {
        return useCases.registerBook;
    }

    public FindBook getFindBook() {
        return useCases.findBook;
    }

    public SearchBooks getListBooks() {
        return useCases.searchBooks;
    }

    public CreateLoan getCreateLoan() {
        return useCases.createLoan;
    }

    public SearchLoans getSearchLoans() {
        return useCases.searchLoans;
    }

    public LoanReturn getLoanReturn() {
        return useCases.loanReturn;
    }

    public CloseOverdueLoan getCloseOverdueLoan() {
        return useCases.closeOverdueLoan;
    }

    public BookController getBookController() {
        return controllers.bookController;
    }

    public CustomerController getCustomerController() {
        return controllers.customerController;
    }

    public LoanController getLoanController() {
        return controllers.loanController;
    }

    public OpenApiContractHandler getOpenApiContractHandler() {
        return documentation.openApiContractHandler;
    }

    public SwaggerUiHandler getSwaggerUiHandler() {
        return documentation.swaggerUiHandler;
    }

    public BookMetadataGateway getBookMetadataGateway() {
        return gateways.bookMetadataGateway;
    }

    private static final class Database {

        private final TransactionManager transactionManager;
        private final DataSource transactionAwareDataSource;

        private Database(DataSource dataSource) {
            this.transactionManager = new TransactionManagerDb(dataSource);
            this.transactionAwareDataSource = new TransactionAwareDataSource(dataSource);
        }
    }

    private static final class Repositories {

        private final BookDbRepository bookRepository;
        private final CustomerDbRepository customerRepository;
        private final LoanDbRepository loanRepository;

        private Repositories(DataSource dataSource) {
            this.bookRepository = new BookDbRepository(dataSource);
            this.customerRepository = new CustomerDbRepository(dataSource);
            this.loanRepository = new LoanDbRepository(dataSource);
        }
    }

    private static final class Validators {

        private final BookAvailabilityValidator bookAvailabilityValidator;
        private final LoanEligibilityValidator loanEligibilityValidator;
        private final LoanPeriodValidator loanPeriodValidator;

        private Validators(Repositories repositories) {
            this.bookAvailabilityValidator = new BookAvailabilityValidator(repositories.bookRepository);
            this.loanEligibilityValidator = new LoanEligibilityValidator(repositories.loanRepository);
            this.loanPeriodValidator = new LoanPeriodValidator();
        }
    }

    private static final class Gateways {

        private final NativeHttpClient nativeHttpClient;
        private final BookMetadataGateway bookMetadataGateway;

        private Gateways() {
            this.nativeHttpClient = new NativeHttpClient();
            this.bookMetadataGateway = BookMetadataGatewayFactory.create(ConfigRegistry.getAppEnv(), nativeHttpClient);
        }
    }

    private static final class UseCases {

        private final CreateCustomer createCustomer;
        private final FindCustomer findCustomer;
        private final SearchCustomers searchCustomers;
        private final UpdateCustomerEmail updateCustomerEmail;
        private final RegisterBook registerBook;
        private final FindBook findBook;
        private final SearchBooks searchBooks;
        private final CreateLoan createLoan;
        private final SearchLoans searchLoans;
        private final LoanReturn loanReturn;
        private final CloseOverdueLoan closeOverdueLoan;

        private UseCases(
                TransactionManager transactionManager,
                Repositories repositories,
                Validators validators,
                Gateways gateways) {
            this.createCustomer = new CreateCustomer(repositories.customerRepository, CustomerMapper.INSTANCE);
            this.findCustomer = new FindCustomer(repositories.customerRepository, CustomerMapper.INSTANCE);
            this.searchCustomers = new SearchCustomers(repositories.customerRepository, CustomerMapper.INSTANCE);
            this.updateCustomerEmail =
                    new UpdateCustomerEmail(repositories.customerRepository, CustomerMapper.INSTANCE);

            this.registerBook =
                    new RegisterBook(repositories.bookRepository, gateways.bookMetadataGateway, BookMapper.INSTANCE);
            this.findBook = new FindBook(repositories.bookRepository, BookMapper.INSTANCE);
            this.searchBooks = new SearchBooks(repositories.bookRepository, BookMapper.INSTANCE);

            Clock clock = Clock.systemDefaultZone();
            this.createLoan = new CreateLoan(
                    transactionManager,
                    repositories.loanRepository,
                    repositories.bookRepository,
                    repositories.customerRepository,
                    validators.loanEligibilityValidator,
                    validators.bookAvailabilityValidator,
                    validators.loanPeriodValidator);
            this.searchLoans = new SearchLoans(repositories.loanRepository, LoanMapper.INSTANCE);
            this.loanReturn =
                    new LoanReturn(transactionManager, repositories.loanRepository, repositories.bookRepository, clock);
            this.closeOverdueLoan = new CloseOverdueLoan(repositories.loanRepository, clock);
        }
    }

    private static final class Controllers {

        private final BookController bookController;
        private final CustomerController customerController;
        private final LoanController loanController;

        private Controllers(AppContext appContext) {
            this.bookController = new BookController(appContext);
            this.customerController = new CustomerController(appContext);
            this.loanController = new LoanController(appContext);
        }
    }

    private static final class Documentation {

        private final OpenApiContractHandler openApiContractHandler;
        private final SwaggerUiHandler swaggerUiHandler;

        private Documentation() {
            this.openApiContractHandler = new OpenApiContractHandler();
            this.swaggerUiHandler = new SwaggerUiHandler();
        }
    }
}
