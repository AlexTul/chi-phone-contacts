package com.tuleninov.chiphonecontacts.repository;

import com.tuleninov.chiphonecontacts.model.email.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {

    boolean existsByValue(String name);

}
