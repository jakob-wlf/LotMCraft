package de.jakob.lotm.beyonders.abilities.common;

import de.jakob.lotm.attachments.AllyComponent;
import de.jakob.lotm.beyonders.abilities.abyss.MaliceSeed.MaliceSeedAbility;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Phantom.PhantomDetectionUtil;
import de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Shapeshifter.SpiritDisguiseUtil;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryLoosingControlHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.PlayPhotonEntityEffectPacket;
import de.jakob.lotm.network.packets.toClient.SyncSpiritVisionAbilityPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.mixin.EntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector3f;

import java.util.*;

public class SpiritVisionAbility extends ToggleAbility {
    private final Map<UUID, Set<Entity>> seedOutlined = new HashMap<>();
    private static final String MALICE_TEAM = "lotm_malice_black";
    private static final PlayerTeam MALICE_TEAM_OBJ = new PlayerTeam(new Scoreboard(), MALICE_TEAM);
    static { MALICE_TEAM_OBJ.setColor(ChatFormatting.BLACK); }
    public SpiritVisionAbility(String id) {
        super(id);

        canBeCopied = false;
        canBeUsedByNPC = false;
        doesNotIncreaseDigestion = true;
        cannotBeStolen = true;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        autoClear = false;
        tickRate = 1;
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
                AllyUtil.syncAllyData(player);
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

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f, false, true);

        if (lookedAt != null) {
            if (VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(entity), lookedAt)|| PhantomDetectionUtil.isHiddenFromSpiritVision(entity, lookedAt))
                return;
        }

        PacketHandler.sendToPlayer(player, new PlayPhotonEntityEffectPacket("spirit_vision", player.getId(), 0, 0, 0, 1, null, true, true, UUID.randomUUID(), true));

        PacketHandler.sendToPlayer(player, new SyncSpiritVisionAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));
        AllyComponent allyComponent = entity.getData(ModAttachments.ALLY_COMPONENT);

        if (lookedAt != null) {
            if (!allyComponent.isAlly(lookedAt.getUUID())) {
                if (shouldLooseControl(entity, lookedAt)) {
                    VisionaryLoosingControlHandler.applyEffect(lookedAt, entity, this);
                    return;
                }
            }
        }

        entity.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION, 20 * 25, 1, false, false, false));

        List<LivingEntity> nearbyEntities = AbilityUtil.getNearbyEntities(entity, (ServerLevel) level, entity.getEyePosition(), 30, false, true)
                .stream()
                .filter(nearbyEntity -> {
                    return !VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(entity), nearbyEntity) && !PhantomDetectionUtil.isHiddenFromSpiritVision(entity, nearbyEntity);
                })
                .toList();


        for (LivingEntity nearbyEntity : nearbyEntities) {
            updateSeedOutlines(player, nearbyEntities);
            setGlowingForPlayer(nearbyEntity, (ServerPlayer) entity, true);
        }

        glowingEntities.putIfAbsent(entity.getUUID(), new HashSet<>(Set.of()));
        glowingEntities.get(entity.getUUID()).addAll(nearbyEntities);

    }

    public static boolean shouldLooseControl(LivingEntity player, LivingEntity target) {
        int playerSeq = BeyonderData.getSequence(player);
        int targetSeq = BeyonderData.getSequence(target);

        if (player.getData(ModAttachments.ALLY_COMPONENT.get()).isAlly(target.getUUID()))
            return false;
        if (SpiritDisguiseUtil.isFooledByDisguise(player, target)) return false;

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
            Set<Entity> outlined = seedOutlined.remove(entity.getUUID());
            if (outlined != null && !outlined.isEmpty()) {
                outlined.forEach(e -> setSeedOutline(player, e, false));
                player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(MALICE_TEAM_OBJ));
            }

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
    private static void setSeedOutline(ServerPlayer viewer, Entity e, boolean on) {
        viewer.connection.send(ClientboundSetPlayerTeamPacket.createPlayerPacket(MALICE_TEAM_OBJ, e.getScoreboardName(), on ? ClientboundSetPlayerTeamPacket.Action.ADD : ClientboundSetPlayerTeamPacket.Action.REMOVE));
    }
    private void updateSeedOutlines(ServerPlayer viewer, List<LivingEntity> nearby) {
        Set<Entity> previous = seedOutlined.computeIfAbsent(viewer.getUUID(), k -> new HashSet<>());
        Set<Entity> current = new HashSet<>();
        for (LivingEntity e : nearby) {if (MaliceSeedAbility.canObserverSeeSeed(viewer, e)) current.add(e);}
        if (previous.isEmpty() && !current.isEmpty()) {viewer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(MALICE_TEAM_OBJ, true));}
        for (Entity e : current) if (!previous.contains(e)) setSeedOutline(viewer, e, true);
        for (Entity e : previous) if (!current.contains(e)) setSeedOutline(viewer, e, false);
        previous.clear();
        previous.addAll(current);
    }

}
