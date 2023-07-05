package com.tuleninov.chiphonecontacts.repository;

import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.model.user.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<CustomUser, Long> {

    Optional<CustomUser> findByActivationCode(String activationCode);

    Optional<CustomUser> findByEmail(String email);

    Optional<CustomUser> findByEmailOrNickname(String email, String nickname);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    void deleteByEmail(String email);

    @Query("update CustomUser u set u.status = :status where u.email = :email")
    @Modifying
    void changeStatusByEmail(String email, UserStatus status);

}
