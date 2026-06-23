package com.featureflag.service;

import com.featureflag.domain.FeatureFlag;
import com.featureflag.dto.ConsumerFeatureFlagDto;
import com.featureflag.dto.CreateFeatureFlagRequest;
import com.featureflag.dto.FeatureFlagDto;
import com.featureflag.dto.UpdateFeatureFlagRequest;
import com.featureflag.exception.ConflictException;
import com.featureflag.exception.NotFoundException;
import com.featureflag.mapper.DtoMapper;
import com.featureflag.repository.ApplicationRepository;
import com.featureflag.repository.FeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FeatureFlagService {

    @Inject
    FeatureFlagRepository featureFlagRepository;

    @Inject
    ApplicationRepository applicationRepository;

    public List<ConsumerFeatureFlagDto> listForConsumer(UUID applicationId) {
        return featureFlagRepository.findByApplicationId(applicationId).stream()
                .map(DtoMapper::toConsumerDto)
                .toList();
    }

    public ConsumerFeatureFlagDto getForConsumer(UUID applicationId, String key) {
        FeatureFlag flag = featureFlagRepository.findByApplicationIdAndKey(applicationId, key)
                .orElseThrow(() -> new NotFoundException("Feature flag not found"));
        return DtoMapper.toConsumerDto(flag);
    }

    public List<FeatureFlagDto> listForAdmin(UUID applicationId) {
        requireApplication(applicationId);
        return featureFlagRepository.findByApplicationId(applicationId).stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    public FeatureFlagDto getForAdmin(UUID applicationId, String key) {
        requireApplication(applicationId);
        return DtoMapper.toDto(findFlagOrThrow(applicationId, key));
    }

    @Transactional
    public FeatureFlagDto create(UUID applicationId, CreateFeatureFlagRequest request) {
        requireApplication(applicationId);
        if (featureFlagRepository.findByApplicationIdAndKey(applicationId, request.key()).isPresent()) {
            throw new ConflictException("Feature flag key already exists for this application");
        }
        FeatureFlag flag = new FeatureFlag();
        flag.applicationId = applicationId;
        flag.key = request.key();
        flag.enabled = request.enabled();
        flag.description = request.description();
        flag.metadata = request.metadata();
        featureFlagRepository.persist(flag);
        return DtoMapper.toDto(flag);
    }

    @Transactional
    public FeatureFlagDto update(UUID applicationId, String key, UpdateFeatureFlagRequest request) {
        FeatureFlag flag = findFlagOrThrow(applicationId, key);
        if (request.enabled() != null) {
            flag.enabled = request.enabled();
        }
        if (request.description() != null) {
            flag.description = request.description();
        }
        if (request.metadata() != null) {
            flag.metadata = request.metadata();
        }
        featureFlagRepository.persist(flag);
        return DtoMapper.toDto(flag);
    }

    @Transactional
    public void delete(UUID applicationId, String key) {
        FeatureFlag flag = findFlagOrThrow(applicationId, key);
        featureFlagRepository.delete(flag);
    }

    private void requireApplication(UUID applicationId) {
        applicationRepository.findByIdOptional(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }

    private FeatureFlag findFlagOrThrow(UUID applicationId, String key) {
        requireApplication(applicationId);
        return featureFlagRepository.findByApplicationIdAndKey(applicationId, key)
                .orElseThrow(() -> new NotFoundException("Feature flag not found"));
    }
}
