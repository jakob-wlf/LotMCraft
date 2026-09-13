package de.jakob.lotm.beyonders.sefirah;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.block.ModBlocks;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BroodHiveEventHandler {

    @SubscribeEvent
    public static void onBreeding(BabyEntitySpawnEvent event) {
        Player player = event.getCausedByPlayer();
        
        if(!(player instanceof ServerPlayer serverPlayer)) return;

        boolean breedingBlockNearby = AbilityUtil.getBlocksInSphereRadius(
                serverPlayer.serverLevel(),
                player.position(), 5, true
        ).stream().anyMatch(b ->
            serverPlayer.level().getBlockState(b).is(ModBlocks.BREEDING_BLOCK)
        );

        if(!breedingBlockNearby) return;

        // Check for pathway requirement
        if(!BeyonderData.isBeyonder(serverPlayer) ||
                (!BeyonderData.getPathway(serverPlayer).equalsIgnoreCase("mother") &&
                        !BeyonderData.getPathway(serverPlayer).equalsIgnoreCase("moon"))) {
            AbilityUtil.sendActionBar(serverPlayer, Component.translatable("lotm.sefirot.brood_hive_reaction").withColor(BeyonderData.pathwayInfos.get("moon").color()));
            return;
        }

        if(BeyonderData.getSequence(player) > 6) {
            AbilityUtil.sendActionBar(serverPlayer, Component.translatable("lotm.sefirot.not_strong_enough").withColor(BeyonderData.pathwayInfos.get("moon").color()));
            return;
        }

        if (SefirahHandler.hasSefirot(serverPlayer)) {
            AbilityUtil.sendActionBar(serverPlayer, Component.translatable("lotm.sefirot.conflicting_sefirah").withColor(BeyonderData.pathwayInfos.get("moon").color()));
            return;
        }

        // Claim Sefirah Castle
        if (!SefirahHandler.claimSefirot(serverPlayer, "brood_hive")) {
            AbilityUtil.sendActionBar(serverPlayer, Component.translatable("lotm.sefirot.brood_hive_already_occupied").withColor(BeyonderData.pathwayInfos.get("moon").color()));
            return;
        }

        player.level().playSound(player, BlockPos.containing(player.position()), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 1, 1);

        SefirahHandler.teleportToOwnSefirot(serverPlayer, true);
    }


    // Disable abilities inside the castle and disable griefing inside completely --------------------
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if(!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.level().dimension().equals(ModDimensions.BROOD_HIVE_DIMENSION_KEY)) {
            return;
        }

        // Disable griefing
        if (entity instanceof Player player) {
            BeyonderData.setGriefingEnabled(player, false);
        }

        // Disable ability use
        if(!(entity instanceof ServerPlayer player) || !SefirahHandler.getClaimedSefirot(player).equalsIgnoreCase("sefirah_castle")) {
            DisabledAbilitiesComponent component = entity.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            component.disableAbilityUsageForTime("sefirah_castle", 20 * 20, entity);
        }
    }

}