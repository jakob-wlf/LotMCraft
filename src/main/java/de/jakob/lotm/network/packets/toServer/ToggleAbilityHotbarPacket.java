package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityHotbarManager;
import de.jakob.lotm.attachments.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleAbilityHotbarPacket(boolean toggle) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleAbilityHotbarPacket> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "toggle_ability_hotbar"));
    
    public static final StreamCodec<ByteBuf, ToggleAbilityHotbarPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
            (buf, value) -> buf.writeBoolean(value),
            buf -> buf.readBoolean()
        ),
        ToggleAbilityHotbarPacket::toggle,
        ToggleAbilityHotbarPacket::new
    );
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ToggleAbilityHotbarPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AbilityHotbarManager manager = player.getData(ModAttachments.ABILITY_HOTBAR);
                
                if (packet.toggle) {
                    manager.toggleAbilityHotbar(player);
                } else {
                    manager.cycleAbilityHotbar(player);
                }
                
                player.inventoryMenu.broadcastChanges();
            }
        });
    }
}