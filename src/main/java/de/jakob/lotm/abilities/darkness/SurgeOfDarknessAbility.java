package de.jakob.lotm.abilities.darkness;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class SurgeOfDarknessAbility extends AbilityItem {
    public SurgeOfDarknessAbility(Properties properties) {
        super(properties, 10);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1000;
    }

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {

    }
}