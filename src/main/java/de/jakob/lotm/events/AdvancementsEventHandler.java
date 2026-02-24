package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class AdvancementsEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if(BeyonderData.isBeyonder(player)) {
            AdvancementHolder advancement = player.getServer()
                    .getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "become_beyonder"));

            if(advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if(!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                }
            }

            String sequenceName = BeyonderData.pathwayInfos.get(BeyonderData.getPathway(player)).getRawSequenceName(BeyonderData.getSequence(player));

            advancement = player.getServer()
                    .getAdvancements()
                    .get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "become_" + sequenceName.toLowerCase()));

            if(advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if(!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }

}
