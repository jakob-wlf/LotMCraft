package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InnateSpiritVisionAbility extends PassiveAbilityItem {
    private static final Set<UUID> forcedActive = ConcurrentHashMap.newKeySet();

    public InnateSpiritVisionAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("wheel_of_fortune", 9);
    }

    @Override
    public boolean shouldApplyTo(LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);
        return "wheel_of_fortune".equals(BeyonderData.getPathway(entity))
                && (sequence == 9 || sequence == 8);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;

        SpiritVisionAbility spiritVision = getSpiritVision();
        if (spiritVision == null) return;

        if (SpiritVisionAbility.isPermanentlyActiveFor(entity)) {
            forcedActive.add(entity.getUUID());
            if (!spiritVision.isActiveForEntity(entity)) {
                spiritVision.onAbilityUse(level, entity);
            }
        } else if (forcedActive.remove(entity.getUUID()) && spiritVision.isActiveForEntity(entity)) {
            spiritVision.onAbilityUse(level, entity);
        }
    }

    @Override
    public void onPassiveAbilityRemoved(LivingEntity entity, ServerLevel serverLevel) {
        SpiritVisionAbility spiritVision = getSpiritVision();
        if (forcedActive.remove(entity.getUUID())
                && spiritVision != null
                && spiritVision.isActiveForEntity(entity)) {
            spiritVision.onAbilityUse(serverLevel, entity);
        }
    }

    private static SpiritVisionAbility getSpiritVision() {
        return LOTMCraft.abilityHandler.getById("spirit_vision_ability") instanceof SpiritVisionAbility ability
                ? ability
                : null;
    }
}