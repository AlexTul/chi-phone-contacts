package com.tuleninov.chiphonecontacts.repository;

import com.tuleninov.chiphonecontacts.model.phone.Phone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<Phone, Long> {

    boolean existsByValue(String name);

}
