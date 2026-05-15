package com.grown.smartoffice.domain.user.repository;

import com.grown.smartoffice.domain.user.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
}
