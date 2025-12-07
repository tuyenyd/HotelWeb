package com.example.hotel.repository;

import com.example.hotel.entity.Amenity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    // Tìm theo từ khóa + loại tiện ích
    @Query("""
        SELECT a FROM Amenity a 
        WHERE (LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.description) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:type IS NULL OR a.type = :type)
    """)
    Page<Amenity> search(
            @Param("query") String query,
            @Param("type") String type,
            Pageable pageable
    );

    // Tìm theo ID (mặc định JpaRepository đã có, nhưng vẫn để lại cũng không sao)
    Optional<Amenity> findById(Long id);
}
