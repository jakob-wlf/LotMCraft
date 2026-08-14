package de.jakob.lotm.addons.rituals.sun;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.addons.rituals.RitualEffectHandlerEvent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
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
public class Seq5 {

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        if(!BeyonderData.getPathway(player).equals("sun") ||
                BeyonderData.getSequence(player) != 6) return;
        if(!(player.level() instanceof ServerLevel level)) return;

        var component = player.getData(ModAttachments.RITUALS.get());

        BlockPos pos = player.blockPosition();

        if (level.getMaxLocalRawBrightness(pos) != 0) {
            if(component.isCompleted())
                RitualEffectHandlerEvent.removeRitualWithMessage(player);
            return;
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || direction == Direction.UP) {
                continue;
            }

            BlockPos adjacent = pos.relative(direction);

            if (!level.getBlockState(adjacent).is(Blocks.PACKED_ICE)) {
                if (component.isCompleted()) {
                    RitualEffectHandlerEvent.removeRitualWithMessage(player);
                }
                return;
            }
        }

        component.setCompleted(true);
    }

}
