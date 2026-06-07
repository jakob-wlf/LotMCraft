package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.attachments.AbilityBarComponent;
import de.jakob.lotm.attachments.FoolingComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
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
            ServerPlayer player = (ServerPlayer) context.player();

            // Fooling effect: 25% chance to fail entirely, otherwise scramble to a random ability.
            if (player.getData(ModAttachments.FOOLING_COMPONENT).isFooled()) {
                if (new java.util.Random().nextFloat() < 0.25f) return;
                String pathway = BeyonderData.getPathway(player);
                int sequence   = BeyonderData.getSequence(player);
                Ability randomAbility = LOTMCraft.abilityHandler.getRandomAbility(pathway, sequence, new java.util.Random(), false, java.util.Collections.emptyList());
                if (randomAbility != null) {
                    randomAbility.useAbility(player.serverLevel(), player);
                }
                return;
            }

            AbilityBarComponent abilityBarComponent = player.getData(ModAttachments.ABILITY_BAR_COMPONENT);
            if (packet.selectedAbility() < 0 || packet.selectedAbility() >= abilityBarComponent.getAbilities().size()) {
                return;
            }
            String rawEntry = abilityBarComponent.getAbilities().get(packet.selectedAbility());

            // Soul abilities (from Internal Underworld) are prefixed with "soul|" and must
            // bypass the hasAbility check since they belong to a different pathway.
            boolean isSoulAbility = rawEntry.startsWith("soul|");
            String effectiveEntry = isSoulAbility ? rawEntry.substring(5) : rawEntry; // strip "soul|"

            Ability ability = LOTMCraft.abilityHandler.getById(effectiveEntry.split(":")[0]);
            if (ability == null) return;

            if(ability instanceof SelectableAbility && getIndex(effectiveEntry) != -1) {
                ((SelectableAbility) ability).addSubAbilityOverride(player, getIndex(effectiveEntry));
            }

            if (isSoulAbility) {
                // Cast as a soul ability: skip hasAbility and requirements checks.
                ability.useAbility(player.serverLevel(), player, true, false, false);
            } else {
                ability.useAbility(player.serverLevel(), player);
            }
        });
    }

    private static int getIndex(String s) {
        String[] parts = s.split(":");
        if (parts.length < 2) return -1;
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}