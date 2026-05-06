package ru.vinpin.statisticservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "checklist_item")
public class ChecklistItemEntity {
    @Id
    private Short id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(name = "max_penalty", nullable = false)
    private Short maxPenalty;
}