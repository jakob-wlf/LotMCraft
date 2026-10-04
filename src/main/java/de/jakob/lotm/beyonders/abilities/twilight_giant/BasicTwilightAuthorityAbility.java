package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.twilight_giant.handlers.TwilightAging;
import de.jakob.lotm.dimension.ModDimensions;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BasicTwilightAuthorityAbility extends SelectableAbility {

    private static final int REBORN_TICKS = 100;
    private static final int GRATEFUL_INTERVAL = 20;
    private static final float GRATEFUL_SPIRITUALITY = 200;
    private static final float GRATEFUL_YEARS = 5f;
    private static final double GRATEFUL_BASE_RADIUS = 50.0D;
    private static final double GRATEFUL_RADIUS_PER_SEQUENCE = 25.0D;
    private static final double GRATEFUL_MAX_RADIUS = 100.0D;
    private static final int GRATEFUL_RING_POINTS = 48;
    private static final int IMBUE_TICKS = 20 * 60;
    private static final float IMBUE_YEARS = 10f;
    private static final int IMBUE_CHECK_INTERVAL = 100;
    private static final String IMBUE_TAG = "lotm_twilight_imbued_until";

    private static final Map<UUID, Integer> rebirths = new HashMap<>();
    private static final Set<UUID> gratefulDead = new HashSet<>();
    private static final Set<UUID> imbuers = new HashSet<>();

    public BasicTwilightAuthorityAbility(String id) {
        super(id, 60);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 5000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.basic_twilight_authority.reborn",
                "ability.lotmcraft.basic_twilight_authority.grateful_dead",
                "ability.lotmcraft.basic_twilight_authority.imbue",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        switch (selectedAbility) {
            case 0 -> toggleRebirth(player);
            case 1 -> toggleGratefulDead(player);
            default -> imbue(serverLevel, player);
        }
    }

    private static void toggleRebirth(ServerPlayer player) {
        if (rebirths.remove(player.getUUID()) != null) {
            BeyonderData.incrementSpirituality(player, 5000);
            bar(player, "ability.lotmcraft.basic_twilight_authority.reborn_stopped");
            return;
        }
        rebirths.put(player.getUUID(), 0);
        bar(player, "ability.lotmcraft.basic_twilight_authority.reborn_started");
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, player.getSoundSource(), 1.5f, 0.5f);
    }

    private static void toggleGratefulDead(ServerPlayer player) {
        if (gratefulDead.remove(player.getUUID())) {
            BeyonderData.incrementSpirituality(player, 5000);
            bar(player, "ability.lotmcraft.basic_twilight_authority.grateful_dead_stopped");
            return;
        }
        gratefulDead.add(player.getUUID());
        bar(player, "ability.lotmcraft.basic_twilight_authority.grateful_dead_started");
        player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_AMBIENT, player.getSoundSource(), 2f, 0.5f);
    }

    private static void imbue(ServerLevel level, ServerPlayer player) {
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty()) {
            bar(player, "ability.lotmcraft.basic_twilight_authority.no_weapon");
            return;
        }
        long until = level.getGameTime() + IMBUE_TICKS;
        CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> tag.putLong(IMBUE_TAG, until));
        weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        imbuers.add(player.getUUID());
        level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(1.0), player.getZ(), 30, 0.5, 0.5, 0.5, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 1.5f, 0.6f);
        bar(player, "ability.lotmcraft.basic_twilight_authority.imbued");
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        if (!player.isAlive()) {
            rebirths.remove(id);
            gratefulDead.remove(id);
            return;
        }
        ServerLevel level = player.serverLevel();
        Integer progress = rebirths.get(id);
        if (progress != null) tickRebirth(level, player, progress + 1);
        if (gratefulDead.contains(id) && player.tickCount % GRATEFUL_INTERVAL == 0) tickGratefulDead(level, player);
        if (imbuers.contains(id) && player.tickCount % IMBUE_CHECK_INTERVAL == 0) clearExpired(level, player);
    }

    private static void tickRebirth(ServerLevel level, ServerPlayer player, int progress) {
        float fraction = progress / (float) REBORN_TICKS;
        int count = 2 + (int) (fraction * 12);
        level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(0.5), player.getZ(), count, 0.4, 0.9, 0.4, 0.03 + fraction * 0.1);
        level.sendParticles(TwilightAging.ASH_DUST, player.getX(), player.getY(0.8), player.getZ(), count, 0.3, 0.6, 0.3, 0.06);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, (int) (fraction * 4), false, false));
        if (progress < REBORN_TICKS) {
            rebirths.put(player.getUUID(), progress);
            return;
        }
        rebirths.remove(player.getUUID());
        reborn(level, player);
    }

    private static void reborn(ServerLevel level, ServerPlayer player) {
        level.sendParticles(TwilightAging.TWILIGHT_DUST, player.getX(), player.getY(0.5), player.getZ(), 120, 0.6, 1.0, 0.6, 0.2);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5), player.getZ(), 60, 0.5, 1.0, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, player.getSoundSource(), 1.5f, 0.8f);

        player.removeAllEffects();
        player.clearFire();
        TwilightAging.clear(player);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);

        ServerLevel spiritWorld = level.getServer().getLevel(ModDimensions.SPIRIT_WORLD_DIMENSION_KEY);
        if (spiritWorld != null) {
            var border = spiritWorld.getWorldBorder();
            int x = Mth.floor(Mth.lerp(spiritWorld.random.nextDouble(), border.getMinX() + 16, border.getMaxX() - 16));
            int z = Mth.floor(Mth.lerp(spiritWorld.random.nextDouble(), border.getMinZ() + 16, border.getMaxZ() - 16));
            int surface = spiritWorld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, surface <= spiritWorld.getMinBuildHeight() ? 64 : surface, z);
            while (!spiritWorld.getBlockState(pos).isAir() && pos.getY() < spiritWorld.getMaxBuildHeight() - 1) pos = pos.above();
            if (spiritWorld.getBlockState(pos.below()).isAir()) spiritWorld.setBlockAndUpdate(pos.below(), Blocks.END_STONE.defaultBlockState());
            player.teleportTo(spiritWorld, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        }
        ServerLevel current = player.serverLevel();
        current.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(0.5), player.getZ(), 80, 0.5, 1.0, 0.5, 0.15);
        current.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, player.getSoundSource(), 1.5f, 0.8f);
        bar(player, "ability.lotmcraft.basic_twilight_authority.reborn_done");
    }

    private static void tickGratefulDead(ServerLevel level, ServerPlayer player) {
        if (!player.hasInfiniteMaterials()) {
            if (BeyonderData.getSpirituality(player) < GRATEFUL_SPIRITUALITY) {
                gratefulDead.remove(player.getUUID());
                bar(player, "ability.lotmcraft.basic_twilight_authority.grateful_dead_stopped");
                return;
            }
            BeyonderData.reduceSpirituality(player, GRATEFUL_SPIRITUALITY);
        }
        double radius = gratefulRadius(player);
        for (LivingEntity target : AbilityUtil.getNearbyEntities(player, level, player.position(), radius)) {
            if (AbilityUtil.mayDamage(player, target)) TwilightAging.age(level, target, player, GRATEFUL_YEARS);
        }
        double phase = player.tickCount * 0.05;
        for (int i = 0; i < GRATEFUL_RING_POINTS; i++) {
            double angle = phase + i * Math.PI * 2 / GRATEFUL_RING_POINTS;
            Vec3 point = player.position().add(Math.cos(angle) * radius, 1, Math.sin(angle) * radius);
            level.sendParticles(TwilightAging.TWILIGHT_DUST, point.x, point.y, point.z, 1, 0.2, 0.5, 0.2, 0);
        }
        level.sendParticles(TwilightAging.ASH_DUST, player.getX(), player.getY(0.5), player.getZ(), 40, radius / 3, 2, radius / 3, 0.02);
    }

    private static double gratefulRadius(LivingEntity entity) {
        int sequence = BeyonderData.getSequence(entity);
        return Math.min(GRATEFUL_MAX_RADIUS, GRATEFUL_BASE_RADIUS + GRATEFUL_RADIUS_PER_SEQUENCE * Math.max(0, 2 - sequence));
    }

    private static void clearExpired(ServerLevel level, ServerPlayer player) {
        Inventory inventory = player.getInventory();
        boolean anyLeft = false;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            long until = imbuedUntil(stack);
            if (until == 0) continue;
            if (until > level.getGameTime()) {
                anyLeft = true;
                continue;
            }
            unimbue(stack);
        }
        if (!anyLeft) imbuers.remove(player.getUUID());
    }

    private static long imbuedUntil(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(IMBUE_TAG)) return 0;
        return data.copyTag().getLong(IMBUE_TAG);
    }

    private static void unimbue(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(IMBUE_TAG));
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getSource().getDirectEntity() != attacker) return;
        if (!(attacker.level() instanceof ServerLevel level)) return;
        ItemStack weapon = attacker.getMainHandItem();
        long until = imbuedUntil(weapon);
        if (until == 0) return;
        if (until <= level.getGameTime()) {
            unimbue(weapon);
            return;
        }
        LivingEntity target = event.getEntity();
        ServerScheduler.scheduleDelayed(1, () -> TwilightAging.age(level, target, attacker, IMBUE_YEARS), level);
    }

    private static void bar(Player player, String key) {
        AbilityUtil.sendActionBar(player, Component.translatable(key).withColor(TwilightAging.TWILIGHT_TEXT));
    }
}
