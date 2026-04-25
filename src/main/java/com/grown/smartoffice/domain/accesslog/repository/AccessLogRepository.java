package com.grown.smartoffice.domain.accesslog.repository;

import com.grown.smartoffice.domain.accesslog.entity.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {
}
