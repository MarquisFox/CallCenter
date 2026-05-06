package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vinpin.statisticservice.entity.CallChecklistResultEntity;

import java.time.LocalDate;
import java.util.List;

public interface CallChecklistResultRepository extends JpaRepository<CallChecklistResultEntity, Long> {

    @Query(value = """
        SELECT DISTINCT COALESCE(ccr.recommendation, ci.description) AS description
        FROM call c
        JOIN call_checklist_result ccr ON c.id = ccr.call_id
        JOIN checklist_item ci ON ccr.checklist_item_id = ci.id
        WHERE c.manager_id = :managerId
          AND ccr.is_completed = false
          AND (CAST(:startDate AS DATE) IS NULL OR c.date >= CAST(:startDate AS DATE))
          AND (CAST(:endDate AS DATE) IS NULL OR c.date <= CAST(:endDate AS DATE))
          AND (CAST(:tonalityIdsStr AS TEXT) IS NULL OR c.tonality_id = ANY(string_to_array(:tonalityIdsStr, ',')::smallint[]))
    """, nativeQuery = true)
    List<String> findDistinctErrorsByManager(
            @Param("managerId") Long managerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("tonalityIdsStr") String tonalityIdsStr);
}