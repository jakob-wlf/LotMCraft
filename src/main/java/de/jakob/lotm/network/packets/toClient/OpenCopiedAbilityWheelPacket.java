package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.gui.custom.CopiedAbilityWheel.CopiedAbilityWheelMenu;
import de.jakob.lotm.gui.custom.CopiedAbilityWheel.CopiedAbilityWheelScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenCopiedAbilityWheelPacket() implements CustomPacketPayload {

    public static final Type<OpenCopiedAbilityWheelPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_copied_ability_wheel"));

    public static final StreamCodec<ByteBuf, OpenCopiedAbilityWheelPacket> STREAM_CODEC = StreamCodec.unit(new OpenCopiedAbilityWheelPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenCopiedAbilityWheelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, inventory, p) -> new CopiedAbilityWheelMenu(id, inventory),
                        Component.translatable("lotm.copied_ability_wheel.title")
                ));
            }
        });
    }
}
