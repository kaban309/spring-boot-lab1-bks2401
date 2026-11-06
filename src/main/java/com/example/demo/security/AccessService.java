package com.example.demo.security;

import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.HarvestRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Проверки доступа с учётом контекста (кому принадлежит объект). Используются в выражениях
 * {@code @PreAuthorize("... @access.ownsBed(#id)")}. Если объекта нет, проверка даёт false —
 * обычный пользователь получает 403 и не может выяснить, существует ли чужой идентификатор.
 */
@Component("access")
public class AccessService {

    private final GardenBedRepository beds;
    private final PlantingRepository plantings;
    private final HarvestRepository harvests;

    public AccessService(GardenBedRepository beds, PlantingRepository plantings, HarvestRepository harvests) {
        this.beds = beds;
        this.plantings = plantings;
        this.harvests = harvests;
    }

    public boolean isSelf(Long userId) {
        AppUserDetails me = currentUser();
        return me != null && userId != null && me.getId().equals(userId);
    }

    public boolean ownsBed(Long bedId) {
        AppUserDetails me = currentUser();
        return me != null && bedId != null && beds.isOwnedBy(bedId, me.getId());
    }

    public boolean ownsPlanting(Long plantingId) {
        AppUserDetails me = currentUser();
        return me != null && plantingId != null && plantings.isOwnedBy(plantingId, me.getId());
    }

    public boolean ownsHarvest(Long harvestId) {
        AppUserDetails me = currentUser();
        return me != null && harvestId != null && harvests.isOwnedBy(harvestId, me.getId());
    }

    private AppUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof AppUserDetails details ? details : null;
    }
}
