package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "harvests")
public class Harvest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planting_id", nullable = false)
    private Planting planting;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal mass;

    protected Harvest() {
    }

    public Harvest(Planting planting, LocalDate date, BigDecimal mass) {
        this.planting = planting;
        this.date = date;
        this.mass = mass;
    }

    public Long getId() { return id; }
    public Planting getPlanting() { return planting; }
    public void setPlanting(Planting planting) { this.planting = planting; }
    public LocalDate getDate() { return date; }
    public BigDecimal getMass() { return mass; }
    public void setMass(BigDecimal mass) { this.mass = mass; }
}
