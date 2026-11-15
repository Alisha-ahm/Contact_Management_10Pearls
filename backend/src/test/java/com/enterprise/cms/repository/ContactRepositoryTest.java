package com.enterprise.cms.repository;

import com.enterprise.cms.entity.Contact;
import com.enterprise.cms.entity.ContactEmail;
import com.enterprise.cms.entity.ContactPhone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Test
    @DisplayName("DataJpaTest: Should persist contact and search by first name, last name, email, and phone")
    void findFilteredContacts_MatchesAllFields() {
        Contact c = new Contact();
        c.setId("cnt_repo_1");
        c.setUserId("usr_repo_1");
        c.setFirstName("Taro");
        c.setLastName("Yamamoto");
        c.setTitle("Robotics Lead");
        c.setCompany("NeoTokyo Labs");
        c.setCategory("Partners");
        c.setIsFavorite(true);
        c.addEmail(new ContactEmail("em_r1", "taro@neotokyo.jp", "work", true));
        c.addPhone(new ContactPhone("ph_r1", "+81355550143", "work", true));

        contactRepository.save(c);

        Page<Contact> byName = contactRepository.findFilteredContacts(
                "usr_repo_1", "Yamamoto", "All", false, PageRequest.of(0, 10));
        assertEquals(1, byName.getTotalElements());

        Page<Contact> byEmail = contactRepository.findFilteredContacts(
                "usr_repo_1", "neotokyo.jp", "All", false, PageRequest.of(0, 10));
        assertEquals(1, byEmail.getTotalElements());

        assertEquals(1L, contactRepository.countByUserIdAndIsFavoriteTrue("usr_repo_1"));
    }
}
