package de.jakob.lotm.addons.rituals.door;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {
   public static Set<UUID> affected = new HashSet<>();

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!BeyonderData.getPathway(player).equals("door") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        if(!affected.contains(player.getUUID())){
            RitualEffectHandlerEvent.removeRitual(player);
            return;
        }

        if(component.getStage() != 2) {
            if (player.level().dimension()
                    == ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY) {
                component.setStage(1);
            } else {
                if (component.getStage() > 0) {
                    player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
                }
                component.setStage(0);
            }
        }

        var disabled = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT.get());
        if(component.getStage() == 1 && disabled.isAbilityUsageDisabled()){
            component.setStage(2);
        }
        else if(!disabled.isAbilityUsageDisabled()){
            player.sendSystemMessage(Component.literal("You lost your ritual progress!").withStyle(ChatFormatting.RED));
            component.setStage(0);
        }

        if(component.getStage() == 2 && player.level().dimension()
                != ModDimensions.CONCEALMENT_WORLD_DIMENSION_KEY){
            component.setCompleted(true);
            affected.remove(player.getUUID());
        }
    }

}
