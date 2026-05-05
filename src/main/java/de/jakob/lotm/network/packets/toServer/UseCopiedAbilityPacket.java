package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SelectedCopiedAbilityComponent;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            CopiedAbilityComponent copiedComponent = serverPlayer.getData(ModAttachments.COPIED_ABILITY_COMPONENT);
            CopiedAbilityComponent.CopiedAbilityData data = copiedComponent.getAbility(packet.abilityIndex());
            if (data == null) return;

            if (LOTMCraft.abilityHandler.getById(data.abilityId()) == null) return;

            // Add to wheel (allowing duplicates) and select it
            AbilityWheelComponent wheelComponent = serverPlayer.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            wheelComponent.getAbilities().add(data.abilityId());
            int newIndex = wheelComponent.getAbilities().size() - 1;
            wheelComponent.setSelectedAbility(newIndex);
            AbilityWheelHelper.syncToClient(serverPlayer);

            // Store which copied ability index this maps to for decrement after use
            SelectedCopiedAbilityComponent selected = serverPlayer.getData(ModAttachments.SELECTED_COPIED_ABILITY_COMPONENT);
            selected.setSelection(packet.abilityIndex(), data.remainingUses());
        });
    }
}
