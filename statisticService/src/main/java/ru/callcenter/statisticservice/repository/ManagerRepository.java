package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vinpin.statisticservice.entity.ManagerEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ManagerRepository extends JpaRepository<ManagerEntity, Long> {

    boolean existsByCode(String code);

    Optional<ManagerEntity> findByCode(String code);

    @Query("SELECT m FROM ManagerEntity m WHERE m.code = ?1")
    ManagerEntity getByCode(String code);

    @Query(value = """
        SELECT 
            m.id,
            m.name,
            CASE 
                WHEN COUNT(DISTINCT c.id) = 0 THEN 0
                ELSE (100.0 * COUNT(DISTINCT CASE WHEN ccr.is_completed = false THEN c.id END) / COUNT(DISTINCT c.id))
            END AS error_rate
        FROM manager m
        LEFT JOIN call c ON m.id = c.manager_id
        LEFT JOIN call_checklist_result ccr ON c.id = ccr.call_id
        GROUP BY m.id, m.name
        ORDER BY m.name
    """, nativeQuery = true)
    List<Object[]> findAllManagersWithErrorRate();

    @Query(value = """
        SELECT 
            m.id,
            CASE 
                WHEN COUNT(DISTINCT c.id) = 0 THEN 0
                ELSE (100.0 * COUNT(DISTINCT CASE WHEN ccr.is_completed = false THEN c.id END) / COUNT(DISTINCT c.id))
            END AS error_rate
        FROM manager m
        LEFT JOIN call c ON m.id = c.manager_id 
            AND (CAST(:startDate AS DATE) IS NULL OR c.date >= CAST(:startDate AS DATE))
            AND (CAST(:endDate AS DATE) IS NULL OR c.date <= CAST(:endDate AS DATE))
        LEFT JOIN call_checklist_result ccr ON c.id = ccr.call_id
        WHERE m.id IN :managerIds
        GROUP BY m.id
    """, nativeQuery = true)
    List<Object[]> findErrorRateByManagerIds(
            @Param("managerIds") List<Long> managerIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}