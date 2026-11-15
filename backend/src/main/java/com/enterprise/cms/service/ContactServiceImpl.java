package com.enterprise.cms.service;

import com.enterprise.cms.dto.ContactRequest;
import com.enterprise.cms.entity.Contact;
import com.enterprise.cms.entity.ContactEmail;
import com.enterprise.cms.entity.ContactPhone;
import com.enterprise.cms.exception.ResourceNotFoundException;
import com.enterprise.cms.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactServiceImpl.class);

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Contact> getContacts(
            String userId, String search, String category, boolean favoriteOnly, Pageable pageable) {
        log.debug("Querying contacts for userId [{}], search [{}], category [{}], favoriteOnly [{}]",
                userId, search, category, favoriteOnly);
        return contactRepository.findFilteredContacts(userId, search, category, favoriteOnly, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Contact getContactById(String userId, String contactId) {
        return contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> {
                    log.warn("Contact ID [{}] not found for user [{}]", contactId, userId);
                    return new ResourceNotFoundException("Contact not found with ID: " + contactId);
                });
    }

    @Override
    public Contact createContact(String userId, ContactRequest request) {
        Contact contact = new Contact();
        contact.setId("cnt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        contact.setUserId(userId);
        applyRequestToEntity(contact, request);

        Contact saved = contactRepository.save(contact);
        log.info("Created contact [{}] ({} {}) for user [{}]",
                saved.getId(), saved.getFirstName(), saved.getLastName(), userId);
        return saved;
    }

    @Override
    public Contact updateContact(String userId, String contactId, ContactRequest request) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + contactId));

        applyRequestToEntity(contact, request);
        Contact updated = contactRepository.save(contact);
        log.info("Updated contact [{}] ({} {}) for user [{}]",
                updated.getId(), updated.getFirstName(), updated.getLastName(), userId);
        return updated;
    }

    @Override
    public boolean toggleFavorite(String userId, String contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + contactId));
        boolean nextState = !Boolean.TRUE.equals(contact.getIsFavorite());
        contact.setIsFavorite(nextState);
        contactRepository.save(contact);
        log.debug("Toggled favorite flag for contact [{}] to {}", contactId, nextState);
        return nextState;
    }

    @Override
    public void deleteContact(String userId, String contactId) {
        Contact contact = contactRepository.findByIdAndUserId(contactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with ID: " + contactId));
        contactRepository.delete(contact);
        log.info("Deleted contact [{}] and cascaded child email/phone records for user [{}]", contactId, userId);
    }

    @Override
    public int bulkDelete(String userId, List<String> contactIds) {
        int deleted = 0;
        for (String id : contactIds) {
            var opt = contactRepository.findByIdAndUserId(id, userId);
            if (opt.isPresent()) {
                contactRepository.delete(opt.get());
                deleted++;
            }
        }
        log.info("Bulk deleted {} contacts for user [{}]", deleted, userId);
        return deleted;
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotal(String userId) {
        return contactRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFavorites(String userId) {
        return contactRepository.countByUserIdAndIsFavoriteTrue(userId);
    }

    private void applyRequestToEntity(Contact contact, ContactRequest request) {
        contact.setFirstName(request.getFirstName().trim());
        contact.setLastName(request.getLastName().trim());
        contact.setTitle(request.getTitle() != null ? request.getTitle().trim() : null);
        contact.setCompany(request.getCompany() != null ? request.getCompany().trim() : null);
        contact.setDepartment(request.getDepartment() != null ? request.getDepartment().trim() : null);
        contact.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        contact.setCity(request.getCity() != null ? request.getCity().trim() : null);
        contact.setState(request.getState() != null ? request.getState().trim() : null);
        contact.setCountry(request.getCountry() != null ? request.getCountry().trim() : null);
        contact.setCategory(request.getCategory() != null ? request.getCategory() : "General");
        contact.setIsFavorite(Boolean.TRUE.equals(request.getIsFavorite()));
        contact.setAvatarColor(request.getAvatarColor() != null ? request.getAvatarColor() : "indigo");
        contact.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        contact.clearEmails();
        if (request.getEmails() != null) {
            for (ContactRequest.EmailItem item : request.getEmails()) {
                String emailId = item.getId() != null && !item.getId().isBlank()
                        ? item.getId()
                        : "em_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                contact.addEmail(new ContactEmail(emailId, item.getEmail().trim(), item.getLabel(), item.getIsPrimary()));
            }
        }

        contact.clearPhones();
        if (request.getPhones() != null) {
            for (ContactRequest.PhoneItem item : request.getPhones()) {
                String phoneId = item.getId() != null && !item.getId().isBlank()
                        ? item.getId()
                        : "ph_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                contact.addPhone(new ContactPhone(phoneId, item.getNumber().trim(), item.getLabel(), item.getIsPrimary()));
            }
        }
    }
}
