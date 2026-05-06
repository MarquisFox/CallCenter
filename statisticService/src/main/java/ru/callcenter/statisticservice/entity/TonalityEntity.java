package ru.vinpin.statisticservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tonality")
public class TonalityEntity {
    @Id
    private Short id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "name_ru", nullable = false)
    private String nameRu;
}
