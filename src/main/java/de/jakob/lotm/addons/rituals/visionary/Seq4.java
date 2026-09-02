package de.jakob.lotm.addons.rituals.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class Seq4 {
    private static final int AMOUNT = 3;
    private static final float PERCENT = 0.4f;

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!(player.level() instanceof ServerLevel level))return;
        if(!BeyonderData.getPathway(player).equals("visionary") ||
                BeyonderData.getSequence(player) != 5) return;

        var component = player.getData(ModAttachments.RITUALS.get());
        if(component.isCompleted()) return;

        var nearby = AbilityUtil.getNearbyEntities(player, level, player.position(), 120)
                .stream().filter(obj -> obj instanceof ServerPlayer target
                        && BeyonderData.getSequence(target) <= 7
                        && !VisionaryHandler.isInvisible(target))
                .toList();

        int online = level.players().size();

        if(nearby.size() >= AMOUNT && nearby.size() >= (online * PERCENT)){
            component.setCompleted(true);
        }

    }


}
