package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.FoolCardData;
import de.jakob.lotm.events.FoolCardItemHandler;
import de.jakob.lotm.events.GreySealEventHandler;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.beyonders.sefirah.SefirahHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * Client → Server: the Sefirah Castle owner activates one of the three Grey Fog powers.
 *
 * action 0 — Seal Surroundings:    400-block barrier ring + fog + teleport lock.
 * action 1 — Apply Anti-Divination: paper → tracked Fool Card (global uniqueness, 48 h cooldown if destroyed).
 * action 2 — Fool Negative Effects: swap all effects (bad→good, good→bad) on every entity in 400 blocks.
 */
public record GreyFogActionPacket(int action) implements CustomPacketPayload {

    public static final int SEAL            = 0;
    public static final int ANTI_DIVINATION = 1;
    public static final int FOOL_EFFECTS    = 2;

    public static final Type<GreyFogActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "grey_fog_action"));

    public static final StreamCodec<ByteBuf, GreyFogActionPacket> STREAM_CODEC =
            ByteBufCodecs.INT.map(GreyFogActionPacket::new, GreyFogActionPacket::action);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(GreyFogActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer caster)) return;
            if (!"sefirah_castle".equals(SefirahHandler.getClaimedSefirot(caster))) return;
            if (!(caster.level() instanceof ServerLevel level)) return;

            switch (packet.action()) {
                case SEAL            -> applySeal(caster, level);
                case ANTI_DIVINATION -> applyAntiDivination(caster);
                case FOOL_EFFECTS    -> applyFoolEffects(caster, level);
            }
        });
    }

    // ── Seal Surroundings ─────────────────────────────────────────────────────

    private static void applySeal(ServerPlayer caster, ServerLevel level) {
        if (GreySealEventHandler.isSealActive()) {
            // Owner toggles off — cooldown handled inside deactivate()
            GreySealEventHandler.activate(caster, level); // will toggle off
            return;
        }
        if (GreySealEventHandler.isOnCooldown()) {
            long ms = GreySealEventHandler.getCooldownRemainingMs();
            long h  = ms / 3_600_000;
            long m  = (ms % 3_600_000) / 60_000;
            caster.sendSystemMessage(Component.literal(
                    "§cSeal Surroundings is on cooldown for " + h + "h " + m + "m."));
            return;
        }
        GreySealEventHandler.activate(caster, level);
    }

    // ── Apply Anti-Divination → Fool Card ────────────────────────────────────

    private static void applyAntiDivination(ServerPlayer caster) {
        FoolCardData data = FoolCardData.get(caster.getServer());

        if (!data.canCreate()) {
            if (data.hasCard()) {
                caster.sendSystemMessage(Component.literal(
                        "§cThe Fool Card already exists somewhere in the world."));
            } else {
                long ms   = data.getCooldownRemainingMs();
                long h    = ms / 3_600_000;
                long m    = (ms % 3_600_000) / 60_000;
                caster.sendSystemMessage(Component.literal(
                        "§cThe Fool Card is on cooldown for " + h + "h " + m + "m."));
            }
            return;
        }

        ItemStack hand = caster.getItemInHand(InteractionHand.MAIN_HAND);
        if (!hand.is(Items.PAPER)) {
            caster.sendSystemMessage(Component.literal(
                    "§cYou must hold a piece of paper in your main hand."));
            return;
        }

        // Convert paper to a tracked Fool Card
        hand.shrink(1);
        ItemStack card = new ItemStack(ModItems.FOOL_Card.get());
        UUID cardId = UUID.randomUUID();
        FoolCardItemHandler.setCardId(card, cardId);
        caster.addItem(card);

        data.registerCard(cardId, caster.level().getGameTime());
        caster.sendSystemMessage(Component.literal(
                "§8[Grey Fog] §7A Fool Card has been woven from the grey mist."));
    }

    // ── Fool Effects (persistent 1-minute aura) ──────────────────────────────

    private static void applyFoolEffects(ServerPlayer caster, ServerLevel level) {
        GreySealEventHandler.activateFoolEffects(caster, level);
    }
}
