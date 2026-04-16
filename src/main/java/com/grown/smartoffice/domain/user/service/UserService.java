package com.grown.smartoffice.domain.user.service;

import com.grown.smartoffice.domain.user.dto.*;
import com.grown.smartoffice.global.common.PageResponse;

public interface UserService {

    PageResponse<UserListItemResponse> getUsers(Long departmentId, String status, String keyword,
                                                int page, int size);

    UserCreateResponse createUser(UserCreateRequest request);

    UserDetailResponse getUserDetail(Long userId);

    UserUpdateResponse updateUser(Long userId, UserUpdateRequest request);

    void deactivateUser(Long userId);

    UserMeInfoResponse getMyInfo(String email);

    UserMeUpdateResponse updateMyInfo(String email, UserMeUpdateRequest request);
}
