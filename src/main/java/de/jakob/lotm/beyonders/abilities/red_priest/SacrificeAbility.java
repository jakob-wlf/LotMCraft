package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.KillCountComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SacrificeRevertComponent;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.WarBannerEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncKillCountPacket;
import de.jakob.lotm.network.packets.toClient.SyncSacrificeDurationPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
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
import java.util.concurrent.atomic.AtomicReference;

public class SacrificeAbility extends Ability {

    private static final int KILLS_PER_SECOND = 500;
    private static final int MAX_DURATION_SECONDS = 60;

    public SacrificeAbility(String id) {
        super(id, 20 * 60 * 5);
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 3000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ControllingDataComponent controllingData = player.getData(ModAttachments.CONTROLLING_DATA);
        if (controllingData.getTargetUUID() != null) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                    net.minecraft.network.chat.Component.literal("Sacrifice cannot be used while controlling a puppet")
                            .withStyle(net.minecraft.ChatFormatting.RED)
            ));
            return;
        }

        int currentSeq = BeyonderData.getSequence(player);
        if (currentSeq > 3 || currentSeq < 1) return;

        KillCountComponent killCount = player.getData(ModAttachments.KILL_COUNT_COMPONENT);
        int kills = killCount.getKillCount();

        if (kills < KILLS_PER_SECOND) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(
                    net.minecraft.network.chat.Component.literal("Not enough kill count to use Sacrifice (need at least " + KILLS_PER_SECOND + " kills)")
                            .withStyle(net.minecraft.ChatFormatting.RED)
            ));
            return;
        }

        int durationSeconds = Math.min(kills / KILLS_PER_SECOND, MAX_DURATION_SECONDS);
        int durationTicks = durationSeconds * 20;
        int killsConsumed = durationSeconds * KILLS_PER_SECOND;

        // Consume the kills
        killCount.setKillCount(kills - killsConsumed);
        PacketHandler.sendToPlayer(player, new SyncKillCountPacket(killCount.getKillCount()));

        int tempSeq = currentSeq - 1;
        String pathway = BeyonderData.getPathway(player);

        level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1, 1);

        spawnSacrificeEffect(serverLevel, player);

        // Activate after animation completes (20 ticks banner + 15 ticks ball = 35 ticks)
        int animationTicks = 35;
        ServerScheduler.scheduleDelayed(animationTicks, () -> {
            float savedDigestion = BeyonderData.getDigestionProgress(player);
            // Temporary advance that reverts after the duration — must not trigger the acting cap
            de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = true;
            try {
                BeyonderData.setBeyonder(player, pathway, tempSeq, true, false, true, false);
            } finally {
                de.jakob.lotm.beyonders.acting.ActingCapHelper.skipNextCapApplication = false;
            }
            // Temp sequence starts at 0 digestion — prevents drinking potions to exploit the advance
            BeyonderData.setDigestionProgress(player, 0);
            PacketHandler.syncBeyonderDataToPlayer(player);
            AbilityWheelHelper.removeUnusableAbilities(player);

            SacrificeRevertComponent revert = player.getData(ModAttachments.SACRIFICE_REVERT_COMPONENT);
            revert.set(serverLevel.getGameTime() + durationTicks, currentSeq, pathway, savedDigestion);

            PacketHandler.sendToPlayer(player, new SyncSacrificeDurationPacket(durationTicks));

            // Revert while online after duration expires
            ServerScheduler.scheduleDelayed(durationTicks, () -> {
                SacrificeRevertComponent r = player.getData(ModAttachments.SACRIFICE_REVERT_COMPONENT);
                if (!r.isActive()) return;
                if (BeyonderData.isBeyonder(player)
                        && BeyonderData.getPathway(player).equals(pathway)
                        && BeyonderData.getSequence(player) == tempSeq) {
                    float digestion = r.getSavedDigestion();
                    BeyonderData.setBeyonder(player, pathway, currentSeq, true, false, false, false);
                    BeyonderData.setDigestionProgress(player, digestion);
                    PacketHandler.syncBeyonderDataToPlayer(player);
                    AbilityWheelHelper.removeUnusableAbilities(player);
                }
                r.clear();
            }, serverLevel);
        }, serverLevel);
    }

    private static final DustParticleOptions RED_DUST = new DustParticleOptions(new Vector3f(0.9f, 0.05f, 0.05f), 2.0f);

    private void spawnSacrificeEffect(ServerLevel level, ServerPlayer player) {
        // Phase 1: Banner spawns 10 blocks in front, 2 above — no surrounding fire, visible for 1 second
        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 bannerPos = player.position().add(look.x * 10, 2.0, look.z * 10);

        WarBannerEntity banner = new WarBannerEntity(ModEntities.WAR_BANNER.get(), level, 20, player.getUUID(), true);
        banner.setPos(bannerPos.x, bannerPos.y, bannerPos.z);
        level.addFreshEntity(banner);

        // After 1 second, discard banner and launch particle ball toward the player
        ServerScheduler.scheduleDelayed(20, () -> {
            if (!banner.isRemoved()) banner.discard();

            // Phase 2: Dense particle ball flies from banner pos to player over 15 ticks
            Vec3 ballStart = new Vec3(bannerPos.x, bannerPos.y, bannerPos.z);
            Vec3 playerCenter = player.position().add(0, player.getBbHeight() / 2.0, 0);
            AtomicReference<Vec3> ballPos = new AtomicReference<>(ballStart);
            int flyTicks = 15;

            ServerScheduler.scheduleForDuration(0, 1, flyTicks, () -> {
                Vec3 pos = ballPos.get();
                Vec3 dir = playerCenter.subtract(pos);
                Vec3 newPos = pos.add(dir.scale(1.0 / flyTicks));
                ballPos.set(newPos);
                ParticleUtil.spawnParticles(level, ParticleTypes.FLAME, newPos, 10, 0.25, 0.25, 0.25, 0.02);
                ParticleUtil.spawnParticles(level, RED_DUST, newPos, 8, 0.2, 0.2, 0.2, 0);
            }, () -> {
                // Phase 3: Blood Inferno vortex erupts around the player for 2 seconds
                level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.5f);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 0.7f);
                EffectManager.playEffect(EffectManager.Effect.BLOOD_INFERNO,
                        player.getX(), player.getY(), player.getZ(), level, player);
            }, level);
        }, level);
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) return;

        KillCountComponent killCount = player.getData(ModAttachments.KILL_COUNT_COMPONENT);
        int kills = killCount.getKillCount();

        player.displayClientMessage(Component.translatable("lotm.kills").append(": " + kills).withStyle(ChatFormatting.RED), true);
    }
}
