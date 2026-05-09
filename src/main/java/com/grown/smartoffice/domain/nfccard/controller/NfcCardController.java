package com.grown.smartoffice.domain.nfccard.controller;

import com.grown.smartoffice.domain.nfccard.dto.*;
import com.grown.smartoffice.domain.nfccard.entity.NfcCardStatus;
import com.grown.smartoffice.domain.nfccard.service.NfcCardService;
import com.grown.smartoffice.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "NfcCards", description = "NFC 카드 관리 [ADMIN 전용]")
@RestController
@RequestMapping("/api/v1/nfc-cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NfcCardController {

    private final NfcCardService nfcCardService;

    @Operation(summary = "NFC 카드 등록 [ADMIN]")
    @PostMapping
    public ResponseEntity<ApiResponse<NfcCardRegisterResponse>> registerCard(
            @RequestBody @Valid NfcCardRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("NFC 카드가 발급되었습니다.", nfcCardService.registerCard(request)));
    }

    @Operation(summary = "전체 NFC 카드 목록 조회 [ADMIN]")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NfcCardListItemResponse>>> getAllCards(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) NfcCardStatus status) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", nfcCardService.getAllCards(userId, cardType, status)));
    }

    @Operation(summary = "NFC 카드 상세 조회 [ADMIN]")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NfcCardDetailResponse>> getCardDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("정상 조회되었습니다.", nfcCardService.getCardDetail(id)));
    }

    @Operation(summary = "NFC 카드 수정 (분실 처리 등) [ADMIN]")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NfcCardUpdateResponse>> updateCard(
            @PathVariable Long id,
            @RequestBody NfcCardUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("카드 정보가 수정되었습니다.", nfcCardService.updateCard(id, request)));
    }

    @Operation(summary = "NFC 카드 삭제 [ADMIN]")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCard(@PathVariable Long id) {
        nfcCardService.deleteCard(id);
        return ResponseEntity.ok(ApiResponse.success("NFC 카드가 삭제되었습니다."));
    }
}
