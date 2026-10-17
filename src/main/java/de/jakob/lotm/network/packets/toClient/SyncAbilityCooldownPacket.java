package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.data.ClientAbilityCooldowns;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Tells the owning client that an ability went on cooldown, so the HUD can display it.
 * An empty abilityId clears all cooldowns (e.g. /reset_cooldowns).
 */
public record SyncAbilityCooldownPacket(String abilityId, int ticks) implements CustomPacketPayload {
    public static final Type<SyncAbilityCooldownPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_ability_cooldown"));

    public static final StreamCodec<ByteBuf, SyncAbilityCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SyncAbilityCooldownPacket::abilityId,
            ByteBufCodecs.VAR_INT,
            SyncAbilityCooldownPacket::ticks,
            SyncAbilityCooldownPacket::new
    );

    public static SyncAbilityCooldownPacket clearAll() {
        return new SyncAbilityCooldownPacket("", 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncAbilityCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.abilityId.isEmpty()) {
                ClientAbilityCooldowns.clear();
            } else {
                ClientAbilityCooldowns.set(packet.abilityId, packet.ticks);
            }
        });
    }
}
