package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.darkness_pathway.ConcealedDomainEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ConcealedDomainAbility extends Ability {

    public ConcealedDomainAbility(String id) {
        super(id, 2f);
        this.canBeCopied = false;
        this.canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 2));
    }

    @Override
    public float getSpiritualityCost() {
        return 5500;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // If an active domain exists within max range, discard it instead of spawning a new one
        ConcealedDomainEntity existing = ConcealedDomainEntity.getActiveForOwner(entity.getUUID());
        if (existing != null) {
            double distSq = entity.distanceToSqr(existing);
            if (distSq <= ConcealedDomainEntity.RADIUS * ConcealedDomainEntity.RADIUS * 4) {
                existing.discard();
                return;
            }
        }

        ConcealedDomainEntity domain = new ConcealedDomainEntity(ModEntities.CONCEALED_DOMAIN.get(), serverLevel);
        domain.setOwner(entity);
        domain.setPos(entity.position());
        serverLevel.addFreshEntity(domain);
        ConcealedDomainEntity.registerForOwner(entity.getUUID(), domain);
    }
}