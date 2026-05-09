package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Map;

public class FoolingTimeAbility extends Ability {

    public FoolingTimeAbility() {
        super("fooling_time", 25);
        this.canBeCopied = false;
        this.canBeReplicated = false;
        this.canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("fool", 0);
    }

    @Override
    protected float getSpiritualityCost() {
        return 300;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        int color = BeyonderData.pathwayInfos.get("fool").color();

        if (entity.isShiftKeyDown()) {
            FoolingAbility.playPulse(serverLevel, entity.position(), ParticleTypes.FLASH, 5, 2.0);
            var future = FoolingAbility.findRandomFuturePosition(serverLevel, entity.blockPosition());
            entity.teleportTo(future.x, future.y, future.z);
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_time.future").withColor(color));
        } else {
            FoolingAbility.TemporalSnapshot snapshot = FoolingAbility.getOldestSnapshot(entity);
            if (snapshot == null) {
                AbilityUtil.sendActionBar(entity,
                        Component.translatable("lotmcraft.ability.fooling_time.no_history").withColor(color));
                return;
            }
            entity.teleportTo(snapshot.position.x, snapshot.position.y, snapshot.position.z);
            entity.setHealth(Math.min(snapshot.health, entity.getMaxHealth()));
            BeyonderData.setSpirituality(entity, snapshot.spirituality);
            AbilityUtil.sendActionBar(entity,
                    Component.translatable("lotmcraft.ability.fooling_time.rewound").withColor(color));
        }
    }
}
