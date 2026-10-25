package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Crop;
import com.example.demo.model.GardenBed;
import com.example.demo.model.Harvest;
import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;
import com.example.demo.model.User;
import com.example.demo.repository.CropRepository;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.HarvestRepository;
import com.example.demo.repository.PlantingRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Бизнес-операции огорода. Каждая выполняется в одной транзакции: либо все изменения применяются,
 * либо (при любой ошибке) откатываются полностью.
 * <p>
 * Как обеспечивается корректность:
 * <ul>
 *   <li>проверки состояния перед изменением (409 Conflict, если операция недопустима);</li>
 *   <li>пессимистическая блокировка грядки (SELECT ... FOR UPDATE) при проверке свободной площади;</li>
 *   <li>оптимистическая блокировка посадки (@Version) против параллельных изменений;</li>
 *   <li>ограничения CHECK / FK / UNIQUE в самой БД как последний рубеж.</li>
 * </ul>
 */
@Service
public class GardenOperationsService {

    private final GardenBedRepository beds;
    private final PlantingRepository plantings;
    private final CropRepository crops;
    private final HarvestRepository harvests;
    private final UserRepository users;
    private final GardenAreaCalculator calculator;

    public GardenOperationsService(GardenBedRepository beds, PlantingRepository plantings, CropRepository crops,
                                   HarvestRepository harvests, UserRepository users, GardenAreaCalculator calculator) {
        this.beds = beds;
        this.plantings = plantings;
        this.crops = crops;
        this.harvests = harvests;
        this.users = users;
        this.calculator = calculator;
    }

    /**
     * Операция 1. Посадить культуру на грядку (Грядка + Культура -> новая Посадка).
     * Допустимо, только если грядка доступна и на ней хватает свободной площади
     * (минимальная площадь на растение × количество). Посадка сразу получает статус GROWING.
     */
    @Transactional
    public Planting plant(Long bedId, Long cropId, Integer plantCount) {
        Validation.required(cropId, "cropId");
        Validation.positive(plantCount, "plantCount");
        GardenBed bed = lockBed(bedId);
        Crop crop = crops.findById(cropId)
                .orElseThrow(() -> new BadRequestException("Культура " + cropId + " не существует"));
        if (!bed.isAvailable()) {
            throw new ConflictException("Грядка недоступна для посадки");
        }
        calculator.ensureFits(bed, crop, plantCount, null);
        return plantings.save(new Planting(bed, crop, plantCount, PlantingStatus.GROWING, LocalDate.now()));
    }

    /**
     * Операция 2. Собрать урожай (Посадка -> новый Сбор + смена статуса Посадки).
     * Допустимо только для растущей посадки (GROWING). Дату ставит сервер.
     * Если finalHarvest = true, посадка переходит в HARVESTED и освобождает площадь грядки.
     */
    @Transactional
    public Harvest harvest(Long plantingId, BigDecimal mass, Boolean finalHarvest) {
        Validation.decimal(mass, "mass", 7, 3);
        Planting planting = plantings.findByIdForUpdate(plantingId)
                .orElseThrow(() -> new NotFoundException("Посадка " + plantingId + " не найдена"));
        if (planting.getStatus() != PlantingStatus.GROWING) {
            throw new ConflictException("Собирать урожай можно только с растущей посадки, текущий статус: "
                    + planting.getStatus());
        }
        Harvest harvest = harvests.save(new Harvest(planting, LocalDate.now(), mass));
        if (Boolean.TRUE.equals(finalHarvest)) {
            planting.setStatus(PlantingStatus.HARVESTED);
        }
        return harvest;
    }

    /**
     * Операция 3. Передать грядку другому пользователю (Грядка + Пользователь, проверка Посадок).
     * Недопустимо, если новый владелец совпадает с текущим или на грядке есть растущие посадки.
     */
    @Transactional
    public GardenBed transfer(Long bedId, Long newOwnerId) {
        Validation.required(newOwnerId, "newOwnerId");
        GardenBed bed = lockBed(bedId);
        User newOwner = users.findById(newOwnerId)
                .orElseThrow(() -> new BadRequestException("Пользователь " + newOwnerId + " не существует"));
        if (bed.getOwner().getId().equals(newOwnerId)) {
            throw new ConflictException("Грядка уже принадлежит этому пользователю");
        }
        if (plantings.existsByGardenBedIdAndStatus(bedId, PlantingStatus.GROWING)) {
            throw new ConflictException("Нельзя передать грядку, на которой есть растущие посадки");
        }
        bed.setOwner(newOwner);
        return bed;
    }

    /**
     * Операция 4. Перенести посадку на другую грядку (Посадка + две Грядки).
     * Допустимо для незавершённой посадки, если целевая грядка доступна и на ней достаточно места.
     * Площадь исходной грядки освобождается автоматически.
     */
    @Transactional
    public Planting move(Long plantingId, Long targetBedId) {
        Validation.required(targetBedId, "targetBedId");
        Planting planting = plantings.findById(plantingId)
                .orElseThrow(() -> new NotFoundException("Посадка " + plantingId + " не найдена"));
        Long sourceBedId = planting.getGardenBed().getId();
        if (sourceBedId.equals(targetBedId)) {
            throw new ConflictException("Посадка уже находится на этой грядке");
        }
        if (!beds.existsById(targetBedId)) {
            throw new BadRequestException("Грядка " + targetBedId + " не существует");
        }
        // блокируем обе грядки в порядке возрастания id — так параллельные переносы не создадут взаимную блокировку
        GardenBed first = lockBed(Math.min(sourceBedId, targetBedId));
        GardenBed second = lockBed(Math.max(sourceBedId, targetBedId));
        GardenBed target = first.getId().equals(targetBedId) ? first : second;

        if (planting.getStatus() == PlantingStatus.HARVESTED) {
            throw new ConflictException("Нельзя переносить уже собранную посадку");
        }
        if (!target.isAvailable()) {
            throw new ConflictException("Целевая грядка недоступна для посадки");
        }
        calculator.ensureFits(target, planting.getCrop(), planting.getPlantCount(), null);
        planting.setGardenBed(target);
        return planting;
    }

    /**
     * Операция 5. Закрыть сезон на грядке (Грядка + Посадки + Сборы).
     * Все растущие посадки грядки переводятся в HARVESTED. Допустимо только если растущие посадки есть
     * и у каждой уже есть хотя бы один сбор урожая. Операция атомарна: либо закрываются все, либо ни одна.
     */
    @Transactional
    public List<Planting> closeSeason(Long bedId) {
        lockBed(bedId);
        List<Planting> growing = plantings.findByGardenBedIdAndStatus(bedId, PlantingStatus.GROWING);
        if (growing.isEmpty()) {
            throw new ConflictException("На грядке нет растущих посадок");
        }
        List<Long> withoutHarvest = growing.stream()
                .filter(p -> !harvests.existsByPlantingId(p.getId()))
                .map(Planting::getId)
                .toList();
        if (!withoutHarvest.isEmpty()) {
            throw new ConflictException("Нельзя закрыть сезон: у посадок " + withoutHarvest + " не было сборов урожая");
        }
        growing.forEach(p -> p.setStatus(PlantingStatus.HARVESTED));
        return growing;
    }

    private GardenBed lockBed(Long id) {
        return beds.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Грядка " + id + " не найдена"));
    }
}
