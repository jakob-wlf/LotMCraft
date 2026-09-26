package de.jakob.lotm.beyonders.abilities.death;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.common.SpiritVisionAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.events.AdvancementsEventHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.entity.custom.spirits.SpiritBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBizarroBaneEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBlueWizardEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritBubblesEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritDervishEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritGhostEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritMalmouthEntity;
import de.jakob.lotm.entity.custom.spirits.SpiritTranslucentWizardEntity;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncEyeOfDeathAbilityPacket;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class EyeOfDeathAbility extends ToggleAbility {

    /** Players with Eye of Death currently active (server-side). */
    public static final HashSet<UUID> activePlayers = new HashSet<>();

    /** Test ghost beyonder currently glowing for a given player, so it can be un-glowed when the look target changes. */
    private static final HashMap<UUID, LivingEntity> glowingGhostByPlayer = new HashMap<>();


    public EyeOfDeathAbility(String id) {
        super(id);

        canBeCopied = false;
        canBeShared = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("death", 8));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            activePlayers.add(entity.getUUID());
            if (entity instanceof ServerPlayer player) {
                PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(true, -1));
            }
            return;
        }
        entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!level.isClientSide() && InteractionHandler.isInteractionPossibleStrictlyHigher(new Location(entity.position(), (ServerLevel) level), "purification", BeyonderData.getSequence(entity), -1)) {
            stop(level, entity);
            return;
        }

        if (level.isClientSide()) {
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;

        LivingEntity lookedAt = AbilityUtil.getTargetEntity(entity, 40, 1.2f);

        if(lookedAt != null){
            if(VisionaryHandler.shouldStayInvisible(BeyonderData.getSequence(entity), lookedAt))
                return;
        }

        PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(true, lookedAt == null ? -1 : lookedAt.getId()));

        entity.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 25, 1, false, false, false));

        updateGhostGlow(player, lookedAt);
    }

    /** Makes a ghost beyonder glow only for the player currently looking at it via Eye of Death. */
    private static void updateGhostGlow(ServerPlayer player, LivingEntity lookedAt) {
        LivingEntity newGlowTarget = (lookedAt != null && AdvancementsEventHandler.isGhostBeyonder(lookedAt))
                ? lookedAt : null;

        LivingEntity previousGlowTarget = glowingGhostByPlayer.get(player.getUUID());

        if (previousGlowTarget != null && previousGlowTarget != newGlowTarget && previousGlowTarget.isAlive()) {
            SpiritVisionAbility.setGlowingForPlayer(previousGlowTarget, player, false);
        }

        if (newGlowTarget != null) {
            // Re-sent every tick: the invisibility effect keeps re-syncing this entity's real (non-glowing)
            // shared flags to all trackers, which would otherwise stomp our player-only glow override.
            SpiritVisionAbility.setGlowingForPlayer(newGlowTarget, player, true);
            glowingGhostByPlayer.put(player.getUUID(), newGlowTarget);
        } else {
            glowingGhostByPlayer.remove(player.getUUID());
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            entity.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
        } else {
            activePlayers.remove(entity.getUUID());
            if (!(entity instanceof ServerPlayer player)) return;

            player.removeEffect(MobEffects.NIGHT_VISION);

            LivingEntity previousGlowTarget = glowingGhostByPlayer.remove(player.getUUID());
            if (previousGlowTarget != null && previousGlowTarget.isAlive()) {
                SpiritVisionAbility.setGlowingForPlayer(previousGlowTarget, player, false);
            }

            PacketHandler.sendToPlayer(player, new SyncEyeOfDeathAbilityPacket(false, -1));
        }
    }

    @Override
    protected float getSpiritualityCost() {
        return 0.5f;
    }

    public static boolean isSpiritEntity(LivingEntity entity) {
        return entity instanceof SpiritBaneEntity
                || entity instanceof SpiritBizarroBaneEntity
                || entity instanceof SpiritBlueWizardEntity
                || entity instanceof SpiritBubblesEntity
                || entity instanceof SpiritDervishEntity
                || entity instanceof SpiritGhostEntity
                || entity instanceof SpiritMalmouthEntity
                || entity instanceof SpiritTranslucentWizardEntity;
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!activePlayers.contains(attacker.getUUID())) return;

        LivingEntity target = event.getEntity();
        boolean isUndead = AbilityUtil.isUndead(target);
        boolean isSpirit = isSpiritEntity(target);

        if (isUndead || isSpirit) {
            event.setAmount(event.getAmount() * 1.35f);
        }
    }
}
