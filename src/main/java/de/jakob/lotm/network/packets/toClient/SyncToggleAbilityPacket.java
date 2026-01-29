package de.jakob.lotm.network.packets.toClient;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.ToggleAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncToggleAbilityPacket(int entityId, String abilityId, int action) implements CustomPacketPayload {
    
    public static final Type<SyncToggleAbilityPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_toggle_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncToggleAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SyncToggleAbilityPacket::entityId,
                    ByteBufCodecs.STRING_UTF8,
                    SyncToggleAbilityPacket::abilityId,
                    ByteBufCodecs.INT,
                    SyncToggleAbilityPacket::action,
                    SyncToggleAbilityPacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncToggleAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
            if(!(ability instanceof ToggleAbility toggleAbility) || !(entity instanceof LivingEntity living)) {
                return;
            }

            switch (packet.action) {
                case 0 -> {
                    toggleAbility.start(living.level(), living);
                    toggleAbility.updateClientCache(living, true);
                }
                case 1 -> toggleAbility.prepareTick(living.level(), living);
                case 2 -> {
                    toggleAbility.stop(living.level(), living);
                    toggleAbility.updateClientCache(living, true);
                }

            }
        });
    }

    public enum Action {
        START(0),
        TICK(1),
        STOP(2);

        private final int value;

        Action(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}