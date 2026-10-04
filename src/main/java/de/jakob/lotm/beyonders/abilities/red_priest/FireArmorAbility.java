package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.FirePlateEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FireArmorAbility extends ToggleAbility {
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("lotmcraft", "fire_armor");
    private static int INTERVAL;
    private static boolean IN_IFRAME = false;
    private static final int IFRAME_DELAY = 2;
    private static final int[] LIMITS = new int[]{3,4,5,6};
    private static final int[] INTERVALS = new int[]{40,30,24,20};
    private static final HashMap<Integer, Integer[]> COSTS_ONE = new HashMap<>();
    private static final HashMap<Integer, Integer[]> COSTS_ALL = new HashMap<>();
    private static List<FirePlateEntity> plates = new ArrayList<>();
    private static final Set<UUID> FLAME_CLOAK_ACTIVE = new HashSet<>();
    private static int ARMOR_SHARDS;
    private static LivingEntity player;
    private static int MAX;
    public FireArmorAbility(String id) {
        super(id, "burning_armor");
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        if (ARMOR_SHARDS <= 0 || player == null) {
            cancel((ServerLevel) level, entity);
            return;
        }
        if(!(entity instanceof Player player1)) return;
        if(!FLAME_CLOAK_ACTIVE.contains(player.getUUID())) return;
        spawnFlameCloak((ServerLevel) level, player1);
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        setupOneCosts();
        setupMaxCosts();
        player = entity;
        int Seq = BeyonderData.getSequence(entity);
        ARMOR_SHARDS = (Seq <= 1) ? LIMITS[3] : (Seq <= 3) ? LIMITS[2] : (Seq <= 5) ? LIMITS[1] : LIMITS[0];
        INTERVAL = (Seq <= 1) ? INTERVALS[3] : (Seq <= 3) ? INTERVALS[2] : (Seq <= 5) ? INTERVALS[1] : INTERVALS[0];
        MAX = ARMOR_SHARDS;
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR);
        if (toughnessAttribute != null) {
            if (!toughnessAttribute.hasModifier(TOUGHNESS_MODIFIER_ID)) {
                double toughnessValue = 15D;
                AttributeModifier modifier = new AttributeModifier(
                        TOUGHNESS_MODIFIER_ID,
                        toughnessValue,
                        AttributeModifier.Operation.ADD_VALUE
                );
                toughnessAttribute.addPermanentModifier(modifier);

                BeyonderData.reduceSpirituality(entity, (BeyonderData.getSpirituality(entity)/10));

                AtomicInteger j = new AtomicInteger();
                for (int i = 0;i < MAX; i++) {

                    FirePlateEntity firePlateEntity = new FirePlateEntity(ModEntities.FIRE_PLATE.get(), level, entity.getUUID());
                    firePlateEntity.setPos(entity.getX()+0.5D, entity.getY(), entity.getZ());
                    plates.add(firePlateEntity);
                }
                ServerScheduler.scheduleRepeating(0, INTERVAL, MAX, () -> {
                    FirePlateEntity firePlateEntity = plates.get(j.get());
                    level.addFreshEntity(firePlateEntity);
                    j.getAndIncrement();
                });
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.7F);
                FLAME_CLOAK_ACTIVE.add(entity.getUUID());
                entity.sendSystemMessage(Component.literal("You are now shielded with fire, you are immune to the next " + plates.size() + " attacks").withStyle(ChatFormatting.GOLD));
            }
        }
    }



    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR);
        if (toughnessAttribute != null) {
            if (toughnessAttribute.hasModifier(TOUGHNESS_MODIFIER_ID)) {
                toughnessAttribute.removeModifier(TOUGHNESS_MODIFIER_ID);
                ARMOR_SHARDS=0;
                entity.sendSystemMessage(Component.literal("Your armor has depleted, stay safe").withStyle(ChatFormatting.DARK_RED));
                if(plates.isEmpty()) return;
                for(FirePlateEntity firePlateEntity : plates) {
                    firePlateEntity.discard();
                }
                player = null;
                INTERVAL = 0;
                MAX = ARMOR_SHARDS;
                plates.clear();
                plates = new ArrayList<>();
                FLAME_CLOAK_ACTIVE.remove(entity.getUUID());
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    private static void negateDamage(LivingIncomingDamageEvent event, int COST) {
        ARMOR_SHARDS-=COST;
        IN_IFRAME=true;
        event.getEntity().playSound(SoundEvents.BELL_RESONATE, 1.0F, 1.0F);
        if (ARMOR_SHARDS > 0) event.getEntity().sendSystemMessage(Component.literal("Your fire armor protected you.").withStyle(ChatFormatting.RED));
        event.setCanceled(true);
        ServerScheduler.scheduleDelayed(20*IFRAME_DELAY, () -> IN_IFRAME=false);
        if(plates.isEmpty()) return;
        plates.getLast().discard();
        plates.removeLast();
        BeyonderData.reduceSpirituality(event.getEntity(), (BeyonderData.getSpirituality(event.getEntity())/2));
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) return;
        if (player == null) return;
        if(event.getEntity().level().isClientSide) return;
        int COST;
        if(event.getEntity().equals(player)) {

            if(event.getSource().getEntity() == null) return;
            if(BeyonderData.isBeyonder((LivingEntity) event.getSource().getEntity())) {
                int playerSeq =  BeyonderData.getSequence(event.getEntity());
                int enemySeq =   BeyonderData.getSequence((LivingEntity) event.getSource().getEntity());
                COST = estimateCost(playerSeq, enemySeq);
                if(COST == 0) return;
                if(ARMOR_SHARDS < COST) return;
                if(IN_IFRAME) {
                    event.setCanceled(true);
                    return;
                }
                if(event.getSource().is(DamageTypes.PLAYER_EXPLOSION) || event.getSource().is(DamageTypes.MOB_ATTACK) || event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
                    event.setCanceled(true);
                    return;
                }

                negateDamage(event, COST);

            }
        }
    }

    private static int estimateCost(int playerSeq, int enemySeq) {
        if(playerSeq <= enemySeq) return 1;
        if(Arrays.asList(COSTS_ALL.get(playerSeq)).contains(enemySeq)) return ARMOR_SHARDS;
        else if(Arrays.asList(COSTS_ONE.get(playerSeq)).contains(enemySeq)) return 1;
        else return 0;
    }

    private void setupMaxCosts() {
        FireArmorAbility.COSTS_ALL.put(7, new Integer[]{5});
        FireArmorAbility.COSTS_ALL.put(6, new Integer[]{});
        FireArmorAbility.COSTS_ALL.put(5, new Integer[]{4});
        FireArmorAbility.COSTS_ALL.put(4, new Integer[]{});
        FireArmorAbility.COSTS_ALL.put(3, new Integer[]{2});
        FireArmorAbility.COSTS_ALL.put(2, new Integer[]{});
        FireArmorAbility.COSTS_ALL.put(1, new Integer[]{0});
        FireArmorAbility.COSTS_ALL.put(0, new Integer[]{});
    }

    private void setupOneCosts() {
        FireArmorAbility.COSTS_ONE.put(7, new Integer[]{9, 8, 7, 6});
        FireArmorAbility.COSTS_ONE.put(6, new Integer[]{9, 8, 7, 6, 5});
        FireArmorAbility.COSTS_ONE.put(5, new Integer[]{9, 8, 7, 6, 5});
        FireArmorAbility.COSTS_ONE.put(4, new Integer[]{9, 8, 7, 6, 5, 4, 3});
        FireArmorAbility.COSTS_ONE.put(3, new Integer[]{9, 8, 7, 6, 5, 4, 3});
        FireArmorAbility.COSTS_ONE.put(2, new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1});
        FireArmorAbility.COSTS_ONE.put(1, new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1});
        FireArmorAbility.COSTS_ONE.put(0, new Integer[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0});
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FLAME_CLOAK_ACTIVE.remove(event.getEntity().getUUID());
    }

    private static void spawnFlameCloak(ServerLevel level, Player player) {
        RandomSource rand = level.random;
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        double yaw = Math.toRadians(player.yBodyRot);

        // Player's basis vectors on the XZ plane
        double rightX = -Math.cos(yaw);
        double rightZ = -Math.sin(yaw);
        double backX  =  Math.sin(yaw);
        double backZ  = -Math.cos(yaw);

        // ---------------- BODY: stops below the head so vision stays clear ----------------
        int bodyCount = 26;
        for (int i = 0; i < bodyCount; i++) {
            double h = rand.nextDouble() * 1.30;
            double t = h / 1.30;

            double radius = 0.42 + 0.30 * Math.pow(t, 1.3);
            radius += (rand.nextDouble() - 0.5) * 0.16;

            double angle = rand.nextDouble() * Math.PI * 2.0;
            double x = px + Math.cos(angle) * radius;
            double y = py + h;
            double z = pz + Math.sin(angle) * radius;

            level.sendParticles(ParticleTypes.FLAME, x, y, z,
                    1, 0, 0.02 + rand.nextDouble() * 0.03, 0, 0.0);

            if (rand.nextFloat() < 0.30F) {
                level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0, 0.02, 0, 0.0);
            }
            if (rand.nextFloat() < 0.04F) {
                level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0, 0, 0, 0.0);
            }
        }

        // ---------------- WINGS: anchored behind the shoulders, spreading out & back ----------------
        for (int side = -1; side <= 1; side += 2) {
            int wingCount = 48;
            for (int i = 0; i < wingCount; i++) {
                double t = rand.nextDouble(); // 0 = root at shoulder, 1 = wing tip

                // Root at shoulder height (~1.40), rises 0.80 total
                double wingHeight = 1.40 + t * 0.80;

                // Lateral spread grows with t — wings reach further out toward the tips
                double lateralSpread = 0.15 + t * 1.00 + Math.pow(t, 2) * 0.50;

                // Backward offset — starts right behind the shoulder and trails back with t
                double backOffset = 0.35 + t * 0.55;

                double jitter = (rand.nextDouble() - 0.5) * (0.30 - t * 0.15);

                double lateral = lateralSpread * side;
                double x = px + rightX * lateral + backX * backOffset
                        + (rand.nextDouble() - 0.5) * 0.22;
                double z = pz + rightZ * lateral + backZ * backOffset
                        + (rand.nextDouble() - 0.5) * 0.22;
                double y = py + wingHeight + jitter;

                // Flames drift slightly upward and outward along the wing
                double vy = 0.015 + rand.nextDouble() * 0.030 + t * 0.015;
                double vx = rightX * side * 0.03 * t + backX * 0.02;
                double vz = rightZ * side * 0.03 * t + backZ * 0.02;

                level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, vx, vy, vz, 0.012);

                if (rand.nextFloat() < 0.55F) {
                    level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, vx, vy, vz, 0.0);
                }
                if (rand.nextFloat() < 0.06F) {
                    level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0, 0, 0, 0.0);
                }
            }
        }

        // ---------------- TOP WISPS: short trails above the wing tips ----------------
        for (int i = 0; i < 2; i++) {
            if (rand.nextFloat() < 0.45F) {
                double side = rand.nextBoolean() ? 1 : -1;
                double lateral = 1.30 + rand.nextDouble() * 0.30;
                double back = 0.90 + rand.nextDouble() * 0.20;
                double x = px + rightX * lateral * side + backX * back
                        + (rand.nextDouble() - 0.5) * 0.20;
                double z = pz + rightZ * lateral * side + backZ * back
                        + (rand.nextDouble() - 0.5) * 0.20;
                double y = py + 2.20 + rand.nextDouble() * 0.25;
                level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.055, 0, 0.012);
            }
        }

        // ---------------- FOOT EMBERS ----------------
        if (rand.nextFloat() < 0.6F) {
            double a = rand.nextDouble() * Math.PI * 2.0;
            double r = 0.45 + rand.nextDouble() * 0.20;
            level.sendParticles(ParticleTypes.FLAME,
                    px + Math.cos(a) * r, py + 0.05, pz + Math.sin(a) * r,
                    1, 0, 0.025, 0, 0.0);
        }
    }

