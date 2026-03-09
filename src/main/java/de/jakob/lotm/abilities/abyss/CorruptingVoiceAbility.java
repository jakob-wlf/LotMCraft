package de.jakob.lotm.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class CorruptingVoiceAbility extends ToggleAbility {

    private static final Random RANDOM = new Random();
    private static final DustParticleOptions DARK_DUST = new DustParticleOptions(new Vector3f(0.15f, 0.0f, 0.25f), 1.8f);
    private static final DustParticleOptions WHISPER_DUST = new DustParticleOptions(new Vector3f(0.45f, 0.05f, 0.6f), 1.2f);

    private static final int CORRUPTING_VOICE_RANGE = 50;
    private static final float BASE_DAMAGE_HEARTS = 4f;
    private static final float DAMAGE_PER_CHAR_HEARTS = 2f;
    private static final int MAX_CHAR_COUNT = 50;

    public CorruptingVoiceAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 3));
    }

    @Override
    public float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.corrupting_voice.activated").withColor(0x3d005c));

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 pos = entity.getEyePosition();
        for (int i = 0; i < 30; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double radius = 0.5 + RANDOM.nextDouble() * 1.5;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            ParticleUtil.spawnParticles(serverLevel, DARK_DUST, new Vec3(x, pos.y + RANDOM.nextDouble() * 0.5, z), 1, 0.1, 0.05);
        }
        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, pos, 20, 0.4, 0.4, 0.4, 0.05);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if (level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 pos = entity.getEyePosition();

        for (int i = 0; i < 5; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double radius = 0.3 + RANDOM.nextDouble() * 1.0;
            double x = pos.x + Math.cos(angle) * radius;
            double z = pos.z + Math.sin(angle) * radius;
            double y = pos.y + (RANDOM.nextDouble() - 0.3) * 0.6;
            ParticleUtil.spawnParticles(serverLevel, WHISPER_DUST, new Vec3(x, y, z), 1, 0.05, 0.02);
        }

        if (RANDOM.nextInt(4) == 0) {
            ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE, pos, 2, 0.3, 0.3, 0.3, 0.02);
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.corrupting_voice.deactivated").withColor(0x9932cc));
    }

    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (LOTMCraft.abilityHandler == null) {
            return;
        }

        CorruptingVoiceAbility ability = (CorruptingVoiceAbility) LOTMCraft.abilityHandler.getById("corrupting_voice_ability");
        if (ability == null || !ability.isActiveForEntity(player)) {
            return;
        }

        String message = event.getRawText();
        int charCount = Math.min(message.length(), MAX_CHAR_COUNT);

        // Damage = (base hearts + chars * hearts_per_char) * 2 (convert hearts to Minecraft damage units)
        // Maximum at MAX_CHAR_COUNT=50: (4 + 50*2) * 2 = 208 damage (104 hearts) — intentional for Seq 3
        float damage = (BASE_DAMAGE_HEARTS + charCount * DAMAGE_PER_CHAR_HEARTS) * 2f;

        AbilityUtil.getNearbyEntities(player, serverLevel, player.position(), CORRUPTING_VOICE_RANGE, false)
                .stream()
                .filter(target -> AbilityUtil.mayDamage(player, target))
                .forEach(target -> {
                    target.hurt(serverLevel.damageSources().magic(), damage);
                    applyRandomNegativeEffects(target);

                    // Particle burst on targets
                    ParticleUtil.spawnParticles(serverLevel, DARK_DUST,
                            target.getEyePosition(), 15, 0.4, 0.4, 0.4, 0.08);
                    ParticleUtil.spawnParticles(serverLevel, ParticleTypes.SMOKE,
                            target.getEyePosition(), 10, 0.3, 0.3, 0.3, 0.05);
                });

        int heartsDealt = (int) (damage / 2f);
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.corrupting_voice.triggered")
                .append(Component.literal(charCount + " chars \u2014 " + heartsDealt + " \u2665"))
                .withColor(0x6600cc));
    }

    private static void applyRandomNegativeEffects(LivingEntity entity) {
        int effectCount = 1 + RANDOM.nextInt(3);
        for (int i = 0; i < effectCount; i++) {
            int effectChoice = RANDOM.nextInt(10);
            int amplifier = RANDOM.nextInt(5);
            int duration = 20 * (10 + RANDOM.nextInt(21));

            switch (effectChoice) {
                case 0 -> entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false));
                case 1 -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, false));
                case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0, false, false));
                case 3 -> entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false));
                case 4 -> entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, amplifier, false, false));
                case 5 -> entity.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, amplifier, false, false));
                case 6 -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, duration, amplifier, false, false));
                case 7 -> entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, amplifier, false, false));
                case 8 -> entity.addEffect(new MobEffectInstance(ModEffects.LOOSING_CONTROL, duration, amplifier, false, false));
            }
        }
    }
}