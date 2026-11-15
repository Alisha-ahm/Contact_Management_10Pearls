package com.enterprise.cms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "contact_emails", indexes = {
    @Index(name = "ix_contact_emails_contact_id", columnList = "contact_id")
})
public class ContactEmail {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    @JsonIgnore
    private Contact contact;

    @NotBlank
    @Email
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String label;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    public ContactEmail() {}

    public ContactEmail(String id, String email, String label, Boolean isPrimary) {
        this.id = id;
        this.email = email;
        this.label = label;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}
