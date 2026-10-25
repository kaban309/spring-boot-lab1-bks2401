package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "garden_beds")
public class GardenBed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal area;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private boolean available = true;

    @OneToMany(mappedBy = "gardenBed", cascade = CascadeType.REMOVE)
    private List<Planting> plantings = new ArrayList<>();

    protected GardenBed() {
    }

    public GardenBed(BigDecimal area, User owner, boolean available) {
        this.area = area;
        this.owner = owner;
        this.available = available;
    }

    public Long getId() { return id; }
    public BigDecimal getArea() { return area; }
    public void setArea(BigDecimal area) { this.area = area; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
