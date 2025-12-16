package com.example.hotel.repository;

import com.example.hotel.entity.Amenity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    @Query("""
        SELECT a FROM Amenity a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    Page<Amenity> searchByNameOrDescription(String q, Pageable pageable);

    Page<Amenity> findByCategory(String category, Pageable pageable);

    @Query("""
        SELECT a FROM Amenity a
        WHERE (LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))
           OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%')))
        AND a.type = :type
    """)
    Page<Amenity> searchByNameAndCategory(
            String q,
            String category,
            Pageable pageable
    );
}
