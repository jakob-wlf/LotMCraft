package de.jakob.lotm.beyonders.abilities.black_emperor;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.demoness.CharmAbility;
import de.jakob.lotm.entity.custom.projectiles.BribeProjectile;
import de.jakob.lotm.particle.ModParticles;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import de.jakob.lotm.util.helper.BribeConnectionRegistry;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BribeAbility extends SelectableAbility {



    public enum BribeTypes {
        WEAKEN, CHARM, ARROGANCE, CONNECTION;
        public static final BribeTypes[] VALUES = values();
    }
    private static final RandomSource RANDOM = RandomSource.create();
    private static final Map<UUID, BribeTypes> SELECTED = new ConcurrentHashMap<>();
    private static final HashMap<UUID, UUID> charmed = CharmAbility.getCharmed();
    private static final HashSet<UUID> onCharmedCooldown = new HashSet<>();
    private static final Map<UUID, UUID> ARROGANCE = new HashMap<>();

    protected static final String WEAKEN_TAG = "BRIBE_WEAKEN";
    protected static final String CHARM_TAG = "BRIBE_CHARM";
    protected static final String ARROGANCE_TAG = "BRIBE_ARROGANCE";
    protected static final String CONNECTION_TAG = "BRIBE_CONNECTION";


    public BribeAbility(String id) {
        super(id, 5);
        hasDynamicSpirituality = true;
        hasDynamicCooldown = true;

        dynamicSpirituality = new LinkedList<>(List.of(4000f, 2000f, 1000f, 600f, 400f, 200f, 120f, 90f));
        dynamicCooldown = new LinkedList<>(List.of(1,2,2,4,4,6,6,6));
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.bribe.weaken",
                "ability.lotmcraft.bribe.charm",
                "ability.lotmcraft.bribe.arrogance",
                "ability.lotmcraft.bribe.connection"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0,1,2,3: use(level, entity, selectedAbility);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("black_emperor", 7);
    }

    @Override
    public float getSpiritualityCost() {
        return 15.0f;
    }


    public BribeTypes getSelected(LivingEntity entity) {
        return SELECTED.getOrDefault(entity.getUUID(), BribeTypes.WEAKEN);
    }


    public void use(Level level, LivingEntity entity, int selectedAbility) {
        if (level.isClientSide()) return;
        SELECTED.put(entity.getUUID(), BribeTypes.VALUES[selectedAbility]);

        ItemStack offhand = entity.getOffhandItem();
        if (offhand.isEmpty()) {
            entity.sendSystemMessage(Component.literal("You must be holding an item in your off-hand").withStyle(ChatFormatting.RED));
            if(entity instanceof Player player) AbilityUtil.refund(player, this);
            return;
        }

        BribeTypes type = getSelected(entity);

        BribeProjectile projectile = new BribeProjectile(level, entity, offhand, type);
        projectile.setPos(
                entity.getX(),
                entity.getEyeY() - 0.1,
                entity.getZ()
        );
        projectile.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 1.5F, 1.0F);
        level.addFreshEntity(projectile);

        // Consume the item (unless creative)
        boolean consume = !(entity instanceof Player player && player.isCreative());
        if (consume) {
            offhand.shrink(1);
            if (offhand.isEmpty()) {
                entity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }

        // Feedback
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.7f, 1.0f);

    }

    public static void apply(Level level, LivingEntity player, LivingEntity target, BribeTypes type) {
        if (!(level instanceof ServerLevel level1)) return;
        if(BeyonderData.isBeyonder(target)) {
            double chance = EloquenceAbility.calculateMultiplier(BeyonderData.getSequence(player), BeyonderData.getSequence(target));
            if(RANDOM.nextDouble() > chance) {
                player.sendSystemMessage(Component.literal("Your bribe attempt failed due to sequence gap").withStyle(ChatFormatting.DARK_GRAY));
                return;
            }

        }
        switch (type) {
            case WEAKEN     -> applyWeaken(player, target, level1);
            case ARROGANCE  -> applyArrogance(player, target, level1);
            case CHARM      -> applyCharm(player, target, level1);
            case CONNECTION -> applyConnection(player, target, level1);
        }
    }

    private static void applyWeaken(LivingEntity player, LivingEntity target, ServerLevel level) {
        int duration = 200; // 10s

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,      duration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
        applyTagFor(player, target, WEAKEN_TAG, duration);
    }

    private static void applyArrogance(LivingEntity player, LivingEntity target, ServerLevel level) {
        int duration = 20;
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0));
        if(!(level instanceof ServerLevel serverLevel)) return;
        ARROGANCE.put(target.getUUID(), player.getUUID());
        applyTagFor(player, target, ARROGANCE_TAG, duration*20);
        ServerScheduler.scheduleForDuration(
                0,
                20 * 5,
                20*duration,
                () -> {
                    UUID mappedCaster = ARROGANCE.get(target.getUUID());
                    if (mappedCaster == null || !mappedCaster.equals(player.getUUID())) {
                        return;
                    }

                    ParticleUtil.createParticleSpirals(serverLevel, ModParticles.LIGHTNING.get(),
                            target.position().add(0, 0.5, 0),
                            0.25, 0.9, 1.4, 0.07, 8, 24, 2, 2);

//                    target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 40, 0, false, false, true));

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
                    if (mappedCaster != null && mappedCaster.equals(player.getUUID())) {
                        ARROGANCE.remove(target.getUUID());
                    }
                },
                serverLevel
        );
    }

    private static void applyCharm(LivingEntity player, LivingEntity target, ServerLevel level) {
        if(charmed.containsKey(target.getUUID()) || onCharmedCooldown.contains(target.getUUID())) {
            if(player instanceof ServerPlayer serverPlayer) {
                ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Component.literal("The entity was charmed already.").withColor(0xFFf980ff));
                serverPlayer.connection.send(packet);
                AbilityUtil.refund(serverPlayer, new BribeAbility("briber_ability"));
            }
            return;
        }
        charmed.put(target.getUUID(), player.getUUID());
        onCharmedCooldown.add(target.getUUID());
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING,  200, 0));
        applyTagFor(player, target, CHARM_TAG, 200);
        ServerScheduler.scheduleDelayed(20 * 10, () -> {
            onCharmedCooldown.remove(target.getUUID());
            charmed.remove(target.getUUID());
        });

        target.sendSystemMessage(Component.literal("You feel yourself being charmed.").withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.literal("You charmed " + target.getDisplayName().getString()).withStyle(ChatFormatting.DARK_GRAY));

        // Force-ally the direct target
        if (RANDOM.nextDouble() < 0.20) {
            if (target instanceof Mob mob) {
                mob.setTarget(null);
            }
            AllyUtil.acceptAllyRequest(target, player.getUUID());

        }

        // 5% chance to remove allies
        double radius = 12.0;
        for (LivingEntity nearby : AbilityUtil.getNearbyEntities(target, level, target.getEyePosition(), radius)) {
            if (RANDOM.nextDouble() < 0.05) {
                if (nearby == target || nearby == player) continue;
                if(AllyUtil.areAllies(nearby, target)) {
                    AllyUtil.removeAllies(nearby, target);
                    target.sendSystemMessage(Component.literal("You feel your self compelled to betray " + nearby.getName()).withStyle(ChatFormatting.GOLD));
                }
            }
        }

    }

    private static void applyConnection(LivingEntity player, LivingEntity target, ServerLevel level) {
        int duration = 600; // 30s
        BribeConnectionRegistry.link(player, target, duration);
        player.sendSystemMessage(Component.literal("You established a connection with " + target.getDisplayName().getString()).withStyle(ChatFormatting.DARK_GRAY));
        applyTagFor(player, target, CONNECTION_TAG, duration);
    }


    private static void applyTagFor(LivingEntity player, LivingEntity target, String tag, int delay) {
        target.getPersistentData().putBoolean(tag, true);
        target.getPersistentData().putUUID(tag+"_TO", player.getUUID());
        ServerScheduler.scheduleDelayed(delay, () -> {
            target.getPersistentData().putBoolean(tag, false);
            target.getPersistentData().remove(tag+"_FROM");
        });
    }
}