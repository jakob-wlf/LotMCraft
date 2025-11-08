package de.jakob.lotm.abilities.visionary;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class DreamTraversalAbility extends AbilityItem {
    public DreamTraversalAbility(Properties properties) {
        super(properties, .5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 60;
    }

    private final DustParticleOptions dust = new DustParticleOptions(
            new Vector3f(250 / 255f, 201 / 255f, 102 / 255f),
            1f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 200, 2);

        if(target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.frenzy.no_target").withColor(0xFFff124d));
            return;
        }

        if(!target.hasEffect(ModEffects.ASLEEP)) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.dream_traversal.must_be_asleep").withColor(0xFFff124d));
            return;
        }

        entity.teleportTo(target.getX(), target.getY(), target.getZ());
        ParticleUtil.spawnParticles(serverLevel, dust, target.position().add(0, entity.getEyeHeight() / 2, 0), 100, .35, entity.getEyeHeight() / 2, .35, 0);
    }
}
