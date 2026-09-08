package de.jakob.lotm.addons.rituals.mixins;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

import static de.jakob.lotm.addons.rituals.visionary.Seq5.wakeUp;

@Mixin(SleepStatus.class)
public class VisionarySleepStatusMixin {

    @ModifyVariable(
            method = "update",
            at = @At("HEAD"),
            argsOnly = true
    )
    private List<ServerPlayer> removeRitualPlayerFromSleepCount(
            List<ServerPlayer> players
    ) {
        return players.stream()
                .filter(player -> !shouldIgnore(player))
                .toList();
    }

    private static boolean shouldIgnore(ServerPlayer player) {
        if (!BeyonderData.getPathway(player).equals("visionary")) {
            return false;
        }

        if (BeyonderData.getSequence(player) != 6) {
            return false;
        }

        var component = player.getData(ModAttachments.RITUALS.get());

        return !component.isCompleted()
                && player.hasEffect(ModEffects.ASLEEP)
                && !wakeUp.contains(player.getUUID());
    }
}