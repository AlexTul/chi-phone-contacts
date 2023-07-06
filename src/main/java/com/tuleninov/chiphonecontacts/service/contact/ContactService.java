package com.tuleninov.chiphonecontacts.service.contact;

import com.tuleninov.chiphonecontacts.exceptions.contact.ContactExceptions;
import com.tuleninov.chiphonecontacts.exceptions.email.EmailExceptions;
import com.tuleninov.chiphonecontacts.exceptions.phone.PhoneExceptions;
import com.tuleninov.chiphonecontacts.exceptions.user.UserExceptions;
import com.tuleninov.chiphonecontacts.model.contact.Contact;
import com.tuleninov.chiphonecontacts.model.contact.request.SaveContactRequest;
import com.tuleninov.chiphonecontacts.model.contact.response.ContactResponse;
import com.tuleninov.chiphonecontacts.model.email.Email;
import com.tuleninov.chiphonecontacts.model.phone.Phone;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;
import com.tuleninov.chiphonecontacts.repository.ContactRepository;
import com.tuleninov.chiphonecontacts.repository.EmailRepository;
import com.tuleninov.chiphonecontacts.repository.PhoneRepository;
import com.tuleninov.chiphonecontacts.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService implements ContactOperations {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final PhoneRepository phoneRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository,
                          EmailRepository emailRepository, PhoneRepository phoneRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.emailRepository = emailRepository;
        this.phoneRepository = phoneRepository;
    }

    /**
     * Create the contact in the database.
     *
     * @param email   the user`s login
     * @param request request with contact`s parameters
     * @return the contact from the database in response format
     */
    @Override
    @Transactional
    public ContactResponse create(String email, SaveContactRequest request) {
        validateUniqueFields(request);
        var uniqueEmails = getUniqueSequence(request.emails());
        var uniquePhones = getUniqueSequence(request.phones());
        return ContactResponse.fromContactWithBasicAttributes(save(email, request, uniqueEmails, uniquePhones));
    }

    /**
     * Find all contacts in the database in response format with pagination information.
     *
     * @param email    user`s login
     * @param pageable abstract interface for pagination information
     * @return all contacts from the database in response format
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ContactResponse> list(String email, Pageable pageable) {
        var user = getUser(email);
        return contactRepository.findAllByUser(user, pageable)
                .map(ContactResponse::fromContact);
    }

    /**
     * Merge the contact in the database.
     *
     * @param email   the login of the user
     * @param name    the name of contact
     * @param request the request with contact parameters
     */
    @Override
    @Transactional
    public ContactResponse mergeByName(String email, String name, SaveContactRequest request) {
        var user = getUser(email);
        var contact = getContact(user, name);
        return ContactResponse.fromContact(merge(contact, request));
    }

    /**
     * Delete the contact in the database.
     *
     * @param email user`s login
     * @param name  the name of the contact
     * @return the contact from the database in response format
     */
    @Override
    @Transactional
    public Optional<ContactResponse> deleteByName(String email, String name) {
        if (!contactRepository.existsByName(name)) throw ContactExceptions.contactNotFound(name);

        var user = getUser(email);
        Optional<Contact> contact = contactRepository.findByUserAndName(user, name);
        contact.ifPresent(contactRepository::delete);
        return contact.map(ContactResponse::fromContact);
    }

    /**
     * Validate contact`s fields.
     *
     * @param request request with contact`s fields
     */
    private void validateUniqueFields(SaveContactRequest request) {
        String name = request.name();
        if (contactRepository.existsByName(name)) throw ContactExceptions.duplicateName(name);

        List<String> emails = request.emails();
        List<String> uniqueEmails = getUniqueSequence(emails);
        for (String email : uniqueEmails) {
            if (emailRepository.existsByValue(email)) throw EmailExceptions.duplicateEmail(email);
        }

        List<String> phones = request.phones();
        List<String> uniquePhones = getUniqueSequence(phones);
        for (String phone : uniquePhones) {
            if (phoneRepository.existsByValue(phone)) throw PhoneExceptions.duplicatePhone(phone);
        }
    }

    /**
     * Get the unique sequence from user`s values.
     *
     * @param values the values from user`s values
     * @return the saved entity
     */
    private List<String> getUniqueSequence(List<String> values) {
        List<String> uniqueValues = new ArrayList<>();
        for (String email : values) {
            if (!uniqueValues.contains(email)) {
                uniqueValues.add(email);
            }
        }
        return uniqueValues;
    }

    /**
     * Save contact in the database.
     *
     * @param request request with contact`s fields
     * @return the saved entity
     */
    private Contact save(String email, SaveContactRequest request,
                         List<String> uniqueEmails, List<String> uniquePhones) {
        var contact = new Contact();
        var user = getUser(email);
        contact.setName(request.name());
        contact.setUser(user);
        Contact saveContact = contactRepository.save(contact);

        var listEmails = createListEmailsEntities(uniqueEmails, saveContact);
        var listPhones = createListPhonesEntities(uniquePhones, saveContact);
        emailRepository.saveAll(listEmails);
        phoneRepository.saveAll(listPhones);

        return saveContact;
    }

    /**
     * Get current user in the database.
     *
     * @param email authentication principal
     * @return current user
     */
    private CustomUser getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserExceptions.userNotFound(email));
    }

    /**
     * Get current contact in the database.
     *
     * @param name contact`s name
     * @return current contact
     */
    private Contact getContact(CustomUser user, String name) {
        return contactRepository.findByUserAndName(user, name)
                .orElseThrow(() -> ContactExceptions.contactNotFound(name));
    }

    /**
     * Create list of emails.
     *
     * @param emails the list of emails from user
     * @return the list of email entity
     */
    private List<Email> createListEmailsEntities(List<String> emails, Contact contact) {
        List<Email> emailEntities = new ArrayList<>();
        for (String email : emails) {
            var emailEntity = new Email(email, contact);
            emailEntities.add(emailEntity);
        }
        return emailEntities;
    }

    /**
     * Create list of phones.
     *
     * @param phones the list of phones from user
     * @return the list of number entity
     */
    private List<Phone> createListPhonesEntities(List<String> phones, Contact contact) {
        List<Phone> phoneEntities = new ArrayList<>();
        for (String phone : phones) {
            var numberEntity = new Phone(phone, contact);
            phoneEntities.add(numberEntity);
        }
        return phoneEntities;
    }

    /**
     * Merge the contact in the database.
     *
     * @param contact the contact from the database
     * @param request request with contact`s data
     * @return the contact that was merged
     */
    private Contact merge(Contact contact, SaveContactRequest request) {
        String name = request.name();
        if (name != null && !name.equals(contact.getName())) {
            if (contactRepository.existsByName(name)) throw ContactExceptions.duplicateName(name);
            contact.setName(name);
        }

        var uniqueEmails = getUniqueSequence(request.emails());
        var listEmails = createListEmailsEntities(uniqueEmails, contact);
        if (!listEmails.equals(contact.getEmails())) {
            for (String email : uniqueEmails) {
                if (emailRepository.existsByValue(email)) throw EmailExceptions.duplicateEmail(email);
            }
            contact.setEmails(listEmails);
        }

        var uniquePhones = getUniqueSequence(request.phones());
        var listPhones = createListPhonesEntities(uniquePhones, contact);
        if (!listPhones.equals(contact.getPhones())) {
            for (String phone : uniquePhones) {
                if (phoneRepository.existsByValue(phone)) throw PhoneExceptions.duplicatePhone(phone);
            }
            contact.setPhones(listPhones);
        }

        return contact;
    }
}
