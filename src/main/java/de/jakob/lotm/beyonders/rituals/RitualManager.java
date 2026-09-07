package de.jakob.lotm.beyonders.rituals;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.rendering.effectRendering.EffectIds;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.rendering.effectRendering.EffectParams;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RitualManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static Map<ResourceLocation, RitualRecipe> rituals = new HashMap<>();

    private static final String FAILURE_RITUAL_ID = "nature_of_degeneracy";

    public RitualManager() {
        super(GSON, "rituals");
    }

    @Override
    protected void apply(Map<ResourceLocation, com.google.gson.JsonElement> jsonMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<ResourceLocation, RitualRecipe> newRituals = new HashMap<>();
        for (var entry : jsonMap.entrySet()) {
            try {
                RitualRecipe ritual = GSON.fromJson(entry.getValue(), RitualRecipe.class);
                newRituals.put(entry.getKey(), ritual);
            } catch (Exception e) {
                LOGGER.error("Failed to parse ritual recipe {}", entry.getKey(), e);
            }
        }
        rituals = newRituals;
        LOGGER.info("Loaded {} ritual recipes", rituals.size());
    }

    @Nullable
    public static RitualRecipe getRitualByRecipe(
            ItemStack candles,
            List<ItemStack> sacrifices,
            int sequence,
            List<String> honorificLines
    ) {
        for (RitualRecipe ritual : rituals.values()) {
            if (ritual.matches(candles, sacrifices, sequence, honorificLines)) {
                return ritual;
            }
        }
        return null;
    }

    private static ItemStack createBookItemStack(RitualRecipe ritual) {
        String bookTitle = Component.translatable("ritual.lotmcraft." + ritual.discoveredFrom().split(":")[1]).getString();
        String candleName = BuiltInRegistries.ITEM.get(ResourceLocation.parse(ritual.candle())).getDescription().getString();
        List<String> sacrificeNames = ritual.sacrifices().stream()
                .map(sacrifice -> {
                    return BuiltInRegistries.ITEM.get(ResourceLocation.parse(sacrifice.item())).getDescription().getString() + " x" + sacrifice.count();
                })
                .toList();
        List<String> honorificLines = ritual.honorific().lines();

        Component generalDescription = Component.translatable("ritual.lotmcraft.general_description")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.valueOf(ritual.bookColor()));

        Component candles = Component.literal("")
                .append(Component.translatable("ritual.lotmcraft.candles")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.valueOf(ritual.bookColor())))
                .append(Component.literal(candleName));

        Component sacrifices = Component.literal("")
                .append(Component.translatable("ritual.lotmcraft.sacrifices")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.valueOf(ritual.bookColor())))
                .append(Component.literal(String.join(", ", sacrificeNames)));

        Component honorifics = Component.literal("")
                .append(Component.translatable("ritual.lotmcraft.honorifics")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.valueOf(ritual.bookColor())))
                .append(Component.literal("\n"))
                .append(Component.literal(String.join("\n", honorificLines.stream().map(s -> " - " + s).toList())));

        Component minSequence = Component.literal("")
                .append(Component.translatable("ritual.lotmcraft.min_sequence")
                        .withStyle(ChatFormatting.BOLD, ChatFormatting.valueOf(ritual.bookColor())))
                .append(Component.literal(String.valueOf(ritual.conditions().minSequence() > 9 ? "none" : ritual.conditions().minSequence())));

        Component pageOne = Component.literal("")
                .append(generalDescription).append(Component.literal("\n\n"))
                .append(candles).append(Component.literal("\n\n"))
                .append(sacrifices).append(Component.literal("\n\n"))
                .append(minSequence);

        Component pageTwo = honorifics;

        List<Filterable<Component>> pages = List.of(Filterable.passThrough(pageOne), Filterable.passThrough(pageTwo));

        WrittenBookContent bookContentObj = new WrittenBookContent(
                Filterable.passThrough(bookTitle),
                "Unknown",
                0,
                pages,
                false
        );

        ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
        bookStack.set(DataComponents.WRITTEN_BOOK_CONTENT, bookContentObj);
        return bookStack;
    }

    public static ItemStack getRandomRitualBook() {
        if (rituals.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RitualRecipe randomRitual = rituals.values().stream().skip((int) (rituals.size() * Math.random())).findFirst().orElse(null);
        if (randomRitual == null) {
            return ItemStack.EMPTY;
        }
        return createBookItemStack(randomRitual);
    }

    private static Vec3 rotateForFacing(Direction facing, Vec3 local) {
        double dx = local.x - 0.5;
        double dz = local.z - 0.5;
        double y = local.y;

        return switch (facing) {
            case EAST  -> new Vec3(0.5 - dz, y, 0.5 + dx);
            case SOUTH -> new Vec3(0.5 - dx, y, 0.5 - dz);
            case WEST  -> new Vec3(0.5 + dz, y, 0.5 - dx);
            default    -> local; // NORTH, no rotation
        };
    }

    private static Vec3 toWorldPos(BlockPos blockPos, Direction facing, Vec3 local) {
        Vec3 rotated = rotateForFacing(facing, local);
        return new Vec3(
                blockPos.getX() + rotated.x,
                blockPos.getY() + rotated.y,
                blockPos.getZ() + rotated.z
        );
    }

    public static void performRitual(@Nullable RitualRecipe ritual, ServerPlayer player, BlockPos tablePos) {
        if (ritual == null) {
            ritual = rituals.get(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, FAILURE_RITUAL_ID));
            if(ritual == null) {
                return;
            }
        }
        RitualRecipe.Result result = ritual.result();

        RitualResultHandler handler = RitualResultRegistry.get(result.type());
        if (handler == null) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockState state = level.getBlockState(tablePos);
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;

        level.playSound(null, tablePos, ModSounds.MIDNIGHT_POEM.get(), player.getSoundSource(), 1.0F, 1.0F);

        DustParticleOptions dust = new DustParticleOptions(
                new Vector3f(
                        ritual.particleColor().r(),
                        ritual.particleColor().g(),
                        ritual.particleColor().b()
                ),
                1.0F
        );

        Vec3 effectPos = toWorldPos(tablePos, facing, new Vec3(0.5, 1.2, 0.5));

        ServerScheduler.scheduleForDuration(0, 10, 30, () -> {
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1D, 50);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1.5D, 60);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1.75D, 80);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 2.5D, 90);
        });

        EffectManager.playEffect(
                EffectIds.RITUAL,
                effectPos.x(), effectPos.y(), effectPos.z(),
                player.serverLevel(),
                EffectParams.ofParams(
                        ritual.particleColor().r(),
                        ritual.particleColor().g(),
                        ritual.particleColor().b()
                )
        );

        handler.perform(result.params(), player, tablePos);
    }

    public static Map<ResourceLocation, RitualRecipe> getRituals() {
        return rituals;
    }
}