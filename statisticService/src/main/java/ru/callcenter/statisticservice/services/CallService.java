package ru.vinpin.statisticservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vinpin.statisticservice.dto.api.CallDto;
import ru.vinpin.statisticservice.dto.kafka.CallRegistrationMessage;
import ru.vinpin.statisticservice.dto.kafka.GigachatOutputMessage;
import ru.vinpin.statisticservice.dto.kafka.SentimentOutputMessage;
import ru.vinpin.statisticservice.entity.*;
import ru.vinpin.statisticservice.mapper.CallMapper;
import ru.vinpin.statisticservice.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CallService {
    private final CallRepository callRepository;
    private final CallMapper callMapper;
    private final ManagerService managerService;
    private final StatusRepository statusRepository;
    private final TonalityRepository tonalityRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final CallChecklistResultRepository callChecklistResultRepository;
    private final StatisticsService statisticsService;

    @Transactional
    public void createCall(CallRegistrationMessage msg) {
        if (existByCallName(msg.getCallId())) {
            return;
        }

        ManagerEntity manager = managerService.findOrCreateManager(
                msg.getManagerId(),
                msg.getManagerName(),
                msg.getManagerPosition()
        );

        StatusEntity defaultStatus = statusRepository.findByCode("created");
        CallEntity callEntity = callMapper.toEntity(msg, manager, defaultStatus);

        callRepository.save(callEntity);
    }

    @Transactional
    public void updateTonality(SentimentOutputMessage msg) {
        CallEntity callEntity = findCallByCallName(msg.getCallId());
        if (callEntity == null) {
            return;
        }
        if (callEntity.getTonality() != null) {
            return;
        }

        callEntity.setTonality(tonalityRepository.findByCodeIgnoreCase(msg.getTonality()));
        String currentStatus = callEntity.getStatus().getCode();

        if (currentStatus.equals("created")) {
            callEntity.setStatus(statusRepository.findByCode("sentiment_done"));
        } else if (currentStatus.equals("gigachat_done")) {
            callEntity.setStatus(statusRepository.findByCode("completed"));
        }

        callRepository.save(callEntity);

        if (callEntity.getStatus().getCode().equals("completed")) {
            statisticsService.updateManagerStats(callEntity.getManager().getId());
        }
    }

    @Transactional
    public void updateGigachatResult(GigachatOutputMessage msg) {
        CallEntity callEntity = findCallByCallName(msg.getCallId());
        if (callEntity == null) {
            return;
        }
        if (callEntity.getRating() != null) {
            return;
        }

        if (msg.getRating() != null) callEntity.setRating(msg.getRating());
        if (msg.getErrorRate() != null) callEntity.setErrorRate(msg.getErrorRate());

        if (msg.getItems() != null) {
            for (GigachatOutputMessage.ChecklistItemDto item : msg.getItems()) {
                saveChecklistItem(callEntity, item);
            }
        }

        String currentStatus = callEntity.getStatus().getCode();
        if (currentStatus.equals("created")) {
            callEntity.setStatus(statusRepository.findByCode("gigachat_done"));
        } else if (currentStatus.equals("sentiment_done")) {
            callEntity.setStatus(statusRepository.findByCode("completed"));
        }

        callRepository.save(callEntity);

        if (callEntity.getStatus().getCode().equals("completed")) {
            statisticsService.updateManagerStats(callEntity.getManager().getId());
        }
    }

    public CallEntity findCallByCallName(String callName) {
        return callRepository.findByCallNameIgnoreCase(callName);
    }

    public boolean existByCallName(String callName){
        return callRepository.existsByCallNameIgnoreCase(callName);
    }

    @Transactional
    public void save(CallEntity callEntity) {
        callRepository.save(callEntity);
    }

    private void saveChecklistItem(CallEntity callEntity, GigachatOutputMessage.ChecklistItemDto item) {
        ChecklistItemEntity checklistItem = checklistItemRepository.findByCode(item.getCode());

        CallChecklistResultEntity result = new CallChecklistResultEntity();
        result.setCall(callEntity);
        result.setChecklistItem(checklistItem);
        result.setIsCompleted(item.getCompleted() != null ? item.getCompleted() : false);
        result.setPenaltyPoints(item.getPenalty() != null ? item.getPenalty().shortValue() : 0);
        result.setRecommendation(item.getRecommendation());

        callChecklistResultRepository.save(result);
    }

    public Page<CallDto> getAllCalls(Pageable pageable) {
        Page<CallEntity> callPage = callRepository.findAllWithManagerAndTonality(pageable);
        return callPage.map(callMapper::toDto);
    }
}
