package de.jakob.lotm.abilities.black_emperor;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.helper.RingEffectManager;

import java.util.*;

//@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BriberAbility extends SelectableAbility {


    // This tracks who is currently weakened so their outgoing damage can be reduced too.
    private static final Set<UUID> WEAKENED = new HashSet<>();

    private static final ResourceLocation WEAKEN_ARMOR_ID =
            ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "briber_weaken_armor");

    private static final UUID WEAKEN_ARMOR_UUID =
            UUID.fromString("6d3f58b2-2d0e-4f7d-bd31-6f8f3f61c2a1");

    private static final Map<UUID, UUID> CHARMED = new HashMap<>();
    private static final Map<UUID, UUID> ARROGANCE = new HashMap<>();

    public BriberAbility(String id) {
        super(id, 8.5f);
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("black_emperor", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 125;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "Weaken",
                "Arrogance",
                "Charm"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHandItem.isEmpty()) {
            AbilityUtil.sendActionBar(entity, Component.literal("Put an item in your offhand first.").withColor(0xFF5555));
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(entity, 18*(int) Math.max(multiplier(entity)/4,1), 1.5f);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.literal("No target found.").withColor(0xFF5555));
            return;
        }

        if (!canAffectTarget(entity, target)) {
            AbilityUtil.sendActionBar(entity, Component.literal("Ability failed.").withColor(0xFF5555));
            return;
        }

        // Shared cast burst for all three bribe styles.
        RingEffectManager.createRingForAll(target.position(), 2.0f, 16,
                0.68f, 0.42f, 0.88f, 1.0f, 0.14f, 0.55f, serverLevel);
        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.9, 14);

        transferOffhandItem(target, offHandItem);

        switch (abilityIndex) {
            case 0 -> applyWeaken(serverLevel, target);
            case 1 -> applyArrogance(serverLevel, entity, target);
            case 2 -> applyCharm(serverLevel, entity, target);
            default -> AbilityUtil.sendActionBar(entity, Component.literal("Unknown bribe sub-ability.").withColor(0xFF5555));
        }

        offHandItem.setCount(0);
    }

    private boolean canAffectTarget(LivingEntity caster, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) {
            return true;
        }

        int casterSeq = BeyonderData.getSequence(caster);
        int targetSeq = BeyonderData.getSequence(target);

        if (targetSeq >= casterSeq) {
            return true;
        }

        if (targetSeq == 0 && casterSeq != 0) {
            return false;
        }

        int diff = casterSeq - targetSeq;

        if (diff == 1) {
            return random.nextFloat() < 0.5f;
        }

        return false;
    }

    private void transferOffhandItem(LivingEntity target, ItemStack offHandItem) {
        if (offHandItem.isEmpty()) {
            return;
        }

        var capability = target.getCapability(Capabilities.ItemHandler.ENTITY);

        if (capability != null) {
            ItemStack toInsert = offHandItem.copy();

            for (int i = 0; i < capability.getSlots(); i++) {
                toInsert = capability.insertItem(i, toInsert, false);
                if (toInsert.isEmpty()) {
                    break;
                }
            }

            if (!toInsert.isEmpty()) {
                target.spawnAtLocation(toInsert);
            }
        } else {
            target.spawnAtLocation(offHandItem.copy());
        }
    }


    // This applies the debuff to the target and also marks them so their outgoing damage is reduced.
    private void applyWeaken(ServerLevel serverLevel, LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 30, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 30, 0, false, false, true));

        // Reduce armor while the weaken effect is active.
        AttributeInstance armor = target.getAttribute(Attributes.ARMOR);
        if (armor != null) {
            AttributeModifier modifier = new AttributeModifier(
                    WEAKEN_ARMOR_ID,
                    -0.20D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

            armor.removeModifier(WEAKEN_ARMOR_ID);
            armor.addTransientModifier(modifier);
        }

        // Mark this target as weakened so their outgoing melee and ability damage is reduced.
        UUID targetId = target.getUUID();
        WEAKENED.add(targetId);

        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 1.0, 16);
        RingEffectManager.createRingForAll(target.position(), 2.2f, 14,
                0.18f, 0.10f, 0.18f, 0.9f, 0.12f, 0.5f, serverLevel);

        ServerScheduler.scheduleDelayed(20 * 30, () -> {
            AttributeInstance liveArmor = target.getAttribute(Attributes.ARMOR);
            if (liveArmor != null) {
                liveArmor.removeModifier(WEAKEN_ARMOR_ID);
            }

            // Remove the weaken flag after the duration ends.
            WEAKENED.remove(targetId);
        }, serverLevel);
    }

    private void applyArrogance(ServerLevel serverLevel, LivingEntity caster, LivingEntity target) {
        ARROGANCE.put(target.getUUID(), caster.getUUID());

        ServerScheduler.scheduleForDuration(
                0,
                20 * 10,
                20 * 30,
                () -> {
                    UUID mappedCaster = ARROGANCE.get(target.getUUID());
                    if (mappedCaster == null || !mappedCaster.equals(caster.getUUID())) {
                        return;
                    }

                    ParticleUtil.createParticleSpirals(serverLevel, ModParticles.LIGHTNING.get(),
                            target.position().add(0, 0.5, 0),
                            0.25, 0.9, 1.4, 0.07, 8, 24, 2, 2);

                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, true));

                    target.setDeltaMovement(
                            target.getDeltaMovement().add(
                                    (serverLevel.random.nextDouble() - 0.5D) * 0.12D,
                                    0.0D,
                                    (serverLevel.random.nextDouble() - 0.5D) * 0.12D
                            )
                    );
                },
                () -> {
                    UUID mappedCaster = ARROGANCE.get(target.getUUID());
                    if (mappedCaster != null && mappedCaster.equals(caster.getUUID())) {
                        ARROGANCE.remove(target.getUUID());
                    }
                },
                serverLevel
        );
    }

    private void applyCharm(ServerLevel serverLevel, LivingEntity caster, LivingEntity target) {
        CHARMED.put(target.getUUID(), caster.getUUID());

        ParticleUtil.spawnSphereParticles(serverLevel, ModParticles.BLACK.get(),
                target.position().add(0, 1, 0), 0.8, 10);
        RingEffectManager.createRingForAll(target.position(), 1.8f, 12,
                0.72f, 0.45f, 0.92f, 1.0f, 0.10f, 0.45f, serverLevel);

        ServerScheduler.scheduleForDuration(
                0,
                10,
                20 * 30,
                () -> {
                    UUID mappedCaster = CHARMED.get(target.getUUID());
                    if (mappedCaster == null || !mappedCaster.equals(caster.getUUID())) {
                        return;
                    }

                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 2, false, false, true));
                },
                () -> {
                    UUID mappedCaster = CHARMED.get(target.getUUID());
                    if (mappedCaster != null && mappedCaster.equals(caster.getUUID())) {
                        CHARMED.remove(target.getUUID());
                    }
                },
                serverLevel
        );
    }

    // This resolves the real attacker for melee, projectiles, and many ability hits.
    private static LivingEntity resolveDamageDealer(DamageSource source) {
        Entity causingEntity = source.getEntity();
        if (causingEntity instanceof LivingEntity living) {
            return living;
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof LivingEntity living) {
            return living;
        }

        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {

        // It catches the actual attacker for melee and ability damage.
        LivingEntity attacker = resolveDamageDealer(event.getSource());
        if (attacker == null) {
            return;
        }

        LivingEntity victim = event.getEntity();

        // Charm: if the charmed target attacks the caster, cancel the hit and break the charm.
        UUID charmedCaster = CHARMED.get(attacker.getUUID());
        if (charmedCaster != null && charmedCaster.equals(victim.getUUID())) {
            CHARMED.remove(attacker.getUUID());
            event.setNewDamage(0);
            return;
        }

        // Arrogance: the attacker can completely miss the victim.
        // This now applies to ability damage too, not just melee.
        UUID arroganceCaster = ARROGANCE.get(attacker.getUUID());
        if (arroganceCaster != null && arroganceCaster.equals(victim.getUUID())) {
            if (victim.level().random.nextFloat() < 0.30f) {
                event.setNewDamage(0);


                // This shows a message when the dodge triggers.
                if (victim instanceof ServerPlayer player) {
                    AbilityUtil.sendActionBar(
                            player,
                            Component.literal("The enemy missed due to your arrogance.")
                                    .withColor(0xD8A0FF)
                    );
                }

                return;
            }
        }

        // Weaken: the weakened attacker deals less outgoing damage, including ability damage.
        if (WEAKENED.contains(attacker.getUUID())) {
            event.setNewDamage(event.getNewDamage() * 0.80F);
        }
    }
}