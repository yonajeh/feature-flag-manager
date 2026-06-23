package com.featureflag.service;

import com.featureflag.domain.Application;
import com.featureflag.dto.*;
import com.featureflag.exception.NotFoundException;
import com.featureflag.mapper.DtoMapper;
import com.featureflag.repository.ApiTokenRepository;
import com.featureflag.repository.ApplicationRepository;
import com.featureflag.repository.FeatureFlagRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ExportService {

    private static final int EXPORT_VERSION = 1;

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    FeatureFlagRepository featureFlagRepository;

    @Inject
    ApiTokenRepository tokenRepository;

    public ApplicationDataExportDto exportApplication(UUID applicationId) {
        Application application = applicationRepository
                .findByIdOptional(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found"));
        ExportedApplication bundle = toExportedApplication(application);
        return new ApplicationDataExportDto(
                Instant.now(),
                EXPORT_VERSION,
                bundle.application(),
                bundle.featureFlags(),
                bundle.tokens());
    }

    public FullDataExportDto exportAll() {
        List<ExportedApplication> applications = applicationRepository.listAll().stream()
                .map(this::toExportedApplication)
                .toList();
        return new FullDataExportDto(Instant.now(), EXPORT_VERSION, applications);
    }

    private ExportedApplication toExportedApplication(Application application) {
        UUID applicationId = application.id;
        List<FeatureFlagDto> featureFlags = featureFlagRepository.findByApplicationId(applicationId).stream()
                .map(DtoMapper::toDto)
                .toList();
        List<ApiTokenMetadataDto> tokens = tokenRepository.findByApplicationId(applicationId).stream()
                .map(DtoMapper::toMetadataDto)
                .toList();
        return new ExportedApplication(DtoMapper.toDto(application), featureFlags, tokens);
    }
}
