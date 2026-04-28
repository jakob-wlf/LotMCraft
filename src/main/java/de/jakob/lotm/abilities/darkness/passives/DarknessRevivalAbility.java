package de.jakob.lotm.abilities.darkness.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.PassiveAbilityHandler;
import de.jakob.lotm.abilities.PassiveAbilityItem;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.abilities.justiciar.LawAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DarknessRevivalAbility extends PassiveAbilityItem {
    private static final int MAX_LIGHT_LEVEL = 7;
    private static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(20 / 255f, 0 / 255f, 40 / 255f), 2f);

    public DarknessRevivalAbility(Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 1));
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        if (!((DarknessRevivalAbility) PassiveAbilityHandler.DARKNESS_REVIVAL.get()).shouldApplyTo(entity)) return;

        if (LawAbility.SOLACE_KILLED.contains(entity.getUUID())) return;

        // Must be at or below light level 7
        int lightLevel = serverLevel.getMaxLocalRawBrightness(entity.blockPosition());
        if (lightLevel > MAX_LIGHT_LEVEL) return;

        // Drain sanity — if not enough sanity (like around 5% ig), revival wont trigger
        SanityComponent sanity = entity.getData(ModAttachments.SANITY_COMPONENT);
        if (sanity.getSanity() < 0.05) return;

        event.setCanceled(true);
        entity.setHealth(entity.getMaxHealth());

        // Drain sanity after revival (like 5%? idk im bad at math tbh)
        sanity.increaseSanityAndSync(-1.1f, entity);

        ParticleUtil.spawnParticles(serverLevel, dust, entity.position().add(0, entity.getEyeHeight() / 2, 0),
                40, .5, entity.getEyeHeight() / 2, .5, 0.1);
    }
}