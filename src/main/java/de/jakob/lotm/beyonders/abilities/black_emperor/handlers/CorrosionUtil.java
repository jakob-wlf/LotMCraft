package de.jakob.lotm.beyonders.abilities.black_emperor.handlers;

import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.gui.custom.introspect.IntrospectMenuProvider;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CorrosionUtil {

    private record Corroded(
            LivingEntity entity,
            UUID source,
            long untilTick,
            long nextActionAt,
            int interval
    ) {}

    private static final Map<UUID, Corroded> ACTIVE = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();

    private CorrosionUtil() {}

    public static void corrupt(LivingEntity entity, LivingEntity caster,
                               long untilTick, int firstDelay, int interval) {
        long now = entity.level().getGameTime();
        ACTIVE.put(entity.getUUID(),
                new Corroded(entity, caster.getUUID(), untilTick, now + firstDelay, interval));
    }

    public static boolean isCorroded(UUID id) { return ACTIVE.containsKey(id); }

    public static UUID getSourceId(UUID id) {
        Corroded c = ACTIVE.get(id);
        return c == null ? null : c.source();
    }

    public static void remove(UUID id) { ACTIVE.remove(id); }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();

        for (UUID id : new ArrayList<>(ACTIVE.keySet())) {
            Corroded c = ACTIVE.get(id);
            if (c == null) continue;

            LivingEntity entity = c.entity();

            if (entity == null || !entity.isAlive() || now >= c.untilTick()) {
                ACTIVE.remove(id);
                continue;
            }

            if (entity.level() != level) continue;

            if (entity instanceof Mob mob) {
                LivingEntity current = mob.getTarget();
                if (current instanceof Player
                        || (current != null && current.getUUID().equals(c.source()))) {
                    mob.setTarget(null);
                    mob.getNavigation().stop();
                }
            }

            if (now >= c.nextActionAt()) {
                fireAction(entity, level);
                ACTIVE.put(id, new Corroded(entity, c.source(),
                        c.untilTick(), now + c.interval(), c.interval()));
            }
        }
    }

    private static void fireAction(LivingEntity entity, ServerLevel level) {
        if (entity instanceof ServerPlayer player)      forcePlayerAction(player, level);
        else if (entity instanceof Mob mob)             forceMobAction(mob);
    }

    public static void forcePlayerAction(ServerPlayer player, ServerLevel level) {
        int roll = RANDOM.nextInt(100);

        if      (roll < 22) forceSwing(player);
        else if (roll < 37) forceCrouch(player);
        else if (roll < 49) forceHotbarSwap(player);
        else if (roll < 61) forceItemUse(player, level);
        else if (roll < 71) forceDropItem(player);
        else if (roll < 83) forceDropArmor(player);
        else if (roll < 93) forceBlockPlace(player, level);
        else                forceOpenGui(player);

        level.sendParticles(ParticleTypes.SQUID_INK,
                player.getX(), player.getY() + 1.2, player.getZ(),
                3, 0.2, 0.2, 0.2, 0.01);
    }

    private static void forceMobAction(Mob mob) {
        int roll = RANDOM.nextInt(10);

        if (roll < 4) {
            mob.setTarget(null);
            Vec3 look = mob.getLookAngle();
            double dx = look.x * 4 + (RANDOM.nextDouble() * 8 - 4);
            double dz = look.z * 4 + (RANDOM.nextDouble() * 8 - 4);
            mob.getNavigation().moveTo(mob.getX() + dx, mob.getY(), mob.getZ() + dz, 1.3);
        } else if (roll < 9) {
            LivingEntity nearest = findNearestOtherMob(mob);
            mob.setTarget(nearest);
        } else {
            if (mob.getHealth() > 4.0f) {
                mob.hurt(ModDamageTypes.source(mob.level(), ModDamageTypes.BEYONDER_GENERIC), RANDOM.nextInt(1, 5));
            }
        }
    }

    private static LivingEntity findNearestOtherMob(Mob mob) {
        return mob.level().getEntitiesOfClass(LivingEntity.class,
                        mob.getBoundingBox().inflate(12.0),
                        e -> e != mob && e instanceof Mob && e.isAlive())
                .stream()
                .min(Comparator.comparingDouble(a -> a.distanceToSqr(mob)))
                .orElse(null);
    }

    private static void forceItemUse(ServerPlayer player, ServerLevel level) {
        var inv = player.getInventory();

        int foundSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.has(net.minecraft.core.component.DataComponents.FOOD)
                    || stack.getItem() instanceof PotionItem) {
                foundSlot = i;
                break;
            }
        }
        if (foundSlot < 0) return;

        if (player.isUsingItem()) {
            player.stopUsingItem();
        }

        if (foundSlot < 9) {
            inv.selected = foundSlot;
        } else {
            ItemStack food    = inv.getItem(foundSlot);
            ItemStack current = inv.getItem(inv.selected);
            inv.setItem(inv.selected, food);
            inv.setItem(foundSlot, current);
        }

        player.connection.send(new ClientboundSetCarriedItemPacket(inv.selected));

        ItemStack toUse = player.getMainHandItem();
        if (toUse.isEmpty()) return;
        player.getCooldowns().removeCooldown(toUse.getItem());

        level.getServer().execute(() -> {
            if (!player.isAlive()) return;
            if (!CorrosionUtil.isCorroded(player.getUUID())) return;
            if (player.isUsingItem()) return;
            player.startUsingItem(InteractionHand.MAIN_HAND);
        });
    }

    private static void forceDropItem(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            held = player.getOffhandItem();
            if (held.isEmpty()) return;
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        ItemEntity drop = player.spawnAtLocation(held.copy(), 1.0f);
        if (drop != null) { drop.setPickUpDelay(40); drop.setThrower(player); }
    }

    private static void forceOpenGui(ServerPlayer player) {
        if(!BeyonderData.isBeyonder(player))
            return;

        int sequence = BeyonderData.getSequence(player, false, true);
        String pathway = BeyonderData.getPathway(player, true);
        float digestionProgress = BeyonderData.getDigestionProgress(player);

        SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
        float sanity = sanityComponent.getSanity();

        AbilityWheelHelper.removeUnusableAbilities(player);

        player.openMenu(new IntrospectMenuProvider(sequence, pathway, digestionProgress, sanity), buf -> {
            buf.writeInt(sequence);
            buf.writeUtf(pathway);
        });
    }

    private static void forceSwing(ServerPlayer player) {
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    private static void forceCrouch(ServerPlayer player) {
        player.setShiftKeyDown(!player.isShiftKeyDown());
    }

    private static void forceDropArmor(ServerPlayer player) {
        var inv = player.getInventory();

        int start = RANDOM.nextInt(4);
        for (int i = 0; i < 4; i++) {
            int slot = 36 + ((start + i) % 4);
            ItemStack armor = inv.getItem(slot);
            if (armor.isEmpty()) continue;

            inv.setItem(slot, ItemStack.EMPTY);

            ItemEntity drop = player.spawnAtLocation(armor.copy(), 0.4f);
            if (drop != null) {
                drop.setPickUpDelay(60);
                drop.setThrower(player);
            }

            player.level().playSound(null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                    SoundSource.PLAYERS, 0.8f, 0.6f);
            return;
        }
    }

    private static void forceBlockPlace(ServerPlayer player, ServerLevel level) {
        var inv = player.getInventory();

        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!(s.getItem() instanceof BlockItem blockItem)) continue;

            var block = blockItem.getBlock();
            if (block == Blocks.BEACON) continue;
            if (block == Blocks.SPAWNER) continue;
            if (block.defaultBlockState().is(BlockTags.SHULKER_BOXES)) continue;

            slot = i;
            break;
        }
        if (slot < 0) return;

        ItemStack stack = inv.getItem(slot);
        BlockItem blockItem = (BlockItem) stack.getItem();
        BlockState placeState = blockItem.getBlock().defaultBlockState();

        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = RANDOM.nextInt(7) - 3;
            int dy = RANDOM.nextInt(3);
            int dz = RANDOM.nextInt(7) - 3;
            BlockPos target = player.blockPosition().offset(dx, dy, dz);

            if (!level.getBlockState(target).canBeReplaced()) continue;
            if (target.equals(player.blockPosition())
                    || target.equals(player.blockPosition().above())) continue;
            BlockPos below = target.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;

            level.setBlockAndUpdate(target, placeState);
            stack.shrink(1);
            if (stack.isEmpty()) inv.setItem(slot, ItemStack.EMPTY);

            level.playSound(null, target,
                    placeState.getSoundType().getPlaceSound(),
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            return;
        }
    }

    private static void forceHotbarSwap(ServerPlayer player) {
        int newSlot = RANDOM.nextInt(9);
        player.getInventory().selected = newSlot;
        player.connection.send(new ClientboundSetCarriedItemPacket(newSlot));
    }
}