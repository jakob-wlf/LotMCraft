package de.jakob.lotm.addons.rituals.mixins;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(ServerGamePacketListenerImpl.class)
public class VisionarySleepMixin {
//    @Inject(
//            method = "handlePlayerCommand",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void preventWakeUp(
//            ServerboundPlayerCommandPacket packet,
//            CallbackInfo ci
//    ) {
//        ServerGamePacketListenerImpl handler =
//                (ServerGamePacketListenerImpl) (Object) this;
//
//        ServerPlayer player = handler.player;
//
//        if (packet.getAction() != ServerboundPlayerCommandPacket.Action.STOP_SLEEPING) {
//            return;
//        }
//
//        if (!player.isSleeping()) {
//            return;
//        }
//
//        if (BeyonderData.getPathway(player).equals("visionary")
//                && BeyonderData.getSequence(player) == 6) {
//
//            var component = player.getData(ModAttachments.RITUALS.get());
//
//            if (!component.isCompleted()
//                    && player.hasEffect(ModEffects.ASLEEP)
//                    && !wakeUp.contains(player.getUUID())) {
//
//                ci.cancel();
//            }
//        }
//    }
}
