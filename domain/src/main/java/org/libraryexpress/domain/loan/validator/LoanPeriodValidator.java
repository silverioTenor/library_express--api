package org.libraryexpress.domain.loan.validator;

import org.libraryexpress.domain.loan.exception.DateOutOfBoundException;

import java.time.LocalDate;
import java.util.Objects;

public class LoanPeriodValidator {

    public static final int MAX_LOAN_DAYS = 15;

    public void validate(LocalDate startDate, LocalDate dueDate) {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(dueDate, "Due date cannot be null");

        LocalDate maxDateLimit = startDate.plusDays(MAX_LOAN_DAYS);
        if (dueDate.isAfter(maxDateLimit)) {
            throw new DateOutOfBoundException("End date cannot exceed the 15-day limit from start date");
        }
    }
}
