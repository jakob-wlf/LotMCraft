package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.command.BeyonderCommand;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.client.*;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.entity.custom.FireRavenEntity;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.SpiritualityProgressTracker;
import de.jakob.lotm.villager.ModVillagers;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.*;

import static de.jakob.lotm.util.BeyonderData.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FlamingSpearProjectileModel.LAYER_LOCATION, FlamingSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(UnshadowedSpearProjectileModel.LAYER_LOCATION, UnshadowedSpearProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        event.registerLayerDefinition(WindBladeModel.LAYER_LOCATION, WindBladeModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeDoorModel.LAYER_LOCATION, ApprenticeDoorModel::createBodyLayer);
        event.registerLayerDefinition(TravelersDoorModel.LAYER_LOCATION, TravelersDoorModel::createBodyLayer);
        event.registerLayerDefinition(ApprenticeBookModel.LAYER_LOCATION, ApprenticeBookModel::createBodyLayer);
        event.registerLayerDefinition(PaperDaggerProjectileModel.LAYER_LOCATION, PaperDaggerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(FireRavenModel.LAYER_LOCATION, FireRavenModel::createBodyLayer);
        event.registerLayerDefinition(FrostSpearProjectileModel.LAYER_LOCATION, FrostSpearProjectileModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FIRE_RAVEN.get(), FireRavenEntity.createAttributes().build());
        event.put(ModEntities.BEYONDER_NPC.get(), BeyonderNPCEntity.createAttributes().build());
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(
                ModEntities.BEYONDER_NPC.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mob::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BeyonderCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        // Only copy data if the original player was a beyonder
        if (isBeyonder(original)) {
            String pathway = getPathway(original);
            int sequence = getSequence(original);
            boolean griefingEnabled = original.getPersistentData().getBoolean(NBT_GRIEFING_ENABLED);

            // Copy the data to the new player
            CompoundTag newTag = newPlayer.getPersistentData();
            newTag.putString(NBT_PATHWAY, pathway);
            newTag.putInt(NBT_SEQUENCE, sequence);
            newTag.putFloat(NBT_SPIRITUALITY, BeyonderData.getMaxSpirituality(sequence));
            newTag.putBoolean(NBT_GRIEFING_ENABLED, griefingEnabled);

            // Update spirituality progress tracker
            if (getMaxSpirituality(sequence) > 0) {
                float progress = 1;
                SpiritualityProgressTracker.setProgress(newPlayer.getUUID(), progress);
            }
        }
    }
}
