package de.jakob.lotm.entity.custom.ability_entities;

import com.mojang.authlib.GameProfile;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ControlBodyDouble extends Mob {

    @Nullable
    private ChunkPos forcedChunkPos = null;

    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(ControlBodyDouble.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Boolean> MAKE_INVISIBLE =
            SynchedEntityData.defineId(ControlBodyDouble.class, EntityDataSerializers.BOOLEAN);

    private boolean cancelling = false;
    private final List<HeldEffect> heldEffects = new ArrayList<>();

    public ControlBodyDouble(EntityType<? extends ControlBodyDouble> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        forceCurrentChunk();
    }

    @Override
    public void onRemovedFromLevel() {
        releaseForcedChunk();
        super.onRemovedFromLevel();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            ChunkPos currentChunk = new ChunkPos(this.blockPosition());
            if (this.forcedChunkPos == null || !this.forcedChunkPos.equals(currentChunk)) {
                forceCurrentChunk();
            }

            tickHeldEffects();
        }
    }

    private void tickHeldEffects() {
        heldEffects.removeIf(held -> {
            held.duration--;
            return held.duration <= 0;
        });
    }

    private void forceCurrentChunk() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ChunkPos newChunk = new ChunkPos(this.blockPosition());
        if (this.forcedChunkPos != null && this.forcedChunkPos.equals(newChunk)) {
            return;
        }
        releaseForcedChunk();
        serverLevel.setChunkForced(newChunk.x, newChunk.z, true);
        this.forcedChunkPos = newChunk;
    }

    private void releaseForcedChunk() {
        if (this.forcedChunkPos != null && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.setChunkForced(this.forcedChunkPos.x, this.forcedChunkPos.z, false);
        }
        this.forcedChunkPos = null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(MAKE_INVISIBLE, false);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(uuid));
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    @Nullable
    public Player getOwner() {
        UUID id = getOwnerUUID();
        if (id == null) return null;
        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(id);
            if (entity instanceof Player living) {
                return living;
            }
        }
        return null;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID owner = getOwnerUUID();
        if (owner != null) {
            tag.putUUID("OwnerUUID", owner);
        }

        ListTag effectsTag = new ListTag();
        for (HeldEffect held : heldEffects) {
            effectsTag.add(held.save());
        }
        tag.put("HeldEffects", effectsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("OwnerUUID")) {
            setOwnerUUID(tag.getUUID("OwnerUUID"));
        }

        heldEffects.clear();
        if (tag.contains("HeldEffects", Tag.TAG_LIST)) {
            ListTag effectsTag = tag.getList("HeldEffects", Tag.TAG_COMPOUND);
            for (int i = 0; i < effectsTag.size(); i++) {
                HeldEffect held = HeldEffect.load(effectsTag.getCompound(i));
                if (held != null) {
                    heldEffects.add(held);
                }
            }
        }
    }

    public static ControlBodyDouble create(Level level, Player player, boolean makeInvisible) {
        ControlBodyDouble bodyDouble = new ControlBodyDouble(ModEntities.CONTROL_BODY_DOUBLE.get(), level);

        bodyDouble.setOwnerUUID(player.getUUID());
        bodyDouble.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        bodyDouble.yBodyRot = player.yBodyRot;
        bodyDouble.yHeadRot = player.yHeadRot;

        bodyDouble.copyAttributesAndHealthFrom(player);
        bodyDouble.copyEquipmentFrom(player);
        bodyDouble.copyMobEffectsFrom(player);

        bodyDouble.entityData.set(MAKE_INVISIBLE, makeInvisible);

        return bodyDouble;
    }

    public boolean renderInvisible() {
        return this.entityData.get(MAKE_INVISIBLE);
    }

    public void copyAttributesAndHealthFrom(LivingEntity source) {
        for (Holder<Attribute> attributeHolder : BuiltInRegistries.ATTRIBUTE.asHolderIdMap()) {

            if (attributeHolder.is(Attributes.MOVEMENT_SPEED)) {
                continue;
            }

            AttributeInstance sourceInstance = source.getAttribute(attributeHolder);
            AttributeInstance targetInstance = getAttribute(attributeHolder);

            if (sourceInstance != null && targetInstance != null) {
                targetInstance.setBaseValue(sourceInstance.getBaseValue());
            }
        }

        if(source.getAttribute(Attributes.MAX_HEALTH) == null && getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(source.getMaxHealth());
        }

        BeyonderData.setBeyonder(this, BeyonderData.getPathway(source), BeyonderData.getSequence(source), true, true, false, true, true, false);
        float maxHealth = getMaxHealth();
        float sourceHealth = source.getHealth();
        float newHealth = Math.min(sourceHealth, maxHealth);
        ServerScheduler.scheduleDelayed(20, () -> { // delayed to ensure that the health is set after the enhancements from being a beyonder are applied

            setHealth(newHealth);
        });
    }

    public void copyMobEffectsFrom(LivingEntity source) {
        heldEffects.clear();
        for (MobEffectInstance activeEffect : source.getActiveEffects()) {
            heldEffects.add(HeldEffect.fromInstance(activeEffect));
        }
    }

    public void reapplyHeldEffectsTo(LivingEntity target) {
        for (HeldEffect held : heldEffects) {
            MobEffectInstance instance = held.toInstance();
            if (instance != null) {
                target.addEffect(instance);
            }
        }
        heldEffects.clear();
    }


    // Copied from Player class
    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_DAMAGE, (double)1.0F).add(Attributes.MOVEMENT_SPEED, (double)0.1F).add(Attributes.ATTACK_SPEED).add(Attributes.LUCK).add(Attributes.BLOCK_INTERACTION_RANGE, (double)4.5F).add(Attributes.ENTITY_INTERACTION_RANGE, (double)3.0F).add(Attributes.BLOCK_BREAK_SPEED).add(Attributes.SUBMERGED_MINING_SPEED).add(Attributes.SNEAKING_SPEED).add(Attributes.MINING_EFFICIENCY).add(Attributes.FOLLOW_RANGE).add(Attributes.SWEEPING_DAMAGE_RATIO).add(NeoForgeMod.CREATIVE_FLIGHT);
    }


    public void copyEquipmentFrom(Player player) {
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            this.setItemSlot(slot, player.getItemBySlot(slot).copy());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !this.cancelling) {
            this.cancelling = true;
            Player owner = getOwner();
            if (owner instanceof ServerPlayer serverPlayer) {
                ControllingUtils.cancel(serverPlayer, amount, true, false);
            }
        }
        return super.hurt(source, amount);
    }

    private static class HeldEffect {
        final Holder<MobEffect> effect;
        final int amplifier;
        int duration;
        final boolean ambient;
        final boolean visible;
        final boolean showIcon;

        private HeldEffect(Holder<MobEffect> effect, int amplifier, int duration, boolean ambient, boolean visible, boolean showIcon) {
            this.effect = effect;
            this.amplifier = amplifier;
            this.duration = duration;
            this.ambient = ambient;
            this.visible = visible;
            this.showIcon = showIcon;
        }

        static HeldEffect fromInstance(MobEffectInstance instance) {
            return new HeldEffect(
                    instance.getEffect(),
                    instance.getAmplifier(),
                    instance.getDuration(),
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon()
            );
        }

        @Nullable
        MobEffectInstance toInstance() {
            if (duration <= 0) return null;
            return new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon);
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
            if (id != null) {
                tag.putString("Id", id.toString());
            }
            tag.putInt("Amplifier", amplifier);
            tag.putInt("Duration", duration);
            tag.putBoolean("Ambient", ambient);
            tag.putBoolean("Visible", visible);
            tag.putBoolean("ShowIcon", showIcon);
            return tag;
        }

        @Nullable
        static HeldEffect load(CompoundTag tag) {
            if (!tag.contains("Id")) return null;
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Id"));
            if (id == null || !BuiltInRegistries.MOB_EFFECT.containsKey(id)) return null;

            MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(id);
            if (mobEffect == null) return null;

            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect);

            int duration = tag.getInt("Duration");
            if (duration <= 0) return null;

            return new HeldEffect(
                    holder,
                    tag.getInt("Amplifier"),
                    duration,
                    tag.getBoolean("Ambient"),
                    tag.getBoolean("Visible"),
                    tag.getBoolean("ShowIcon")
            );
        }
    }
}