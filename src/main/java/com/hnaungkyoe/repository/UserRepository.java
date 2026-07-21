package com.hnaungkyoe.repository;

import com.hnaungkyoe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ─── Existing (မပြောင်းဘူး) ─────────────────────────────────
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByRoleIn(List<User.Role> roles);
    long countByRoleIn(List<User.Role> roles);

    // ─── NEW: Township matching for auto-assign ──────────────────
    // Priority 1: Same township
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'ROLE_VOLUNTEER'
        AND u.township = :township
        ORDER BY u.createdAt ASC
    """)
    List<User> findVolunteersByTownship(@Param("township") String township);

    // Priority 2: Same district (city)
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'ROLE_VOLUNTEER'
        AND u.city = :district
        ORDER BY u.createdAt ASC
    """)
    List<User> findVolunteersByDistrict(@Param("district") String district);

    // Priority 3: Same division/state
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'ROLE_VOLUNTEER'
        AND u.division = :division
        ORDER BY u.createdAt ASC
    """)
    List<User> findVolunteersByDivision(@Param("division") String division);

    // All volunteers in a division (admin dropdown list)
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'ROLE_VOLUNTEER'
        AND (u.division = :division OR u.township = :township OR :township IS NULL)
        ORDER BY u.township ASC, u.fullName ASC
    """)
    List<User> findVolunteersForAssignment(
            @Param("division") String division,
            @Param("township") String township
    );

    // All admins (for notifications)
    @Query("""
        SELECT u FROM User u
        WHERE u.role IN ('ROLE_SUPER_ADMIN', 'ROLE_DIVISION_ADMIN',
                         'ROLE_CITY_ADMIN', 'ROLE_SUB_ADMIN')
    """)
    List<User> findAllAdmins();

    List<User> findAvailableVolunteersByTownship(String township);
    List<User> findByTownshipAndRoleIn(String township, List<User.Role> roles);
}
