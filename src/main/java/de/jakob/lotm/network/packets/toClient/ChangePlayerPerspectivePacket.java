package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChangePlayerPerspectivePacket(int entityId, int perspective) implements CustomPacketPayload {
    
    public static final Type<ChangePlayerPerspectivePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "change_perspective"));

    public static final StreamCodec<FriendlyByteBuf, ChangePlayerPerspectivePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ChangePlayerPerspectivePacket::entityId,
                    ByteBufCodecs.INT,
                    ChangePlayerPerspectivePacket::perspective,
                    ChangePlayerPerspectivePacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ChangePlayerPerspectivePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            if(!(entity instanceof LivingEntity living)) {
                return;
            }

            switch (packet.perspective) {
                case 0 -> ClientHandler.changeToFirstPerson(living);
                case 1 -> ClientHandler.changeToThirdPerson(living);

            }
        });
    }

    public enum PERSPECTIVE {
        FIRST(0),
        THIRD(1);

        private final int value;

        PERSPECTIVE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}