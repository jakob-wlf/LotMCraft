package de.jakob.lotm.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.CompletableFuture;

public class StructureMapItem extends Item {

    private final TagKey<Structure> structureTag;
    private final String mapName;

    public StructureMapItem(TagKey<Structure> structureTag, String mapName, Properties props) {
        super(props.stacksTo(1));
        this.structureTag = structureTag;
        this.mapName = mapName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos playerPos = player.blockPosition();

        // Tell the player we're searching so the use doesn't feel broken
        player.displayClientMessage(
                Component.literal("§SSearching location..."), true);

        CompletableFuture.runAsync(() -> {
            BlockPos structurePos = serverLevel.findNearestMapStructure(
                    structureTag, playerPos, 1000, false);

            serverLevel.getServer().execute(() -> {
                if (structurePos == null) {
                    player.displayClientMessage(
                            Component.translatable("item.lotmcraft.structure_map.not_found"), true);
                    return;
                }

                ItemStack filledMap = createStructureMap(serverLevel, structurePos);
                player.setItemInHand(hand, filledMap);
            });
        });

        // Consume the item optimistically — the async callback will replace it with the map,
        // or leave the slot empty on failure (shrink by 1 below if you prefer that behaviour)
        return InteractionResultHolder.success(stack);
    }

    private ItemStack createStructureMap(ServerLevel level, BlockPos structurePos) {
        int scale = 2;

        ItemStack mapStack = MapItem.create(level, structurePos.getX(), structurePos.getZ(),
                (byte) scale, true, true);

        Holder<MapDecorationType> decorationType = level.registryAccess()
                .registryOrThrow(Registries.MAP_DECORATION_TYPE)
                .getHolderOrThrow(MapDecorationTypes.TARGET_X.getKey());

        MapItemSavedData.addTargetDecoration(mapStack, structurePos, "+", decorationType);

        mapStack.set(DataComponents.CUSTOM_NAME,
                Component.translatable(mapName).withStyle(ChatFormatting.GOLD));

        return mapStack;
    }
}