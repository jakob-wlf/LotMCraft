package de.jakob.lotm.abilities.sun;

import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.dimension.SpiritWorldHandler;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.sun_pathway.SunKingdomEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DivineKingdomManifestationAbility extends Ability {

    private static final Map<UUID, Vec3> ACTIVE_EXCURSIONS = new HashMap<>();
    private static final int SPIRIT_WORLD_TICKS = 20 * 60 * 2; // 2 minutes

    public DivineKingdomManifestationAbility(String id) {
        super(id, 20 * 60*2, "purification", "light_source", "light_strong", "light_weak", "purification_holy");
        canBeCopied = false;
        canBeUsedByNPC = false;
        canBeReplicated = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("sun", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 8000;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SunKingdomEntity sunKingdomEntity = new SunKingdomEntity(ModEntities.SUN_KINGDOM.get(), level, 20 * 60 * 2, entity.getUUID(), BeyonderData.isGriefingEnabled(entity));
        sunKingdomEntity.setPos(entity.getX(), entity.getY() + .5, entity.getZ());
        serverLevel.addFreshEntity(sunKingdomEntity);

        if (!(entity instanceof Player player)) return;

        ServerLevel spiritWorldLevel = serverLevel.getServer().getLevel(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
        if (spiritWorldLevel == null) return;

        Vec3 origin = player.position();
        Vec3 dest = SpiritWorldHandler.getCoordinatesInSpiritWorld(origin, serverLevel);
        ACTIVE_EXCURSIONS.put(player.getUUID(), origin);

        int safeY = spiritWorldLevel.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int) dest.x, (int) dest.y, (int) dest.z)
        ).getY();

        player.teleportTo(spiritWorldLevel, dest.x, safeY + 1.0, dest.z, Set.of(), player.getYRot(), player.getXRot());

        ServerScheduler.scheduleForDuration(0, 4, SPIRIT_WORLD_TICKS, () -> {
            for (int i = 0; i < 36; i++) {
                double angle = i * Math.PI * 2 / 36;
                double px = dest.x + Math.cos(angle) * 12;
                double pz = dest.z + Math.sin(angle) * 12;
                for (double py = safeY; py < safeY + 5; py += 1.5) {
                    spiritWorldLevel.sendParticles(
                            new DustParticleOptions(new Vector3f(0.5f, 0.5f, 0.5f), 1.5f),
                            px, py, pz, 1, 0, 0, 0, 0);
                }
                spiritWorldLevel.sendParticles(ParticleTypes.ASH,
                        dest.x + spiritWorldLevel.random.nextGaussian() * 10,
                        safeY + spiritWorldLevel.random.nextFloat() * 5,
                        dest.z + spiritWorldLevel.random.nextGaussian() * 10,
                        2, 0, 0, 0, 0);
            }
        }, spiritWorldLevel);

        ServerScheduler.scheduleDelayed(SPIRIT_WORLD_TICKS, () -> {
            Player p = spiritWorldLevel.getPlayerByUUID(player.getUUID());
            Vec3 returnOrigin = ACTIVE_EXCURSIONS.remove(player.getUUID());
            if (p != null && returnOrigin != null) {
                p.teleportTo(serverLevel, returnOrigin.x, returnOrigin.y, returnOrigin.z,
                        Set.of(), p.getYRot(), p.getXRot());
            }
        }, spiritWorldLevel);
    }
}
