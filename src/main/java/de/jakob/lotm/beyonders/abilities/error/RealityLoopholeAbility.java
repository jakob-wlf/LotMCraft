package de.jakob.lotm.beyonders.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncRealityLoopholePacket;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class RealityLoopholeAbility extends ToggleAbility {

    private static final String CAUSE = "reality_loophole";
    public static final HashSet<UUID> phasedOut = new HashSet<>();
    public static HashSet<UUID> phasedOutClient = new HashSet<>();

    public RealityLoopholeAbility(String id) {
        super(id);

        canAlwaysBeUsed = true;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
        canBeUsedWhileControlling = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 0));
    }

    @Override
    public float getSpiritualityCost() {
        return 75;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        phasedOut.add(entity.getUUID());
        syncActiveToAllPlayers();

        entity.setInvisible(true);
        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 10, 0, false, false, false));

        entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).disableAbilityUsage(CAUSE);

        EffectManager.playEffect(EffectIds.SEFIRAH_CASTLE, entity.getX(), entity.getY() + .5, entity.getZ(), (ServerLevel) level);
        entity.playSound(SoundEvents.ENDER_CHEST_OPEN);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 10, 0, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            return;
        }

        phasedOut.remove(entity.getUUID());
        syncActiveToAllPlayers();

        entity.setInvisible(false);
        entity.removeEffect(MobEffects.INVISIBILITY);

        entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).enableAbilityUsage(CAUSE);

        entity.playSound(SoundEvents.ENDER_CHEST_CLOSE);
    }

    private static void forceStop(LivingEntity entity) {
        if(!phasedOut.contains(entity.getUUID())) {
            return;
        }

        phasedOut.remove(entity.getUUID());
        syncActiveToAllPlayers();

        DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
        component.enableAbilityUsage(CAUSE);
    }

    private static void syncActiveToAllPlayers() {
        PacketHandler.sendToAllPlayers(new SyncRealityLoopholePacket(new HashSet<>(phasedOut)));
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if(phasedOut.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if(newTarget != null && phasedOut.contains(newTarget.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if(phasedOut.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if(phasedOut.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if(phasedOut.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if(phasedOut.contains(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if(player != null && phasedOut.contains(player.getUUID())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        forceStop(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        forceStop(event.getEntity());
    }
}
