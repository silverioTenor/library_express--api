package org.libraryexpress.domain.loan.entity;

import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.core.exception.InvalidArgumentException;
import org.libraryexpress.domain.loan.enums.LoanStatus;
import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;

import java.time.Clock;
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

    private Loan(
            String id,
            Isbn ISBN,
            String customerId,
            LoanStatus status,
            LocalDate startDate,
            LocalDate endDate
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

    public void changeStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isOverdue(Clock clock) {
        return LocalDate.now(clock).isAfter(endDate);
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

    public static class Builder {

        private String id;

        private String ISBN;

        private String customerId;

        private LoanStatus status;

        private LocalDate startDate;

        private LocalDate endDate;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setISBN(String ISBN) {
            this.ISBN = ISBN;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setStatus(LoanStatus status) {
            this.status = status;
            return this;
        }

        public Builder setStartDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Loan build() {
            return new Loan(id, new Isbn(ISBN), customerId, status, startDate, endDate);
        }
    }
}
