package de.jakob.lotm.util.beyonderMap;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record PendingPrayer(UUID senderUUID, String senderName, String senderPathway, int senderSequence,
                             double x, double y, double z) {

    public static final int MAX_NAME_LENGTH = 64;
    public static final int MAX_PATHWAY_LENGTH = 64;

    public static PendingPrayer fromNetwork(FriendlyByteBuf buf) {
        return new PendingPrayer(
                buf.readUUID(),
                buf.readUtf(MAX_NAME_LENGTH),
                buf.readUtf(MAX_PATHWAY_LENGTH),
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(senderUUID);
        buf.writeUtf(senderName, MAX_NAME_LENGTH);
        buf.writeUtf(senderPathway, MAX_PATHWAY_LENGTH);
        buf.writeInt(senderSequence);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }
}
