package de.jakob.lotm.addons.rituals.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq0 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 1) return;
        if(!(player.level() instanceof ServerLevel level)) return;
        if(!BeyonderData.hasUniqueness(player)) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var sanity = player.getData(ModAttachments.SANITY_COMPONENT.get());

        if(player.getHealth() > player.getMaxHealth() * 0.4f
        && component.getStage() != 2){
            component.setStage(0);
            return;
        }

        if (sanity.getSanity() <= 0.45f && component.getStage() == 1) {
            if(player.position().y <= -2000){
                component.setStage(2);
            }
        }

        if(player.position().y >= 30 && component.getStage() == 2){
            component.setCompleted(true);
        }
    }

    @SubscribeEvent
    private static void onDamage(LivingDamageEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 1) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.getStage() != 0) return;

        var source = event.getSource();
        if(!source.is(ModDamageTypes.DARKNESS)) return;

        var sourceEntity = source.getEntity();
        if(sourceEntity == null) return;
        if(!(sourceEntity instanceof ServerPlayer sourcePlayer)) return;

        if(BeyonderData.getPathway(sourcePlayer).equals("darkness") &&
                BeyonderData.hasUniqueness(sourcePlayer)) {
            if (player.getHealth() <= player.getMaxHealth() * 0.4f) {
                component.setStage(1);
            }
        }
    }

}
