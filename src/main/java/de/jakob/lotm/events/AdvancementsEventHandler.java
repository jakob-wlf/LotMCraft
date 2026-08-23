package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.artifacts.SealedArtifactItem;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

            if (BeyonderData.pathwayInfos.get(BeyonderData.getPathway(player)) != null) {
                String sequenceName = BeyonderData.pathwayInfos.get(BeyonderData.getPathway(player)).getRawSequenceName(BeyonderData.getSequence(player));
                grantAdvancement(player, "become_" + sequenceName.toLowerCase());
            } else {
                LOTMCraft.LOGGER.error("Advancement Error: Missing PathwayInfo for player '{}'. Pathway: '{}', Sequence: {}",
                        player.getName().getString(),
                        BeyonderData.getPathway(player),
                        BeyonderData.getSequence(player)
                );
            }

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
        if (entity instanceof BeyonderNPCEntity deadBeyonder
                && killer instanceof ServerPlayer player) {
            grantAdvancement(player, "kill_rogue_beyonder");

            int kills = player.getPersistentData().getInt("lotm_beyonder_kills") + 1;
            player.getPersistentData().putInt("lotm_beyonder_kills", kills);
            if (kills >= 10) {
                grantAdvancement(player, "kill_ten_beyonders");
            }

            // Chance to spawn an invisible ghost of the rogue beyonder, capturable via Internal Underworld
            maybeSpawnGhostBeyonder(deadBeyonder);
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

    // Chance to spawn an invisible ghost of a killed rogue beyonder, only visible (glowing) to a
    // player looking at it with Eye of Death, and capturable via Internal Underworld
    private static final float GHOST_BEYONDER_CHANCE = 1.0f;
    private static final String GHOST_BEYONDER_TAG = "lotm_ghost_beyonder";

    public static boolean isGhostBeyonder(net.minecraft.world.entity.Entity entity) {
        return entity.getPersistentData().getBoolean(GHOST_BEYONDER_TAG);
    }

    private static void maybeSpawnGhostBeyonder(BeyonderNPCEntity deadBeyonder) {
        if (GHOST_BEYONDER_CHANCE <= 0f) return;
        if (isGhostBeyonder(deadBeyonder)) return;
        if (!(deadBeyonder.level() instanceof ServerLevel serverLevel)) return;
        if (deadBeyonder.getRandom().nextFloat() >= GHOST_BEYONDER_CHANCE) return;

        BeyonderNPCEntity ghost = new BeyonderNPCEntity(
                ModEntities.BEYONDER_NPC.get(),
                serverLevel,
                false,
                deadBeyonder.getSkinName(),
                deadBeyonder.getPathway(),
                deadBeyonder.get_sequence(),
                false,
                false
        );
        ghost.setPos(deadBeyonder.position());
        ghost.getPersistentData().putBoolean(GHOST_BEYONDER_TAG, true);
        ghost.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, false, false));
        serverLevel.addFreshEntity(ghost);
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
