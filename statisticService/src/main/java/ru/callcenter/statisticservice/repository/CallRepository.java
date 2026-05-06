package ru.vinpin.statisticservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vinpin.statisticservice.entity.CallEntity;

import java.util.List;

public interface CallRepository extends JpaRepository<CallEntity, Long>,
        JpaSpecificationExecutor<CallEntity> {

    @Query("SELECT c FROM CallEntity c WHERE c.status.id IN :statusIds")
    Page<CallEntity> findAllByStatusIn(@Param("statusIds") List<Long> statusIds, Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE c.date > :startDate AND c.date <= :endDate AND c.status.id IN :statusIds")
    Page<CallEntity> findAllByDateAndStatusIn(@Param("startDate") java.time.LocalDate startDate,
                                              @Param("endDate") java.time.LocalDate endDate,
                                              @Param("statusIds") List<Long> statusIds,
                                              Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE c.manager.id IN :managerIds")
    Page<CallEntity> findAllByManagerIn(@Param("managerIds") List<Long> managerIds, Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE c.date > :startDate AND c.date <= :endDate")
    Page<CallEntity> findAllByDate(@Param("startDate") java.time.LocalDate startDate,
                                   @Param("endDate") java.time.LocalDate endDate,
                                   Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE c.manager.id IN :managerIds AND c.date BETWEEN :startDate AND :endDate")
    Page<CallEntity> findAllByManagerInAndDate(@Param("managerIds") List<Long> managerIds,
                                               @Param("startDate") java.time.LocalDate startDate,
                                               @Param("endDate") java.time.LocalDate endDate,
                                               Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE c.manager.id IN :managerIds AND c.date BETWEEN :startDate AND :endDate AND c.status.id IN :statusIds")
    Page<CallEntity> findAllByManagerInAndDateAndStatusIn(@Param("managerIds") List<Long> managerIds,
                                                          @Param("startDate") java.time.LocalDate startDate,
                                                          @Param("endDate") java.time.LocalDate endDate,
                                                          @Param("statusIds") List<Long> statusIds,
                                                          Pageable pageable);

    @Query("SELECT c FROM CallEntity c WHERE UPPER(c.callName) = UPPER(?1)")
    CallEntity findByCallNameIgnoreCase(String callName);

    @Query("SELECT (COUNT(c) > 0) FROM CallEntity c WHERE UPPER(c.callName) = UPPER(?1)")
    boolean existsByCallNameIgnoreCase(String callName);

    @Query("SELECT c FROM CallEntity c WHERE c.manager.id = ?1")
    List<CallEntity> findByManager_Id(Long id);

    @Query("SELECT DISTINCT c.manager.id FROM CallEntity c")
    List<Long> findAllManagerIds();

    @Query("SELECT c FROM CallEntity c JOIN FETCH c.manager m LEFT JOIN FETCH c.tonality t ORDER BY c.date DESC")
    Page<CallEntity> findAllWithManagerAndTonality(Pageable pageable);

    // Методы с динамическими фильтрами теперь через Specification,
    // но чтобы сразу подгружать tonality, объявим кастомный метод с EntityGraph
    @EntityGraph(attributePaths = {"tonality"})
    List<CallEntity> findAll(Specification<CallEntity> spec, Sort sort);
}