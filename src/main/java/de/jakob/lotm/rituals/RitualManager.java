package de.jakob.lotm.rituals;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import de.jakob.lotm.rituals.impl.RitualMagicPotionEffect;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RitualManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private static Map<ResourceLocation, RitualRecipe> rituals = new HashMap<>();

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

    private static final Vec3 CANDLE_OFFSET = new Vec3(0.5, 0.7, 0.5);
    private static final Vec3 ITEM_1_OFFSET = new Vec3(0.25, 0.6, 0.25);
    private static final Vec3 ITEM_2_OFFSET = new Vec3(0.75, 0.6, 0.25);
    private static final Vec3 ITEM_3_OFFSET = new Vec3(0.5, 0.6, 0.75);

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
            return;
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

        DustParticleOptions dust2 = new DustParticleOptions(
                new Vector3f(
                        ritual.particleColor().r(),
                        ritual.particleColor().g(),
                        ritual.particleColor().b()
                ),
                1.5F
        );

        // TODO: Fix positioning
        Vec3 candlePos = toWorldPos(tablePos, facing, CANDLE_OFFSET);
//        Vec3 item1Pos  = toWorldPos(tablePos, facing, ITEM_1_OFFSET);
//        Vec3 item2Pos  = toWorldPos(tablePos, facing, ITEM_2_OFFSET);
//        Vec3 item3Pos  = toWorldPos(tablePos, facing, ITEM_3_OFFSET);
//
//        ParticleUtil.spawnParticles(level, dust2, item1Pos, 80, .2, 0);
//        ParticleUtil.spawnParticles(level, dust2, item2Pos, 80, .2, 0);
//        ParticleUtil.spawnParticles(level, dust2, item3Pos, 80, .2, 0);

        ServerScheduler.scheduleForDuration(0, 10, 30, () -> {
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1D, 50);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1.5D, 60);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 1.75D, 80);
            ParticleUtil.spawnCircleParticles(level, dust, player.position(), 2.5D, 90);

            ParticleUtil.spawnParticles(level, dust2, candlePos.add(0, 3, 0), 150, .15, 6, .15, 0);
        });

        handler.perform(result.params(), player);
    }

    public static Map<ResourceLocation, RitualRecipe> getRituals() {
        return rituals;
    }
}