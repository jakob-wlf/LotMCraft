package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.contract.builder.BuilderToken;
import de.jakob.lotm.beyonders.abilities.abyss.contract.builder.ContractBuilderConverter;
import de.jakob.lotm.beyonders.abilities.abyss.contract.builder.ContractTermKind;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SubmitContractBuilderPacket(List<TokenDTO> tokens, int signMode) implements CustomPacketPayload {
    public record TokenDTO(int kindOrdinal, String value) {}
    public static final Type<SubmitContractBuilderPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "submit_contract_builder"));
    private static final StreamCodec<RegistryFriendlyByteBuf, TokenDTO> TOKEN_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TokenDTO::kindOrdinal,
            ByteBufCodecs.STRING_UTF8, TokenDTO::value,
            TokenDTO::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SubmitContractBuilderPacket> STREAM_CODEC = StreamCodec.composite(
            TOKEN_CODEC.apply(ByteBufCodecs.list()), SubmitContractBuilderPacket::tokens,
            ByteBufCodecs.VAR_INT, SubmitContractBuilderPacket::signMode,
            SubmitContractBuilderPacket::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void handle(SubmitContractBuilderPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer caster)) return;
            if (packet.signMode() < 0 || packet.signMode() > 2) return;
            List<BuilderToken> tokens = new ArrayList<>();
            ContractTermKind[] kinds = ContractTermKind.values();
            for (TokenDTO dto : packet.tokens()) {
                if (dto.kindOrdinal() < 0 || dto.kindOrdinal() >= kinds.length) continue;
                tokens.add(new BuilderToken(kinds[dto.kindOrdinal()], dto.value()));
            }
            ContractBuilderConverter.submit(caster, tokens, packet.signMode()
            );
        });
    }
}