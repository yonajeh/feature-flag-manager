package com.featureflag.service;

import com.featureflag.domain.Application;
import com.featureflag.dto.ApplicationDto;
import com.featureflag.dto.CreateApplicationRequest;
import com.featureflag.dto.UpdateApplicationRequest;
import com.featureflag.exception.ConflictException;
import com.featureflag.exception.NotFoundException;
import com.featureflag.mapper.DtoMapper;
import com.featureflag.repository.ApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ApplicationService {

    @Inject
    ApplicationRepository applicationRepository;

    public List<ApplicationDto> listAll() {
        return applicationRepository.listAll().stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    public ApplicationDto getById(UUID id) {
        return DtoMapper.toDto(findOrThrow(id));
    }

    @Transactional
    public ApplicationDto create(CreateApplicationRequest request) {
        if (applicationRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Application name already exists");
        }
        Application app = new Application();
        app.name = request.name();
        app.displayName = request.displayName();
        applicationRepository.persist(app);
        return DtoMapper.toDto(app);
    }

    @Transactional
    public ApplicationDto update(UUID id, UpdateApplicationRequest request) {
        Application app = findOrThrow(id);
        app.displayName = request.displayName();
        applicationRepository.persist(app);
        return DtoMapper.toDto(app);
    }

    @Transactional
    public void delete(UUID id) {
        if (!applicationRepository.deleteById(id)) {
            throw new NotFoundException("Application not found");
        }
    }

    private Application findOrThrow(UUID id) {
        return applicationRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Application not found"));
    }
}
