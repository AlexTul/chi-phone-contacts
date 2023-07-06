package com.tuleninov.chiphonecontacts.service.contact;

import com.tuleninov.chiphonecontacts.model.contact.request.SaveContactRequest;
import com.tuleninov.chiphonecontacts.model.contact.response.ContactResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ContactOperations {

    /**
     * Create the contact in the database.
     *
     * @param email   the user`s login
     * @param request request with contact`s parameters
     * @return the contact from the database in response format
     */
    ContactResponse create(String email, SaveContactRequest request);

    /**
     * Find all contacts in the database in response format with pagination information.
     *
     * @param email    user`s login
     * @param pageable abstract interface for pagination information
     * @return all contacts from the database in response format
     */
    Page<ContactResponse> list(String email, Pageable pageable);

    /**
     * Merge the contact in the database.
     *
     * @param email   the login of the user
     * @param name    the name of contact
     * @param request the request with contact parameters
     */
    ContactResponse mergeByName(String email, String name, SaveContactRequest request);

    /**
     * Delete the contact in the database.
     *
     * @param email user`s login
     * @param name  the name of the contact
     * @return the contact from the database in response format
     */
    Optional<ContactResponse> deleteByName(String email, String name);
}
