package com.enterprise.cms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class ContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String title;
    private String company;
    private String department;
    private String address;
    private String city;
    private String state;
    private String country;
    private String category;
    private Boolean isFavorite;
    private String avatarColor;
    private String notes;

    @Valid
    private List<EmailItem> emails = new ArrayList<>();

    @Valid
    private List<PhoneItem> phones = new ArrayList<>();

    public static class EmailItem {
        private String id;
        @NotBlank(message = "Email value is required")
        @Email(message = "Invalid email format")
        private String email;
        @NotBlank(message = "Email label is required")
        private String label;
        private Boolean isPrimary;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }

    public static class PhoneItem {
        private String id;
        @NotBlank(message = "Phone number is required")
        private String number;
        @NotBlank(message = "Phone label is required")
        private String label;
        private Boolean isPrimary;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getNumber() { return number; }
        public void setNumber(String number) { this.number = number; }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }

    public String getAvatarColor() { return avatarColor; }
    public void setAvatarColor(String avatarColor) { this.avatarColor = avatarColor; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<EmailItem> getEmails() { return emails; }
    public void setEmails(List<EmailItem> emails) { this.emails = emails; }

    public List<PhoneItem> getPhones() { return phones; }
    public void setPhones(List<PhoneItem> phones) { this.phones = phones; }
}
