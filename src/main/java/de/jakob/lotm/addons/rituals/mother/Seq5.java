package de.jakob.lotm.addons.rituals.mother;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.LivingEntity;
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
public class Seq5 {
    private static final int NEED_AMOUNT = 1000;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 6) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if (component.isCompleted()) return;

        if (component.getStage() >= NEED_AMOUNT) {
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if(BeyonderData.isBeyonder(livingEntity)) return;

        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("mother") ||
                BeyonderData.getSequence(player) != 6) return;

        int value = 1;
        if(livingEntity instanceof ServerPlayer){
            value = 10;
        }

        var component = player.getData(ModAttachments.RITUALS.get());
        component.setStage(component.getStage() + value);

    }

}
