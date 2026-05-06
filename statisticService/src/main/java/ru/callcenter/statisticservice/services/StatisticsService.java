package ru.vinpin.statisticservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vinpin.statisticservice.entity.ManagerStatsEntity;
import ru.vinpin.statisticservice.repository.ManagerStatsRepository;
import ru.vinpin.statisticservice.repository.projection.ManagerStatsProjection;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final ManagerStatsRepository managerStatsRepository;

    @Transactional
    public void updateManagerStats(Long managerId) {
        ManagerStatsProjection stats = managerStatsRepository.getManagerStats(managerId);

        ManagerStatsEntity statsEntity = managerStatsRepository.findByManagerId(managerId)
                .orElseGet(() -> {
                    ManagerStatsEntity newStats = new ManagerStatsEntity();
                    newStats.setManagerId(managerId);
                    return newStats;
                });

        statsEntity.setTotalCalls(stats.getTotalCalls() != null ? stats.getTotalCalls() : 0);
        statsEntity.setAvgRating(stats.getAvgRating() != null ? stats.getAvgRating() : BigDecimal.ZERO);
        statsEntity.setAvgErrorRate(stats.getAvgErrorRate() != null ? stats.getAvgErrorRate() : BigDecimal.ZERO);
        statsEntity.setLastUpdated(Instant.now());

        managerStatsRepository.save(statsEntity);
    }
}
