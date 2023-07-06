package com.tuleninov.chiphonecontacts.model.contact.response;

import com.tuleninov.chiphonecontacts.model.contact.Contact;
import com.tuleninov.chiphonecontacts.model.email.Email;
import com.tuleninov.chiphonecontacts.model.phone.Phone;

import java.util.List;

/**
 * Record for the Contact response.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
public record ContactResponse(String name,
                              List<String> emails,
                              List<String> phones
) {

    public static ContactResponse fromContact(Contact contact) {
        List<String> emails = contact.getEmails().stream()
                .map(Email::getValue)
                .toList();
        List<String> phones = contact.getPhones().stream()
                .map(Phone::getValue)
                .toList();

        return new ContactResponse(
                contact.getName(),
                emails,
                phones
        );
    }

    public static ContactResponse fromContactWithBasicAttributes(Contact contact) {
        return new ContactResponse(
                contact.getName(),
                null,
                null
        );
    }
}
