package com.grown.smartoffice.domain.user.service;

import com.grown.smartoffice.domain.user.dto.UserPreferencesResponse;
import com.grown.smartoffice.domain.user.dto.UserPreferencesUpdateRequest;
import com.grown.smartoffice.domain.user.entity.User;
import com.grown.smartoffice.domain.user.entity.UserPreferences;
import com.grown.smartoffice.domain.user.repository.UserPreferencesRepository;
import com.grown.smartoffice.domain.user.repository.UserRepository;
import com.grown.smartoffice.global.exception.CustomException;
import com.grown.smartoffice.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final UserRepository userRepository;

    /** 본인 환경설정 조회. 설정 행이 없으면 기본값으로 생성 후 반환. */
    @Transactional
    public UserPreferencesResponse getMyPreferences(String email) {
        return UserPreferencesResponse.from(getOrCreate(findUser(email)));
    }

    /** 본인 환경설정 수정. 설정 행이 없으면 기본값으로 생성 후 부분 수정. */
    @Transactional
    public UserPreferencesResponse updateMyPreferences(String email,
                                                       UserPreferencesUpdateRequest request) {
        UserPreferences preferences = getOrCreate(findUser(email));
        preferences.update(
                request.getNotificationsEnabled(),
                request.getLanguage(),
                request.getTheme(),
                request.getPushToken());
        return UserPreferencesResponse.from(preferences);
    }

    private UserPreferences getOrCreate(User user) {
        return userPreferencesRepository.findById(user.getUserId())
                .orElseGet(() -> userPreferencesRepository.save(UserPreferences.defaults(user.getUserId())));
    }

    private User findUser(String email) {
        return userRepository.findByEmployeeEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
