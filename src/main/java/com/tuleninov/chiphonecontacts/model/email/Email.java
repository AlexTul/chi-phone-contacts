package com.tuleninov.chiphonecontacts.model.email;

import com.tuleninov.chiphonecontacts.model.contact.Contact;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class for Email entity.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
@Entity
@Table(name ="emails")
public class Email {

    public Email() {
    }

    public Email(String value, Contact contact) {
        this.value = value;
        this.contact = contact;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
