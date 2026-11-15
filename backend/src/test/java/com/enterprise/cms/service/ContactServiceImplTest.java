package com.enterprise.cms.service;

import com.enterprise.cms.dto.ContactRequest;
import com.enterprise.cms.entity.Contact;
import com.enterprise.cms.exception.ResourceNotFoundException;
import com.enterprise.cms.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository contactRepository;

    @InjectMocks
    private ContactServiceImpl contactService;

    private Contact sampleContact;
    private ContactRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleContact = new Contact();
        sampleContact.setId("cnt_001");
        sampleContact.setUserId("usr_001");
        sampleContact.setFirstName("Sarah");
        sampleContact.setLastName("Chen");
        sampleContact.setTitle("VP of Engineering");
        sampleContact.setCompany("Strata Cloud");
        sampleContact.setCountry("United States");

        sampleRequest = new ContactRequest();
        sampleRequest.setFirstName("Sarah");
        sampleRequest.setLastName("Chen");
        sampleRequest.setTitle("VP of Engineering");
        sampleRequest.setCompany("Strata Cloud");
        sampleRequest.setCountry("United States");

        ContactRequest.EmailItem emailItem = new ContactRequest.EmailItem();
        emailItem.setEmail("sarah.chen@stratacloud.io");
        emailItem.setLabel("work");
        emailItem.setIsPrimary(true);
        sampleRequest.setEmails(List.of(emailItem));

        ContactRequest.PhoneItem phoneItem = new ContactRequest.PhoneItem();
        phoneItem.setNumber("+1 415 782 9901");
        phoneItem.setLabel("work");
        phoneItem.setIsPrimary(true);
        sampleRequest.setPhones(List.of(phoneItem));
    }

    @Test
    @DisplayName("Should create contact with labeled emails and phones")
    void createContact_Success() {
        when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));

        Contact created = contactService.createContact("usr_001", sampleRequest);

        assertNotNull(created.getId());
        assertEquals("Sarah", created.getFirstName());
        assertEquals(1, created.getEmails().size());
        assertEquals("work", created.getEmails().get(0).getLabel());
        assertEquals(1, created.getPhones().size());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    @DisplayName("Should return paginated filtered contacts")
    void getContacts_Paginated() {
        Page<Contact> page = new PageImpl<>(List.of(sampleContact));
        when(contactRepository.findFilteredContacts("usr_001", "Sarah", "All", false, PageRequest.of(0, 10)))
                .thenReturn(page);

        Page<Contact> result = contactService.getContacts("usr_001", "Sarah", "All", false, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Chen", result.getContent().get(0).getLastName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when contact does not exist")
    void getContactById_NotFound() {
        when(contactRepository.findByIdAndUserId("cnt_missing", "usr_001")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> contactService.getContactById("usr_001", "cnt_missing"));
    }

    @Test
    @DisplayName("Should delete contact when owned by user")
    void deleteContact_Success() {
        when(contactRepository.findByIdAndUserId("cnt_001", "usr_001")).thenReturn(Optional.of(sampleContact));

        contactService.deleteContact("usr_001", "cnt_001");

        verify(contactRepository, times(1)).delete(sampleContact);
    }
}
