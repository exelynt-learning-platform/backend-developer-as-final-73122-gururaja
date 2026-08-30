package com.example.booking.service;

import com.example.booking.dto.ResourceRequest;
import com.example.booking.dto.ResourceResponse;
import com.example.booking.entity.Resource;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> getAllResources() {
        return resourceRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceResponse getResourceById(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return mapToResponse(resource);
    }

    @Transactional
    public ResourceResponse createResource(ResourceRequest request) {
        Resource resource = new Resource();
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        Resource saved = resourceRepository.save(resource);
        return mapToResponse(saved);
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        Resource updated = resourceRepository.save(resource);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteResource(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        resourceRepository.delete(resource);
    }

    private ResourceResponse mapToResponse(Resource resource) {
        return new ResourceResponse(resource.getId(), resource.getName(), resource.getDescription());
    }
}
