package org.libraryexpress.application.loan.usecase;

import org.libraryexpress.application.loan.dto.request.LoanFilterDTO;
import org.libraryexpress.application.loan.dto.response.LoanDTO;
import org.libraryexpress.application.loan.mapper.LoanMapper;
import org.libraryexpress.domain.core.dto.OutputPaginationDto;
import org.libraryexpress.domain.core.repository.QueryResult;
import org.libraryexpress.domain.loan.entity.Loan;
import org.libraryexpress.domain.loan.repository.LoanRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SearchLoans {

    private final LoanRepository loanRepository;
    private final LoanMapper mapper;

    public SearchLoans(LoanRepository loanRepository, LoanMapper mapper) {
        this.loanRepository = loanRepository;
        this.mapper = mapper;
    }

    public OutputPaginationDto<LoanDTO> execute(LoanFilterDTO filterDto) {

        QueryResult<Loan> result = this.loanRepository.search(filterDto.toLoanFilter());

        List<LoanDTO> loansDto =  result.items().stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());

        if (filterDto.paginationDto() == null || !filterDto.paginationDto().isPaginated()) {
            return OutputPaginationDto.unpaginated(loansDto);
        }

        int totalPage = Math.toIntExact(result.total() / filterDto.paginationDto().limit());

        return new OutputPaginationDto<>(
                loansDto,
                filterDto.paginationDto().page(),
                filterDto.paginationDto().limit(),
                totalPage,
                result.total()
        );
    }
}
