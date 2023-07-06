package com.tuleninov.chiphonecontacts.model.phone;

import com.tuleninov.chiphonecontacts.model.contact.Contact;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class for Number entity.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
@Entity
@Table(name ="phones")
public class Phone {

    public Phone() {
    }

    public Phone(String value, Contact contact) {
        this.value = value;
        this.contact = contact;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
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
        Phone phone = (Phone) o;
        return Objects.equals(value, phone.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
