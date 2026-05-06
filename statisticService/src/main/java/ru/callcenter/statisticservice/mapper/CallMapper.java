package ru.vinpin.statisticservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.vinpin.statisticservice.dto.api.CallDto;
import ru.vinpin.statisticservice.dto.kafka.CallRegistrationMessage;
import ru.vinpin.statisticservice.entity.CallEntity;
import ru.vinpin.statisticservice.entity.ManagerEntity;
import ru.vinpin.statisticservice.entity.StatusEntity;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {Duration.class, LocalTime.class, java.time.Instant.class}
)
public interface CallMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "callName", source = "message.callId")
    @Mapping(target = "manager", source = "manager")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "date", source = "message.date")
    @Mapping(target = "duration", expression = "java(parseDuration(message))")
    @Mapping(target = "durationSeconds", expression = "java(extractDurationSeconds(message))")
    @Mapping(target = "createdAt", expression = "java(Instant.now())")
    @Mapping(target = "tonality", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "errorRate", ignore = true)
    CallEntity toEntity(CallRegistrationMessage message,
                        ManagerEntity manager,
                        StatusEntity status);

    @Mapping(target = "id", source = "callName")
    @Mapping(target = "manager", source = "manager.name")
    @Mapping(target = "duration", expression = "java(formatDuration(entity.getDuration()))")
    @Mapping(target = "tonality", source = "tonality.nameRu")
    CallDto toDto(CallEntity entity);


    default Duration parseDuration(CallRegistrationMessage message) {
        if (message.getDurationSeconds() != null) {
            return Duration.ofSeconds(message.getDurationSeconds());
        }
        if (message.getDuration() != null && !message.getDuration().isEmpty()) {
            try {
                return Duration.parse(message.getDuration());
            } catch (DateTimeParseException e) {
                try {
                    LocalTime time = LocalTime.parse(message.getDuration());
                    return Duration.ofSeconds(time.toSecondOfDay());
                } catch (DateTimeParseException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    default Integer extractDurationSeconds(CallRegistrationMessage message) {
        if (message.getDurationSeconds() != null) {
            return message.getDurationSeconds();
        }
        Duration duration = parseDuration(message);
        return duration != null ? (int) duration.getSeconds() : null;
    }

    default String formatDuration(Duration duration) {
        if (duration == null) {
            return null;
        }
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
