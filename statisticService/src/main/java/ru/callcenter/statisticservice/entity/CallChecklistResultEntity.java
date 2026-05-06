package ru.vinpin.statisticservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "call_checklist_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"call_id", "checklist_item_id"}))
public class CallChecklistResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_id", nullable = false)
    private CallEntity call;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "checklist_item_id", nullable = false)
    private ChecklistItemEntity checklistItem;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "penalty_points", nullable = false)
    private Short penaltyPoints;

    @Column(name = "recommendation")
    private String recommendation;
}