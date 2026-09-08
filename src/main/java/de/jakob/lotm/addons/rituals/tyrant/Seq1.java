package de.jakob.lotm.addons.rituals.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq1 {
    private static final int NEED_AMOUNT = 3;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() >= NEED_AMOUNT){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        var sourceEntity = event.getSource().getEntity();

        if(sourceEntity == null) return;
        if(!(sourceEntity instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 2) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!(entity instanceof ServerPlayer target)) return;
        int seq = BeyonderData.getSequence(entity);
        int value = seq <= 2? (seq == 2 ? 1 : (seq == 1 ? 2 : 3)) : 0;

        component.setStage(component.getStage() + value);
    }

}
