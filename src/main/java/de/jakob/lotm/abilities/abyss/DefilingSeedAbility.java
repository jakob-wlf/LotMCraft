package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefilingSeedAbility extends Ability {

    /**
     * Tracks all currently afflicted entities.
     * Key:   UUID of the afflicted target
     * Value: entry holding the caster's sequence at the time of use and
     *        the UUID of the running scheduleForDuration task so it can
     *        be cancelled by purification mid-effect.
     */
    private static final Map<UUID, DefilingSeedEntry> defiledEntities = new HashMap<>();

    public static Map<UUID, DefilingSeedEntry> getDefiledEntities() {
        return Collections.unmodifiableMap(defiledEntities);
    }

    public record DefilingSeedEntry(int casterSequence, UUID schedulerTaskId) {}


    public static void purify(LivingEntity target, LivingEntity purifier, ServerLevel level) {
        DefilingSeedEntry entry = defiledEntities.remove(target.getUUID());
        if (entry == null) return;

        ServerScheduler.cancel(entry.schedulerTaskId());

        target.removeEffect(ModEffects.LOOSING_CONTROL);

        // Purification particles on the freed target
        ParticleUtil.spawnParticles(level, ParticleTypes.END_ROD, target.getEyePosition(), 50, 1.2, 0);
        level.playSound(null, BlockPos.containing(target.position()),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1f, 1.5f);
    }

    // -------------------------------------------------------------------------

    private final DustParticleOptions blackDust = new DustParticleOptions(
            new Vector3f(.2f, 0f, 0f), 2.5f
    );

    public DefilingSeedAbility(String id) {
        super(id, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 60;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.5f);

        // No valid target, or target is already defiled
        if (target == null || defiledEntities.containsKey(target.getUUID())) {
            if (entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.translatable("ability.lotmcraft.defiling_seed.no_target")
                                .withColor(0xFFff124d)
                ));
            }
            return;
        }

        // Backfire if the target is significantly stronger than the caster
        if (AbilityUtil.isTargetSignificantlyStronger(entity, target)) {
            entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 5, 3));
            entity.hurt(ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity), 10);
            return;
        }

        ParticleUtil.spawnParticles(serverLevel, blackDust, target.getEyePosition(), 40, 1.5, 0);
        level.playSound(null, BlockPos.containing(entity.position()),
                SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1f, 1f);

        // Capture now — the scheduler lambda must not close over a mutable field
        final int casterSequence = BeyonderData.getSequence(entity);
        final float damageMultiplier = (float) multiplier(entity);

        UUID taskId = ServerScheduler.scheduleForDuration(
                0,
                8,
                20 * 60 * 2,
                () -> {
                    switch (random.nextInt(22)) {
                        case 0, 2, 3 -> target.hurt(
                                ModDamageTypes.source(level, ModDamageTypes.LOOSING_CONTROL, entity),
                                6 * damageMultiplier);
                        case 1 -> target.addEffect(new MobEffectInstance(
                                ModEffects.LOOSING_CONTROL, 20 * 4, random.nextInt(4)));
                        case 4, 5 -> target.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN, 20 * 9, random.nextInt(2, 7)));
                    }
                },
                // onFinish: when the full 2-minute duration expires naturally,
                // wait 5 more seconds before allowing the target to be defiled again.
                // This task is linked to taskId and will be cancelled automatically
                // if purify() cancels before the duration expires.
                () -> ServerScheduler.scheduleDelayed(
                        20 * 5,
                        () -> defiledEntities.remove(target.getUUID()),
                        serverLevel),
                serverLevel
        );

        defiledEntities.put(target.getUUID(), new DefilingSeedEntry(casterSequence, taskId));
    }
}