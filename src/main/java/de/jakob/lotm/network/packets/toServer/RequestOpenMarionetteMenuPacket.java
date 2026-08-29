package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.MarionetteOwnerComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.gui.custom.marionettes.MarionetteMenuProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RequestOpenMarionetteMenuPacket() implements CustomPacketPayload {

    public static final Type<RequestOpenMarionetteMenuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "request_open_marionette_menu"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestOpenMarionetteMenuPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestOpenMarionetteMenuPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RequestOpenMarionetteMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;
            ServerLevel level = serverPlayer.serverLevel();

            MarionetteOwnerComponent ownerData = serverPlayer.getData(ModAttachments.MARIONETTE_OWNER_COMPONENT);

            List<LivingEntity> resolvedEntities = new ArrayList<>();
            for (UUID marionetteUUID : ownerData.getMarionettes()) {
                Entity entity = level.getEntity(marionetteUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    resolvedEntities.add(livingEntity);
                }
            }

            open(serverPlayer, resolvedEntities);
        });
    }

    private static void open(ServerPlayer player, List<LivingEntity> marionettes) {
        player.openMenu(
                new MarionetteMenuProvider(marionettes),
                buf -> {
                    buf.writeVarInt(marionettes.size());
                    for (LivingEntity entity : marionettes) {
                        buf.writeVarInt(entity.getId());
                    }
                }
        );
    }
}