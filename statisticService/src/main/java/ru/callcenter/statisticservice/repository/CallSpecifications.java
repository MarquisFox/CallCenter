package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.vinpin.statisticservice.entity.CallEntity;

import java.time.LocalDate;
import java.util.List;

public class CallSpecifications {

    public static Specification<CallEntity> managerIdEqual(Long managerId) {
        return (root, query, cb) -> cb.equal(root.get("manager").get("id"), managerId);
    }

    public static Specification<CallEntity> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start != null && end != null) {
                return cb.between(root.get("date"), start, end);
            } else if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), start);
            } else if (end != null) {
                return cb.lessThanOrEqualTo(root.get("date"), end);
            }
            return cb.conjunction();
        };
    }

    public static Specification<CallEntity> tonalityIdIn(List<Short> ids) {
        return (root, query, cb) -> {
            if (ids == null || ids.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("tonality").get("id").in(ids);
        };
    }

    public static Specification<CallEntity> ratingNotNull() {
        return (root, query, cb) -> cb.isNotNull(root.get("rating"));
    }

    public static Specification<CallEntity> errorRateNotNull() {
        return (root, query, cb) -> cb.isNotNull(root.get("errorRate"));
    }
}