package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncCorrosionFovPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CorrosionAbility extends ToggleAbility {

    // Tracks how many ticks each nearby entity has spent in the aura
    // so corruption deepens the longer they stay.
    private final Map<UUID, Integer> exposureTicks = new HashMap<>();

    // Thresholds (in ticks) for escalating corruption stages
    private static final int STAGE_1_TICKS = 20 * 3;   // 3s  — greedy, distracted
    private static final int STAGE_2_TICKS = 20 * 8;   // 8s  — irrational, retargets
    private static final int STAGE_3_TICKS = 20 * 15;  // 15s — fully corrupted, uses own abilities chaotically

    public CorrosionAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 6));
    }

    @Override
    public float getSpiritualityCost() {
        // Drain per tick — moderate cost for a powerful aura
        return 2.0f;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.getPersistentData().putBoolean("lotm_corrosion_active", true);
        entity.sendSystemMessage(Component.literal("§5Corrosion: ON"));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        int selfSeq = BeyonderData.getSequence(entity);

        List<LivingEntity> inRange = level.getEntitiesOfClass(
                LivingEntity.class,
                entity.getBoundingBox().inflate(10));

        // Track which UUIDs are still in range this tick so we can decay absent ones
        java.util.Set<UUID> currentlyInRange = new java.util.HashSet<>();

        for (LivingEntity target : inRange) {
            if (target == entity) continue;

            // Stronger Beyonders fully resist Corrosion
            if (BeyonderData.isBeyonder(target)) {
                int targetSeq = BeyonderData.getSequence(target);
                if (targetSeq < selfSeq) continue;
            }

            UUID id = target.getUUID();
            currentlyInRange.add(id);

            int ticks = exposureTicks.getOrDefault(id, 0) + 1;
            exposureTicks.put(id, ticks);

            applyCorruption(serverLevel, entity, target, ticks);
        }

        // Decay exposure for entities that left the aura
        exposureTicks.entrySet().removeIf(entry -> {
            if (!currentlyInRange.contains(entry.getKey())) {
                int remaining = entry.getValue() - 2;
                if (remaining <= 0) {
                    // Reset FOV if this was a player
                    if (level instanceof ServerLevel sl) {
                        ServerPlayer leaver = sl.getServer().getPlayerList().getPlayer(entry.getKey());
                        if (leaver != null) {
                            PacketHandler.sendToPlayer(leaver, new SyncCorrosionFovPacket(1.0f));
                        }
                    }
                    return true;
                }
                entry.setValue(remaining);
            }
            return false;
        });
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        entity.getPersistentData().remove("lotm_corrosion_active");

        // Reset FOV for any players that were in the aura
        if (level instanceof ServerLevel sl) {
            for (UUID uuid : exposureTicks.keySet()) {
                ServerPlayer player = sl.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    PacketHandler.sendToPlayer(player, new SyncCorrosionFovPacket(1.0f));
                }
            }
        }

        exposureTicks.clear();
        entity.sendSystemMessage(Component.literal("§cCorrosion: OFF"));
    }

    // ----- Corruption logic -----

    private void applyCorruption(ServerLevel level, LivingEntity caster,
                                 LivingEntity target, int ticks) {

        // Stage 1 — Greed awakens: target is slowed and distracted
        if (ticks >= STAGE_1_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));

            // Mobs stop pursuing their current goal and wander briefly
            if (target instanceof Mob mob && level.getGameTime() % 40 == 0) {
                if (level.random.nextFloat() < 0.35f) {
                    mob.getNavigation().stop();
                }
            }

            // Players see a subtle warning that something feels off
            if (target instanceof Player player && ticks == STAGE_1_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("You feel an inexplicable greed stirring within you...")
                                .withColor(0x883399));
            }
        }

        // Stage 2 — Irrationality takes hold: mobs retarget randomly, players get confusion + random FOV
        if (ticks >= STAGE_2_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION, 60, 0, false, false, false));

            if (target instanceof Mob mob && level.getGameTime() % 60 == 0) {
                if (level.random.nextFloat() < 0.50f) {
                    List<LivingEntity> nearby = level.getEntitiesOfClass(
                            LivingEntity.class,
                            target.getBoundingBox().inflate(12),
                            e -> e != target && e != caster
                    );
                    if (!nearby.isEmpty()) {
                        mob.setTarget(nearby.get(level.random.nextInt(nearby.size())));
                    }
                }
            }

            // Every 5 seconds, send a random FOV multiplier to player targets
            if (target instanceof ServerPlayer player && level.getGameTime() % 100 == 0) {
                float fov = 0.6f + level.random.nextFloat() * 1.2f; // range: 0.6x – 1.8x
                PacketHandler.sendToPlayer(player, new SyncCorrosionFovPacket(fov));
            }

            if (target instanceof Player player && ticks == STAGE_2_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("The greed is overwhelming — your thoughts are scattered.")
                                .withColor(0x661188));
            }
        }

        // Stage 3 — Full corruption: target loses sanity every 3 seconds
        if (ticks >= STAGE_3_TICKS) {
            target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS, 40, 1, false, false, false));

            if (level.getGameTime() % 60 == 0) {
                var sanity = target.getData(ModAttachments.SANITY_COMPONENT);
                sanity.increaseSanityAndSync(-0.01f, target);
            }

            if (target instanceof Player player && ticks == STAGE_3_TICKS) {
                AbilityUtil.sendActionBar(player,
                        Component.literal("You can no longer control yourself. Darkness consumes you.")
                                .withColor(0x440066));
            }
        }
    }
}