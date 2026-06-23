package com.featureflag.service;

import com.featureflag.domain.Application;
import com.featureflag.domain.FeatureFlag;
import com.featureflag.dto.ConsumerFeatureFlagDto;
import com.featureflag.dto.CreateFeatureFlagRequest;
import com.featureflag.dto.FeatureFlagDto;
import com.featureflag.dto.UpdateFeatureFlagRequest;
import com.featureflag.exception.ConflictException;
import com.featureflag.exception.NotFoundException;
import com.featureflag.repository.ApplicationRepository;
import com.featureflag.repository.FeatureFlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

    @Mock
    FeatureFlagRepository featureFlagRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    FeatureFlagService featureFlagService;

    private UUID appId;
    private FeatureFlag flag;

    @BeforeEach
    void setUp() {
        appId = UUID.randomUUID();
        flag = new FeatureFlag();
        flag.id = UUID.randomUUID();
        flag.applicationId = appId;
        flag.key = "dark-mode";
        flag.enabled = true;
        flag.description = "Dark mode toggle";
    }

    @Test
    void listForConsumer_returnsFlags() {
        when(featureFlagRepository.findByApplicationId(appId)).thenReturn(List.of(flag));

        List<ConsumerFeatureFlagDto> result = featureFlagService.listForConsumer(appId);

        assertEquals(1, result.size());
        assertEquals("dark-mode", result.get(0).key());
        assertTrue(result.get(0).enabled());
    }

    @Test
    void create_duplicateKey_throwsConflict() {
        Application app = new Application();
        app.id = appId;
        when(applicationRepository.findByIdOptional(appId)).thenReturn(Optional.of(app));
        when(featureFlagRepository.findByApplicationIdAndKey(appId, "dark-mode")).thenReturn(Optional.of(flag));

        assertThrows(ConflictException.class, () ->
                featureFlagService.create(appId, new CreateFeatureFlagRequest("dark-mode", false, null)));
    }

    @Test
    void create_success() {
        Application app = new Application();
        app.id = appId;
        when(applicationRepository.findByIdOptional(appId)).thenReturn(Optional.of(app));
        when(featureFlagRepository.findByApplicationIdAndKey(appId, "new-flag")).thenReturn(Optional.empty());
        doAnswer(inv -> {
            FeatureFlag f = inv.getArgument(0);
            f.id = UUID.randomUUID();
            return null;
        }).when(featureFlagRepository).persist(any(FeatureFlag.class));

        FeatureFlagDto created = featureFlagService.create(appId,
                new CreateFeatureFlagRequest("new-flag", true, "desc"));

        assertEquals("new-flag", created.key());
        assertTrue(created.enabled());
    }

    @Test
    void update_changesEnabled() {
        Application app = new Application();
        app.id = appId;
        when(applicationRepository.findByIdOptional(appId)).thenReturn(Optional.of(app));
        when(featureFlagRepository.findByApplicationIdAndKey(appId, "dark-mode")).thenReturn(Optional.of(flag));

        FeatureFlagDto updated = featureFlagService.update(appId, "dark-mode",
                new UpdateFeatureFlagRequest(false, null));

        assertFalse(updated.enabled());
    }

    @Test
    void getForConsumer_notFound() {
        when(featureFlagRepository.findByApplicationIdAndKey(appId, "missing"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> featureFlagService.getForConsumer(appId, "missing"));
    }
}
