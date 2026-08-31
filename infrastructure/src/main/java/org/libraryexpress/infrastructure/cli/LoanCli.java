package org.libraryexpress.infrastructure.cli;

import org.libraryexpress.application.book.dto.response.BookDto;
import org.libraryexpress.application.customer.dto.response.CustomerDto;
import org.libraryexpress.application.customer.usecase.FindCustomer;
import org.libraryexpress.application.loan.dto.request.CreateLoanDto;
import org.libraryexpress.application.loan.dto.request.FilterLoansDto;
import org.libraryexpress.application.loan.dto.response.LoanDto;
import org.libraryexpress.application.loan.usecase.*;
import org.libraryexpress.domain.book.exception.BookNotFoundException;
import org.libraryexpress.domain.book.exception.BookUnavailableException;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.customer.exception.CustomerNotFoundException;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.InvalidLoanStatusException;
import org.libraryexpress.domain.loan.exception.LoanLimitReachedException;
import org.libraryexpress.domain.loan.exception.LoanNotFoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;
import org.libraryexpress.infrastructure.config.AppContext;
import org.libraryexpress.infrastructure.config.logging.CorrelationIdSupport;
import org.libraryexpress.infrastructure.util.JsonPrinter;

import java.util.Scanner;
import java.util.Set;

class LoanCli {

    private final FindCustomer findCustomer;
    private final CreateLoan createLoan;
    private final SearchLoans searchLoans;
    private final ReturnLoan returnLoan;
    private final CloseOverdueLoan closeOverdueLoan;

    public LoanCli(AppContext context) {
        this.findCustomer = context.getFindCustomer();
        this.createLoan = context.getCreateLoan();
        this.searchLoans = context.getSearchLoans();
        this.returnLoan = context.getReturnLoan();
        this.closeOverdueLoan = context.getCloseOverdueLoan();
    }

    public void init(Scanner scan) {

        var loop = true;

        do {
            System.out.println(" ");
            System.out.println("[1] - New");
            System.out.println("[2] - Search");
            System.out.println("[3] - Devolution");
            System.out.println("[4] - Close Overdue Loan");
            System.out.println("[6] - Back");
            System.out.println(" ");

            int option = scan.nextInt();

            switch (option) {
                case 1 -> this.createLoan(scan);
                case 2 -> this.searchLoan(scan);
                case 3 -> this.returnLoan(scan);
                case 4 -> this.closeOverdueLoan(scan);
                case 6 -> loop = false;
                default -> System.out.println("Invalid option!");
            }

        } while (loop);
    }

    public void createLoan(Scanner scan) {
        CorrelationIdSupport.start();

        System.out.println("  ");
        System.out.println("Enter the customer ID");
        String customerId = scan.next();

        System.out.println("  ");
        System.out.println("Enter the ISBN");
        String ISBN = scan.next();

        CustomerDto customer;
        BookDto book;

        try {
            this.findCustomer.execute(customerId);
        } catch (CustomerNotFoundException e) {
            System.out.println(e.getMessage());
            CorrelationIdSupport.clear();
            return;
        }

        CreateLoanDto createLoanDto = new CreateLoanDto(customerId, ISBN);

        try {
            this.createLoan.execute(createLoanDto);

            System.out.println("  ");
            System.out.println("Loan successfully processed!");

        } catch (LoanLimitReachedException | OverdueLoanException | BookUnavailableException | BookNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }

    public void searchLoan(Scanner scan) {
        CorrelationIdSupport.start();

        scan.nextLine();

        System.out.println("  ");
        System.out.println("Enter the customer ID");
        String customerId = scan.nextLine().trim();

        customerId = !customerId.isEmpty() ? customerId : null;

        System.out.println("  ");
        System.out.println("Enter the ISBN");
        String ISBN = scan.nextLine().trim();

        ISBN = !ISBN.isEmpty() ? ISBN : null;

        System.out.println("  ");
        System.out.println("Enter the getStatus");
        String inputStatus = scan.nextLine().trim();

        LoanStatus status;

        try {
            status = !inputStatus.isEmpty()
                    ? LoanStatus.valueOf(inputStatus.toUpperCase())
                    : null;

        } catch (IllegalArgumentException e) {
            System.out.println("  ");
            System.out.println("The provided status is invalid!");
            return;
        }

        Set<LoanStatus> statuses = status != null ? Set.of(status) : null;

        FilterLoansDto filterDto = new FilterLoansDto(customerId, ISBN, statuses, null);

        try {
            OutputPaginationDto<LoanDto> loans = this.searchLoans.execute(filterDto);

            System.out.println(JsonPrinter.print(loans.items()));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }

    public void returnLoan(Scanner scan) {
        CorrelationIdSupport.start();

        System.out.println("  ");
        System.out.println("Enter the loan's ID");
        String loanId = scan.next();

        try {
            this.returnLoan.execute(loanId);

            System.out.println("  ");
            System.out.println("Loan successfully completed!");

        } catch (LoanNotFoundException | InvalidLoanStatusException e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }

    public void closeOverdueLoan(Scanner scan) {
        CorrelationIdSupport.start();

        System.out.println("  ");
        System.out.println("Enter the loan ID");
        String loanId = scan.next();

        try {
            this.closeOverdueLoan.execute(loanId);

            System.out.println("  ");
            System.out.println("Operation successfully completed!");

        } catch (LoanNotFoundException | OverdueLoanException e) {
            System.out.println(e.getMessage());
        } finally {
            CorrelationIdSupport.clear();
        }
    }
}
