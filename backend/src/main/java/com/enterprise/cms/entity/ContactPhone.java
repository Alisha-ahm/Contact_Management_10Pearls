package com.enterprise.cms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "contact_phones", indexes = {
    @Index(name = "ix_contact_phones_contact_id", columnList = "contact_id")
})
public class ContactPhone {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    @JsonIgnore
    private Contact contact;

    @NotBlank
    @Column(name = "phone_number", nullable = false, length = 60)
    private String number;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String label;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    public ContactPhone() {}

    public ContactPhone(String id, String number, String label, Boolean isPrimary) {
        this.id = id;
        this.number = number;
        this.label = label;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Contact getContact() { return contact; }
    public void setContact(Contact contact) { this.contact = contact; }

    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getIsPrimary() { return isPrimary; }
    public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
}
