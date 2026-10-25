package com.example.demo.dto;

import java.math.BigDecimal;

public final class OperationsDto {
    private OperationsDto() {}

    public record PlantRequest(Long cropId, Integer plantCount) {}

    public record HarvestRequest(BigDecimal mass, Boolean finalHarvest) {}

    public record TransferRequest(Long newOwnerId) {}

    public record MoveRequest(Long targetBedId) {}
}
