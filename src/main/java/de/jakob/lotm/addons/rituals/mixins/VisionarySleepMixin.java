package de.jakob.lotm.addons.rituals.mixins;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.jakob.lotm.addons.rituals.visionary.Seq5.wakeUp;

@Mixin(ServerGamePacketListenerImpl.class)
public class VisionarySleepMixin {
    @Inject(
            method = "handlePlayerCommand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventWakeUp(
            ServerboundPlayerCommandPacket packet,
            CallbackInfo ci
    ) {
        ServerGamePacketListenerImpl handler =
                (ServerGamePacketListenerImpl) (Object) this;

        ServerPlayer player = handler.player;

        if (packet.getAction() != ServerboundPlayerCommandPacket.Action.STOP_SLEEPING) {
            return;
        }

        if (!player.isSleeping()) {
            return;
        }

        if (BeyonderData.getPathway(player).equals("visionary")
                && BeyonderData.getSequence(player) == 6) {

            var component = player.getData(ModAttachments.RITUALS.get());

            if (!component.isCompleted()
                    && player.hasEffect(ModEffects.ASLEEP)
                    && !wakeUp.contains(player.getUUID())) {

                ci.cancel();
            }
        }
    }
}
