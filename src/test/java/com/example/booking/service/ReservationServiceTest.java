package com.example.booking.service;

import com.example.booking.dto.ReservationRequest;
import com.example.booking.dto.ReservationResponse;
import com.example.booking.entity.Reservation;
import com.example.booking.entity.ReservationStatus;
import com.example.booking.entity.Resource;
import com.example.booking.entity.Role;
import com.example.booking.entity.User;
import com.example.booking.exception.BadRequestException;
import com.example.booking.repository.ReservationRepository;
import com.example.booking.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ReservationService reservationService;

    private User testUser;
    private User testAdmin;
    private Resource testResource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User(1L, "user@example.com", "password", Role.USER);
        testAdmin = new User(2L, "admin@example.com", "password", Role.ADMIN);
        testResource = new Resource(1L, "Conference Room A", "Large conference room");
    }

    @Test
    public void createReservation_Success() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"),
                "PENDING"
        );

        Reservation savedReservation = new Reservation(
                1L, testUser, testResource,
                request.getStartTime(), request.getEndTime(),
                request.getPrice(), ReservationStatus.PENDING
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.createReservation(request, testUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals("PENDING", response.getStatus());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    public void createReservation_InvalidDates_ThrowsBadRequest() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(1).plusHours(2), // Start time after end time
                LocalDateTime.now().plusDays(1),
                new BigDecimal("100.00"),
                "PENDING"
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(request, testUser);
        });
    }

    @Test
    public void createReservation_InvalidStatus_ThrowsBadRequest() {
        ReservationRequest request = new ReservationRequest(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("100.00"),
                "INVALID_STATUS"
        );

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(testResource));

        assertThrows(BadRequestException.class, () -> {
            reservationService.createReservation(request, testUser);
        });
    }

    @Test
    public void getReservationById_Success_Owner() {
        Reservation reservation = new Reservation(
                1L, testUser, testResource,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                new BigDecimal("50.00"), ReservationStatus.CONFIRMED
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, testUser);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    public void getReservationById_Forbidden_NotOwner() {
        User anotherUser = new User(3L, "other@example.com", "password", Role.USER);
        Reservation reservation = new Reservation(
                1L, testUser, testResource,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                new BigDecimal("50.00"), ReservationStatus.CONFIRMED
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(AccessDeniedException.class, () -> {
            reservationService.getReservationById(1L, anotherUser);
        });
    }

    @Test
    public void getReservationById_Success_AdminNotOwner() {
        Reservation reservation = new Reservation(
                1L, testUser, testResource,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2),
                new BigDecimal("50.00"), ReservationStatus.CONFIRMED
        );

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ReservationResponse response = reservationService.getReservationById(1L, testAdmin);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }
}
