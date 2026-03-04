package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseCopiedAbilityPacket(int abilityIndex) implements CustomPacketPayload {

    public static final Type<UseCopiedAbilityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_copied_ability"));

    public static final StreamCodec<ByteBuf, UseCopiedAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            UseCopiedAbilityPacket::abilityIndex,
            UseCopiedAbilityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseCopiedAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                CopiedAbilityComponent component = serverPlayer.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
                CopiedAbilityComponent.CopiedAbilityData data = component.getAbility(packet.abilityIndex());

                if (data == null) return;

                Ability ability = LOTMCraft.abilityHandler.getById(data.abilityId());
                if (ability == null) return;

                if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                    ability.useAbility(serverLevel, serverPlayer, true, false, false);

                    component.decrementUses(packet.abilityIndex());

                    CopiedAbilityHelper.syncToClient(serverPlayer);
                }
            }
        });
    }
}
