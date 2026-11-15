package com.enterprise.cms.service;

import com.enterprise.cms.dto.ContactRequest;
import com.enterprise.cms.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactService {
    Page<Contact> getContacts(String userId, String search, String category, boolean favoriteOnly, Pageable pageable);
    Contact getContactById(String userId, String contactId);
    Contact createContact(String userId, ContactRequest request);
    Contact updateContact(String userId, String contactId, ContactRequest request);
    boolean toggleFavorite(String userId, String contactId);
    void deleteContact(String userId, String contactId);
    int bulkDelete(String userId, List<String> contactIds);
    long countTotal(String userId);
    long countFavorites(String userId);
}
