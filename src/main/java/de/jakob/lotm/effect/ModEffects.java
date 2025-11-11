package de.jakob.lotm.effect;

import de.jakob.lotm.LOTMCraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, LOTMCraft.MOD_ID);

    public static final Holder<MobEffect> ASLEEP = MOB_EFFECTS.register("asleep",
            () -> new AsleepEffect(MobEffectCategory.HARMFUL, 0x2E2E5C)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "asleep"), -10f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)); // Dark blue color

    public static final Holder<MobEffect> LUCK = MOB_EFFECTS.register("luck",
            () -> new LuckEffect(MobEffectCategory.BENEFICIAL, 0xdaeaf5));

    public static final Holder<MobEffect> UNLUCK = MOB_EFFECTS.register("unluck",
            () -> new UnluckEffect(MobEffectCategory.HARMFUL, 0xf0b05d));

    public static final Holder<MobEffect> PETRIFICATION = MOB_EFFECTS.register("petrification",
            () -> new PetrificationEffect(MobEffectCategory.HARMFUL, 0x7532a8));

    public static final Holder<MobEffect> CONQUERED = MOB_EFFECTS.register("conquered",
            () -> new ConqueredEffect(MobEffectCategory.HARMFUL, 0x701010)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "conquered"), -20f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));


    public static final Holder<MobEffect> LOOSING_CONTROL = MOB_EFFECTS.register("loosing_control",
            () -> new LoosingControlEffect(MobEffectCategory.HARMFUL, 0x493269)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "loosing_control"), -.45f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

}
