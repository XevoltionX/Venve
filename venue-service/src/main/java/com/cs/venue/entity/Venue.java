package com.cs.venue.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venue")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "business_hours", length = 50)
    private String businessHours = "09:00-21:00";

    @Column(name = "table_count")
    private Integer tableCount = 1;

    @Column(name = "price_per_hour", precision = 10, scale = 2)
    private BigDecimal pricePerHour = BigDecimal.valueOf(30);

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VenueStatus status = VenueStatus.AVAILABLE;

    public enum VenueStatus {
        AVAILABLE, MAINTENANCE
    }

    public Venue() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
    public Integer getTableCount() { return tableCount; }
    public void setTableCount(Integer tableCount) { this.tableCount = tableCount; }
    public BigDecimal getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(BigDecimal pricePerHour) { this.pricePerHour = pricePerHour; }
    public VenueStatus getStatus() { return status; }
    public void setStatus(VenueStatus status) { this.status = status; }
}
