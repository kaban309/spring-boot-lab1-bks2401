package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Season season;

    @Column(name = "min_area_per_plant", nullable = false, precision = 8, scale = 3)
    private BigDecimal minAreaPerPlant;

    @OneToMany(mappedBy = "crop", cascade = CascadeType.REMOVE)
    private List<Planting> plantings = new ArrayList<>();

    protected Crop() {
    }

    public Crop(String name, Season season, BigDecimal minAreaPerPlant) {
        this.name = name;
        this.season = season;
        this.minAreaPerPlant = minAreaPerPlant;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Season getSeason() { return season; }
    public void setSeason(Season season) { this.season = season; }
    public BigDecimal getMinAreaPerPlant() { return minAreaPerPlant; }
    public void setMinAreaPerPlant(BigDecimal minAreaPerPlant) { this.minAreaPerPlant = minAreaPerPlant; }
}
