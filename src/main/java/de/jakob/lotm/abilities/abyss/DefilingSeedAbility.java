package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.abilities.AbilityItem;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class DefilingSeedAbility extends AbilityItem {
    private static final HashSet<UUID> defiledEntities = new HashSet<>();

    public DefilingSeedAbility(Properties properties) {
        super(properties, 2);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 5));
    }

    @Override
    protected float getSpiritualityCost() {
        return 60;
    }

    private final DustParticleOptions blackDust = new DustParticleOptions(
            new Vector3f(.2f, 0, 0),
            2.5f
    );

    @Override
    protected void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.5f);
            if(target == null || defiledEntities.contains(target.getUUID()))
                return;
            ParticleUtil.spawnParticles((ClientLevel) level, blackDust, target.getEyePosition(), 40, 1.5, 0);
            level.playSound(null, BlockPos.containing(entity.position()), SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 1, 1);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 25, 2.5f);
        if(target == null || defiledEntities.contains(target.getUUID())) {
            if(entity instanceof ServerPlayer player) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.translatable("ability.lotmcraft.defiling_seed.no_target").withColor(0xFFff124d));
                player.connection.send(packet);
            }
            return;
        }

        defiledEntities.add(target.getUUID());

        ServerScheduler.scheduleForDuration(0, 8, 20 * 60 * 2, () -> {
            switch (random.nextInt(22)) {
                case 0, 2, 3 -> target.hurt(entity.damageSources().source(ModDamageTypes.LOOSING_CONTROL), 6 * (float) multiplier(entity));
                case 1 -> target.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, 20 * 4, random.nextInt(4)));
                case 4, 5 ->  target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 9, random.nextInt(2, 7)));
            }
        }, () -> {
            ServerScheduler.scheduleDelayed(20 * 5, () -> defiledEntities.remove(target.getUUID()));
        }, (ServerLevel) level);
    }
}
