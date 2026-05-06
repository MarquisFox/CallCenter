package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.vinpin.statisticservice.entity.StatusEntity;

@Repository
public interface StatusRepository extends JpaRepository<StatusEntity, Byte> {

    @Query("select s from StatusEntity s where s.code = ?1")
    StatusEntity findByCode(String code);
}