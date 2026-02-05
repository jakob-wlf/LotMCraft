package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.AbilityBarComponent;
import de.jakob.lotm.attachments.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseKeyboundAbilityPacket(int selectedAbility) implements CustomPacketPayload {
    public static final Type<UseKeyboundAbilityPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_keybound_ability"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UseKeyboundAbilityPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            UseKeyboundAbilityPacket::selectedAbility,
            UseKeyboundAbilityPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseKeyboundAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbilityBarComponent abilityBarComponent = context.player().getData(ModAttachments.ABILITY_BAR_COMPONENT);
            ServerPlayer player = (ServerPlayer) context.player();
            if(packet.selectedAbility() < 0 || packet.selectedAbility() >= abilityBarComponent.getAbilities().size()) {
                return;
            }
            Ability ability = LOTMCraft.abilityHandler.getById(abilityBarComponent.getAbilities().get(packet.selectedAbility()));
            ability.useAbility(player.serverLevel(), player);
        });
    }
}