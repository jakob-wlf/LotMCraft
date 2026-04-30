package de.jakob.lotm.abilities.death;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UndyingSealAbility extends Ability {

    private static final int DURATION_TICKS = 20 * 60;

    private static final ConcurrentHashMap<UUID, Long> sealedPlayers = new ConcurrentHashMap<>();

    private static final DustParticleOptions SEAL_DUST       = new DustParticleOptions(new Vector3f(0.0f, 0.72f, 0.9f),  2.0f);
    private static final DustParticleOptions SEAL_DUST_SMALL = new DustParticleOptions(new Vector3f(0.0f, 0.72f, 0.9f),  1.0f);
    private static final DustParticleOptions SEAL_DUST_DARK  = new DustParticleOptions(new Vector3f(0.05f, 0.25f, 0.45f), 1.5f);

    public UndyingSealAbility(String id) {
        super(id, 80f);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 350;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        if (InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), serverLevel), "purification", BeyonderData.getSequence(entity), -1)) return;

        long expiresAt = serverLevel.getGameTime() + DURATION_TICKS;
        sealedPlayers.put(player.getUUID(), expiresAt);

        spawnSealActivationEffects(serverLevel, player);

        player.sendSystemMessage(Component.translatable("ability.lotmcraft.undying_seal.activated")
                .withStyle(ChatFormatting.DARK_AQUA));
    }

    private void spawnSealActivationEffects(ServerLevel level, ServerPlayer player) {
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,      SoundSource.PLAYERS, 0.8f, 0.5f);
        level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,    SoundSource.PLAYERS, 1.0f, 0.6f);
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_AMBIENT,       SoundSource.PLAYERS, 0.4f, 1.8f);

        Vec3 center = player.position().add(0, player.getEyeHeight() / 2.0, 0);

        ParticleUtil.spawnParticles(level, ParticleTypes.SOUL, center, 20, 0.4, player.getEyeHeight() / 2.0, 0.4, 0.03f);
        ParticleUtil.spawnParticles(level, SEAL_DUST, center, 30, 0.5, player.getEyeHeight() / 2.0, 0.5, 0);

        ServerScheduler.scheduleForDuration(0, 1, 40, () -> {
            if (player.isDeadOrDying()) return;

            long t = level.getGameTime();
            double progress = (t % 40) / 40.0;
            Vec3 pos = player.position().add(0, player.getEyeHeight() / 2.0, 0);
            double ringRadius = 1.2 - progress * 0.5;

            ParticleUtil.spawnCircleParticles(level, SEAL_DUST,             pos.add(0, -player.getEyeHeight() * 0.4, 0), ringRadius, 12);
            ParticleUtil.spawnCircleParticles(level, SEAL_DUST_DARK,        pos.add(0, -player.getEyeHeight() * 0.4, 0), ringRadius, 4);
            ParticleUtil.spawnCircleParticles(level, SEAL_DUST,             pos.add(0,  player.getEyeHeight() * 0.4, 0), ringRadius, 12);
            ParticleUtil.spawnCircleParticles(level, SEAL_DUST_DARK,        pos.add(0,  player.getEyeHeight() * 0.4, 0), ringRadius, 4);

            ParticleUtil.spawnParticles(level, ParticleTypes.SOUL_FIRE_FLAME, pos, 2, 0.2, player.getEyeHeight() / 2.0, 0.2, 0.01f);
            ParticleUtil.spawnParticles(level, SEAL_DUST_SMALL, pos.add(0, player.getEyeHeight() * 0.5 * progress, 0), 3, 0.15);
        }, () -> {
            Vec3 pos = player.position().add(0, player.getEyeHeight() / 2.0, 0);
            ParticleUtil.spawnCircleParticles(level, SEAL_DUST,         pos, 1.2, 24);
            ParticleUtil.spawnCircleParticles(level, ParticleTypes.SOUL, pos, 1.2, 12);
            ParticleUtil.spawnParticles(level, ParticleTypes.ENCHANT, pos, 40, 0.6, player.getEyeHeight() / 2.0, 0.6, 0.05f);
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 0.8f);
        }, level, () -> 1.0);
    }

    public static boolean isSealed(UUID playerUUID, long currentGameTime) {
        Long expiresAt = sealedPlayers.get(playerUUID);
        if (expiresAt == null) return false;
        if (currentGameTime >= expiresAt) {
            sealedPlayers.remove(playerUUID);
            return false;
        }
        return true;
    }
}