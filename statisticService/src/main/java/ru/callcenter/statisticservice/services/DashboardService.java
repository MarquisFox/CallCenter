package ru.vinpin.statisticservice.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.vinpin.statisticservice.dto.api.DashboardAggregatedDto;
import ru.vinpin.statisticservice.entity.CallEntity;
import ru.vinpin.statisticservice.repository.CallRepository;
import ru.vinpin.statisticservice.repository.ManagerRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CallRepository callRepository;
    private final ManagerRepository managerRepository;

    @Cacheable(value = "dashboardAggregated", keyGenerator = "customKeyGenerator")
    public DashboardAggregatedDto getAggregatedData(LocalDate startDate, LocalDate endDate, List<Long> managerIds) {
        DashboardAggregatedDto result = new DashboardAggregatedDto();


        List<Object[]> callsByDayData = getCallsCountByDay(startDate, endDate, managerIds);
        Map<String, Integer> callsByDay = new LinkedHashMap<>();
        for (Object[] row : callsByDayData) {
            callsByDay.put(row[0].toString(), ((Number) row[1]).intValue());
        }
        result.setCallsByDay(callsByDay);

        List<Object[]> durationByDayData = getDurationByDay(startDate, endDate, managerIds);
        Map<String, Integer> durationByDay = new LinkedHashMap<>();
        for (Object[] row : durationByDayData) {
            durationByDay.put(row[0].toString(), ((Number) row[1]).intValue());
        }
        result.setDurationByDay(durationByDay);


        List<Object[]> callsByManagerData = getCallsCountByManager(startDate, endDate, managerIds);
        Map<Long, Integer> callsByManager = new LinkedHashMap<>();
        for (Object[] row : callsByManagerData) {
            callsByManager.put((Long) row[0], ((Number) row[1]).intValue());
        }
        result.setCallsByManager(callsByManager);


        List<Long> effectiveManagerIds = (managerIds == null || managerIds.isEmpty())
                ? callRepository.findAllManagerIds() : managerIds;

        if (!effectiveManagerIds.isEmpty()) {
            List<Object[]> errorRateData = managerRepository.findErrorRateByManagerIds(
                    effectiveManagerIds, startDate, endDate);
            Map<Long, BigDecimal> errorRateByManager = new LinkedHashMap<>();
            for (Object[] row : errorRateData) {
                Long managerId = (Long) row[0];
                BigDecimal errorRate = (BigDecimal) row[1];
                errorRateByManager.put(managerId, errorRate != null ? errorRate : BigDecimal.ZERO);
            }
            result.setErrorRateByManager(errorRateByManager);
        } else {
            result.setErrorRateByManager(Map.of());
        }

        return result;
    }

    private List<Object[]> getCallsCountByDay(LocalDate startDate, LocalDate endDate, List<Long> managerIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<CallEntity> c = cq.from(CallEntity.class);

        cq.multiselect(c.get("date"), cb.count(c));
        cq.where(buildPredicates(cb, c, startDate, endDate, managerIds));
        cq.groupBy(c.get("date"));
        cq.orderBy(cb.asc(c.get("date")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Object[]> getDurationByDay(LocalDate startDate, LocalDate endDate, List<Long> managerIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<CallEntity> c = cq.from(CallEntity.class);

        cq.multiselect(c.get("date"), cb.sum(c.get("durationSeconds")));
        Predicate base = cb.isNotNull(c.get("durationSeconds"));
        Predicate[] extra = buildPredicates(cb, c, startDate, endDate, managerIds);
        Predicate[] all = new Predicate[extra.length + 1];
        all[0] = base;
        System.arraycopy(extra, 0, all, 1, extra.length);
        cq.where(all);
        cq.groupBy(c.get("date"));
        cq.orderBy(cb.asc(c.get("date")));

        return entityManager.createQuery(cq).getResultList();
    }

    private List<Object[]> getCallsCountByManager(LocalDate startDate, LocalDate endDate, List<Long> managerIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<CallEntity> c = cq.from(CallEntity.class);

        cq.multiselect(c.get("manager").get("id"), cb.count(c));
        cq.where(buildPredicates(cb, c, startDate, endDate, managerIds));
        cq.groupBy(c.get("manager").get("id"));

        return entityManager.createQuery(cq).getResultList();
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CallEntity> callRoot,
                                        LocalDate startDate, LocalDate endDate, List<Long> managerIds) {
        List<Predicate> predicates = new ArrayList<>();
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(callRoot.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(callRoot.get("date"), endDate));
        }
        if (managerIds != null && !managerIds.isEmpty()) {
            predicates.add(callRoot.get("manager").get("id").in(managerIds));
        }
        return predicates.toArray(new Predicate[0]);
    }
}