package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.artifacts.SealedArtifactData;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.BeyonderPotion;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.beyonderMap.StoredData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Objects;

import static de.jakob.lotm.util.BeyonderData.beyonderMap;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BeyonderEventHandler {

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Sync beyonder data when player joins
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);

            if (!beyonderMap.contains(serverPlayer)) {
                beyonderMap.put(serverPlayer);
            } else {
                StoredData data = beyonderMap.get(serverPlayer).get();

                // Only restore from map if player has NO beyonder data (data loss scenario)
                // Or when marked to do so by server admin
                if (!BeyonderData.isBeyonder(serverPlayer) || data.modified()) {
                    BeyonderData.setBeyonder(serverPlayer, data.pathway(), data.sequence());
                    beyonderMap.markModified(serverPlayer, false);

                } else if (beyonderMap.isDiffPathSeq(serverPlayer)) {
                    // If they have data but it differs, update the map to match NBT (NBT is source of truth)
                    beyonderMap.put(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onTotemUsed(LivingUseTotemEvent event) {
        LivingEntity entity = event.getEntity();

        if(BeyonderData.isBeyonder(entity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Re-sync data when changing dimensions
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            if (!AbilityUtil.mayDamage(source, event.getEntity())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // Re-sync data on respawn
            PacketHandler.syncBeyonderDataToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            // Clear client cache when player logs out
            ClientBeyonderCache.removePlayer(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!BeyonderData.isBeyonder(player)) return;
        if (!player.serverLevel().getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)) return;

        var stack = new ItemStack(Objects.requireNonNull(BeyonderCharacteristicItemHandler
                .selectCharacteristicOfPathwayAndSequence(
                        BeyonderData.getPathway(player), BeyonderData.getSequence(player))).asItem());

        ItemEntity itemEntity = new ItemEntity(
                player.level(),
                player.getX(),
                player.getY(),
                player.getZ(),
                stack
        );

        if(beyonderMap.get(player).isEmpty()) return;

        var data = beyonderMap.get(player).get();
        BeyonderData.setBeyonder(player, data.pathway(), data.sequence());

        event.getDrops().add(itemEntity);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {

            if (!BeyonderData.isBeyonder(player)) return;
            if(beyonderMap.get(player).isEmpty()) return;
            if (!player.level().getGameRules().getBoolean(ModGameRules.REGRESS_SEQUENCE_ON_DEATH)) return;

            StoredData data = beyonderMap.get(player).get().regressSeq();

            beyonderMap.put(player, data);

            if (Objects.equals(data.sequence(), LOTMCraft.NON_BEYONDER_SEQ)) {
                ClientBeyonderCache.removePlayer(player.getUUID());
            } else
                ClientBeyonderCache.updateData(player.getUUID(), data.pathway(), data.sequence(),
                        0.0f, false, true, 0.0f);
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity().level().isClientSide()) return;

        var player = event.getEntity();

        Objects.requireNonNull(player.getServer()).execute(() -> {
            var container = event.getContainer();

            for (Slot slot : container.slots) {
                ItemStack stack = slot.getItem();
                if (stack.isEmpty()) continue;

                Item item = stack.getItem();

                if (item instanceof PotionIngredient obj) {
                    for (var path : obj.getPathways()) {
                        if (!BeyonderData.beyonderMap.check(path, obj.getSequence())) {
                            slot.set(ItemStack.EMPTY);
                            break;
                        }
                    }
                }

                else if (item instanceof BeyonderPotion potion) {
                    if (!BeyonderData.beyonderMap.check(
                            potion.getPathway(), potion.getSequence())) {
                        slot.set(ItemStack.EMPTY);
                    }
                }

                else if (item instanceof BeyonderCharacteristicItem cha) {
                    if (!BeyonderData.beyonderMap.check(
                            cha.getPathway(), cha.getSequence())) {
                        slot.set(ItemStack.EMPTY);
                    }
                }

                else{
                    SealedArtifactData data = stack.get(ModDataComponents.SEALED_ARTIFACT_DATA);
                    if (data != null
                            && !beyonderMap.check(data.pathway(), data.sequence())) {
                        slot.set(ItemStack.EMPTY);
                    }
                }
            }

            container.broadcastChanges();
        });

    }

}