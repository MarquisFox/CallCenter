package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vinpin.statisticservice.entity.ManagerStatsEntity;
import ru.vinpin.statisticservice.repository.projection.ManagerStatsProjection;

import java.util.Optional;

public interface ManagerStatsRepository extends JpaRepository<ManagerStatsEntity, Long> {

    Optional<ManagerStatsEntity> findByManagerId(Long managerId);

    @Query("""
        SELECT COUNT(c) as totalCalls,
               AVG(c.rating) as avgRating,
               AVG(c.errorRate) as avgErrorRate
        FROM CallEntity c
        WHERE c.manager.id = :managerId
    """)
    ManagerStatsProjection getManagerStats(@Param("managerId") Long managerId);

}