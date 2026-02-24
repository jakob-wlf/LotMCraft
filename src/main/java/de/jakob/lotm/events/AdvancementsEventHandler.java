package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.artifacts.SealedArtifactItem;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AdvancementsEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (BeyonderData.isBeyonder(player)) {
            grantAdvancement(player, "become_beyonder");

            String sequenceName = BeyonderData.pathwayInfos.get(BeyonderData.getPathway(player))
                    .getRawSequenceName(BeyonderData.getSequence(player));
            grantAdvancement(player, "become_" + sequenceName.toLowerCase());

            int sequence = BeyonderData.getSequence(player);
            if (sequence <= 5) grantAdvancement(player, "reach_sequence_5");
            if (sequence <= 3) grantAdvancement(player, "reach_sequence_3");
            if (sequence <= 1) grantAdvancement(player, "reach_sequence_1");
        }

        // Only run the heavier checks every 20 ticks to reduce performance impact
        if (player.tickCount % 20 != 0) return;

        checkStructureAdvancements(player);
        checkItemAdvancements(player);
        checkMysticalRingSummon(player);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        Entity killer = event.getSource().getEntity();

        // Award advancement for killing a rogue beyonder
        if (entity instanceof BeyonderNPCEntity
                && killer instanceof ServerPlayer player) {
            grantAdvancement(player, "kill_rogue_beyonder");

            int kills = player.getPersistentData().getInt("lotm_beyonder_kills") + 1;
            player.getPersistentData().putInt("lotm_beyonder_kills", kills);
            if (kills >= 10) {
                grantAdvancement(player, "kill_ten_beyonders");
            }
        }

        // Award advancement for dying as a beyonder
        if (entity instanceof ServerPlayer player && BeyonderData.isBeyonder(player)) {
            grantAdvancement(player, "die_as_beyonder");
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            grantAdvancement(player, "root");
        }
    }

    private static void checkStructureAdvancements(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (!isAdvancementDone(player, "enter_evernight_church")
                && isInStructure(level, player, "evernight_church")) {
            grantAdvancement(player, "enter_evernight_church");
        }
        if (!isAdvancementDone(player, "enter_blazing_sun_church")
                && isInStructure(level, player, "blazing_sun_church")) {
            grantAdvancement(player, "enter_blazing_sun_church");
        }
        if (!isAdvancementDone(player, "enter_red_priest_castle")
                && isInStructure(level, player, "red_priest_castle")) {
            grantAdvancement(player, "enter_red_priest_castle");
        }
        if (!isAdvancementDone(player, "discover_beyonder_house")
                && isInStructure(level, player, "beyonder_house")) {
            grantAdvancement(player, "discover_beyonder_house");
        }
    }

    private static boolean isInStructure(ServerLevel level, ServerPlayer player, String structureName) {
        ResourceKey<Structure> structureKey = ResourceKey.create(
                Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, structureName)
        );
        return level.registryAccess()
                .registry(Registries.STRUCTURE)
                .flatMap(registry -> registry.getHolder(structureKey))
                .map(holder -> {
                    StructureStart start = level.structureManager()
                            .getStructureWithPieceAt(player.blockPosition(), HolderSet.direct(holder));
                    return start != null && start.isValid();
                })
                .orElse(false);
    }

    private static void checkItemAdvancements(ServerPlayer player) {
        boolean needCharacteristic = !isAdvancementDone(player, "obtain_characteristic");
        boolean needRecipe = !isAdvancementDone(player, "obtain_recipe");
        boolean needArtifact = !isAdvancementDone(player, "obtain_sealed_artifact");

        if (!needCharacteristic && !needRecipe && !needArtifact) return;

        for (ItemStack stack : player.getInventory().items) {
            if (needCharacteristic && stack.getItem() instanceof BeyonderCharacteristicItem) {
                grantAdvancement(player, "obtain_characteristic");
                needCharacteristic = false;
            }
            if (needRecipe && stack.getItem() instanceof PotionRecipeItem) {
                grantAdvancement(player, "obtain_recipe");
                needRecipe = false;
            }
            if (needArtifact && stack.getItem() instanceof SealedArtifactItem) {
                grantAdvancement(player, "obtain_sealed_artifact");
                needArtifact = false;
            }
            if (!needCharacteristic && !needRecipe && !needArtifact) break;
        }
    }

    private static void checkMysticalRingSummon(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("lotm_summoned_beyonder_with_ring")) {
            grantAdvancement(player, "summon_beyonder_with_ring");
        }
    }

    public static void grantAdvancement(ServerPlayer player, String advancementPath) {
        if (player.getServer() == null) return;
        AdvancementHolder advancement = player.getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, advancementPath));
        if (advancement == null) return;
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
    }

    private static boolean isAdvancementDone(ServerPlayer player, String advancementPath) {
        if (player.getServer() == null) return false;
        AdvancementHolder advancement = player.getServer()
                .getAdvancements()
                .get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, advancementPath));
        if (advancement == null) return false;
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

}
