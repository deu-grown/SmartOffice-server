package com.grown.smartoffice.domain.salary.service;

import com.grown.smartoffice.domain.salary.dto.SalaryRecordResponse;
import com.grown.smartoffice.domain.salary.entity.SalaryRecord;
import com.grown.smartoffice.domain.salary.entity.SalaryStatus;
import com.grown.smartoffice.domain.salary.repository.SalaryRecordRepository;
import com.grown.smartoffice.global.common.PageResponse;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalaryRecordService {

    private final SalaryRecordRepository salaryRecordRepository;

    @Transactional
    public SalaryRecordResponse confirm(Long id) {
        SalaryRecord record = salaryRecordRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SALARY_RECORD_NOT_FOUND));

        if (record.getSalrecStatus() == SalaryStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.SALARY_RECORD_ALREADY_CONFIRMED);
        }

        record.confirm();
        return SalaryRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public SalaryRecordResponse getMy(String email, int year, int month) {
        return salaryRecordRepository.findMyConfirmed(email, year, month)
                .map(SalaryRecordResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.SALARY_RECORD_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<SalaryRecordResponse> getAll(int year, int month, Long userId, String status, int page, int size) {
        SalaryStatus salaryStatus = status != null ? SalaryStatus.valueOf(status) : null;
        Page<SalaryRecord> p = salaryRecordRepository.findAllByYearMonthFiltered(
                year, month, userId, salaryStatus, PageRequest.of(page, size));
        return new PageResponse<>(
                p.getContent().stream().map(SalaryRecordResponse::from).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), p.isLast());
    }
}
