package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncSpiritVisionAbilityPacket;
import de.jakob.lotm.sefirah.SefirahHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.*;

public class SpiritVisionAbility extends ToggleAbility {

    public SpiritVisionAbility(String id) {
        super(id);

        canBeCopied = false;
        canBeUsedByNPC = false;
        doesNotIncreaseDigestion = true;
        cannotBeStolen = true;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        autoClear = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        Map<String, Integer> reqs = new HashMap(
                Map.of(
                        "fool", 9,
                        "door", 7,
                        "hermit", 9,
                        "demoness", 7,
                        "mother", 8,
                        "wheel_of_fortune", 9,
                        "darkness", 7,
                        "abyss", 9,
                        "red_priest", 8
                ));

        for (String pathway : BeyonderData.pathways) {
            if (!reqs.containsKey(pathway))
                reqs.put(pathway, 5);
        }
        return reqs;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            if (entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncSpiritVisionAbilityPacket(true, -1));
            }
            return;
        }

        entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
    }

    private final DustParticleOptions dust = new DustParticleOptions(new Vector3f(255, 255, 255), 2f);

    private final HashMap<UUID, Set<Entity>> glowingEntities = new HashMap<>();

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide()) {
            return;
        }

        if (!(entity instanceof ServerPlayer player))
            return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);

        if (lookedAt != null) {
            if (VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(entity), lookedAt))
                return;
        }

        PacketHandler.sendToPlayer(player, new SyncSpiritVisionAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        if (lookedAt != null) {
            if (shouldLooseControl(entity, lookedAt)) {
                if (!entity.hasEffect(ModEffects.LOOSING_CONTROL))
                    entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 25, 4, false, false, false));

                    return;
                }

                // Sefirah Castle halo: show fool/error/door pathway particles around the owner's head
                // to any viewer, as long as the owner has not yet reached seq 4 (seq number > 4).
                // Once the owner ascends to seq 4 or higher in power, the halo vanishes.
                if (lookedAt instanceof net.minecraft.server.level.ServerPlayer targetPlayer) {
                    String claimedSefirot = SefirahHandler.getClaimedSefirot(targetPlayer);
                    if ("sefirah_castle".equals(claimedSefirot)
                            && BeyonderData.getSequence(targetPlayer) > 4) {
                        spawnCastleOwnerHalo(player, lookedAt, (net.minecraft.server.level.ServerLevel) level);
                    }
                }

                // Wheel of Fortune: if a WoF beyonder at seq 5 or higher (weaker) uses spirit vision
                // and sees the castle owner, they are instantly killed. Seq 4 and below are safe.
                if ("wheel_of_fortune".equals(BeyonderData.getPathway(player))
                        && BeyonderData.getSequence(player) >= 5
                        && lookedAt instanceof ServerPlayer seenPlayer
                        && "sefirah_castle".equals(SefirahHandler.getClaimedSefirot(seenPlayer))) {
                    player.sendSystemMessage(Component.literal(
                            "The threads of fate snap — you should not have looked.")
                            .withStyle(ChatFormatting.DARK_RED));
                    player.hurt(player.damageSources().magic(), Float.MAX_VALUE);
                    return;
                }
            }

        entity.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 20 * 25, 1, false, false, false));

        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 30)
                .stream()
                .filter(nearbyEntity -> {
                    return !VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(entity), nearbyEntity);
                })
                .toList();


        for (LivingEntity nearbyEntity : nearbyEntities) {
            setGlowingForPlayer(nearbyEntity, (ServerPlayer) entity, true);
        }

            glowingEntities.putIfAbsent(entity.getUUID(), new HashSet<>(Set.of()));
            glowingEntities.get(entity.getUUID()).addAll(nearbyEntities);

            // Wheel of Fortune proximity warning: while spirit vision is active, if the castle owner
            // is within 50 blocks send a warning message (throttled to once every 2 seconds).
            // Applies to seq 6 and below (seq 0-6); seq 7+ are too detached from fate to feel it.
            // Uses level.players() directly to avoid the creative-mode filter in getNearbyEntities.
            if ("wheel_of_fortune".equals(BeyonderData.getPathway(player))
                    && BeyonderData.getSequence(player) <= 6) {
                long gameTick = ((ServerLevel) level).getGameTime();
                if (gameTick % 40 == 0) { // every 2 seconds
                    boolean ownerNearby = ((ServerLevel) level).players().stream()
                            .filter(sp -> !sp.getUUID().equals(player.getUUID()))
                            .anyMatch(sp -> sp.position().distanceTo(player.position()) <= 50
                                    && "sefirah_castle".equals(SefirahHandler.getClaimedSefirot(sp)));
                    if (ownerNearby) {
                        player.sendSystemMessage(Component.literal(
                                "You sense a presence that sees all threads of fate. Leave \u2014 now.")
                                .withStyle(ChatFormatting.DARK_PURPLE));
                    }
                }
            }
        }
    }

    public static boolean shouldLooseControl(LivingEntity player, LivingEntity target) {
        int playerSeq = BeyonderData.getSequence(player);
        int targetSeq = BeyonderData.getSequence(target);

        if (player.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(target.getUUID()))
            return false;

        return AbilityUtil.isTargetSignificantlyStronger(player, target);
    }

    public static void setGlowingForPlayer(Entity entity, ServerPlayer player, boolean glowing) {
        EntityDataAccessor<Byte> FLAGS = EntityAccessor.getSharedFlagsId();


        // Current flags from the entity
        byte flags = entity.getEntityData().get(FLAGS);

        if (glowing) {
            flags |= 0x40; // glowing bit
        } else {
            flags &= ~0x40; // clear glowing bit
        }

        // Build a list of data values (only the one we care about)
        List<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(FLAGS, flags));

        // Send metadata update ONLY to that player
        ClientboundSetEntityDataPacket packet =
                new ClientboundSetEntityDataPacket(entity.getId(), values);
        player.connection.send(packet);
    }

    /**
     * Spawns a three-colour halo (fool purple / error blue / door cyan) as orbiting dust
     * particles around the Sefirah Castle owner's head, visible only to {@code viewer}.
     */
    private static void spawnCastleOwnerHalo(net.minecraft.server.level.ServerPlayer viewer,
                                              LivingEntity owner,
                                              net.minecraft.server.level.ServerLevel level) {
        double cx = owner.getX();
        double cy = owner.getY() + owner.getEyeHeight() + 0.3;
        double cz = owner.getZ();
        double radius = 0.55;

        // fool (purple), error (blue/indigo), door (cyan) — one particle per colour per tick
        org.joml.Vector3f[] colors = {
                new org.joml.Vector3f(0x86 / 255f, 0x4e / 255f, 0xc7 / 255f), // fool purple
                new org.joml.Vector3f(0x00 / 255f, 0x18 / 255f, 0xb8 / 255f), // error indigo
                new org.joml.Vector3f(0x89 / 255f, 0xf5 / 255f, 0xf5 / 255f), // door cyan
        };

        long tick = level.getGameTime();
        for (int i = 0; i < colors.length; i++) {
            double angle = (tick * 0.15 + i * (2 * Math.PI / colors.length)) % (2 * Math.PI);
            double px = cx + radius * Math.cos(angle);
            double pz = cz + radius * Math.sin(angle);
            net.minecraft.core.particles.DustParticleOptions dust =
                    new net.minecraft.core.particles.DustParticleOptions(colors[i], 1.4f);
            level.sendParticles(viewer, dust, true, px, cy, pz, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
        } else {
            if (!(entity instanceof ServerPlayer player))
                return;

            player.removeEffect(MobEffects.NIGHT_VISION);

            if (glowingEntities.containsKey(entity.getUUID()))
                glowingEntities.get(entity.getUUID()).forEach(e -> setGlowingForPlayer(e, player, false));
            glowingEntities.remove(entity.getUUID());

            PacketHandler.sendToPlayer(player, new SyncSpiritVisionAbilityPacket(false, -1));

            clearArtifactScaling(entity);
        }
    }

    private int getRandomColor() {
        Random random = new Random();
        int alpha = 0xFF; // Full opacity
        int red = random.nextInt(256);   // 0 to 255
        int green = random.nextInt(256); // 0 to 255
        int blue = random.nextInt(256);  // 0 to 255

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    protected float getSpiritualityCost() {
        return .5f;
    }

}
