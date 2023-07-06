package com.tuleninov.chiphonecontacts.repository;

import com.tuleninov.chiphonecontacts.model.contact.Contact;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    boolean existsByName(String name);

    Page<Contact> findAllByUser(CustomUser user, Pageable pageable);

    Optional<Contact> findByUserAndName(CustomUser user, String name);

}
