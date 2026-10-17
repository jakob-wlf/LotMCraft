package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.beyonders.abilities.death.EyeOfDeathAbility;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toClient.SyncDawnSpearThrowPacket;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ArsenalOfDawnAbility extends SelectableAbility {
    private static final ResourceLocation DAMAGE_ID = ResourceLocation.withDefaultNamespace("base_attack_damage");
    private static final ResourceLocation SPEED_ID = ResourceLocation.withDefaultNamespace("base_attack_speed");
    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "dawn_spear_reach");
    private static final double SPEAR_REACH_BONUS = 1.0D;
    private static final int FULL_DRAW_TICKS = 20;
    private static final int DAWN_DRAW_TICKS = 4;
    private static final double BOW_RANGE = 48.0D;
    private static final int BOW_DAMAGE = 22;
    private static final int BOW_EVIL_MULTIPLIER = 2;

    public static final String HELMET = "helmet";
    public static final String CHESTPLATE = "chestplate";
    public static final String LEGGINGS = "leggings";
    public static final String BOOTS = "boots";
    public static final String AXE = "axe";
    public static final String SPEAR = "spear";
    public static final String SWORD = "sword";
    public static final Set<String> ARMOR = Set.of(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
    public static final String BOW = "bow";
    public static final Set<String> WEAPONS = Set.of(AXE, SPEAR, SWORD, BOW);
    public static final String TWILIGHT_SWORD = "twilight_sword";
    public static final Set<String> TWILIGHT = Set.of(TWILIGHT_SWORD);
    public static final Set<String> SWORDS = Set.of(SWORD, TWILIGHT_SWORD);
    public static final String SILVER_HELMET = "silver_helmet";
    public static final String SILVER_CHESTPLATE = "silver_chestplate";
    public static final String SILVER_LEGGINGS = "silver_leggings";
    public static final String SILVER_BOOTS = "silver_boots";
    public static final Set<String> SILVER_ARMOR = Set.of(SILVER_HELMET, SILVER_CHESTPLATE, SILVER_LEGGINGS, SILVER_BOOTS);

    private static final String TAG = "lotm_dawn_gear";
    public static final String REDUCTION_TAG = "lotm_dawn_reduction";
    private static final String OWNER_TAG = "lotm_dawn_owner";
    private static final String CREATOR_TAG = "lotm_dawn_creator";
    private static final int SWEEP_INTERVAL = 2*20;
    private static final Set<UUID> restoringArmor = new HashSet<>();
    private static final float EVIL_DAMAGE_MULTIPLIER = 2.0f;
    private static final String[] PURIFICATION_FLAGS = {"purification"};
    private static final int UPKEEP_INTERVAL = 20;
    private static final float UPKEEP_COST = 8;

    public ArsenalOfDawnAbility(String id) {
        super(id, 0);
        canBeUsedByNPC = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(200f, 150f, 120f, 100f, 80f, 60f, 50f));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 50;
    }

    @Override
    protected String[] getAbilityNames() {
        return namesFor(ClientBeyonderCache.localSequence());
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        String[] names = namesFor(BeyonderData.getSequence(entity));
        int selected = getSelectedAbilityIndex(entity.getUUID());
        if (selected < 0 || selected >= names.length) selected = 0;
        return names[selected];
    }

    @Override
    public void setSelectedAbility(ServerPlayer player, int selectedAbility) {
        if (selectedAbility < 0 || selectedAbility >= namesFor(BeyonderData.getSequence(player)).length) return;
        selectedAbilities.put(player.getUUID(), selectedAbility);
    }

    private static String[] namesFor(int sequence) {
        if (sequence <= 3) return new String[]{
                "ability.lotmcraft.arsenal_of_dawn.axe",
                "ability.lotmcraft.arsenal_of_dawn.spear",
                "ability.lotmcraft.arsenal_of_dawn.sword",
                "ability.lotmcraft.arsenal_of_dawn.bow",
        };
        return new String[]{
                "ability.lotmcraft.arsenal_of_dawn.axe",
                "ability.lotmcraft.arsenal_of_dawn.spear",
                "ability.lotmcraft.arsenal_of_dawn.sword",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof Player player)) return;
        String kind = switch (selectedAbility) {
            case 1 -> SPEAR;
            case 2 -> SWORD;
            case 3 -> BOW;
            default -> AXE;
        };
        if (hasAny(player, Set.of(kind))) {
            removeAll(player, Set.of(kind));
            BeyonderData.incrementSpirituality(player, getSpiritualityCost());
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 0.7f, 1.4f);
            return;
        }
        boolean silverKnight = BeyonderData.getSequence(player) <= 3;
        if (selectedAbility == 3 && !silverKnight) {
            BeyonderData.incrementSpirituality(player, getSpiritualityCost());
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.arsenal_of_dawn.bow_locked").withColor(0xFFFFF3D6));
            return;
        }

        ItemStack weapon = switch (selectedAbility) {
            case 1 -> spear();
            case 2 -> weapon(ModItems.DAWN_SWORD.get(), SWORD, 5.5, -2.4, ItemAttributeModifiers.builder());
            case 3 -> create(Items.BOW, BOW, ItemAttributeModifiers.EMPTY);
            default -> weapon(ModItems.DAWN_AXE.get(), AXE, 8.0, -3.05, ItemAttributeModifiers.builder());
        };
        CustomData.update(DataComponents.CUSTOM_DATA, weapon, tag -> tag.putString(CREATOR_TAG, player.getUUID().toString()));
        if (silverKnight) makeShareable(weapon, player);

        removeAll(player, WEAPONS);
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        } else if (!player.getInventory().add(weapon)) {
            return;
        }

        ParticleUtil.spawnParticles(serverLevel, ParticleTypes.END_ROD, player.position().add(0, 1.2, 0), 30, 0.4, 0.6, 0.4, 0.04);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 0.7f, 1.8f);
    }

    private static ItemStack spear() {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(REACH_ID, SPEAR_REACH_BONUS, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        return weapon(ModItems.DAWN_SPEAR.get(), SPEAR, 5.5, -2.9, builder);
    }

    @SubscribeEvent
    public static void onSpearSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof ThrownTrident trident) || !is(trident.getWeaponItem(), SPEAR)) return;
        ServerScheduler.scheduleDelayed(1, () -> PacketHandler.sendToTrackingAndSelf(trident, new SyncDawnSpearThrowPacket(trident.getId())), (ServerLevel) event.getLevel());
    }

    @SubscribeEvent
    public static void onTrackSpear(PlayerEvent.StartTracking event) {
        if (!(event.getTarget() instanceof ThrownTrident trident) || !(event.getEntity() instanceof ServerPlayer player) || !is(trident.getWeaponItem(), SPEAR)) return;
        PacketHandler.sendToPlayer(player, new SyncDawnSpearThrowPacket(trident.getId()));
    }

    @SubscribeEvent
    public static void onSpearHit(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownTrident trident) || trident.level().isClientSide() || !is(trident.getWeaponItem(), SPEAR)) return;
        ServerScheduler.scheduleDelayed(1, trident::discard, (ServerLevel) trident.level());
    }

    private static ItemStack weapon(Item base, String kind, double damage, double speed, ItemAttributeModifiers.Builder builder) {
        ItemAttributeModifiers modifiers = builder
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_ID, damage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
        return create(base, kind, modifiers);
    }

    public static ItemStack create(Item item, String kind, ItemAttributeModifiers modifiers) {
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG, kind);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.lotmcraft.dawn_" + kind));
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        return stack;
    }

    public static void bind(Level level, ItemStack stack) {
        stack.enchant(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
        ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants != null) stack.set(DataComponents.ENCHANTMENTS, enchants.withTooltip(false));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
    }

    public static String kind(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(TAG)) return "";
        return data.copyTag().getString(TAG);
    }

    public static boolean is(ItemStack stack, Set<String> kinds) {
        return !stack.isEmpty() && kinds.contains(kind(stack));
    }

    public static boolean is(ItemStack stack, String kind) {
        return !stack.isEmpty() && kind.equals(kind(stack));
    }

    public static void makeShareable(ItemStack stack, Player owner) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(OWNER_TAG, owner.getUUID()));
    }

    public static boolean isShareable(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(OWNER_TAG);
    }

    public static boolean isOwnedBy(ItemStack stack, Player player) {
        return !isShareable(stack) || player.getUUID().equals(stack.get(DataComponents.CUSTOM_DATA).copyTag().getUUID(OWNER_TAG));
    }

    public static boolean hasAny(Player player, Set<String> kinds) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (is(stack, kinds) && isOwnedBy(stack, player)) return true;
        }
        return false;
    }

    public static void removeAll(Player player, Set<String> kinds) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (is(stack, kinds) && isOwnedBy(stack, player)) inventory.setItem(i, ItemStack.EMPTY);
        }
    }

    public static boolean isEvil(LivingEntity entity) {
        if (AbilityUtil.isUndead(entity) || EyeOfDeathAbility.isSpiritEntity(entity)) return true;
        return BeyonderData.isEvilPathway(entity);
    }

    public static void purify(LivingIncomingDamageEvent event, LivingEntity attacker, ServerLevel level) {
        LivingEntity target = event.getEntity();
        if (isEvil(target)) event.setAmount(event.getAmount() * EVIL_DAMAGE_MULTIPLIER);
        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(level, target.position(), attacker, null, PURIFICATION_FLAGS, 1.5, 10));
    }

    public static boolean isMeleeHit(LivingIncomingDamageEvent event) {
        return event.getSource().getEntity() instanceof LivingEntity attacker && event.getSource().getDirectEntity() == attacker;
    }

    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (kind(stack).isEmpty() || isShareable(stack)) return;
        event.setCanceled(true);
        event.getPlayer().getInventory().add(stack);
    }

    @SubscribeEvent
    public static void onArmorChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (restoringArmor.contains(player.getUUID()) || player.isDeadOrDying()) return;
        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) return;
        ItemStack from = event.getFrom();
        if (!wornArmorLocked(player, from)) return;
        ItemStack to = event.getTo();
        if (is(from, ARMOR) && is(to, ARMOR)) return;
        if (is(from, SILVER_ARMOR) && is(to, SILVER_ARMOR)) return;
        ItemStack snapshot = from.copy();
        boolean kept;
        restoringArmor.add(player.getUUID());
        try {
            kept = keepArmor(player, slot, snapshot, to);
        } finally {
            restoringArmor.remove(player.getUUID());
        }
        if (kept || !(player.level() instanceof ServerLevel serverLevel)) return;
        ServerScheduler.scheduleDelayed(1, () -> {
            if (!player.isAlive() || player.isDeadOrDying() || restoringArmor.contains(player.getUUID())) return;
            if (!wornArmorLocked(player, snapshot)) return;
            if (samePiece(player.getItemBySlot(slot), snapshot)) return;
            restoringArmor.add(player.getUUID());
            try {
                keepArmor(player, slot, snapshot, player.getItemBySlot(slot));
            } finally {
                restoringArmor.remove(player.getUUID());
            }
        }, serverLevel);
    }

    private static boolean wornArmorLocked(Player player, ItemStack stack) {
        if (is(stack, ARMOR)) return toggleActive(player, "dawn_armor_ability");
        return is(stack, SILVER_ARMOR) && toggleActive(player, "silver_armor_ability");
    }

    private static boolean toggleActive(Player player, String id) {
        for (ToggleAbility ability : ToggleAbility.getActiveAbilitiesForEntity(player)) {
            if (id.equals(ability.getId())) return true;
        }
        return false;
    }

    private static boolean keepArmor(Player player, EquipmentSlot slot, ItemStack from, ItemStack to) {
        ItemStack carried = player.containerMenu.getCarried();
        if (samePiece(carried, from)) {
            player.containerMenu.setCarried(to);
            player.setItemSlot(slot, carried);
            return true;
        }
        Inventory inventory = player.getInventory();
        ItemStack worn = player.getItemBySlot(slot);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == worn || !samePiece(stack, from)) continue;
            inventory.setItem(i, to);
            player.setItemSlot(slot, stack);
            return true;
        }
        return false;
    }

    private static boolean samePiece(ItemStack stack, ItemStack from) {
        return !stack.isEmpty() && !from.isEmpty() && (stack == from || ItemStack.matches(stack, from));
    }

    @SubscribeEvent
    public static void onPlaceGear(PlayerInteractEvent.EntityInteract event) {
        if (!displayWeapon(event.getItemStack(), event.getTarget())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent
    public static void onPlaceGearSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!displayWeapon(event.getItemStack(), event.getTarget())) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    private static boolean displayWeapon(ItemStack stack, Entity target) {
        if (!is(stack, WEAPONS) && !is(stack, TWILIGHT)) return false;
        return target instanceof ItemFrame || target instanceof ArmorStand;
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            removeAll(player, ARMOR);
            removeAll(player, WEAPONS);
            removeAll(player, SILVER_ARMOR);
            removeAll(player, TWILIGHT);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % UPKEEP_INTERVAL != 0 || player.hasInfiniteMaterials()) return;
        upkeep(player, WEAPONS, UPKEEP_COST);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % SWEEP_INTERVAL != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            strip(player.getInventory().items, id);
            strip(player.getInventory().armor, id);
            strip(player.getInventory().offhand, id);
            if (storedAway(player.containerMenu.getCarried(), id)) player.containerMenu.setCarried(ItemStack.EMPTY);
            for (Slot slot : player.containerMenu.slots) {
                if (!storedAway(slot.getItem(), slot.container instanceof Inventory ? id : null)) continue;
                slot.set(ItemStack.EMPTY);
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.containerMenu.sendAllDataToRemote();
            clearAway(player.getEnderChestInventory());
        }
        for (ServerLevel level : server.getAllLevels()) {
            List<ItemEntity> drops = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity item && is(item.getItem(), WEAPONS)) drops.add(item);
                else if (entity instanceof ItemFrame frame && is(frame.getItem(), WEAPONS)) frame.setItem(ItemStack.EMPTY);
                else if (entity instanceof Container container) clearAway(container);
            }
            for (ItemEntity drop : drops) drop.discard();
            int range = server.getPlayerList().getViewDistance();
            Set<Long> seen = new HashSet<>();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.serverLevel() != level) continue;
                int cx = player.chunkPosition().x;
                int cz = player.chunkPosition().z;
                for (int x = cx - range; x <= cx + range; x++) {
                    for (int z = cz - range; z <= cz + range; z++) {
                        if (!seen.add(ChunkPos.asLong(x, z))) continue;
                        LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                        if (chunk == null) continue;
                        for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                            if (blockEntity instanceof Container container) clearAway(container);
                        }
                    }
                }
            }
        }
    }

    private static void strip(List<ItemStack> stacks, UUID holder) {
        for (int i = 0; i < stacks.size(); i++) {
            if (!storedAway(stacks.get(i), holder)) continue;
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    private static void clearAway(Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!is(container.getItem(i), WEAPONS)) continue;
            container.setItem(i, ItemStack.EMPTY);
            container.setChanged();
        }
    }

    private static boolean storedAway(ItemStack stack, UUID holder) {
        if (!is(stack, WEAPONS)) return false;
        UUID owner = owner(stack);
        return holder == null || owner == null || !owner.equals(holder);
    }

    private static UUID owner(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (tag.contains(CREATOR_TAG, Tag.TAG_STRING)) return UUID.fromString(tag.getString(CREATOR_TAG));
        if (tag.hasUUID(CREATOR_TAG)) return tag.getUUID(CREATOR_TAG);
        if (tag.hasUUID(OWNER_TAG)) return tag.getUUID(OWNER_TAG);
        return null;
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDawnReduction(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        float reduction = setReduction(event.getEntity(), ARMOR);
        if (reduction <= 0) reduction = setReduction(event.getEntity(), SILVER_ARMOR);
        if (reduction <= 0) return;
        event.setAmount(event.getAmount() * (1.0F - reduction));
    }

    private static float setReduction(LivingEntity entity, Set<String> kinds) {
        float reduction = Float.MAX_VALUE;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!is(stack, kinds)) return 0;
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data == null || !data.contains(REDUCTION_TAG)) return 0;
            reduction = Math.min(reduction, data.copyTag().getFloat(REDUCTION_TAG));
        }
        return reduction;
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getSource().getDirectEntity() != attacker) return;
        if (!(attacker.level() instanceof ServerLevel level) || !is(attacker.getMainHandItem(), WEAPONS)) return;
        if (BeyonderData.getSequence(attacker) == 6 && !isEvil(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        purify(event, attacker, level);
    }

    @SubscribeEvent
    public static void onNock(ArrowNockEvent event) {
        if (!is(event.getBow(), BOW)) return;
        event.getEntity().startUsingItem(event.getHand());
        event.setAction(InteractionResultHolder.consume(event.getBow()));
    }

    @SubscribeEvent
    public static void onStartUsing(LivingEntityUseItemEvent.Start event) {
        if (!is(event.getItem(), BOW)) return;
        event.setDuration(event.getDuration() - (FULL_DRAW_TICKS - DAWN_DRAW_TICKS));
    }

    @SubscribeEvent
    public static void onLoose(ArrowLooseEvent event) {
        if (!is(event.getBow(), BOW)) return;
        event.setCanceled(true);
        Player player = event.getEntity();
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        float power = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1f) return;
        shoot(level, player, power);
    }

    private static void upkeep(Player player, Set<String> kinds, float cost) {
        if (!hasAny(player, kinds)) return;
        if (BeyonderData.getSpirituality(player) < cost) {
            removeAll(player, kinds);
            player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, player.getSoundSource(), 0.7f, 1.4f);
            return;
        }
        BeyonderData.reduceSpirituality(player, cost);
    }

    private static void shoot(ServerLevel level, Player player, float power) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(BOW_RANGE));
        Vec3 blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        AABB box = player.getBoundingBox().expandTowards(blockHit.subtract(start)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, start, blockHit, box,
                entity -> entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator() && entity != player, BOW_RANGE * BOW_RANGE);
        Vec3 impact = hit != null ? hit.getLocation() : blockHit;

        ParticleUtil.drawParticleLine(level, ParticleTypes.END_ROD, start.add(0, -0.1, 0), impact, 0.3, 1);
        level.sendParticles(ParticleTypes.FLASH, impact.x, impact.y, impact.z, 1, 0, 0, 0, 0);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, player.getSoundSource(), 1f, 2f);

        if (hit == null) return;
        Entity entity = hit.getEntity();
        if (!(entity instanceof LivingEntity target)) return;
        if (BeyonderData.getSequence(player) == 6 && !isEvil(target)) return;
        int damage = Math.round(BOW_DAMAGE * power);
        if (isEvil(target)) damage *= BOW_EVIL_MULTIPLIER;
        target.hurt(ModDamageTypes.source(level, ModDamageTypes.PURIFICATION, player), damage);
    }
}
