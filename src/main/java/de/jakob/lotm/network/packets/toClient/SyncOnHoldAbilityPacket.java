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

public record SyncOnHoldAbilityPacket(int entityId, String abilityId) implements CustomPacketPayload {
    
    public static final Type<SyncOnHoldAbilityPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "sync_on_hold_ability"));

    public static final StreamCodec<FriendlyByteBuf, SyncOnHoldAbilityPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    SyncOnHoldAbilityPacket::entityId,
                    ByteBufCodecs.STRING_UTF8,
                    SyncOnHoldAbilityPacket::abilityId,
                    SyncOnHoldAbilityPacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncOnHoldAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.entityId());
            Ability ability = LOTMCraft.abilityHandler.getById(packet.abilityId());
            if(!(entity instanceof LivingEntity living)) {
                return;
            }

            ability.onHold(living.level(), living);
        });
    }
}