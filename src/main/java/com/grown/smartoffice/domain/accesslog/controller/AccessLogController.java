package com.grown.smartoffice.domain.accesslog.controller;

import com.grown.smartoffice.domain.accesslog.dto.TagEventRequest;
import com.grown.smartoffice.domain.accesslog.dto.TagEventResponse;
import com.grown.smartoffice.domain.accesslog.service.AccessLogService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Access Logs", description = "NFC 출입 이벤트 처리 (IoT 장치 → 서버, 인증 불필요)")
@RestController
@RequestMapping("/api/v1/access-logs")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    @Operation(summary = "NFC 태그 이벤트 처리",
               description = "IoT 장치(RPi)가 NFC 태그를 감지하면 호출. 출입 판정 후 결과 반환. 인증 불필요.")
    @PostMapping("/tag")
    public ResponseEntity<ApiResponse<TagEventResponse>> processTag(
            @RequestBody @Valid TagEventRequest request) {
        TagEventResponse response = accessLogService.processTag(request);
        return ResponseEntity.ok(ApiResponse.success("출입 판정이 완료되었습니다.", response));
    }
}
