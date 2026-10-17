package de.jakob.lotm.beyonders.abilities.darkness;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.SpiritHousingComponent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.common.AngelFlightAbility;
import de.jakob.lotm.beyonders.abilities.death.EyeOfDeathAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.gui.custom.spirit_command.SpiritCommandMenu;
import de.jakob.lotm.gui.custom.spirit_housing.SpiritManageMenu;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AllyUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class SpiritCommandingAbility extends SelectableAbility {
    private static final String WARD = "lotm_spirit_ward";
    private static final String OWNER = "lotm_spirit_owner";
    public SpiritCommandingAbility(String id) {
        super(id, 1);
        canBeUsedInArtifact = false;
        cannotBeStolen = true;
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("darkness", 5));
    }

    @Override
    public float getSpiritualityCost() {
        return 400;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.spirit_commanding.capture",
                "ability.lotmcraft.spirit_commanding.manage",
                "ability.lotmcraft.spirit_commanding.command",
                "ability.lotmcraft.spirit_commanding.release"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) return;
        switch (abilityIndex) {
            case 1 -> manage(player);
            case 2 -> command(player);
            case 3 -> release(serverLevel, player);
            default -> capture(serverLevel, player);
        }
    }

    public static ItemStack token(String typeId) {
        ItemStack stack = new ItemStack(ModItems.SPIRIT_TOKEN.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("Spirit", typeId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable(entityKey(typeId)));
        return stack;
    }

    public static String typeOf(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return "";
        return data.copyTag().getString("Spirit");
    }

    public static int capacity(LivingEntity entity) {
        return BeyonderData.getSequence(entity) <= 4 ? 15 : 3;
    }

    public static void trim(LivingEntity entity) {
        SpiritHousingComponent housing = entity.getData(ModAttachments.SPIRIT_HOUSING);
        int limit = capacity(entity);
        while (housing.count() > limit && !housing.stored.isEmpty()) {
            housing.stored.remove(housing.stored.size() - 1);
        }
        for (int i = housing.slots.length - 1; i >= 0 && housing.count() > limit; i--) {
            housing.slots[i] = "";
        }
    }

    public static boolean allows(String typeId, int slot) {
        if (slot >= 6) return true;
        boolean arms = slot == 2 || slot == 3;
        boolean legs = slot == 4 || slot == 5;
        return switch (ResourceLocation.parse(typeId).getPath()) {
            case "spirit_ghost" -> slot == 0 || slot == 1;
            case "spirit_bane" -> slot == 1 || arms;
            case "spirit_bizarro_bane" -> slot == 0 || arms;
            case "spirit_blue_wizard" -> slot == 0 || arms;
            case "spirit_translucent_wizard" -> slot == 0 || legs;
            case "spirit_bubbles" -> slot == 1 || legs;
            case "spirit_malmouth" -> slot == 1 || arms;
            default -> true;
        };
    }

    public static void castPower(ServerPlayer player, int index) {
        trim(player);
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        List<String> powers = housing.powers();
        if (index < 0 || index >= powers.size()) return;
        String key = powers.get(index);
        int split = key.lastIndexOf('|');
        String type = key.substring(0, split);
        int slot = Integer.parseInt(key.substring(split + 1));
        if (slot < 0 || slot > 5 || !type.equals(housing.slots[slot])) return;
        ServerLevel level = player.serverLevel();
        LivingEntity target = AbilityUtil.getTargetEntity(player, 16, 1.5f, true);
        boolean arms = slot == 2 || slot == 3;
        boolean legs = slot == 4 || slot == 5;
        if (slot == 0 && target != null) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * 8, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20 * 8, 0, false, false, false));
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.MIND_BASED, player), 6);
        } else if (slot == 1) {
            CompoundTag data = player.getPersistentData();
            boolean active = !data.getBoolean(WARD);
            data.putBoolean(WARD, active);
        } else if (arms && target != null) {
            target.hurt(ModDamageTypes.source(level, ModDamageTypes.DARKNESS, player), 10);
        } else if (legs) {
            fly(level, player);
        }
    }

    private static void fly(ServerLevel level, ServerPlayer player) {
        if (!(LOTMCraft.abilityHandler.getById("angel_authority_flight") instanceof AngelFlightAbility flight)) return;
        flight.useAbility(level, player, true, false, false, false);
    }

    private void capture(ServerLevel level, ServerPlayer player) {
        LivingEntity target = AbilityUtil.getTargetEntity(player, 8, 1.5f, true);
        if (target == null || !EyeOfDeathAbility.isSpiritEntity(target)) {
            refund(player);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.not_spirit").withColor(0xff124d));
            return;
        }
        if (!target.getPersistentData().getBoolean("lotm_soothed_souls")) {
            refund(player);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.need_sooth").withColor(0xff124d));
            return;
        }
        trim(player);
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        if (housing.count() >= capacity(player)) {
            refund(player);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.full").withColor(0xff124d));
            return;
        }
        float chance = Math.min(1f, 0.3f + 0.1f * housing.fails);
        if (player.getRandom().nextFloat() > chance) {
            housing.fails++;
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.failed", (int) (chance * 100)).withColor(0xff124d));
            return;
        }
        housing.fails = 0;
        housing.stored.add(EntityType.getKey(target.getType()).toString());
        target.discard();
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.captured", target.getName()).withColor(0xa0e2fa));
        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ILLUSIONER_CAST_SPELL, player.getSoundSource(), 1f, 0.7f);
    }

    private void manage(ServerPlayer player) {
        trim(player);
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new SpiritManageMenu(id, inventory),
                Component.translatable("ability.lotmcraft.spirit_commanding.manage")));
    }

    private void command(ServerPlayer player) {
        trim(player);
        List<String> powers = player.getData(ModAttachments.SPIRIT_HOUSING).powers();
        if (powers.isEmpty()) {
            refund(player);
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.no_powers").withColor(0xff124d));
            return;
        }
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new SpiritCommandMenu(id, inventory, powers),
                Component.translatable("ability.lotmcraft.spirit_commanding.command")), buf -> {
            buf.writeVarInt(powers.size());
            for (String power : powers) buf.writeUtf(power);
        });
    }

    private void release(ServerLevel level, ServerPlayer player) {
        trim(player);
        if (recall(level, player)) return;
        boolean fromSlots = player.isShiftKeyDown();
        LivingEntity looked = AbilityUtil.getTargetEntity(player, 24, 1.5f, true);
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        if (!canRelease(housing, fromSlots)) {
            refund(player);
            AbilityUtil.sendActionBar(player, Component.translatable(!fromSlots && housed(housing)
                    ? "ability.lotmcraft.spirit_commanding.release_shift"
                    : "ability.lotmcraft.spirit_commanding.no_powers").withColor(0xff124d));
            return;
        }
        housing.releaseFromSlots = fromSlots;
        if (looked == null) {
            housing.releaseArmed = true;
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.release_armed").withColor(0xa0e2fa));
            return;
        }
        spawnAll(level, player, looked);
    }

    private static boolean housed(SpiritHousingComponent housing) {
        for (String slot : housing.slots) if (!slot.isEmpty()) return true;
        return false;
    }

    private static boolean canRelease(SpiritHousingComponent housing, boolean fromSlots) {
        if (fromSlots) return housed(housing) || !housing.stored.isEmpty();
        return !housing.stored.isEmpty();
    }

    @SubscribeEvent
    public static void onWard(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !wardActive(player)) return;
        event.setAmount(event.getAmount() * 0.7f);
    }

    @SubscribeEvent
    public static void onWardTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !wardActive(player)) return;
        if (player.tickCount % 8 != 0 || !(player.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), 4, 0.35, 0.5, 0.35, 0.01);
    }

    private static boolean wardActive(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean(WARD)) return false;
        if (!player.getData(ModAttachments.SPIRIT_HOUSING).slots[1].isEmpty()) return true;
        player.getPersistentData().putBoolean(WARD, false);
        return false;
    }

    @SubscribeEvent
    public static void onHit(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getSource().getDirectEntity() != player) return;
        if (event.isCanceled()) return;
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        if (!housing.releaseArmed) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        spawnAll(level, player, event.getEntity());
    }

    private static boolean recall(ServerLevel level, ServerPlayer player) {
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        if (housing.released.isEmpty()) return false;
        boolean returned = false;
        Iterator<String> spirits = housing.released.iterator();
        while (spirits.hasNext()) {
            String entry = spirits.next();
            int first = entry.indexOf('|');
            int last = entry.lastIndexOf('|');
            if (first < 0 || last <= first) {
                spirits.remove();
                continue;
            }
            UUID id = UUID.fromString(entry.substring(0, first));
            Entity entity = findReleased(player, id);
            if (entity == null || !(entity instanceof Mob mob) || !mob.isAlive()) {
                spirits.remove();
                continue;
            }
            String typeId = entry.substring(first + 1, last);
            int slot = Integer.parseInt(entry.substring(last + 1));
            boolean room = housing.count() < capacity(player);
            if (room && slot >= 0 && slot < housing.slots.length && housing.slots[slot].isEmpty()) {
                housing.slots[slot] = typeId;
            } else if (room && housing.stored.size() < 9) {
                housing.stored.add(typeId);
            } else {
                mob.teleportTo(player.getX(), player.getY(), player.getZ());
                mob.setTarget(null);
                returned = true;
                continue;
            }
            spirits.remove();
            forget(player, mob);
            mob.discard();
            returned = true;
        }
        if (returned) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.spirit_commanding.returned").withColor(0xa0e2fa));
            level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.ILLUSIONER_CAST_SPELL, player.getSoundSource(), 1f, 1.2f);
            return true;
        }
        return false;
    }

    private static Entity findReleased(ServerPlayer player, UUID id) {
        if (player.getServer() == null) return null;
        for (ServerLevel other : player.getServer().getAllLevels()) {
            Entity entity = other.getEntity(id);
            if (entity != null) return entity;
        }
        if (!(player.level() instanceof ServerLevel level)) return null;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(96), found -> found.getUUID().equals(id))) {
            return mob;
        }
        return null;
    }

    private static void spawnAll(ServerLevel level, ServerPlayer player, LivingEntity victim) {
        SpiritHousingComponent housing = player.getData(ModAttachments.SPIRIT_HOUSING);
        List<String> types = housing.takeForRelease(housing.releaseFromSlots);
        if (types.isEmpty()) return;
        List<Mob> spawned = new ArrayList<>();
        int count = types.size();
        for (int i = 0; i < count; i++) {
            String entry = types.get(i);
            int split = entry.lastIndexOf('|');
            String typeId = entry.substring(0, split);
            var type = EntityType.byString(typeId);
            if (type.isEmpty()) continue;
            Entity created = type.get().create(level);
            if (!(created instanceof Mob mob)) continue;
            double angle = (Math.PI * 2 * i) / count;
            double radius = Math.max(4.0, victim.getBbWidth() + 2.5);
            double x = victim.getX() + Math.cos(angle) * radius;
            double z = victim.getZ() + Math.sin(angle) * radius;
            float yaw = (float) (Math.toDegrees(Math.atan2(victim.getZ() - z, victim.getX() - x)) - 90f);
            mob.moveTo(x, victim.getY(), z, yaw, 0);
            mob.getPersistentData().putString(OWNER, player.getStringUUID());
            mob.setTarget(victim);
            mob.setPersistenceRequired();
            level.addFreshEntity(mob);
            AllyUtil.makeAllies(mob, player, false);
            for (Mob other : spawned) AllyUtil.makeAllies(mob, other, false);
            for (String previous : housing.released) {
                int mark = previous.indexOf('|');
                if (mark <= 0) continue;
                if (findReleased(player, UUID.fromString(previous.substring(0, mark))) instanceof Mob other && other != mob) {
                    AllyUtil.makeAllies(mob, other, false);
                }
            }
            spawned.add(mob);
            housing.released.add(mob.getUUID() + "|" + entry);
        }
    }

    private static void forget(ServerPlayer player, LivingEntity spirit) {
        if (!player.getStringUUID().equals(spirit.getPersistentData().getString(OWNER))) return;
        AllyUtil.removeAllies(player, spirit, false);
    }

    private static boolean commandedTogether(LivingEntity first, LivingEntity second) {
        if (first == null || second == null) return false;
        String owner = first.getPersistentData().getString(OWNER);
        String other = second.getPersistentData().getString(OWNER);
        if (!owner.isEmpty() && (owner.equals(second.getStringUUID()) || owner.equals(other))) return true;
        return !other.isEmpty() && other.equals(first.getStringUUID());
    }

    @SubscribeEvent
    public static void onSpiritTarget(LivingChangeTargetEvent event) {
        if (commandedTogether(event.getEntity(), event.getNewAboutToBeSetTarget())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onSpiritFriendlyFire(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (commandedTogether(attacker, event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onSpiritDeath(LivingDeathEvent event) {
        String owner = event.getEntity().getPersistentData().getString(OWNER);
        if (owner.isEmpty() || !(event.getEntity().level() instanceof ServerLevel level)) return;
        if (level.getServer().getPlayerList().getPlayer(UUID.fromString(owner)) instanceof ServerPlayer player) {
            AllyUtil.removeAllies(player, event.getEntity(), false);
        }
    }

    private void refund(LivingEntity entity) {
        BeyonderData.incrementSpirituality(entity, spiritualityCost(BeyonderData.getSequence(entity)));
    }

    private static String entityKey(String typeId) {
        return "entity." + typeId.replace(':', '.');
    }
}
