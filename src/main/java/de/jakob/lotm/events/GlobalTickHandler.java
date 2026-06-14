package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.attachments.DoorAuthorityData;
import de.jakob.lotm.attachments.SealedDimensionData;
import de.jakob.lotm.attachments.ActiveBlessingComponent;
import de.jakob.lotm.attachments.AnchorComponent;
import de.jakob.lotm.attachments.CorruptionComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncAnchorsPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class GlobalTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Only process once per second (20 ticks) to save resources
        if (player.level().getGameTime() % 20 != 0) {
            return;
        }

        AnchorComponent anchorComp = player.getData(ModAttachments.ANCHOR_COMPONENT);
        Map<UUID, Float> anchors = anchorComp.getAnchors();
        if (anchors.isEmpty()) {
            return;
        }

        int passiveDecreaseVal = player.level().getGameRules().getInt(ModGameRules.ANCHOR_PASSIVE_CORRUPTION_DECREASE);
        float totalPassiveDecrease = 0;

        Iterator<Map.Entry<UUID, Float>> iterator = anchors.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Float> entry = iterator.next();
            UUID anchorUUID = entry.getKey();
            float strength = entry.getValue();

            // Passive corruption decrease contribution
            totalPassiveDecrease += (strength * passiveDecreaseVal) / 100000f;

            // Anchor decay: 1% per second if anchor is not praying
            // Since strength is updated to 1.0 on prayer, it takes 100 seconds to fully decay
            // if we decrease by 0.01 every second.
            float newStrength = strength - 0.000001f;
            if (newStrength <= 0) {
                iterator.remove();
            } else {
                entry.setValue(newStrength);
            }
        }

        if (totalPassiveDecrease > 0) {
            CorruptionComponent corruptionComp = player.getData(ModAttachments.CORRUPTION_COMPONENT);
            corruptionComp.decreaseCorruptionAndSync(totalPassiveDecrease, player);
        }

        // Handle Blessing/Disabled Characteristic Ticks
        ActiveBlessingComponent activeBlessingComp = player.getData(ModAttachments.ACTIVE_BLESSING_COMPONENT);
        if (!activeBlessingComp.getDisabledCharacteristics().isEmpty()) {
            List<de.jakob.lotm.util.DisabledCharacteristic> expired = activeBlessingComp.getDisabledCharacteristics().stream()
                    .filter(dc -> dc.ticksLeft() <= 20)
                    .toList();

            activeBlessingComp.tick();

            if (!expired.isEmpty()) {
                de.jakob.lotm.attachments.BeyonderComponent beyonder = player.getData(ModAttachments.BEYONDER_COMPONENT);
                for (de.jakob.lotm.util.DisabledCharacteristic dc : expired) {
                    beyonder.adjustDisabledStacks(dc.pathway(), dc.sequence(), -1);
                }
                PacketHandler.syncBeyonderDataToPlayer(player);
            }
        }

        // Handle Received Blessings Ticks
        de.jakob.lotm.attachments.ReceivedBlessingComponent receivedBlessingComp = player.getData(ModAttachments.RECEIVED_BLESSING_COMPONENT);
        if (!receivedBlessingComp.getBlessings().isEmpty()) {
            receivedBlessingComp.tick();
            // We might need to sync if something changed, but passives/abilities check every tick or on change
        }

        // Always sync anchors to ensure client has latest decay info
        PacketHandler.sendToPlayer(player, new SyncAnchorsPacket(anchors));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if(event.getLevel().isClientSide()) {
            return;
        }

        InteractionHandler.cleanupInteractions();
        DoorAuthorityData doorData = DoorAuthorityData.get((ServerLevel) event.getLevel());

        if (doorData.isActive()) {
            doorData.tick();
        }

        SealedDimensionData sealedDimensionData = SealedDimensionData.get((ServerLevel) event.getLevel());

        if (sealedDimensionData.isActive()) {
            sealedDimensionData.tick();
        }
    }

}
