package com.example.demo.name;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NameRepository extends JpaRepository<NameRecord, Long> {

    List<NameRecord> findByGender(Gender gender);

    @Query("""
            SELECT n FROM NameRecord n
            WHERE (:gender IS NULL OR n.gender = :gender)
              AND (:origin IS NULL OR LOWER(n.origin) = LOWER(CAST(:origin AS string)))
              AND (:province IS NULL OR LOWER(n.province) = LOWER(CAST(:province AS string)))
              AND (:query IS NULL OR LOWER(n.name) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
            ORDER BY n.popularity DESC, n.name ASC
            """)
    List<NameRecord> search(@Param("query") String query,
                            @Param("gender") Gender gender,
                            @Param("origin") String origin,
                            @Param("province") String province);

    @Query("SELECT DISTINCT n.origin FROM NameRecord n WHERE n.origin IS NOT NULL ORDER BY n.origin ASC")
    List<String> findDistinctOrigins();

    @Query("SELECT DISTINCT n.province FROM NameRecord n WHERE n.province IS NOT NULL ORDER BY n.province ASC")
    List<String> findDistinctProvinces();

    @Query("""
            SELECT DISTINCT n FROM NameRecord n
            WHERE (:gender IS NULL OR n.gender = :gender)
              AND (:origin IS NULL OR LOWER(n.origin) = LOWER(CAST(:origin AS string)))
            ORDER BY n.popularity DESC
            """)
    List<NameRecord> suggest(@Param("gender") Gender gender, @Param("origin") String origin);
}