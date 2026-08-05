package de.jakob.lotm.network.packets.toServer;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.potions.PotionRecipe;
import de.jakob.lotm.beyonders.potions.PotionRecipeItem;
import de.jakob.lotm.beyonders.potions.PotionRecipeItemHandler;
import de.jakob.lotm.gui.custom.Recipe.RecipeMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record OpenRecipeMenuPacket(int sequence, String pathway, boolean fromCard) implements CustomPacketPayload {
    public static final Type<OpenRecipeMenuPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "open_recipe"));

    public static final StreamCodec<FriendlyByteBuf, OpenRecipeMenuPacket> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    OpenRecipeMenuPacket::sequence,
                    StreamCodec.of(FriendlyByteBuf::writeUtf, FriendlyByteBuf::readUtf),
                    OpenRecipeMenuPacket::pathway,
                    StreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean),
                    OpenRecipeMenuPacket::fromCard,
                    OpenRecipeMenuPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenRecipeMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().getReceptionSide().isServer()) {
                ServerPlayer player = (ServerPlayer) context.player();

                int sequence = packet.sequence();
                String pathway = packet.pathway();

                PotionRecipeItem potionRecipeItem = PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);

                List<ItemStack> ingredients = new ArrayList<>();
                if (potionRecipeItem != null && potionRecipeItem.getRecipe() != null) {
                    PotionRecipe recipe = potionRecipeItem.getRecipe();
                    ingredients.add(recipe.supplementaryIngredient1());
                    ingredients.add(recipe.supplementaryIngredient2());
                    ingredients.add(recipe.mainIngredient());
                }

                player.openMenu(
                        new RecipeMenuProvider(ingredients, pathway, sequence, packet.fromCard()),
                        buf -> { buf.writeUtf(pathway); buf.writeVarInt(sequence); buf.writeBoolean(packet.fromCard()); }
                );
            }
        });
    }

}