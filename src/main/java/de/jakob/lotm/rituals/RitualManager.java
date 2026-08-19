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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
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

    public static void performRitual(@Nullable RitualRecipe ritual, ServerPlayer player) {
        if (ritual == null) { // Add penalty later
            return;
        }
        RitualRecipe.Result result = ritual.result();

        RitualResultHandler handler = RitualResultRegistry.get(result.type());
        if (handler == null) {
            return;
        }

        player.serverLevel().playSound(null, BlockPos.containing(player.position()), ModSounds.MIDNIGHT_POEM.get(), player.getSoundSource(), 1.0F, 1.0F);
        DustParticleOptions dust = new DustParticleOptions(
                new Vector3f(
                        ritual.particleColor().r(),
                        ritual.particleColor().g(),
                        ritual.particleColor().b()
                ),
                1.0F
        );
        ServerScheduler.scheduleForDuration(0, 10, 30, () -> {
            ParticleUtil.spawnCircleParticles(player.serverLevel(), dust, player.position(), 1D, 50);
            ParticleUtil.spawnCircleParticles(player.serverLevel(), dust, player.position(), 1.5D, 60);
            ParticleUtil.spawnCircleParticles(player.serverLevel(), dust, player.position(), 1.75D, 80);
            ParticleUtil.spawnCircleParticles(player.serverLevel(), dust, player.position(), 2.5D, 90);
        });

        handler.perform(result.params(), player);
    }

    public static Map<ResourceLocation, RitualRecipe> getRituals() {
        return rituals;
    }
}