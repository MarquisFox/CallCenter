package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.vinpin.statisticservice.entity.ChecklistItemEntity;

import java.util.Optional;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItemEntity, Long> {
    @Query("SELECT c FROM ChecklistItemEntity c WHERE c.code = ?1")
    ChecklistItemEntity findByCode(String code);
}
