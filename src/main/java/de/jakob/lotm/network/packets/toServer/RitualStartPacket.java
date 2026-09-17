package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.block.entity.RitualisticTableBlockEntity;
import de.jakob.lotm.gui.custom.ritualistic_table.RitualMenu;
import de.jakob.lotm.beyonders.rituals.RitualManager;
import de.jakob.lotm.beyonders.rituals.RitualRecipe;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record RitualStartPacket(BlockPos pos, String field1, String field2, String field3)
        implements CustomPacketPayload {

    public static final Type<RitualStartPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("lotmcraft", "ritual_start"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualStartPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RitualStartPacket::pos,
            ByteBufCodecs.STRING_UTF8, RitualStartPacket::field1,
            ByteBufCodecs.STRING_UTF8, RitualStartPacket::field2,
            ByteBufCodecs.STRING_UTF8, RitualStartPacket::field3,
            RitualStartPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final RitualStartPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            Level level = serverPlayer.level();
            if (level.getBlockEntity(payload.pos()) instanceof RitualisticTableBlockEntity be) {

                // basic sanity check so a modified client can't fake distance
                if (serverPlayer.distanceToSqr(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 64.0) {
                    return;
                }

                serverPlayer.closeContainer();

                ItemStack candle = be.itemHandler.getStackInSlot(RitualMenu.CANDLE_SLOT);
                ItemStack sacrifice1 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_1);
                ItemStack sacrifice2 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_2);
                ItemStack sacrifice3 = be.itemHandler.getStackInSlot(RitualMenu.SACRIFICE_SLOT_3);
                List<ItemStack> sacrifices = List.of(sacrifice1, sacrifice2, sacrifice3).stream().filter(stack -> !stack.isEmpty()).toList();

                List<String> honorificLines = List.of(payload.field1(), payload.field2(), payload.field3());

                be.itemHandler.setStackInSlot(RitualMenu.CANDLE_SLOT, candle.getCount() > 1 ? candle.copyWithCount(candle.getCount() - 1) : ItemStack.EMPTY);
                be.itemHandler.setStackInSlot(RitualMenu.SACRIFICE_SLOT_1, ItemStack.EMPTY);
                be.itemHandler.setStackInSlot(RitualMenu.SACRIFICE_SLOT_2, ItemStack.EMPTY);
                be.itemHandler.setStackInSlot(RitualMenu.SACRIFICE_SLOT_3, ItemStack.EMPTY);
                be.setChanged();

                RitualRecipe ritual = RitualManager.getRitualByRecipe(candle, sacrifices, BeyonderData.getSequence(serverPlayer), honorificLines);

                RitualManager.performRitual(ritual, serverPlayer, payload.pos());
            }
        });
    }
}