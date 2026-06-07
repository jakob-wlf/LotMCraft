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
        // Sync inventory to client immediately so the items appear without needing to re-open inventory.
        player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal("§aYour characteristic has been sealed into your fate."));
    }

    private static void handleReroll(ServerPlayer player) {
        int rerollsLeft = player.getPersistentData().getInt(NBT_REROLLS);
        if (rerollsLeft <= 0) {
            // No rerolls left. Do NOT resend the roll screen — doing so would allow
            // the Konami easter-egg to grant unlimited rerolls by resetting the screen
            // (and its konamiUsed counter) every time. The client's spinning animation
            // was already started locally; it will finish and leave the Accept button
            // active so the player can still accept their roll.
            return;
        }
        rerollsLeft--;
        player.getPersistentData().putInt(NBT_REROLLS, rerollsLeft);

        // Send a fresh roll packet
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                buildRollPacket(player, rerollsLeft));
    }

    /** Builds an OpenCharSlotRollPacket with seq-9 chars for pathways that still have seq-8 slots available. */
    public static de.jakob.lotm.network.packets.toClient.OpenCharSlotRollPacket buildRollPacket(
            ServerPlayer player, int rerollsLeft) {
        List<String> pathways  = new ArrayList<>();
        List<String> charNames = new ArrayList<>();

        for (String pathway : BeyonderData.implementedPathways) {
            // Only offer pathways where there is still a seq-8 slot available.
            // A new player starts at seq 9 and needs to reach seq 8 to advance;
            // if seq-8 is already full for this pathway, there is no point rolling it.
            if (!BeyonderData.hasSequenceSlotAvailable(player.serverLevel(), pathway, 8)) continue;

            BeyonderCharacteristicItem item =
                    BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, 9);
            if (item == null) continue;
            pathways.add(pathway);
            charNames.add(item.getDefaultInstance().getHoverName().getString());
        }

        // Fallback: if every pathway is full (extreme edge case) show them all so the
        // wheel never opens empty.
        if (pathways.isEmpty()) {
            for (String pathway : BeyonderData.implementedPathways) {
                BeyonderCharacteristicItem item =
                        BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, 9);
                if (item == null) continue;
                pathways.add(pathway);
                charNames.add(item.getDefaultInstance().getHoverName().getString());
            }
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

        // Always (re-)send the wheel packet. This covers the case where a player was
        // kicked mid-roll (e.g. by a login-auth mod) and still has a stale
        // charSlotRollsLeft key — resending here clears the invincibility state and
        // shows the screen again on next login.
        player.getPersistentData().putInt(NBT_REROLLS, MAX_REROLLS);
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                buildRollPacket(player, MAX_REROLLS));
    }

    /**
     * Called from the /slots command to force-trigger the characteristic wheel for
     * any player, regardless of whether they already received new-player perks.
     */
    public static void forceRollForPlayer(ServerPlayer player) {
        NewPlayerComponent comp = player.getData(ModAttachments.BOOK_COMPONENT);
        comp.setHasReceivedNewPlayerPerks(false);
        player.getPersistentData().putInt(NBT_REROLLS, MAX_REROLLS);
        de.jakob.lotm.network.PacketHandler.sendToPlayer(player,
                buildRollPacket(player, MAX_REROLLS));
    }
}