//    private static void spawnFlameCloak(ServerLevel level, Player player) {
//        RandomSource rand = level.random;
//        double px = player.getX();
//        double py = player.getY();
//        double pz = player.getZ();
//        float bodyYaw = player.yBodyRot;
//
//        // Main cloak: tall pillar that bulges in the middle and flares at the top
//        int count = 24;
//        for (int i = 0; i < count; i++) {
//            double h = rand.nextDouble() * 2.3;
//            double t = h / 2.3;
//
//            // Radius profile: narrow at feet, widest mid-body, wide flare up top
//            double radius = 0.55
//                    + 0.45 * Math.sin(t * Math.PI)    // mid-body bulge
//                    + 0.55 * Math.pow(t, 1.8);         // top flare (matches ref image wings)
//
//            radius += (rand.nextDouble() - 0.5) * 0.40; // jagged edges
//            if (radius < 0.2) radius = 0.2;
//
//            double angle = rand.nextDouble() * Math.PI * 2.0;
//            double ox = Math.cos(angle) * radius;
//            double oz = Math.sin(angle) * radius;
//
//            double x = px + ox;
//            double y = py + h + rand.nextDouble() * 0.12;
//            double z = pz + oz;
//
//            // Flames lick outward from the body and rise
//            double vx = ox * 0.05;
//            double vy = 0.02 + rand.nextDouble() * 0.035;
//            double vz = oz * 0.05;
//
//            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, vx, vy, vz, 0.0);
//
//            if (rand.nextFloat() < 0.40F) {
//                level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, vx, vy, vz, 0.0);
//            }
//            if (rand.nextFloat() < 0.18F) {
//                level.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0, 0, 0, 0.0);
//            }
//            if (rand.nextFloat() < 0.14F && t > 0.45) {
//                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
//                        x, y + 0.15, z, 1, vx * 0.3, 0.03, vz * 0.3, 0.004);
//            }
//        }
//
//        // Outer flare "wings" that rise above the head — the tall spikes from the ref
//        int flares = 4;
//        for (int i = 0; i < flares; i++) {
//            double angle = rand.nextDouble() * Math.PI * 2.0;
//            double radius = 0.7 + rand.nextDouble() * 0.6;
//            double h = 2.2 + rand.nextDouble() * 1.0;
//
//            double x = px + Math.cos(angle) * radius;
//            double y = py + h;
//            double z = pz + Math.sin(angle) * radius;
//
//            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0.06, 0, 0.015);
//            level.sendParticles(ParticleTypes.SMALL_FLAME, x, y, z, 1, 0, 0.05, 0, 0.01);
//        }
//
//        // Low ring at the feet — grounds the effect visually
//        if (level.random.nextFloat() < 0.85F) {
//            double a = rand.nextDouble() * Math.PI * 2.0;
//            double r = 0.6 + rand.nextDouble() * 0.25;
//            level.sendParticles(ParticleTypes.FLAME,
//                    px + Math.cos(a) * r, py + 0.05, pz + Math.sin(a) * r,
//                    1, 0, 0.03, 0, 0.0);
//        }
//    }

}
