package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.red_priest.ConjureAbility;
import de.jakob.lotm.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WhipSlashPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WhipSlashPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "whip_slash"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WhipSlashPayload> STREAM_CODEC =
            StreamCodec.unit(new WhipSlashPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WhipSlashPayload payload, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        ServerLevel level = player.serverLevel();
        if (!player.getMainHandItem().is(ModItems.CONJURED_WHIP)) return;

        Vec3 start = ConjureAbility.whipStart(player);
        double reach = 6.0;
        HitResult hit = player.pick(reach, 0.0F, false);
        Vec3 end = (hit.getType() != HitResult.Type.MISS)
                ? hit.getLocation()
                : start.add(player.getLookAngle().scale(reach));

        ConjureAbility.makeFireSlash(level, start, end);
    }
}