package com.tuleninov.chiphonecontacts.controller.contact;

import com.tuleninov.chiphonecontacts.Routes;
import com.tuleninov.chiphonecontacts.model.contact.request.SaveContactRequest;
import com.tuleninov.chiphonecontacts.model.contact.response.ContactResponse;
import com.tuleninov.chiphonecontacts.service.contact.ContactOperations;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.tuleninov.chiphonecontacts.exceptions.contact.ContactExceptions.contactNotFound;

@RestController
@RequestMapping(Routes.CONTACTS)
public class ContactController {

    private final ContactOperations contactOperations;

    public ContactController(ContactOperations contactOperations) {
        this.contactOperations = contactOperations;
    }

    /**
     * Create the contact in the database.
     *
     * @param request request with contact parameters
     * @return the contact from the database in response format
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(@AuthenticationPrincipal String email,
                                  @RequestBody @Valid SaveContactRequest request) {
        return contactOperations.create(email, request);
    }

    /**
     * Find all contacts in the database in response format with pagination information.
     *
     * @param pageable abstract interface for pagination information
     * @return all contacts from the database in response format
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PageableAsQueryParam
    public Page<ContactResponse> listContacts(@AuthenticationPrincipal String email,
                                              @Parameter(hidden = true) Pageable pageable) {
        return contactOperations.list(email, pageable);
    }

    /**
     * Merge the contact by name in the database.
     *
     * @param email   the login of the user
     * @param name    the name of contact
     * @param request the request with contact parameters
     */
    @PutMapping(
            value = "/{name}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ContactResponse mergeContactByName(@AuthenticationPrincipal String email,
                                              @PathVariable String name,
                                              @RequestBody @Valid SaveContactRequest request) {
        return contactOperations.mergeByName(email, name, request);
    }

    /**
     * Delete the contact by name in the database.
     *
     * @param name the name of the contact
     * @return the contact from the database in response format
     */
    @DeleteMapping(
            value = "/{name}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public ContactResponse deleteContactsByName(@AuthenticationPrincipal String email,
                                                @PathVariable String name) {
        return contactOperations.deleteByName(email, name)
                .orElseThrow(() -> contactNotFound(name));
    }
}
