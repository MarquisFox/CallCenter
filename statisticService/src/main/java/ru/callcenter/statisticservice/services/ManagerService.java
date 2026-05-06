package ru.vinpin.statisticservice.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vinpin.statisticservice.dto.api.CallBriefDto;
import ru.vinpin.statisticservice.dto.api.ErrorDescriptionDto;
import ru.vinpin.statisticservice.dto.api.ManagerDetailsDto;
import ru.vinpin.statisticservice.dto.api.ManagerErrorDto;
import ru.vinpin.statisticservice.entity.CallEntity;
import ru.vinpin.statisticservice.entity.ManagerEntity;
import ru.vinpin.statisticservice.mapper.CallMapper;
import ru.vinpin.statisticservice.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final CallRepository callRepository;
    private final CallChecklistResultRepository checklistResultRepository;
    private final CallMapper callMapper;
    private final ChecklistItemRepository checklistItemRepository;
    private final TonalityRepository tonalityRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @CacheEvict(value = {"managersErrorRate", "managerDetails"}, allEntries = true)
    @Transactional
    public ManagerEntity findOrCreateManager(String externalCode, String name, String position) {
        Optional<ManagerEntity> existing = managerRepository.findByCode(externalCode);
        if (existing.isPresent()) {
            ManagerEntity manager = existing.get();
            if (name != null) manager.setName(name);
            if (position != null) manager.setPosition(position);
            return managerRepository.save(manager);
        } else {
            ManagerEntity newManager = new ManagerEntity();
            newManager.setCode(externalCode);
            newManager.setName(name != null ? name : "Unknown");
            newManager.setPosition(position);
            return managerRepository.save(newManager);
        }
    }

    @Cacheable(value = "managersErrorRate")
    public List<ManagerErrorDto> getAllManagersWithErrorRate() {
        List<Object[]> results = managerRepository.findAllManagersWithErrorRate();
        return results.stream().map(row -> {
            ManagerErrorDto dto = new ManagerErrorDto();
            dto.setId((Long) row[0]);
            dto.setName((String) row[1]);
            BigDecimal errorRate = (BigDecimal) row[2];
            dto.setErrorRate(errorRate != null ? errorRate : BigDecimal.ZERO);
            return dto;
        }).collect(Collectors.toList());
    }

    @Cacheable(value = "managerDetails", keyGenerator = "customKeyGenerator")
    public ManagerDetailsDto getManagerDetails(Long managerId, LocalDate startDate,
                                               LocalDate endDate, List<Short> tonalityIds) {
        ManagerEntity manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        ManagerDetailsDto dto = new ManagerDetailsDto();
        dto.setId(manager.getId());
        dto.setName(manager.getName());

        List<Object[]> ratingStats = getRatingStats(managerId, startDate, endDate, tonalityIds);

        BigDecimal avgRating = ratingStats.stream()
                .findFirst()
                .filter(row -> row.length > 0)
                .map(row -> safeConvertToBigDecimal(row[0]))
                .orElse(BigDecimal.ZERO);

        BigDecimal avgErrorRate = ratingStats.stream()
                .findFirst()
                .filter(row -> row.length > 1)
                .map(row -> safeConvertToBigDecimal(row[1]))
                .orElse(BigDecimal.ZERO);

        dto.setRating(avgRating.setScale(2, RoundingMode.HALF_UP));
        dto.setErrorRate(avgErrorRate.setScale(4, RoundingMode.HALF_UP));

        String tonalityIdsStr = (tonalityIds != null && !tonalityIds.isEmpty())
                ? tonalityIds.stream().map(String::valueOf).collect(Collectors.joining(","))
                : null;

        List<String> errorDescriptions = checklistResultRepository.findDistinctErrorsByManager(
                managerId, startDate, endDate, tonalityIdsStr);

        List<ErrorDescriptionDto> errors = errorDescriptions.stream()
                .map(desc -> {
                    ErrorDescriptionDto errorDto = new ErrorDescriptionDto();
                    errorDto.setDescription(desc);
                    return errorDto;
                })
                .collect(Collectors.toList());
        dto.setErrors(errors);

        List<CallEntity> calls = callRepository.findAll(
                Specification.where(CallSpecifications.managerIdEqual(managerId))
                        .and(CallSpecifications.dateBetween(startDate, endDate))
                        .and(CallSpecifications.tonalityIdIn(tonalityIds)),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date")
        );

        List<CallBriefDto> callBriefs = calls.stream()
                .map(call -> {
                    CallBriefDto brief = new CallBriefDto();
                    brief.setId(call.getCallName());
                    brief.setDate(call.getDate());
                    brief.setDuration(callMapper.formatDuration(call.getDuration()));
                    brief.setTonality(call.getTonality() != null ?
                            call.getTonality().getNameRu() : "Не определена");
                    return brief;
                })
                .collect(Collectors.toList());
        dto.setCalls(callBriefs);

        return dto;
    }

    @Transactional
    public ManagerEntity save(ManagerEntity manager) {
        return managerRepository.save(manager);
    }

    private List<Object[]> getRatingStats(Long managerId, LocalDate startDate,
                                          LocalDate endDate, List<Short> tonalityIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<CallEntity> call = cq.from(CallEntity.class);

        cq.multiselect(
                cb.coalesce(cb.avg(call.get("rating")), 0.0),
                cb.coalesce(cb.avg(call.get("errorRate")), 0.0)
        );

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(call.get("manager").get("id"), managerId));
        predicates.add(cb.isNotNull(call.get("rating")));
        predicates.add(cb.isNotNull(call.get("errorRate")));

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(call.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(call.get("date"), endDate));
        }
        if (tonalityIds != null && !tonalityIds.isEmpty()) {
            predicates.add(call.get("tonality").get("id").in(tonalityIds));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }

    private BigDecimal safeConvertToBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try {
            if (value instanceof BigDecimal) return (BigDecimal) value;
            if (value instanceof Double) return BigDecimal.valueOf((Double) value);
            if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
            String str = value.toString().trim();
            if (str.isEmpty() || str.equalsIgnoreCase("null")) return BigDecimal.ZERO;
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            System.err.println("WARN: Cannot convert to BigDecimal: " + value);
            return BigDecimal.ZERO;
        }
    }
}