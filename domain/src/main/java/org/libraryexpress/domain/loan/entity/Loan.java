package org.libraryexpress.domain.loan.entity;

import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.DomainErrorType;
import org.libraryexpress.domain.core.exception.DomainException;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;
import org.libraryexpress.domain.loan.exception.OverdueLoanException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Loan implements Comparable<Loan> {

    public static final int MAX_ACTIVE_LOANS = 2;

    private final String id;

    private final Isbn ISBN;

    private final String customerId;

    private LoanStatus status;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private Instant closedAt;

    private Loan(
            String id,
            Isbn ISBN,
            String customerId,
            LoanStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant closedAt
    ) {
        Objects.requireNonNull(ISBN, "Book ISBN cannot be null");
        Objects.requireNonNull(status, "Loan status cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (id == null || id.isBlank())
            throw new InvalidArgumentException("Loan ID cannot be null or empty");
        if (customerId == null || customerId.isBlank())
            throw new InvalidArgumentException("Customer ID cannot be null or empty");
        if (endDate.isBefore(startDate))
            throw new DateOutOfBoundException("End date cannot be before start date");

        this.id = id;
        this.ISBN = ISBN;
        this.customerId = customerId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.closedAt = closedAt;
    }

    public static Loan create(
            String id,
            String ISBN,
            String customerId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new Loan(id, new Isbn(ISBN), customerId, LoanStatus.ACTIVE, startDate, endDate, null);
    }

    public static Loan restore(
            String id,
            String ISBN,
            String customerId,
            LoanStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant closedAt
    ) {
        return new Loan(id, new Isbn(ISBN), customerId, status, startDate, endDate, closedAt);
    }

    public void processReturn(Clock clock, boolean isOverdueFlow) {
        if (this.closedAt != null && this.status.equals(LoanStatus.FINISHED)) {
            throw new DomainException(DomainErrorType.DATA_CONFLICT, "Loan already returned");
        }

        // TODO: Apply a late fee if the loan is returned overdue.
        if (this.isOverdue(clock) && !isOverdueFlow) {
            this.changeStatus(LoanStatus.OVERDUE);
            return;
        }

        this.changeStatus(LoanStatus.FINISHED);
        this.closedAt = Instant.now();
    }

    public void changeStatus(LoanStatus status) {
        this.status = status;
    }

    public boolean isOverdue(Clock clock) {
        return LocalDate.now(clock).isAfter(endDate);
    }

    public String getId() {
        return id;
    }

    public Isbn getISBN() {
        return ISBN;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    @Override
    public String toString() {
        return "{\n" +
                " id: " + id + ",\n" +
                " ISBN: " + ISBN + ",\n" +
                " customerId: " + customerId + ",\n" +
                " statuses: " + status.toString() + ",\n" +
                " startDate: " + startDate + ",\n" +
                " endDate: " + endDate + ",\n" +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Loan loan)) return false;
        return Objects.equals(id, loan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Loan o) {
        return Objects.compare(startDate, o.getStartDate(), LocalDate::compareTo);
    }

}
