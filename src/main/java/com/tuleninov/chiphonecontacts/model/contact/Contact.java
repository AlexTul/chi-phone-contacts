package com.tuleninov.chiphonecontacts.model.contact;

import com.tuleninov.chiphonecontacts.model.email.Email;
import com.tuleninov.chiphonecontacts.model.phone.Phone;
import com.tuleninov.chiphonecontacts.model.user.CustomUser;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * Class for Contact entity.
 *
 * @author Oleksandr Tuleninov
 * @version 01
 */
@Entity
@Table(name ="contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private CustomUser user;

    @OneToMany(mappedBy = "contact")
    private List<Email> emails;

    @OneToMany(mappedBy = "contact")
    private List<Phone> phones;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CustomUser getUser() {
        return user;
    }

    public void setUser(CustomUser user) {
        this.user = user;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(name, contact.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
