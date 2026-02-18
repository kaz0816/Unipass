package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("""
            select distinct u.university
            from User u
            where u.university is not null
              and trim(u.university) <> ''
              and lower(u.university) like lower(concat(:query, '%'))
            order by u.university asc
            """)
    List<String> suggestUniversitiesByPrefix(@Param("query") String query, Pageable pageable);

    @Query("""
            select distinct u.university
            from User u
            where u.university is not null
              and trim(u.university) <> ''
              and lower(u.university) like lower(concat('%', :query, '%'))
            order by u.university asc
            """)
    List<String> suggestUniversitiesByContains(@Param("query") String query, Pageable pageable);
}
