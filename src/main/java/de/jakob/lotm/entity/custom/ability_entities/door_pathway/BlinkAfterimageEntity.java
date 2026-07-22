package de.jakob.lotm.entity.custom.ability_entities.door_pathway;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.goals.AbilityUseGoal;
import de.jakob.lotm.events.custom.TargetEntityEvent;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BlinkAfterimageEntity extends Mob {

    private static final int LIFETIME = 40;

    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(BlinkAfterimageEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private final LivingEntity owner;
    private final Ability abilityToUse;

    public BlinkAfterimageEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.owner = null;
        this.abilityToUse = null;
    }

    public BlinkAfterimageEntity(Level level, Vec3 pos, LivingEntity owner, @Nullable Ability ability) {
        super(ModEntities.BLINK_AFTERIMAGE.get(), level);
        this.owner = owner;
        this.abilityToUse = ability;
        setPos(pos);
        if (owner != null) {
            this.entityData.set(OWNER_UUID, Optional.of(owner.getUUID()));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_UUID, Optional.empty());
    }

    @Override
    public void onAddedToLevel() {
        if(owner != null) {
            AllyUtil.makeAllies(owner, this, false);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if(this.tickCount == (LIFETIME / 2) && abilityToUse != null && !level().isClientSide) {
            abilityToUse.useAbility((ServerLevel) level(), this, false, false, false, false);
        }

        if(this.tickCount >= LIFETIME) {
            this.discard();
            return;
        }
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(OWNER_UUID).orElse(null);
    }

    public ResourceLocation getDefaultSkinTexture() {
        return ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID,
                "textures/entity/npc/amon.png");
    }

    @SubscribeEvent
    public static void onTargetSelected(TargetEntityEvent event) {
        if(event.getSourceEntity() instanceof BlinkAfterimageEntity blinkAfterimage) {
            event.setTargetEntity(blinkAfterimage.getTarget());
        }
    }
}