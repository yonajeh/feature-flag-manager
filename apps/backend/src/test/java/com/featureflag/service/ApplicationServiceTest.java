package com.featureflag.service;

import com.featureflag.domain.Application;
import com.featureflag.dto.ApplicationDto;
import com.featureflag.dto.CreateApplicationRequest;
import com.featureflag.dto.UpdateApplicationRequest;
import com.featureflag.exception.ConflictException;
import com.featureflag.exception.NotFoundException;
import com.featureflag.repository.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationService applicationService;

    @Test
    void create_success() {
        when(applicationRepository.findByName("shop")).thenReturn(Optional.empty());
        doAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.id = UUID.randomUUID();
            return null;
        }).when(applicationRepository).persist(any(Application.class));

        ApplicationDto dto = applicationService.create(new CreateApplicationRequest("shop", "Shop App"));

        assertEquals("shop", dto.name());
        assertEquals("Shop App", dto.displayName());
    }

    @Test
    void create_duplicateName_throwsConflict() {
        Application existing = new Application();
        when(applicationRepository.findByName("shop")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () ->
                applicationService.create(new CreateApplicationRequest("shop", "Shop")));
    }

    @Test
    void update_displayName() {
        Application app = new Application();
        app.id = UUID.randomUUID();
        app.name = "shop";
        app.displayName = "Old";
        when(applicationRepository.findByIdOptional(app.id)).thenReturn(Optional.of(app));

        ApplicationDto dto = applicationService.update(app.id, new UpdateApplicationRequest("New Name"));

        assertEquals("New Name", dto.displayName());
        assertEquals("shop", dto.name());
    }

    @Test
    void delete_notFound() {
        when(applicationRepository.deleteById(any())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> applicationService.delete(UUID.randomUUID()));
    }
}
