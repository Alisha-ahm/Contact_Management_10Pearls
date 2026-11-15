package com.enterprise.cms.repository;

import com.enterprise.cms.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {

    Optional<Contact> findByIdAndUserId(String id, String userId);

    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.emails e " +
           "LEFT JOIN c.phones p " +
           "WHERE c.userId = :userId " +
           "AND (:category IS NULL OR :category = 'All' OR c.category = :category) " +
           "AND (:favoriteOnly = false OR c.isFavorite = true) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(c.title, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(c.company, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(COALESCE(e.email, '')) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "COALESCE(p.number, '') LIKE CONCAT('%', :search, '%'))")
    Page<Contact> findFilteredContacts(
            @Param("userId") String userId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("favoriteOnly") boolean favoriteOnly,
            Pageable pageable
    );

    long countByUserId(String userId);

    long countByUserIdAndIsFavoriteTrue(String userId);
}
