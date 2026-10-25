package com.example.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plantings")
public class Planting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garden_bed_id", nullable = false)
    private GardenBed gardenBed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(name = "plant_count", nullable = false)
    private Integer plantCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PlantingStatus status;

    @Column(name = "planted_at", nullable = false)
    private LocalDate plantedAt;

    /** Оптимистическая блокировка: два одновременных изменения одной посадки не затрут друг друга. */
    @Version
    private Long version;

    @OneToMany(mappedBy = "planting", cascade = CascadeType.REMOVE)
    private List<Harvest> harvests = new ArrayList<>();

    protected Planting() {
    }

    public Planting(GardenBed gardenBed, Crop crop, Integer plantCount, PlantingStatus status, LocalDate plantedAt) {
        this.gardenBed = gardenBed;
        this.crop = crop;
        this.plantCount = plantCount;
        this.status = status;
        this.plantedAt = plantedAt;
    }

    public Long getId() { return id; }
    public GardenBed getGardenBed() { return gardenBed; }
    public void setGardenBed(GardenBed gardenBed) { this.gardenBed = gardenBed; }
    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }
    public Integer getPlantCount() { return plantCount; }
    public void setPlantCount(Integer plantCount) { this.plantCount = plantCount; }
    public PlantingStatus getStatus() { return status; }
    public void setStatus(PlantingStatus status) { this.status = status; }
    public LocalDate getPlantedAt() { return plantedAt; }
}
