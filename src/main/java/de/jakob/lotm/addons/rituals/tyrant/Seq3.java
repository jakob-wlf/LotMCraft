package de.jakob.lotm.addons.rituals.tyrant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.prophecy.TokenStream;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

import java.util.*;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq3 {
    private static final Map<UUID, Vec3> posMap = new HashMap<>();
    public static final String message = "I declare this sea as my property";
    private static final int NEED_AMOUNT = 4500;
    private static final int DISTANCE = 500;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(component.getStage() >= NEED_AMOUNT){
            component.setCompleted(true);
            posMap.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    private static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if(!BeyonderData.getPathway(player).equals("tyrant") ||
                BeyonderData.getSequence(player) != 4) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        if(component.getStage() != 0) return;
        if(component.isCompleted()) return;

        String msg = event.getRawText();

        if(message.equals(msg)){
            if(posMap.containsKey(player.getUUID())){
                player.sendSystemMessage(Component.literal("You already declared your territory\n")
                        .withStyle(ChatFormatting.DARK_RED));
                return;
            }

            Holder<Biome> biome = player.level().getBiome(player.blockPosition());
            if (!biome.is(BiomeTags.IS_OCEAN)) {
                player.sendSystemMessage(Component.literal("You have to be in the ocean\n")
                        .withStyle(ChatFormatting.DARK_RED));
                return;
            }

            posMap.put(player.getUUID(), player.position());
            component.setStage(1);
            player.sendSystemMessage(Component.literal("You have declared this part of the ocean as your domain")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    @SubscribeEvent
    private static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();
        var sourceEntity = event.getSource().getEntity();

        if(sourceEntity == null) return;

        if(!posMap.containsKey(sourceEntity.getUUID())) return;

        if(entity.distanceToSqr(posMap.get(sourceEntity.getUUID())) <= DISTANCE * DISTANCE){
            int value = 1;
            if(entity instanceof ServerPlayer){
                value = 100;
            }

            var component = sourceEntity.getData(ModAttachments.RITUALS.get());
            component.setStage(component.getStage() + value);
        }
    }

}
