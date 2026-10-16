package de.jakob.lotm.beyonders.abilities.abyss;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static de.jakob.lotm.entity.custom.BeyonderNPCEntity.getRandomBeyonderSkin;

public class DevilWorshipAbility extends Ability {
    public static final int TARGET_RANGE = 30;

    //swap cooldown
    public static final long COOLDOWN_TICKS = 20L * 60 * 30;

    public static final int DEFAULT_KILLS_FOR_FULL = 4;
    public static final int PLAYER_KILLS_FOR_FULL = 10;
    public static final int NPC_KILLS_FOR_FULL = 50;

    // Teir data
    public static final int[] DEVIL_SEQUENCE = { 6, 5, 4 };
    public static final int[] SUMMON_CHANCE  = { 60, 25, 15 };
    public static final int[] ESCAPE_CHANCE  = { 20, 40, 60 };

    // how login the devil lasts
    public static final int DEVIL_LIFETIME = 300*20;

    public static final String DEVIL_TAG = "DevilProjection";
    public static final String DEVIL_CLEARED_TAG = "DevilProjectionCleared";
    private static final Map<UUID, UUID> ACTIVE = new ConcurrentHashMap<>();

    public static final class Data {
        private Data() {}
        private static final String ROOT = "lotm_devil_worship";
        private static CompoundTag root(ServerPlayer p) { return p.getPersistentData().getCompound(ROOT); }
        private static void save(ServerPlayer p, CompoundTag root) { p.getPersistentData().put(ROOT, root); }
        public static String preferred(ServerPlayer p) { return root(p).getString("mob"); }
        public static void setPreferred(ServerPlayer p, String typeId) {CompoundTag r = root(p); r.putString("mob", typeId); save(p, r);}
        public static float pleasure(ServerPlayer p) { return root(p).getFloat("pleasure"); }
        public static void setPleasure(ServerPlayer p, float value) {CompoundTag r = root(p); r.putFloat("pleasure", Math.clamp(value, 0f, 100f)); save(p, r);}
        public static void setLastSelect(ServerPlayer p, long tick) {CompoundTag r = root(p); r.putLong("lastSelect", tick); save(p, r);}
        public static long cooldownRemaining(ServerPlayer p, long now) {
            CompoundTag r = root(p);
            if (!r.contains("lastSelect")) return 0;
            return Math.max(0, r.getLong("lastSelect") + COOLDOWN_TICKS - now);
        }
    }
    public DevilWorshipAbility(String id) {
        super(id, 1f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        cannotBeStolen = true;
    }
    @Override
    public Map<String, Integer> getRequirements() {return new HashMap<>(Map.of("abyss", 7));}
    @Override
    public float getSpiritualityCost() {return 1;}
    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if (level.isClientSide) {
            if (entity.isShiftKeyDown()) DevilWorshipMenus.open();
            return;
        }
        if (!(entity instanceof ServerPlayer player)) return;
        if (player.isShiftKeyDown()) return; // details menu is client side; no cooldown used
        LivingEntity target = AbilityUtil.getTargetEntity(player, TARGET_RANGE, 1.5f);
        if (target == null) {
            sendStatus(player);
            return;
        }
        selectPreferred(player, target);
    }
    private static void selectPreferred(ServerPlayer player, LivingEntity target) {
        long now = player.level().getGameTime();
        long remaining = Data.cooldownRemaining(player, now);
        if (remaining > 0) {
            say(player, "The Devils will not listen yet. Try again in " + formatTicks(remaining) + ".", 0xFF124D);
            return;
        }
        Data.setPreferred(player, typeId(target));
        Data.setPleasure(player, 0f);
        Data.setLastSelect(player, now);
        say(player, "You offer the Devils a new tribute: " + target.getType().getDescription().getString() + ". Their attention vanishes.", 0x9932CC);
    }
    private static void sendStatus(ServerPlayer player) {
        String preferred = Data.preferred(player);
        if (preferred.isEmpty()) {
            say(player, "You have chosen no tribute. Look at a mob and use the ability.", 0x9932CC);
            return;
        }
        long remaining = Data.cooldownRemaining(player, player.level().getGameTime());
        say(player, "Tribute: " + displayName(preferred) + " | Devil pleasure: " + Math.round(Data.pleasure(player)) + "% | Swap available " + (remaining > 0 ? "in " + formatTicks(remaining) : "now"), 0x9932CC);
        if (Data.pleasure(player) >= 100f) {say(player, "The Devils are fully pleased. Perform the ritual at a ritual table.", 0xFFD700);}
    }
    private static boolean isWorshipper(ServerPlayer player) {
        return BeyonderData.isBeyonder(player) && "abyss".equals(BeyonderData.getPathway(player)) && BeyonderData.getSequence(player) <= 7;
    }
    private static void say(ServerPlayer player, String text, int color) {player.sendSystemMessage(Component.literal(text).withColor(color));}
    private static String typeId(LivingEntity entity) {return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();}
    private static String displayName(String typeId) {return BuiltInRegistries.ENTITY_TYPE.getOptional(net.minecraft.resources.ResourceLocation.parse(typeId)).map(t -> t.getDescription().getString()).orElse(typeId);}
    private static String formatTicks(long ticks) {
        long totalSeconds = ticks / 20;
        return (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s";
    }
    private static boolean hasActiveDevil(ServerPlayer player) {
        UUID devilId = ACTIVE.get(player.getUUID());
        if (devilId == null) return false;
        Entity e = player.serverLevel().getEntity(devilId);
        if (e != null && e.isAlive()) return true;
        ACTIVE.remove(player.getUUID()); // dead, despawned, or in another dimension
        return false;
    }
    public static void perform(ServerPlayer player) {
        if (hasActiveDevil(player)) {
            say(player, "A Devil's projection still lingers. The ritual cannot be repeated yet.", 0xFF124D);
            return;
        }
        if (!isWorshipper(player)) {
            say(player, "The Abyss does not know you.", 0xFF124D);
            return;
        }
        if (Data.pleasure(player) < 100f) {
            say(player, "The Devils are not pleased enough to answer (" + Math.round(Data.pleasure(player)) + "%).", 0xFF124D);
            return;
        }
        int roll = player.getRandom().nextInt(100);
        int idx = 0;
        for (int cumulative = 0; idx < SUMMON_CHANCE.length - 1; idx++) {
            cumulative += SUMMON_CHANCE[idx];
            if (roll < cumulative) break;
        }
        int devilSeq = DEVIL_SEQUENCE[idx];
        boolean escaped = player.getRandom().nextInt(100) < ESCAPE_CHANCE[idx];
        Data.setPleasure(player, 0f); // preferred mob stays so you can keep farming
        say(player, "The ritual is complete. A projection of a Sequence " + devilSeq + " Devil answers your call.", 0x9932CC);
        say(player, escaped ? "It slaps your command and turns on you!" : "It bows, bound to your will.", escaped ? 0xFF124D : 0xFFD700);
        spawnDevil(player, devilSeq, !escaped);
    }
    private static void spawnDevil(ServerPlayer owner, int devilSeq, boolean controlled) {
        ServerLevel level = owner.serverLevel();
        Vec3 pos = owner.position().add(owner.getRandom().nextDouble() * 6 - 3, 0, owner.getRandom().nextDouble() * 6 - 3);
        BeyonderNPCEntity devil = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, false, getRandomBeyonderSkin(), "abyss", devilSeq, false, false);
        devil.setPos(pos);
        devil.getPersistentData().putBoolean(DEVIL_TAG, true);
        devil.getPersistentData().putUUID("DevilProjectionOwner", owner.getUUID());
        if (controlled) {
            devil.setPuppetWarrior(true);
            devil.setMaxLifetimeIfPuppet(DEVIL_LIFETIME);
            devil.getPersistentData().putBoolean("PuppetSummoned", true);
            devil.getPersistentData().putUUID("PuppetSummonOwner", owner.getUUID());
            level.addFreshEntity(devil);
            SubordinateUtils.turnEntityIntoSubordinate(devil, owner, true);
        } else {
            level.addFreshEntity(devil);
            devil.setTarget(owner);
            despawnAfter(level, devil);
        }
        ACTIVE.put(owner.getUUID(), devil.getUUID());
    }
    private static void despawnAfter(ServerLevel level, BeyonderNPCEntity devil) {
        AtomicInteger elapsed = new AtomicInteger();
        AtomicBoolean stop = new AtomicBoolean(false);
        ServerScheduler.scheduleUntil(level, () -> {
            if (!devil.isAlive()) {
                stop.set(true);
                return;
            }
            if (elapsed.addAndGet(20) >= DEVIL_LIFETIME) {
                devil.discard();
                stop.set(true);
            }
        }, 20, null, stop);
    }
    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class Events {
        private static boolean isDevil(Entity e) {return e.getPersistentData().getBoolean(DEVIL_TAG);}
        @SubscribeEvent
        public static void onDevilDrops(LivingDropsEvent event) {if (isDevil(event.getEntity())) event.setCanceled(true);}
        @SubscribeEvent
        public static void onDevilXp(LivingExperienceDropEvent event) {if (isDevil(event.getEntity())) event.setDroppedExperience(0);}
        @SubscribeEvent
        public static void onDevilLeave(EntityLeaveLevelEvent event) {
            if (event.getLevel().isClientSide()) return;
            if (!(event.getEntity() instanceof LivingEntity devil) || !isDevil(devil)) return;
            if (devil.getPersistentData().getBoolean(DEVIL_CLEARED_TAG)) return;
            devil.getPersistentData().putBoolean(DEVIL_CLEARED_TAG, true);
            BeyonderData.clearBeyonderData(devil);
        }
        private static int getKillsForFull(LivingEntity entity) {
            if (entity instanceof Player) {return PLAYER_KILLS_FOR_FULL;}
            if (entity instanceof BeyonderNPCEntity){return NPC_KILLS_FOR_FULL;}
            return DEFAULT_KILLS_FOR_FULL;
        }
        @SubscribeEvent
        public static void onKill(LivingDeathEvent event) {
            LivingEntity dead = event.getEntity();
            if (dead.level().isClientSide) return;
            if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
            if (dead instanceof Player) return;
            if (dead instanceof TamableAnimal pet && pet.isTame()) return;
            if (!isWorshipper(killer)) return;
            String preferred = Data.preferred(killer);
            if (preferred.isEmpty() || !preferred.equals(typeId(dead))) return;
            float before = Data.pleasure(killer);
            int killsRequired = getKillsForFull(dead);
            float after = Math.min(100f, before + 100f / killsRequired);
            Data.setPleasure(killer, after);
            if (after >= 100f && before < 100f) {
                say(killer, "The altar calls The Devil have accepted your offering The ritual may begin...", 0xFFD700);
            } else if (before < 25f && after >= 25f) {
                say(killer, "A Devil senses the blood split..", 0x9932CC);
            } else if (before < 50f && after >= 50f) {
                say(killer, "The Devil Watches with glee continue your offering...", 0x9932CC);
            } else if (before < 75f && after >= 75f) {
                say(killer, "The Devil has seen what you are it is pleased you no longer hide it...", 0x9932CC);
            }
        }
    }
}