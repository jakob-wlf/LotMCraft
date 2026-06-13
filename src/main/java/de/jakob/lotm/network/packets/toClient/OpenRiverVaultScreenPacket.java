package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenRiverVaultScreenPacket(
        List<ItemStack> vaultItems,
        List<ItemStack> iuItems,
        int maxIU,
        int vaultCapacity
) implements CustomPacketPayload {

    public static final Type<OpenRiverVaultScreenPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_river_vault_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRiverVaultScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, OpenRiverVaultScreenPacket packet) {
            buf.writeInt(packet.vaultItems().size());
            for (ItemStack stack : packet.vaultItems()) {
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
            buf.writeInt(packet.iuItems().size());
            for (ItemStack stack : packet.iuItems()) {
                ItemStack.STREAM_CODEC.encode(buf, stack);
            }
            buf.writeInt(packet.maxIU());
            buf.writeInt(packet.vaultCapacity());
        }

        @Override
        public OpenRiverVaultScreenPacket decode(RegistryFriendlyByteBuf buf) {
            int vaultSize = buf.readInt();
            List<ItemStack> vaultItems = new ArrayList<>();
            for (int i = 0; i < vaultSize; i++) {
                vaultItems.add(ItemStack.STREAM_CODEC.decode(buf));
            }
            int iuSize = buf.readInt();
            List<ItemStack> iuItems = new ArrayList<>();
            for (int i = 0; i < iuSize; i++) {
                iuItems.add(ItemStack.STREAM_CODEC.decode(buf));
            }
            int maxIU = buf.readInt();
            int vaultCapacity = buf.readInt();
            return new OpenRiverVaultScreenPacket(vaultItems, iuItems, maxIU, vaultCapacity);
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenRiverVaultScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientHandler.handleOpenRiverVaultScreen(packet);
        });
    }
}
