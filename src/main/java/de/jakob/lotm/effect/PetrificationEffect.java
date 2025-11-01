package de.jakob.lotm.effect;


import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class PetrificationEffect extends MobEffect {

    protected PetrificationEffect(MobEffectCategory category, int color) {
        super(category, color);

        // Dummy attribute modifier to trigger client-side handling without affecting movement speed
        // Also using event handler to sync effect periodically
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "petrification_sync_dummy"),
                -10.0D, // No actual effect on speed
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        livingEntity.setDeltaMovement(new Vec3(0, 0, 0));
        livingEntity.hurtMarked = true;

        BeyonderData.disableAbilityUse(livingEntity, "petrification");
        ServerScheduler.scheduleDelayed(20 * 2, () -> {
            if(!livingEntity.hasEffect(ModEffects.PETRIFICATION)) {
                BeyonderData.enableAbilityUse(livingEntity, "petrification");
            }
        });
        return true;
    }



    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }




}