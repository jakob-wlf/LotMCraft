//package de.jakob.lotm.addons.loginCheck;
//
//import de.jakob.lotm.LOTMCraft;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ServerData;
//import net.minecraft.network.chat.Component;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
//
//import java.util.Locale;
//
//@EventBusSubscriber(modid = LOTMCraft.MOD_ID, value = Dist.CLIENT)
//public class LoginChecker {
//    @SubscribeEvent
//    public static void onClientJoin(ClientPlayerNetworkEvent.LoggingIn event) {
//        Minecraft mc = Minecraft.getInstance();
//
//        if (mc.hasSingleplayerServer() || mc.isLocalServer()) {
//            disconnect(mc, "This mod is only usable on the official server.");
//            return;
//        }
//
//        ServerData server = mc.getCurrentServer();
//
//        if (server == null) {
//            disconnect(mc, "Unauthorized server.");
//            return;
//        }
//
//        String ip = server.ip.toLowerCase(Locale.ROOT);
//
//        boolean allowed = ip.equals("167.235.88.206:2403")
//                || ip.equals("167.235.88.206:2309")
//                || ip.equals("167.235.88.206:2308")
//                || ip.equals("167.235.88.206:2307");
//
//        if (!allowed) {
//            disconnect(mc, "This mod only works on the official server");
//        }
//
//        mc.reloadResourcePacks();
//    }
//
//    private static void disconnect(Minecraft mc, String reason) {
//        mc.execute(() -> {
//            if (mc.player != null) {
//                mc.player.connection.disconnect(Component.literal(reason));
//            } else {
//                mc.stop();
//            }
//        });
//    }
//}
