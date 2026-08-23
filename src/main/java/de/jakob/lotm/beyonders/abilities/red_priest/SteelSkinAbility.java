package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.core.interaction.InteractionHandler;
import de.jakob.lotm.beyonders.abilities.visionary.handlers.VisionaryHandler;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.Location;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SteelSkinAbility extends ToggleAbility {
    public static HashSet<UUID> set = new HashSet<>();

    private final static DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.3f, 0.3f, 0.3f), 2.25f);


    public SteelSkinAbility(String id) {
        super(id);
        canBeCopied = false;
        canBeReplicated =false;
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeShared = false;
        shouldBeHidden = true;

        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(150f, 85f, 55f, 35f, 20f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("red_priest", 4));
    }

    @Override
    protected float getSpiritualityCost() {
        return 500;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;

        set.add(entity.getUUID());
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
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
            case 4 -> 0.35;
            case 3 -> 0.40f;
            case 2 -> 0.55f;
            case 1 -> 0.60f;
            case 0 -> 0.65f;
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