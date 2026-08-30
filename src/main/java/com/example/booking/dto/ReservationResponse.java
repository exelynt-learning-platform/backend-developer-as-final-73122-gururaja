package com.example.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private Long resourceId;
    private String resourceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal price;
    private String status;

    public ReservationResponse() {
    }

    public ReservationResponse(Long id, Long userId, String userEmail, Long resourceId, String resourceName, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, String status) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
