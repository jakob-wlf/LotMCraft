package de.jakob.lotm.abilities.demoness.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class MirrorRevivalAbility extends PassiveAbilityItem {
    public MirrorRevivalAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("demoness", 3));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {

    }

    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(143 / 255f, 52 / 255f, 235 / 255f), 2f);

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if(!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if(!((MirrorRevivalAbility) PassiveAbilityHandler.MIRROR_REVIVAL.get()).shouldApplyTo(entity)) {
            return;
        }

        BlockPos glassPos = getNearestGlassBlock((ServerLevel) entity.level(), entity.position(), 100);
        if(glassPos == null) {
            return;
        }

        event.setCanceled(true);
        entity.setHealth(entity.getMaxHealth());
        entity.teleportTo(glassPos.getX() + 0.5, glassPos.getY() + 1, glassPos.getZ() + 0.5);
        ParticleUtil.spawnParticles(serverLevel, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0), 40, .5, entity.getEyeHeight() / 2, .5, 0.1);
    }

    private static BlockPos getNearestGlassBlock(ServerLevel level, Vec3 pos, int searchRadius) {
        return AbilityUtil.getBlocksInSphereRadius(level, pos, searchRadius, true, true, false).stream().filter(b -> {
            BlockState state = level.getBlockState(b);
            return state.is(Tags.Blocks.GLASS_BLOCKS) || state.is(Tags.Blocks.GLASS_PANES);
        }).min((b1, b2) -> {
            double d1 = b1.distToCenterSqr(pos);
            double d2 = b2.distToCenterSqr(pos);
            return Double.compare(d1, d2);
        }).orElse(null);
    }
}
