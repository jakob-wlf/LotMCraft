package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Set;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ExileReturnHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return;
        long gameTime = server.overworld().getGameTime();

        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities(EntityTypeTest.forClass(LivingEntity.class), e -> true)) {
                if(level.dimension() != Level.END)
                    continue;
                CompoundTag tag = entity.getPersistentData();
                if (tag.getBoolean("Exiled")) {
                    long returnTime = tag.getLong("ReturnTime");
                    if (gameTime >= returnTime) {
                        // Now handle teleport back
                        String returnLevelStr = tag.getString("ReturnLevel");
                        ResourceKey<Level> returnLevelKey =
                                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(returnLevelStr));
                        ServerLevel returnLevel = server.getLevel(returnLevelKey);
                        if (returnLevel != null) {
                            double x = tag.getDouble("ReturnX");
                            double y = tag.getDouble("ReturnY");
                            double z = tag.getDouble("ReturnZ");
                            entity.teleportTo(returnLevel, x, y, z, Set.of(), entity.getYRot(), entity.getXRot());
                            entity.resetFallDistance();
                            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ModParticles.STAR.get(), entity.position(), 100, 1, 2, 1, .1);
                            ParticleUtil.spawnParticles((ServerLevel) entity.level(), ParticleTypes.PORTAL, entity.position(), 100, 1, 2, 1, .1);
                            entity.level().playSound(null, entity.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 2.0f, 0.5f + entity.level().random.nextFloat());
                        }

                        // Clear data
                        tag.remove("Exiled");
                        tag.remove("ReturnTime");
                        tag.remove("ReturnLevel");
                        tag.remove("ReturnX");
                        tag.remove("ReturnY");
                        tag.remove("ReturnZ");
                    }
                }
            }
        }
    }

}
