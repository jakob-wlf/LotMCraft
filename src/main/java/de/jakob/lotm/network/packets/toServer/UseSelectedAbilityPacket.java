package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.attachments.AbilityWheelComponent;
import de.jakob.lotm.attachments.ModAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseSelectedAbilityPacket() implements CustomPacketPayload {

    public static final Type<UseSelectedAbilityPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "use_selected_ability"));

    public static final StreamCodec<ByteBuf, UseSelectedAbilityPacket> STREAM_CODEC = StreamCodec.unit(new UseSelectedAbilityPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseSelectedAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbilityWheelComponent component = serverPlayer.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
                
                if (component.getAbilities().isEmpty()) {
                    return;
                }
                
                int selectedIndex = component.getSelectedAbility();
                if (selectedIndex >= 0 && selectedIndex < component.getAbilities().size()) {
                    String abilityId = component.getAbilities().get(selectedIndex);
                    Ability ability = LOTMCraft.abilityHandler.getById(abilityId);
                    
                    if (ability != null && serverPlayer.level() instanceof ServerLevel serverLevel) {
                        ability.useAbility(serverLevel, serverPlayer);
                    }
                }
            }
        });
    }
}