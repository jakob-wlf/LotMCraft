package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.fool.PuppeteeringAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record MassPuppeteeringSelectedEntitiesPacket(Map<Integer, Integer> entityIds) implements CustomPacketPayload {
    public static final Type<MassPuppeteeringSelectedEntitiesPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mass_puppeteering_selected_entities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MassPuppeteeringSelectedEntitiesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.INT, ByteBufCodecs.INT),
                    MassPuppeteeringSelectedEntitiesPacket::entityIds,
                    MassPuppeteeringSelectedEntitiesPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MassPuppeteeringSelectedEntitiesPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                Level level = player.level();
                Map<LivingEntity, Integer> selectedEntitiesWithTimes = new HashMap<>();

                for (Map.Entry<Integer, Integer> entry : packet.entityIds().entrySet()) {
                    Entity entity = level.getEntity(entry.getKey());
                    if (entity instanceof LivingEntity living && living.isAlive()) {
                        selectedEntitiesWithTimes.put(living, entry.getValue());
                    }
                }
                PuppeteeringAbility ability = (PuppeteeringAbility) LOTMCraft.abilityHandler.getById("puppeteering_ability");
                ability.executeMassPuppeteering(level, player, selectedEntitiesWithTimes);
            }
        });
    }
}
