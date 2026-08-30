package com.example.booking.service;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.dto.ReservationResponse;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.entity.Resource;
import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;

    public ReservationService(ReservationRepository reservationRepository, ResourceRepository resourceRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User currentUser) {
        // Validate resource exists
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        // Validate dates
        validateDates(request.getStartTime(), request.getEndTime());

        // Validate status
        ReservationStatus status = validateAndParseStatus(request.getStatus(), true);

        Reservation reservation = new Reservation();
        reservation.setUser(currentUser);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(status);

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(
            String status, BigDecimal minPrice, BigDecimal maxPrice,
            String sortBy, String sortDir, int page, int size, User currentUser) {

        // Validate status if provided
        if (status != null && !status.trim().isEmpty()) {
            validateAndParseStatus(status, false);
        }

        // Validate sortDir
        String cleanSortDir = sortDir != null ? sortDir.trim().toLowerCase() : "asc";
        if (!cleanSortDir.equals("asc") && !cleanSortDir.equals("desc")) {
            throw new BadRequestException("Sort direction must be either 'asc' or 'desc'");
        }

        // Validate sortBy
        String cleanSortBy = sortBy != null ? sortBy.trim() : "id";
        java.util.List<String> allowedSortFields = java.util.List.of("id", "startTime", "endTime", "price", "status");
        if (!allowedSortFields.contains(cleanSortBy)) {
            throw new BadRequestException("Sorting by field '" + cleanSortBy + "' is not supported. Allowed fields: " + allowedSortFields);
        }

        // Validate page and size
        if (page < 0) {
            throw new BadRequestException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than zero");
        }

        Sort.Direction direction = cleanSortDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, cleanSortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Build Specification
        Specification<Reservation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Enforce ownership: USER can view only their own reservations
            if (currentUser.getRole() != Role.ADMIN) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), currentUser.getId()));
            }

            // Filter by status
            if (status != null && !status.trim().isEmpty()) {
                ReservationStatus statusEnum = ReservationStatus.valueOf(status.toUpperCase().trim());
                predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
            }

            // Filter by min price
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // Filter by max price
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return reservationRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id, User currentUser) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        // Enforce ownership: USER can view only their own reservations
        if (currentUser.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this reservation");
        }

        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));

        // Validate resource exists
        Resource resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + request.getResourceId()));

        // Validate dates
        validateDates(request.getStartTime(), request.getEndTime());

        // Validate status
        ReservationStatus status = validateAndParseStatus(request.getStatus(), false);

        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPrice(request.getPrice());
        reservation.setStatus(status);

        Reservation updated = reservationRepository.save(reservation);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
        reservationRepository.delete(reservation);
    }

    private void validateDates(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new BadRequestException("Start time must be strictly before end time");
        }
    }

    private ReservationStatus validateAndParseStatus(String statusStr, boolean defaultToPending) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            if (defaultToPending) {
                return ReservationStatus.PENDING;
            }
            throw new BadRequestException("Reservation status is required");
        }
        try {
            return ReservationStatus.valueOf(statusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid reservation status. Allowed values are PENDING, CONFIRMED, or CANCELLED");
        }
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getEmail(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus().name()
        );
    }
}
