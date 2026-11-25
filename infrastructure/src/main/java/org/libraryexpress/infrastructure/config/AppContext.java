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
import org.libraryexpress.application.user.usecase.CreateSuperAdmin;
import org.libraryexpress.domain.book.gateway.BookMetadataGateway;
import org.libraryexpress.domain.book.validator.BookAvailabilityValidator;
import org.libraryexpress.domain.core.repository.TransactionManager;
import org.libraryexpress.domain.identity.security.PasswordHasher;
import org.libraryexpress.domain.loan.validator.LoanEligibilityValidator;
import org.libraryexpress.domain.loan.validator.LoanPeriodValidator;
import org.libraryexpress.infrastructure.api.controller.BookController;
import org.libraryexpress.infrastructure.api.controller.CustomerController;
import org.libraryexpress.infrastructure.api.controller.LoanController;
import org.libraryexpress.infrastructure.api.controller.RestrictController;
import org.libraryexpress.infrastructure.api.guard.CreateSuperAdminGuard;
import org.libraryexpress.infrastructure.api.routes.*;
import org.libraryexpress.infrastructure.api.routing.openApi.OpenApiContractHandler;
import org.libraryexpress.infrastructure.api.routing.openApi.SwaggerUiHandler;
import org.libraryexpress.infrastructure.config.database.ConnectionProvider;
import org.libraryexpress.infrastructure.config.database.transaction.TransactionAwareDataSource;
import org.libraryexpress.infrastructure.gateway.BookMetadataGatewayFactory;
import org.libraryexpress.infrastructure.gateway.adapter.NativeHttpClient;
import org.libraryexpress.infrastructure.gateway.aws.AwsSecretsManager;
import org.libraryexpress.infrastructure.gateway.aws.ConfigSecretProvider;
import org.libraryexpress.infrastructure.gateway.aws.SecretProvider;
import org.libraryexpress.infrastructure.repository.jdbc.*;
import org.libraryexpress.infrastructure.security.Argon2Settings;
import org.libraryexpress.infrastructure.security.Argon2idPasswordHasher;

public class AppContext {

    private final Routes routes;

    public AppContext(ConnectionProvider connectionProvider) {
        DataSource dataSource = connectionProvider.getDataSource();

        Database database = new Database(dataSource);
        Repositories repositories = new Repositories(database.transactionAwareDataSource);
        Validators validators = new Validators(repositories);
        Security security = new Security();
        Gateways gateways = new Gateways();
        UseCases useCases = new UseCases(database.transactionManager, repositories, validators, gateways, security);
        Controllers controllers = new Controllers(useCases);
        Documentation documentation = new Documentation();
        Guards guards = new Guards(controllers, repositories, security);
        this.routes = new Routes(guards, controllers, documentation);
    }

    public RouteOrchestrator orchestrator() {
        return routes.orchestrator;
    }

    private static final class Routes {

        private final RouteOrchestrator orchestrator;

        private Routes(Guards guards, Controllers controllers, Documentation documentation) {
            BookRouteModule bookRouteModule = new BookRouteModule(controllers.bookController);
            CustomerRouteModule customerRouteModule = new CustomerRouteModule(controllers.customerController);
            LoanRouteModule loanRouteModule = new LoanRouteModule(controllers.loanController);
            DocsRouteModule docsRouteModule =
                    new DocsRouteModule(documentation.openApiContractHandler, documentation.swaggerUiHandler);
            RestrictRouteModule restrictRouteModule =
                    new RestrictRouteModule(controllers.restrictController, guards.createSuperAdminGuard);
            this.orchestrator = new RouteOrchestrator(
                    bookRouteModule, customerRouteModule, loanRouteModule, docsRouteModule, restrictRouteModule);
        }
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
        private final UserDbRepository userDbRepository;

        private Repositories(DataSource dataSource) {
            this.bookRepository = new BookDbRepository(dataSource);
            this.customerRepository = new CustomerDbRepository(dataSource);
            this.loanRepository = new LoanDbRepository(dataSource);
            this.userDbRepository = new UserDbRepository(dataSource);
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
        private final CreateSuperAdmin createSuperAdmin;

        private UseCases(
                TransactionManager transactionManager,
                Repositories repositories,
                Validators validators,
                Gateways gateways,
                Security security) {
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

            this.createSuperAdmin =
                    new CreateSuperAdmin(repositories.userDbRepository, security.passwordHasher, transactionManager);
        }
    }

    private static final class Controllers {

        private final BookController bookController;
        private final CustomerController customerController;
        private final LoanController loanController;
        private final RestrictController restrictController;

        private Controllers(UseCases useCases) {
            this.bookController = new BookController(useCases.registerBook, useCases.findBook, useCases.searchBooks);
            this.customerController = new CustomerController(
                    useCases.createCustomer, useCases.updateCustomerEmail, useCases.searchCustomers);
            this.loanController = new LoanController(
                    useCases.createLoan, useCases.searchLoans, useCases.loanReturn, useCases.closeOverdueLoan);
            this.restrictController = new RestrictController(useCases.createSuperAdmin);
        }
    }

    private static final class Security {

        private final PasswordHasher passwordHasher;
        private final SecretProvider secretProvider;
        private final String secretId;

        private Security() {
            Argon2Settings argon2Settings = new Argon2Settings(
                    ConfigRegistry.getArgon2MemoryKib(),
                    ConfigRegistry.getArgon2Iterations(),
                    ConfigRegistry.getArgon2Parallelism());
            this.passwordHasher = new Argon2idPasswordHasher(argon2Settings);

            this.secretProvider = (ConfigRegistry.getAppEnv().equals("prod"))
                    ? new AwsSecretsManager()
                    : new ConfigSecretProvider(String::strip);
            this.secretId = ConfigRegistry.getAwsSecretId();
        }
    }

    private static final class Guards {

        private CreateSuperAdminGuard createSuperAdminGuard;

        private Guards(Controllers controllers, Repositories repositories, Security security) {
            this.createSuperAdminGuard = new CreateSuperAdminGuard(
                    repositories.userDbRepository, security.secretProvider, security.secretId);
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
