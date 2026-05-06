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
@Table(name = "status")
public class StatusEntity {
    @Id
    private Short id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(name = "name_ru", nullable = false)
    private String nameRu;
}
