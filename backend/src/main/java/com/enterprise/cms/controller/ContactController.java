package com.enterprise.cms.controller;

import com.enterprise.cms.dto.ContactRequest;
import com.enterprise.cms.entity.Contact;
import com.enterprise.cms.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listContacts(
            Authentication authentication,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(defaultValue = "false") boolean favoriteOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        String userId = authentication.getName();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String safeSortField = switch (sortBy) {
            case "title", "company", "createdAt", "firstName", "lastName" -> sortBy;
            default -> "lastName";
        };
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(direction, safeSortField));

        Page<Contact> resultPage = contactService.getContacts(userId, search, category, favoriteOnly, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("contacts", resultPage.getContent());
        response.put("page", resultPage.getNumber() + 1);
        response.put("pageSize", resultPage.getSize());
        response.put("totalItems", resultPage.getTotalElements());
        response.put("totalPages", Math.max(1, resultPage.getTotalPages()));
        response.put("totalAll", contactService.countTotal(userId));
        response.put("totalFavorites", contactService.countFavorites(userId));

        log.info("GET /api/v1/contacts returned {} items (page {}) for user [{}]",
                resultPage.getNumberOfElements(), page + 1, userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(
            Authentication authentication,
            @PathVariable String id) {
        String userId = authentication.getName();
        log.debug("GET /api/v1/contacts/{} requested by user [{}]", id, userId);
        return ResponseEntity.ok(contactService.getContactById(userId, id));
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(
            Authentication authentication,
            @Valid @RequestBody ContactRequest request) {
        String userId = authentication.getName();
        log.info("POST /api/v1/contacts creating {} {} for user [{}]",
                request.getFirstName(), request.getLastName(), userId);
        Contact created = contactService.createContact(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contact> updateContact(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ContactRequest request) {
        String userId = authentication.getName();
        log.info("PUT /api/v1/contacts/{} updating contact for user [{}]", id, userId);
        return ResponseEntity.ok(contactService.updateContact(userId, id, request));
    }

    @PatchMapping("/{id}/favorite")
    public ResponseEntity<Map<String, Boolean>> toggleFavorite(
            Authentication authentication,
            @PathVariable String id) {
        String userId = authentication.getName();
        boolean isFavorite = contactService.toggleFavorite(userId, id);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(
            Authentication authentication,
            @PathVariable String id) {
        String userId = authentication.getName();
        log.info("DELETE /api/v1/contacts/{} requested by user [{}]", id, userId);
        contactService.deleteContact(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, Integer>> bulkDelete(
            Authentication authentication,
            @RequestBody List<String> ids) {
        String userId = authentication.getName();
        int deleted = contactService.bulkDelete(userId, ids);
        return ResponseEntity.ok(Map.of("deletedCount", deleted));
    }
}
