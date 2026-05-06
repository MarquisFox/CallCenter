package ru.vinpin.statisticservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.vinpin.statisticservice.entity.TonalityEntity;

@Repository
public interface TonalityRepository extends JpaRepository<TonalityEntity, Byte> {

    @Query("select t from TonalityEntity t where upper(t.code) = upper(?1)")
    TonalityEntity findByCodeIgnoreCase(String code);


}
