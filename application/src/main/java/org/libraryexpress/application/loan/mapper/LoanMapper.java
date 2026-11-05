package org.libraryexpress.application.loan.mapper;

import org.libraryexpress.application.loan.dto.response.LoanDTO;
import org.libraryexpress.domain.book.valueobject.Isbn;
import org.libraryexpress.domain.loan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface LoanMapper {
    LoanMapper INSTANCE = Mappers.getMapper((LoanMapper.class));

    LoanDTO toResponseDto(Loan loan);

    default String mapIsbnToString(Isbn isbn) {
        return isbn != null ? isbn.value() : null;
    }
}
