package ru.vinpin.bitrixadapterservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vinpin.bitrixadapterservice.dto.CallEventRequest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallEventMapper {
    private final ObjectMapper objectMapper;
    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)ч\\s*(\\d+)м");

    public Map<String, Object> normalizeToKafkaMessage(CallEventRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("callId", request.getCallId());
        map.put("managerId", hashManagerId(request.getManagerId()));
        map.put("fileUrl", request.getFileUrl());

        String date = request.getDate();
        if (date == null || date.isBlank()) {
            date = LocalDate.now().toString();
        }
        map.put("date", date);

        String managerName = request.getManagerName();
        if (managerName == null || managerName.isBlank()) {
            managerName = "Unknown";
        }
        map.put("managerName", managerName);

        map.put("managerPosition", request.getManagerPosition());
        map.put("duration", request.getDuration());

        Integer durationSeconds = request.getDurationSeconds();
        if (durationSeconds == null && request.getDuration() != null) {
            durationSeconds = parseDuration(request.getDuration());
        }
        map.put("durationSeconds", durationSeconds);

        return map;
    }

    private Integer parseDuration(String duration) {
        var matcher = DURATION_PATTERN.matcher(duration);
        if (matcher.matches()) {
            int hours = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            return hours * 3600 + minutes * 60;
        }
        log.warn("Unable to parse duration: {}", duration);
        return null;
    }

    public String hashManagerId(String rawId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawId.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            return number.toString(16).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
    public String serializeToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }
}