package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.NewPlayerComponent;
import de.jakob.lotm.gamerule.ModGameRules;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.BeyonderCharacteristicItem;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Client → Server: the player accepted a characteristic from the slot-roll screen,
 * or requests a reroll.
 *
 * Actions:
 *   ACCEPT  (0) — accept the currently rolled characteristic (pathway sent)
 *   REROLL  (1) — request another spin (server sends a new OpenCharSlotRollPacket)
 */
public record CharSlotRollResultPacket(int action, String pathway) implements CustomPacketPayload {

    public static final int ACCEPT = 0;
    public static final int REROLL = 1;

    public static final Type<CharSlotRollResultPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "char_slot_roll_result"));

    public static final StreamCodec<ByteBuf, CharSlotRollResultPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,    CharSlotRollResultPacket::action,
            ByteBufCodecs.STRING_UTF8, CharSlotRollResultPacket::pathway,
            CharSlotRollResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    /** Remaining rerolls stored in NBT on the player. */
    private static final String NBT_REROLLS = "charSlotRollsLeft";
    /** Maximum rerolls (3 total spins → first is free, then 2 rerolls). */
    public static final int MAX_REROLLS = 2;

    public static void handle(CharSlotRollResultPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().getReceptionSide().isServer()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Guard: gamerule must be on and player must not have received perks yet
            if (!player.serverLevel().getGameRules().getBoolean(ModGameRules.DO_CHARACTERISTICS_SLOTS)) return;
            NewPlayerComponent comp = player.getData(ModAttachments.BOOK_COMPONENT);
            if (comp.isHasReceivedNewPlayerPerks()) return;

            if (packet.action() == ACCEPT) {
                giveAcceptedChar(player, packet.pathway(), comp);
            } else if (packet.action() == REROLL) {
                handleReroll(player);
            }
        });
    }

    private static void giveAcceptedChar(ServerPlayer player, String pathway, NewPlayerComponent comp) {
        Item characteristic = BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, 9);
        Item recipe          = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, 9);
        if (characteristic != null) player.addItem(new ItemStack(characteristic));
        if (recipe          != null) player.addItem(new ItemStack(recipe));
        player.addItem(new ItemStack(ModItems.GUIDING_BOOK.get()));
        comp.setHasReceivedNewPlayerPerks(true);
        // Clear the reroll counter
        player.getPersistentData().remove(NBT_REROLLS);
        player.sendSystemMessage(Component.literal("§aYour characteristic has been sealed into your fate."));
    }

    private static void handleReroll(ServerPlayer player) {
        int rerollsLeft = player.getPersistentData().getInt(NBT_REROLLS);
        if (rerollsLeft <= 0) {
            // No rerolls left — the client button should prevent this, but as a safety net
            // resend the roll packet so the screen isn't left stuck open. Do NOT mark perks
            // received here because the player hasn't accepted yet.
            de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                    buildRollPacket(player, 0));
            return;
        }
        rerollsLeft--;
        player.getPersistentData().putInt(NBT_REROLLS, rerollsLeft);

        // Send a fresh roll packet
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                buildRollPacket(player, rerollsLeft));
    }

    /** Builds an OpenCharSlotRollPacket with all seq-9 chars and current rerolls remaining. */
    public static de.jakob.lotm.network.packets.toClient.OpenCharSlotRollPacket buildRollPacket(
            ServerPlayer player, int rerollsLeft) {
        List<String> pathways  = new ArrayList<>();
        List<String> charNames = new ArrayList<>();

        for (String pathway : BeyonderData.implementedPathways) {
            BeyonderCharacteristicItem item =
                    BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, 9);
            if (item == null) continue;
            pathways.add(pathway);
            charNames.add(item.getDefaultInstance().getHoverName().getString());
        }

        return new de.jakob.lotm.network.packets.toClient.OpenCharSlotRollPacket(
                pathways, charNames, rerollsLeft);
    }

    /** Called from PlayerEvents when the gamerule is active for a new player. */
    public static void initiateRollForNewPlayer(ServerPlayer player) {
        // Guard: if perks were already received (e.g. event fired twice in a modpack,
        // or a previous session saved the flag), do not re-open the wheel.
        NewPlayerComponent comp = player.getData(ModAttachments.BOOK_COMPONENT);
        if (comp.isHasReceivedNewPlayerPerks()) return;

        // Guard: NBT_REROLLS being present means the wheel packet was already sent this
        // session (e.g. PlayerLoggedInEvent fired again before the player accepted).
        // Skip the second send; the first screen is still open on the client.
        if (player.getPersistentData().contains(NBT_REROLLS)) return;

        player.getPersistentData().putInt(NBT_REROLLS, MAX_REROLLS);
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                buildRollPacket(player, MAX_REROLLS));
    }
}
