package de.jakob.lotm.beyonders.abilities.visionary;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(
        modid = LOTMCraft.MOD_ID
)
public class DragonScalesAbility extends ToggleAbility {
    public static HashSet<UUID> set = new HashSet<>();

    public DragonScalesAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated =false;
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeShared = false;
    }

    @Override
    public float getSpiritualityCost() {
        return 3;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("visionary", 6));
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        set.add(entity.getUUID());
    }

    public static final DustParticleOptions dust = new DustParticleOptions(new Vector3f(255 / 255f, 216 / 255f, 138 / 255f), 1.75f);

    @Override
    public void tick(Level level, LivingEntity entity) {

        entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 5, 1, false, false, false));
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        set.remove(entity.getUUID());
    }

    public static boolean canBlock(DamageSource source){
        return   !source.is(ModDamageTypes.MIND)
                || !source.is(ModDamageTypes.SOUL);
    }

    public static float getDamageReductionPerSeq(int seq){
        return (float) (1.0f - switch (seq){
                    case 6 -> 0.10f;
                    case 5 -> 0.15f;
                    case 4 -> 0.20;
                    case 3 -> 0.25f;
                    case 2 -> 0.30f;
                    case 1 -> 0.35f;
                    case 0 -> 0.45f;
                    default -> 0.0f;
                });
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        var entity = event.getEntity();

        if(!set.contains(entity.getUUID())) return;

        if(canBlock(event.getSource())){
            float damage = event.getAmount();
            float mult = getDamageReductionPerSeq(BeyonderData.getSequence(entity));

            damage *= mult;

            event.setAmount(damage);

            if(!VisionaryHandler.isInvisible(entity))
                ParticleUtil.spawnParticles((ServerLevel) entity.level(), dust, entity.getEyePosition(), 5, .45f, .8, .45f, 0);
        }
    }
}
