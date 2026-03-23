package de.jakob.lotm.artifacts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.AbilityHandler;
import de.jakob.lotm.abilities.error.MundaneConceptualTheft;
import de.jakob.lotm.attachments.DisabledAbilitiesComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.effect.ModEffects;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.GiantLightningEntity;
import de.jakob.lotm.entity.custom.TimeChangeEntity;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.sound.ModSounds;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a negative effect that sealed artifacts inflict on their holders
 */
public class NegativeEffect {

    private final NegativeEffectType type;
    private final int sequence; // 0-9, where higher is more severe
    private final Holder<MobEffect> mobEffect; // Can be null for non-potion effects
    private final int effectAmplifier;

    // Utility variables ofr various effects
    private int loseConceptsCooldown = 0;

    public static final Codec<NegativeEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(NegativeEffectType::valueOf, NegativeEffectType::name)
                            .fieldOf("type").forGetter(e -> e.type),
                    Codec.INT.fieldOf("strength").forGetter(e -> e.sequence),
                    Codec.STRING.optionalFieldOf("mob_effect", "").forGetter(e ->
                            e.mobEffect != null ? e.mobEffect.toString() : ""),
                    Codec.INT.fieldOf("effect_amplifier").forGetter(e -> e.effectAmplifier)
            ).apply(instance, (type, strength, effectStr, amplifier) ->
                    new NegativeEffect(type, strength, null, amplifier))
    );

    public static List<NegativeEffect> createDefault() {
        // Return a safe default negative effect
        return List.of(new NegativeEffect(NegativeEffectType.DRAIN_HEALTH, 9, null, 1));
    }

    public NegativeEffect(NegativeEffectType type, int sequence, Holder<MobEffect> mobEffect, int effectAmplifier) {
        this.type = type;
        this.sequence = Math.max(0, Math.min(9, sequence));
        this.mobEffect = mobEffect;
        this.effectAmplifier = effectAmplifier;
    }

    private void applyFoolEffects(Player player) {
        switch (type) {
            case BREATH_DEPLETION, BURN:
                applyCommonEffects(player);
                break;
            case SLOWER_IN_HOT_PLACES:
                if (player.tickCount % 200 == 0) {
                    if ((player.level().getBiome(player.blockPosition()).value().getBaseTemperature()) >= .8
                            || (getBlockInRadius(player, player.blockPosition(), 5 + getEffectLevelForSequence(sequence), Blocks.FIRE))) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 220, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 220, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 220, getEffectLevelForSequence(sequence), false, false));
                    }
                }
                break;
            case TURN_TO_MARIONETTE:
                break;
            case WISH_CALAMITY:
                switch (player.getRandom().nextInt(5)) {
                    case 0 -> {
                        Vec3 teleportPos = player.position().offsetRandom(player.getRandom(), 30).add(0, -100, 0);
                        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    }
                    case 1 -> {
                        SanityComponent component = player.getData(ModAttachments.SANITY_COMPONENT);
                        component.setSanityAndSync(.1f, player);
                    }
                    case 2 -> {
                        Vec3 loc = player.position();
                        ServerScheduler.scheduleForDuration(0, 20, 20 * 4, () -> {
                            Vec3 targetLoc = new Vec3(loc.x, loc.y, loc.z);
                            for(int i = 0; i < 35; i++) {
                                BlockState state = player.level().getBlockState(BlockPos.containing(targetLoc.subtract(0, 1, 0)));
                                if(state.getCollisionShape(player.level(), BlockPos.containing(targetLoc)).isEmpty())
                                    targetLoc = targetLoc.subtract(0, 1, 0);
                            }

                            GiantLightningEntity lightning = new GiantLightningEntity(player.level(), null, targetLoc, 50, 6, DamageLookup.lookupDamage(2, .4), false, 13, 200, 0x6522a8);
                            player.level().addFreshEntity(lightning);
                        });
                    }
                }
                break;

        }
    }
    private void applyErrorEffects(Player player) {
        switch (type) {
            case GOLD_ITEM_DEBUFF:
                if (player.tickCount % 200 == 0) {
                    for (ItemStack stack : player.getInventory().items) {
                        if (stack.is(ItemTags.PIGLIN_LOVED)) {
                            return;
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, getEffectLevelForSequence(sequence), false, false));
                    }
                }
                break;
            case LOSE_CONCEPTS:
                if(loseConceptsCooldown > 0) {
                    loseConceptsCooldown -= 1;
                    break;
                }

                switch (player.getRandom().nextInt()) {
                    // Lose Health
                    case 0 -> {
                        float healthToSteal = (float) (DamageLookup.lookupDamage(5, 1f));
                        player.hurt(ModDamageTypes.source(player.level(), ModDamageTypes.BEYONDER_GENERIC), healthToSteal);
                        loseConceptsCooldown = 20 * 5;
                    }
                    // Lose Sight
                    case 1 -> {
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 4, false, false, false));
                        loseConceptsCooldown = 20 * 16;
                    }
                    // Lose Walk
                    case 2 -> {
                        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                        if(movementSpeed == null) {
                            return;
                        }
                        if(movementSpeed.getModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk")) != null) {
                            return;
                        }
                        movementSpeed.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"), -100, AttributeModifier.Operation.ADD_VALUE));
                        loseConceptsCooldown = 20 * 16;

                        ServerScheduler.scheduleDelayed(20 * 15, () -> {
                            AttributeInstance movementSpeedInner = player.getAttribute(Attributes.MOVEMENT_SPEED);

                            if(movementSpeedInner != null) {
                                movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
                            }
                        });
                    }
                }

                break;
            case LOSE_ABILITIES:
                if(!BeyonderData.isBeyonder(player)) break;

                String pathway = BeyonderData.getPathway(player);
                int sequence = BeyonderData.getSequence(player);

                List<Ability> possibleAbilitiesToLose = LOTMCraft.abilityHandler.getByPathwayAndSequence(pathway, sequence).stream().filter(a -> !a.canAlwaysBeUsed).toList();
                if (possibleAbilitiesToLose.isEmpty()) break;

                Ability abilityToLose = possibleAbilitiesToLose.get(player.getRandom().nextInt(possibleAbilitiesToLose.size()));
                DisabledAbilitiesComponent component = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                component.disableSpecificAbilityForTime(abilityToLose.getId(), "artifact_lose_ability", 20 * 20);
                break;
            case STOP_TIME:
                TimeChangeEntity entity = new TimeChangeEntity(ModEntities.TIME_CHANGE.get(), player.level(), 20 * 5, null, 8, .001f);
                entity.setPos(player.position());
                player.level().addFreshEntity(entity);
                break;
        }
    }
    private void applyDoorEffects(Player player) {
        switch (type) {
            case FULL_MOON_WHISPERS: // Temporarily reused Whispers Code, may change later
                if (player.tickCount % getWhisperIntervalForSequence(sequence) == 0) {
                    player.displayClientMessage(Component.translatable("lotm.whisper." + player.getRandom().nextInt(5)), true);
                    SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
                    sanityComponent.increaseSanityAndSync(-0.01f * (10 - sequence), player);
                }
                break;
            case RANDOM_TELEPORT:
                if (player.tickCount % getTeleportIntervalForSequence(sequence) == 0) {
                    double range = 5 + (10 - sequence) * 2;
                    double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * range * 2;
                    player.teleportTo(x, player.getY(), z);
                }
                break;
        }
    }
    private void applySunEffects(Player player) {
        switch (type) {
            case SLOWER_IN_COLD_PLACES:
                if (player.tickCount % 200 == 0) {
                    if ((player.level().getBiome(player.blockPosition()).value().coldEnoughToSnow(player.blockPosition()))
                            || (getBlockInRadius(player, player.blockPosition(), 5 + getEffectLevelForSequence(sequence), Blocks.SNOW))) {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 220, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 220, getEffectLevelForSequence(sequence), false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 220, getEffectLevelForSequence(sequence), false, false));
                    }
                }
                break;
            case BURN:
                applyCommonEffects(player);
                break;
            case CONFLICT_WITH_ARTIFACTS:
                break;

        }
    }
    private void applyTyrantEffects(Player player) {
        switch (type) {
            case BREATH_DEPLETION, TARGETED_BY_ENTITIES:
                applyCommonEffects(player);
                break;
            case STRUCK_BY_LIGHTNING:
                if (player.tickCount % getIntervalForSequenceAndMultiplier(sequence, 3) == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverPlayer.serverLevel());

                        if (lightning != null) {
                            lightning.moveTo(player.position());
                            player.hurt(player.damageSources().lightningBolt(), Math.max(2, getEffectLevelForSequence(sequence)));
                            serverPlayer.serverLevel().addFreshEntity(lightning);
                        }
                    }
                }
                break;
            case WEAKNESS_WHEN_ALONE:
                if (player.tickCount % 20 == 0) {
                    boolean entitiesNearby = !player.level().getEntitiesOfClass(
                            LivingEntity.class,
                            player.getBoundingBox().inflate(8.0),
                            e -> e != player
                    ).isEmpty();
                    if (!entitiesNearby) {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2, false, false));
                    }
                }
                break;
        }
    }
    private void applyVisionaryEffects(Player player) {
        switch (type) {
            case MENTAL_PLAGUE:
                if (player.tickCount % 200 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.MENTAL_PLAGUE, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case SPIRIT_HAUNTING:
                if (player.tickCount % getSpiritHauntIntervalForSequence(sequence) == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        double vexHealth = 14 + (getEffectLevelForSequence(sequence) * 2);
                        double vexDamage = 4.0 + getEffectLevelForSequence(sequence) * 0.5;
                        for (int i = 0; i < 5; i++) {
                            Vex vex = EntityType.VEX.create(serverPlayer.serverLevel());
                            if (vex != null) {
                                vex.moveTo(
                                        player.getX() + (player.getRandom().nextDouble() - 0.5) * 4,
                                        player.getY() + 1,
                                        player.getZ() + (player.getRandom().nextDouble() - 0.5) * 4
                                );
                                AttributeInstance maxHealth = vex.getAttribute(Attributes.MAX_HEALTH);
                                if (maxHealth != null) maxHealth.setBaseValue(vexHealth);
                                vex.setHealth((float) vexHealth);
                                AttributeInstance attackDamage = vex.getAttribute(Attributes.ATTACK_DAMAGE);
                                if (attackDamage != null) attackDamage.setBaseValue(vexDamage);
                                serverPlayer.serverLevel().addFreshEntity(vex);
                            }
                        }
                    }
                }
                break;
        }
    }
    private void applyDemonessEffects(Player player) {
        switch (type) {
            case PETRIFICATION:
                if (player.tickCount % 200 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.PETRIFICATION, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case CHARM_BACKLASH:
                if (player.tickCount % 140 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, false));
                }
                break;
            case CURSED:
                switch(player.getRandom().nextInt(3)) {
                    case 0 -> {
                        player.hurt(player.damageSources().onFire(), (float) (DamageLookup.lookupDamage(4, .6)));
                        ParticleUtil.spawnParticles((ServerLevel) player.level(), ModParticles.BLACK_FLAME.get(), player.position().add(0, player.getEyeHeight() / 2, 0), 200, .4, player.getEyeHeight() / 2, .4, 0.01);
                    }
                    case 1 -> {
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 2, 3));
                        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 2, 3));
                    }
                }
                break;
        }
    }
    private void applyHunterEffects(Player player) {
        switch (type) {
            case TARGETED_BY_ENTITIES: applyCommonEffects(player);
                break;
            case WITHER:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case CRIMSON_CHAIN:
                if (player.tickCount % 20 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 1, false, false));
                }
                break;
        }
    }
    private void applyDarknessEffects(Player player) {
        switch (type) {
            case BLINDNESS:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case ASLEEP:
                if (player.tickCount % 200 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.ASLEEP, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
        }
    }
    private void applyMotherEffects(Player player) {
        switch (type) {
            case MUTATED:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(ModEffects.MUTATED, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case POISON:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.POISON, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case SILK_TRAP:
                if (player.tickCount % 300 == 0) {
                    BlockPos feet = player.blockPosition();
                    BlockPos head = feet.above();
                    if (player.level().getBlockState(feet).isAir()) {
                        player.level().setBlock(feet, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                    if (player.level().getBlockState(head).isAir()) {
                        player.level().setBlock(head, Blocks.COBWEB.defaultBlockState(), 3);
                    }
                }
                break;
        }
    }
    private void applyMonsterEffects(Player player) {
        switch (type) {
            case BAD_LUCK:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case CALAMITY_ATTRACTION:
                break;
            case FATE_SPIN:
                if (player.tickCount % 300 == 0) {
                    int roll = player.getRandom().nextInt(5);
                    switch (roll) {
                        case 0 -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0, false, false));
                        case 1 -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false));
                        case 2 -> player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1, false, false));
                        case 3 -> player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 1, false, false));
                        case 4 -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, false, false));
                    }
                }
                break;
        }
    }
    private void applyAbyssEffects(Player player) {
        switch (type) {
            case NAUSEA:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
        }
    }

    private void applyCommonEffects(Player player) {
        switch (type) {
            case BREATH_DEPLETION:
                if (player.tickCount % getIntervalForSequenceAndMultiplier(sequence, 1) == 0) {
                    player.setAirSupply(0);
                    player.hurt(player.damageSources().drown(), Math.max(1, 1 + getEffectLevelForSequence(sequence)/2));
                }
                break;
            case BURN:
                if (player.tickCount % getIntervalForSequenceAndMultiplier(sequence, 1) == 0) {
                    player.setRemainingFireTicks(40);
                    player.hurt(player.damageSources().onFire(), Math.max(1, 1 + getEffectLevelForSequence(sequence)/2));
                }
                break;
            case TARGETED_BY_ENTITIES:
                if (player.tickCount % getIntervalForSequenceAndMultiplier(sequence, 2) == 0) {
                    for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(10 + ((9 - sequence) * 3)))) {
                        mob.setTarget(player);
                    }}
                break;

        }
    }

    private void applyGeneralEffects(Player player) {
        switch (type) {
            case SLOWNESS:
                if (player.tickCount % 80 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case HEAR_SOUNDS:
                if (player.tickCount % getIntervalForSequenceAndMultiplier(sequence, 1) == 0) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.LOUD_SOUND_1.get());

                        serverPlayer.connection.send(new ClientboundSoundPacket(
                                holder,
                                SoundSource.AMBIENT,
                                player.getX(),
                                player.getY(),
                                player.getZ(),
                                1.0f,
                                1.0f,
                                player.level().getRandom().nextLong()
                        ));
                    }
                }
                break;
            case MINING_FATIGUE:
                if (player.tickCount % 100 == 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, getEffectLevelForSequence(sequence), false, false));
                }
                break;
            case DRAIN_HEALTH:
                if (player.tickCount % 20 == 0) {
                    player.hurt(player.damageSources().magic(), (float) DamageLookup.lookupDamage(sequence >= 0 ? Math.clamp(sequence - 1, 0, 9) : -1, 1f) * (float) BeyonderData.getMultiplierForSequence(sequence));
                }
                break;
            case NEAR_DEATH_PULSE:
                if (sequence <= 2) {
                    int ndpInterval = (sequence == 1) ? 20 * 15 : 20 * 30;
                    if (player.tickCount % ndpInterval == 0) {
                        float killChance = (sequence == 1) ? 0.12f : 0.06f;
                        if (player.getRandom().nextFloat() < killChance) {
                            player.hurt(player.damageSources().magic(), player.getMaxHealth() * 2);
                        } else {
                            float damage = Math.max(0, player.getHealth() - 1.0f);
                            if (damage > 0) player.hurt(player.damageSources().magic(), damage);
                        }
                    }
                }
                break;
            case HEART_STOP:
                if (sequence <= 2) {
                    int hsInterval = (sequence == 1) ? 20 * 20 : 20 * 40;
                    if (player.tickCount % hsInterval == 0) {
                        int amplifier = (sequence == 1) ? 2 : 1;
                        player.addEffect(new MobEffectInstance(MobEffects.HARM, 1, amplifier, false, true));
                    }
                }
                break;
            case DRAIN_HUNGER:
                if (player.tickCount % 20 == 0) {
                    player.getFoodData().setFoodLevel(player.getFoodData().getFoodLevel() - 1);
                }
                break;
            case HEARING_WHISPERS:
                if (player.tickCount % getWhisperIntervalForSequence(sequence) == 0) {
                    player.displayClientMessage(Component.translatable("lotm.whisper." + player.getRandom().nextInt(5)), true);
                    SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
                    sanityComponent.increaseSanityAndSync(-0.01f * (10 - sequence), player);
                }
                break;
        }
    }


    private static List<NegativeEffectType> getPathwayEffects(String pathway, int sequence) {
        return switch (pathway) {
            case "fool" -> Stream.of(
                    NegativeEffectType.BURN,
                    NegativeEffectType.BREATH_DEPLETION,
                    NegativeEffectType.SLOWER_IN_HOT_PLACES,
                    (sequence <= 4) ? NegativeEffectType.TURN_TO_MARIONETTE : null,
                    (sequence <= 2) ? NegativeEffectType.WISH_CALAMITY : null
            ).filter(Objects::nonNull).toList();

            case "error" -> Stream.of(
                    NegativeEffectType.GOLD_ITEM_DEBUFF,
                    (sequence <= 6) ? NegativeEffectType.LOSE_ABILITIES : null,
                    (sequence <= 4) ? NegativeEffectType.LOSE_CONCEPTS : null,
                    (sequence <= 2) ? NegativeEffectType.STOP_TIME : null
            ).filter(Objects::nonNull).toList();

            case "door" -> Stream.of(
                    NegativeEffectType.FULL_MOON_WHISPERS,
                    (sequence <= 5) ? NegativeEffectType.RANDOM_TELEPORT : null
            ).filter(Objects::nonNull).toList();

            case "sun" -> Stream.of(
                    NegativeEffectType.BURN,
                    NegativeEffectType.SLOWER_IN_COLD_PLACES,
                    (sequence <= 4) ? NegativeEffectType.CONFLICT_WITH_ARTIFACTS : null
            ).filter(Objects::nonNull).toList();

            case "tyrant" -> Stream.of(
                    NegativeEffectType.BREATH_DEPLETION,
                    NegativeEffectType.WEAKNESS_WHEN_ALONE,
                    (sequence <= 6) ? NegativeEffectType.TARGETED_BY_ENTITIES : null,
                    (sequence <= 4) ? NegativeEffectType.STRUCK_BY_LIGHTNING : null
            ).filter(Objects::nonNull).toList();

            case "visionary" -> Stream.of(
                    NegativeEffectType.MENTAL_PLAGUE,
                    NegativeEffectType.SPIRIT_HAUNTING
            ).filter(Objects::nonNull).toList();

            case "demoness" -> Stream.of(
                    NegativeEffectType.PETRIFICATION,
                    NegativeEffectType.CURSED,
                    NegativeEffectType.CHARM_BACKLASH
            ).filter(Objects::nonNull).toList();

            case "red_priest" -> Stream.of(
                    NegativeEffectType.TARGETED_BY_ENTITIES,
                    NegativeEffectType.WITHER,
                    NegativeEffectType.CRIMSON_CHAIN
            ).filter(Objects::nonNull).toList();

            case "darkness" -> Stream.of(
                    NegativeEffectType.BLINDNESS,
                    NegativeEffectType.ASLEEP,
                    NegativeEffectType.CURSED
            ).filter(Objects::nonNull).toList();

            case "mother" -> Stream.of(
                    NegativeEffectType.POISON,
                    NegativeEffectType.MUTATED,
                    NegativeEffectType.SILK_TRAP
            ).filter(Objects::nonNull).toList();

            case "wheel_of_fortune" -> Stream.of(
                    NegativeEffectType.BAD_LUCK,
                    NegativeEffectType.CALAMITY_ATTRACTION,
                    NegativeEffectType.FATE_SPIN
            ).filter(Objects::nonNull).toList();

            case "abyss" -> Stream.of(
                    NegativeEffectType.NAUSEA
            ).filter(Objects::nonNull).toList();

            default -> List.of(
                    NegativeEffectType.DRAIN_HEALTH,
                    NegativeEffectType.DRAIN_HUNGER,
                    NegativeEffectType.HEARING_WHISPERS,
                    NegativeEffectType.SLOWNESS,
                    NegativeEffectType.MINING_FATIGUE,
                    NegativeEffectType.HEAR_SOUNDS,
                    NegativeEffectType.NEAR_DEATH_PULSE,
                    NegativeEffectType.HEART_STOP
            );
        };
    }

    /**
     * Applies the negative effect to a player
     * @param player The player holding the sealed artifact
     * @param inMainHand Whether the artifact is in the main hand
     */

    public void apply(Player player, boolean inMainHand, List<String> pathway) {
        if (pathway.contains("fool")) applyFoolEffects(player);
        else if (pathway.contains("error")) applyErrorEffects(player);
        else if (pathway.contains("door")) applyDoorEffects(player);
        else if (pathway.contains("sun")) applySunEffects(player);
        else if (pathway.contains("tyrant")) applyTyrantEffects(player);
        else if (pathway.contains("visionary")) applyVisionaryEffects(player);
        else if (pathway.contains("demoness")) applyDemonessEffects(player);
        else if (pathway.contains("hunter")) applyHunterEffects(player);
        else if (pathway.contains("darkness")) applyDarknessEffects(player);
        else if (pathway.contains("mother")) applyMotherEffects(player);
        else if (pathway.contains("monster")) applyMonsterEffects(player);
        else if (pathway.contains("abyss")) applyAbyssEffects(player);
        else applyGeneralEffects(player);
    }

    private int getIntervalForSequenceAndMultiplier(int sequence, int multiplier) {
        if (multiplier == 1){
            return switch (sequence) {
                case 8, 7, 6, 5 -> 20 * 10;
                case 4, 3 -> 20 * 6;
                case 2, 1 -> 20 * 3;
                default -> 20 * 10;
            };
        } else if(multiplier == 2){
            return switch (sequence) {
                case 8, 7 -> 20 * 10;
                case 6, 5 -> 20 * 8;
                case 4, 3 -> 20 * 6;
                case 2, 1 -> 20 * 4;
                default -> 20 * 10;
            };
        } else if(multiplier == 3){
            return switch (sequence) {
                case 8, 7 -> 20 * 10;
                case 6, 5 -> 20 * 8;
                case 4, 3 -> 20 * 6;
                case 2 -> 20 * 4;
                case 1 -> 20 * 2;
                default -> 20 * 10;
            };
        }
        return 20;
    }

    private int getEffectLevelForSequence(int sequence) {
        return switch (sequence) {
            case 8 -> 1;
            case 7 -> 2;
            case 6 -> 3;
            case 5 -> 4;
            case 4 -> 6;
            case 3 -> 8;
            case 2 -> 11;
            case 1 -> 15;
            default -> 1;
        };
    }

    private int getTeleportIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8 -> 20 * 25;
            case 7 -> 20 * 22;
            case 6, 5 -> 20 * 20;
            case 4, 3 -> 20 * 9;
            case 2 -> 20 * 4;
            case 1 -> 50;
            default -> 20 * 30;
        };
    }

    private int getWhisperIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8 -> 20 * 8;
            case 7 -> 20 * 7;
            case 6, 5 -> 20 * 5;
            case 4, 3 -> 20;
            case 2 -> 10;
            case 1 -> 5;
            default -> 20 * 10;
        };
    }

    private int getSpiritHauntIntervalForSequence(int sequence) {
        return switch (sequence) {
            case 8, 9 -> 20 * 120;
            case 6, 7 -> 20 * 90;
            case 4, 5 -> 20 * 60;
            case 2, 3 -> 20 * 40;
            case 1 -> 20 * 12;
            default -> 20 * 120;
        };
    }

    public Component getDisplayName() {
        return Component.translatable("lotm.negative_effect." + type.name().toLowerCase());
    }

    public NegativeEffectType getType() {
        return type;
    }

    public int getSequence() {
        return sequence;
    }

    /**
     * Creates a random negative effect appropriate for a pathway and sequence
     */
    public static List<NegativeEffect> createRandom(String pathway, int sequence, Random random, String baseItem) {
        List<NegativeEffectType> pathwayEffects = new ArrayList<>(getPathwayEffects(pathway, sequence));
        List<NegativeEffectType> defaultEffects = new ArrayList<>(getPathwayEffects("default", sequence));

        Collections.shuffle(pathwayEffects, random);
        Collections.shuffle(defaultEffects, random);

        int totalEffects = (sequence <= 1) ? 5 :
                    (sequence <= 2) ? 4 :
                    (sequence <= 4) ? 3 :
                    (sequence <= 7) ? 2 : 1;

        if (baseItem.equals("star")) {
            totalEffects -= 2;
        } else if (baseItem.equals("gem")) {
            totalEffects -= 1;
        }

        totalEffects = Math.max(0, totalEffects);

        List<NegativeEffect> finalEffects = new ArrayList<>();

        int fromPathway = Math.min(totalEffects, pathwayEffects.size());
        for (int i = 0; i < fromPathway; i++) {
            finalEffects.add(new NegativeEffect(pathwayEffects.get(i), sequence, null, sequence));
        }

        int remainingSlots = totalEffects - fromPathway;
        if (remainingSlots > 0) {
            for (int i = 0; i < Math.min(remainingSlots, defaultEffects.size()); i++) {
                finalEffects.add(new NegativeEffect(defaultEffects.get(i), sequence, null, sequence));
            }
        }

        return finalEffects;
    }

    public enum NegativeEffectType {
        // seer
        SLOWER_IN_HOT_PLACES,
        TURN_TO_MARIONETTE,
        WISH_CALAMITY,

        // error
        GOLD_ITEM_DEBUFF,
        LOSE_CONCEPTS,
        LOSE_ABILITIES,
        STOP_TIME,

        // door
        FULL_MOON_WHISPERS,
        RANDOM_TELEPORT,

        // sun
        SLOWER_IN_COLD_PLACES,
        BURN,
        CONFLICT_WITH_ARTIFACTS,

        // tyrant
        BREATH_DEPLETION,
        STRUCK_BY_LIGHTNING,
        WEAKNESS_WHEN_ALONE,

        // spectator
        MENTAL_PLAGUE,
        SPIRIT_HAUNTING,

        // demoness
        PETRIFICATION,
        CHARM_BACKLASH,

        // hunter
        WITHER,
        TARGETED_BY_ENTITIES,
        CRIMSON_CHAIN,

        // darkness
        BLINDNESS,
        ASLEEP,
        CURSED,

        // mother
        POISON,
        MUTATED,
        SILK_TRAP,

        // monster
        BAD_LUCK,
        CALAMITY_ATTRACTION,
        FATE_SPIN,

        // abyss
        NAUSEA,

        // general
        DRAIN_HEALTH,
        DRAIN_HUNGER,
        HEARING_WHISPERS,
        SLOWNESS,
        MINING_FATIGUE,
        HEAR_SOUNDS,
        NEAR_DEATH_PULSE,
        HEART_STOP;
    }

    public static List<NegativeEffect.NegativeEffectType> handOnlyTick = List.of(
            NegativeEffect.NegativeEffectType.RANDOM_TELEPORT,
            NegativeEffect.NegativeEffectType.DRAIN_HEALTH
    );

    public static List<NegativeEffect.NegativeEffectType> useOnlyTick = List.of(
            NegativeEffect.NegativeEffectType.TURN_TO_MARIONETTE,
            NegativeEffect.NegativeEffectType.WISH_CALAMITY,
            NegativeEffect.NegativeEffectType.STOP_TIME,
            NegativeEffect.NegativeEffectType.LOSE_ABILITIES,
            NegativeEffect.NegativeEffectType.CONFLICT_WITH_ARTIFACTS,
            NegativeEffect.NegativeEffectType.CURSED,
            NegativeEffect.NegativeEffectType.CALAMITY_ATTRACTION
    );

    public static List<NegativeEffect.NegativeEffectType> hotBarOnlyTick = List.of(
            NegativeEffect.NegativeEffectType.SLOWER_IN_HOT_PLACES,
            NegativeEffect.NegativeEffectType.GOLD_ITEM_DEBUFF,
            NegativeEffect.NegativeEffectType.LOSE_CONCEPTS,
            NegativeEffect.NegativeEffectType.SLOWER_IN_COLD_PLACES,
            NegativeEffect.NegativeEffectType.TARGETED_BY_ENTITIES,
            NegativeEffect.NegativeEffectType.MENTAL_PLAGUE,
            NegativeEffect.NegativeEffectType.PETRIFICATION,
            NegativeEffect.NegativeEffectType.ASLEEP,
            NegativeEffect.NegativeEffectType.MUTATED,
            NegativeEffect.NegativeEffectType.BAD_LUCK,
            NegativeEffect.NegativeEffectType.SILK_TRAP,
            NegativeEffect.NegativeEffectType.FATE_SPIN,
            NegativeEffect.NegativeEffectType.SPIRIT_HAUNTING,
            NegativeEffect.NegativeEffectType.DRAIN_HUNGER,
            NegativeEffect.NegativeEffectType.CRIMSON_CHAIN,
            NegativeEffect.NegativeEffectType.HEARING_WHISPERS,
            NegativeEffect.NegativeEffectType.NEAR_DEATH_PULSE,
            NegativeEffect.NegativeEffectType.HEART_STOP
    );

    private static boolean getBlockInRadius (Player player, BlockPos center, int radius, Block block){
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            if (player.level().getBlockState(pos).is(block)) {
                return true;
            }
        }
        return false;
    }
}
