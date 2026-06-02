package de.jakob.lotm.loottables;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.attachments.MysteriousTabletData;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.potions.PotionItemHandler;
import de.jakob.lotm.potions.PotionRecipeItemHandler;
import de.jakob.lotm.util.BeyonderData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

public class ChestLootModifier extends LootModifier {

    public static final Supplier<MapCodec<ChestLootModifier>> CODEC = () ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, ChestLootModifier::new));

    public ChestLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    private static final Random random = new Random();

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();

        if (lootTableId.getPath().contains("chests/")) {
            if (context.getRandom().nextFloat() < 0.45f) {

                String pathway = BeyonderData.implementedPathways.get(random.nextInt(BeyonderData.implementedPathways.size()));
                int sequence = getWeightedHighSequence();
                Item item = getRandomLoot(pathway, sequence);

                if (item != null && (sequence >= 7)) {
                    generatedLoot.add(new ItemStack(item));
                }
            }
        }

        if (isAncientCityChest(lootTableId)) {
            tryAddAncientCityFragment(generatedLoot, context);
        }

        return generatedLoot;
    }

    private static boolean isAncientCityChest(ResourceLocation lootTableId) {
        return lootTableId.getPath().contains("chests/ancient_city");
    }

    private static void tryAddAncientCityFragment(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!hasGoldenApple(generatedLoot)) {
            return;
        }

        var originVec = context.getParamOrNull(LootContextParams.ORIGIN);
        if (originVec == null) {
            return;
        }

        BlockPos origin = BlockPos.containing(originVec);

        Long structureKey = getAncientCityKey(level, origin);
        if (structureKey == null) {
            return;
        }

        MysteriousTabletData data = MysteriousTabletData.get(level.getServer());
        if (data.isAncientCityClaimed(structureKey)
                || !data.canSpawnFragment(MysteriousTabletData.FragmentType.LOWER)) {
            return;
        }

        generatedLoot.add(new ItemStack(ModItems.LOWER_FRAGMENT_OF_A_MYSTERIOUS_TABLET.get()));
        data.markAncientCityClaimed(structureKey);
    }

    private static boolean hasGoldenApple(ObjectArrayList<ItemStack> generatedLoot) {
        for (ItemStack stack : generatedLoot) {
            if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                return true;
            }
        }
        return false;
    }

    private static Long getAncientCityKey(ServerLevel level, BlockPos origin) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE,
                ResourceLocation.fromNamespaceAndPath("minecraft", "ancient_city"));

        StructureStart start = level.registryAccess()
                .registry(Registries.STRUCTURE)
                .flatMap(registry -> registry.getHolder(key)
                        .map(holder -> level.structureManager().getStructureWithPieceAt(origin, HolderSet.direct(holder))))
                .orElse(null);

        if (start == null || !start.isValid()) {
            return null;
        }

        BoundingBox box = start.getBoundingBox();
        return ChunkPos.asLong(box.minX() >> 4, box.minZ() >> 4);
    }

    public static int getWeightedHighSequence() {
        Random random = new Random();
        double normalizedValue = random.nextDouble(); // 0.0 to 1.0

        double weighted = Math.pow(normalizedValue, 0.35); // Lower exponent = stronger bias toward high values

        // Map to range 1-9 (weighted now favors values close to 1.0, which maps to 9)
        return 1 + (int) (weighted * 9);
    }

    public static Item getRandomLoot(String pathway, int sequence) {
        return switch(random.nextInt(4)) {
            case 1 -> ModIngredients.selectRandomIngredientOfPathwayAndSequence(random, pathway, sequence);
            case 2 -> PotionRecipeItemHandler.selectRecipeOfPathwayAndSequence(pathway, sequence);
            case 3 -> BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence);
            default -> PotionItemHandler.selectPotionOfPathwayAndSequence(random, pathway, sequence);
        };
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }


}