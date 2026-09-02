package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.*;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber
public class HistoricalVoidBorrowingAbility extends SelectableAbility {
    public HistoricalVoidBorrowingAbility(String id) {
        super(id, 1);

        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeUsedInArtifact = false;
        canBeShared = false;
        canBeCopied = false;
        canBeReplicated = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 3));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.historical_void_borrowing.borrow_health",
                "ability.lotmcraft.historical_void_borrowing.borrow_spirituality",
                "ability.lotmcraft.historical_void_borrowing.borrow_cleansed_state",
                "ability.lotmcraft.historical_void_borrowing.borrow_sequence",
                "ability.lotmcraft.historical_void_borrowing.borrow_effects",
                "ability.lotmcraft.historical_void_borrowing.return_all_borrows"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if(abilityIndex == 0 || abilityIndex == 2 || abilityIndex == 3 || abilityIndex == 4) {
            if(BeyonderData.getSpirituality(entity) < 3000) return;
            BeyonderData.reduceSpirituality(entity, 3000);
        }

        switch(abilityIndex) {
            case 0:
                historicalVoidBorrowHealth(player, serverLevel);
                break;
            case 1:
                historicalVoidBorrowSpirituality(player, serverLevel);
                break;
            case 2:
                historicalVoidBorrowCleansedState(player, serverLevel);
                break;
            case 3:
                historicalVoidBorrowSequence(player, serverLevel);
                break;
            case 4:
                historicalVoidBorrowEffects(player, serverLevel);
                break;
            case 5:
                returnAllBorrows(player);
                break;
        }
    }


    private static ItemStack createEntityDisplayItem(CompoundTag entityData) {
        String entityId = entityData.getString("EntityType");
        String customName = entityData.getString("CustomName");

        // Create a spawn egg or representation item
        ItemStack display = new ItemStack(Items.PLAYER_HEAD);
        display.set(DataComponents.CUSTOM_NAME,
                Component.literal(customName.isEmpty() ? entityId : customName));

        CompoundTag customTag = new CompoundTag();
        customTag.put("EntityData", entityData);

        display.set(DataComponents.CUSTOM_DATA,
                CustomData.of(customTag)
        );

        return display;
    }

    private static List<CompoundTag> getMarkedEntities(ServerPlayer player) {
        HistoricalMarkedComponent data = player.getData(ModAttachments.HISTORICAL_MARKED_ENTITIES_COMPONENT);
        return data.getMarkedEntities();
    }



    public static void historicalVoidBorrowHealth(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            if (player.getHealth() < player.getMaxHealth()) {

                HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
                for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
                    if (info.type() == HistoricalVoidSummoningAbility.SummonType.HEALTH) {
                        decrementHistoricalBorrowingCount(player, info.summonTime());
                    }
                }

                long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                CompoundTag tag = new CompoundTag();
                tag.putFloat("health", player.getHealth());

                incrementHistoricalBorrowingCount(player, borrowTime, HistoricalVoidSummoningAbility.SummonType.HEALTH, player.getUUID(), tag);

                player.setHealth(player.getMaxHealth());
            }
        }
    }

    public static void historicalVoidBorrowSpirituality(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            if (BeyonderData.getSpirituality(player) < BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player))) {

                HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
                for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
                    if (info.type() == HistoricalVoidSummoningAbility.SummonType.SPIRITUALITY) {
                        decrementHistoricalBorrowingCount(player, info.summonTime());
                    }
                }

                long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                CompoundTag tag = new CompoundTag();
                tag.putFloat("spirituality", BeyonderData.getSpirituality(player));

                incrementHistoricalBorrowingCount(player, borrowTime, HistoricalVoidSummoningAbility.SummonType.SPIRITUALITY, player.getUUID(), tag);

                BeyonderData.setSpirituality(player, BeyonderData.getMaxSpirituality(BeyonderData.getPathway(player), BeyonderData.getSequence(player)));
            }
        }
    }

    public static void historicalVoidBorrowCleansedState(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {

            HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
            for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
                if (info.type() == HistoricalVoidSummoningAbility.SummonType.CLEANSED_STATE) {
                    decrementHistoricalBorrowingCount(player, info.summonTime());
                }
            }

            long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
            CompoundTag tag = new CompoundTag();

            // save if movement was stolen
            AttributeInstance movementSpeedInner = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if(movementSpeedInner != null && movementSpeedInner.hasModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"))) {
                tag.putBoolean("WalkStolen", true);

                // give back movement
                movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
            }

            // save harmful effects to reapply later
            ListTag effectsList = new ListTag();
            for (MobEffectInstance instance : new ArrayList<>(player.getActiveEffects())) {
                if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                    effectsList.add(instance.save());
                    // remove them from the player
                    player.removeEffect(instance.getEffect());
                }
            }
            if(!effectsList.isEmpty()) {
                tag.put("StolenEffects", effectsList);
            }

            // save disabled abilities to reapply later
            DisabledAbilitiesComponent disabledAbilitiesComponent = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
            ListTag abilitiesList = new ListTag();
            for (DisabledAbilitiesComponent.DisabledAbility entry : disabledAbilitiesComponent.getAllDisabledAbilities()) {
                CompoundTag abilityTag = new CompoundTag();
                abilityTag.putString("AbilityName", entry.ability());
                abilityTag.putInt("Amount", entry.amountDisabled());
                abilitiesList.add(abilityTag);
            }
            tag.put("DisabledAbilities", abilitiesList);

            SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
            tag.putFloat("sanity", sanityComponent.getSanity());
            sanityComponent.setSanity(1.0f);

            incrementHistoricalBorrowingCount(player, borrowTime, HistoricalVoidSummoningAbility.SummonType.CLEANSED_STATE, player.getUUID(), tag);
        }
    }

    public static void historicalVoidBorrowEffects(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {

            HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
            for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
                if (info.type() == HistoricalVoidSummoningAbility.SummonType.EFFECT) {
                    decrementHistoricalBorrowingCount(player, info.summonTime());
                }
            }

            if (data.getSavedEffects().isEmpty()) return;

            long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
            CompoundTag tag = new CompoundTag();
            tag.putFloat("spirituality", BeyonderData.getSpirituality(player));

            incrementHistoricalBorrowingCount(player, borrowTime, HistoricalVoidSummoningAbility.SummonType.EFFECT, player.getUUID(), tag);


            for (HistoricalVoidComponent.SavedEffect saved : data.getSavedEffects()) {
                BuiltInRegistries.MOB_EFFECT.getHolder(saved.effectId()).ifPresent(holder -> {
                    player.addEffect(new MobEffectInstance(
                            holder,
                            Math.min(saved.duration(), getMaxHistoricalBorrowingDurationTicks(player)),
                            saved.amplifier()
                    ));
                });
            }

        }
    }

    public static void historicalVoidBorrowSequence(ServerPlayer player, ServerLevel level) {
        if (getHistoricalBorrowingCount(player) <= getMaxHistoricalBorrowingCount(player)) {
            SimpleContainer entityContainer = new SimpleContainer(54) {
                @Override
                public boolean canTakeItem(Container target, int index, ItemStack stack) {
                    return false; // Prevent taking items normally
                }
            };
            List<CompoundTag> markedEntities = getMarkedEntities(player);

            for(int i = 0; i < Math.min(markedEntities.size(), 53); i++) {
                CompoundTag entityData = markedEntities.get(i);
                ItemStack displayItem = createEntityDisplayItem(entityData);
                if (entityData.contains("EntityNBT")) {
                    CompoundTag entityNBT = entityData.getCompound("EntityNBT");
                    CompoundTag nfd = entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component");

                    if (nfd.contains("pathway")) {

                        if (entityData.contains("OriginalPlayerUUID")) {
                            if (entityData.getUUID("OriginalPlayerUUID").equals(player.getUUID()) && nfd.getInt("sequence") > 0) {
                                boolean isMarionette = Optional.of(entityNBT.getCompound("neoforge:attachments").getCompound("lotmcraft:marionette_component")).map(c -> c.getBoolean("isMarionette")).orElse(false);
                                displayItem.set(
                                        DataComponents.LORE,
                                        new ItemLore(List.of(
                                                Component.literal("-------------------").withStyle(style -> style.withColor(0xFFa742f5).withItalic(false)),
                                                Component.translatable("lotm.pathway").append(Component.literal(": ")).append(Component.literal(BeyonderData.pathwayInfos.get(nfd.getString("pathway")).getSequenceName(9))).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                                Component.translatable("lotm.sequence").append(Component.literal(": ")).append(Component.literal(nfd.getInt("sequence") + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false)),
                                                Component.translatable("lotm.marionette").append(Component.literal(": ")).append(Component.literal(isMarionette + "")).withColor(0xa26fc9).withStyle(style -> style.withItalic(false))
                                        )));
                            } else {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
                }
                entityContainer.setItem(i + 1, displayItem);
            }

            final int finalContainerSize = entityContainer.getContainerSize();

            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, entityContainer, 6) {
                        @Override
                        public void clicked(int slotId, int button, ClickType clickType, Player clickPlayer) {
                            if(slotId >= 0 && slotId < finalContainerSize) {
                                ItemStack clickedItem = entityContainer.getItem(slotId);

                                if(clickedItem.isEmpty()) return;

                                CustomData customData = clickedItem.get(DataComponents.CUSTOM_DATA);

                                if(customData == null) return;

                                CompoundTag tag = customData.copyTag();

                                if(tag.contains("EntityData")) {
                                    CompoundTag entityData = tag.getCompound("EntityData");
                                    long borrowTime = level.getGameTime() + getMaxHistoricalBorrowingDurationTicks(player);
                                    CompoundTag anotherTag = new CompoundTag();
                                    anotherTag.putFloat("sequence", BeyonderData.getSequence(player));
                                    anotherTag.putString("pathway", BeyonderData.getPathway(player));

                                    incrementHistoricalBorrowingCount(player, borrowTime, HistoricalVoidSummoningAbility.SummonType.SEQUENCE, player.getUUID(), anotherTag);

                                    BeyonderData.setPathway(player, entityData.getCompound("EntityNBT").getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component").getString("pathway"));
                                    BeyonderData.setSequence(player, entityData.getCompound("EntityNBT").getCompound("neoforge:attachments").getCompound("lotmcraft:beyonder_component").getInt("sequence"));
                                    player.closeContainer();
                                }
                            }
                        }
                    },
                    Component.literal("select your strongest marked version")
            ));
        }
    }

    public static void returnAllBorrows(ServerPlayer serverPlayer) {
        HistoricalVoidComponent data = serverPlayer.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());

        List<Long> activeBorrows = new ArrayList<>(data.activeSummonTimes.keySet());

        for (Long borrowTime : activeBorrows) {
            HistoricalVoidComponent.SummonInfo info = data.activeSummonTimes.get(borrowTime);
            if (info == null) continue;

            decrementHistoricalBorrowingCount(serverPlayer, borrowTime);
        }
        data.reset();
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;

        returnAllBorrows(player);
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (player.tickCount % 20 != 0) return;
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;

        HistoricalVoidComponent data = serverPlayer.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        for (HistoricalVoidComponent.SummonInfo info : data.activeSummonTimes.values()) {
            if (info.type() == HistoricalVoidSummoningAbility.SummonType.HEALTH ||
                    info.type() == HistoricalVoidSummoningAbility.SummonType.SPIRITUALITY ||
                    info.type() == HistoricalVoidSummoningAbility.SummonType.CLEANSED_STATE||
                    info.type() == HistoricalVoidSummoningAbility.SummonType.SEQUENCE) {

                if (serverLevel.getGameTime() > info.summonTime()) {
                    decrementHistoricalBorrowingCount(serverPlayer, info.summonTime());
                }
            }
        }
    }


    private static void incrementHistoricalBorrowingCount(ServerPlayer player, long borrowTime, HistoricalVoidSummoningAbility.SummonType type, UUID entityUUID, CompoundTag originalBeforeBorrowing) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.historicalBorrowingCount++;
        HistoricalVoidComponent.SummonInfo info = new HistoricalVoidComponent.SummonInfo(
                borrowTime,
                type,
                entityUUID,
                originalBeforeBorrowing
        );
        data.activeSummonTimes.put(borrowTime, info);
    }

    private static void decrementHistoricalBorrowingCount(ServerPlayer player, long borrowTime) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        data.historicalBorrowingCount = Math.max(0, data.historicalBorrowingCount - 1);

        HistoricalVoidComponent.SummonInfo specificInfo = data.activeSummonTimes.get(borrowTime);
        if(specificInfo != null) {

            if(specificInfo.type() == HistoricalVoidSummoningAbility.SummonType.HEALTH) {
                player.setHealth(specificInfo.originalBeforeBorrowing().getFloat("health"));
            }
            else if (specificInfo.type() == HistoricalVoidSummoningAbility.SummonType.SPIRITUALITY) {
                BeyonderData.setSpirituality(player, specificInfo.originalBeforeBorrowing().getFloat("spirituality"));
            }
            else if (specificInfo.type() == HistoricalVoidSummoningAbility.SummonType.CLEANSED_STATE) {
                CompoundTag tag = specificInfo.originalBeforeBorrowing();
                if (tag.getBoolean("WalkStolen")) {
                    AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (movementSpeed != null) {
                        movementSpeed.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
                        movementSpeed.addTransientModifier(new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"),
                                -100.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ));
                    }
                    ServerScheduler.scheduleDelayed(20 * 20, () -> {
                        AttributeInstance movementSpeedInner = player.getAttribute(Attributes.MOVEMENT_SPEED);

                        if(movementSpeedInner != null) {
                            movementSpeedInner.removeModifier(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, "mundane_conceptual_theft_walk"));
                        }
                    });
                }
                if (tag.contains("StolenEffects")) {
                    ListTag effectsList = tag.getList("StolenEffects", Tag.TAG_COMPOUND);
                    for (int i = 0; i < effectsList.size(); i++) {
                        MobEffectInstance effect = MobEffectInstance.load(effectsList.getCompound(i));
                        if (effect != null) {
                            player.addEffect(effect);
                        }
                    }
                }
                if (tag.contains("DisabledAbilities")) {
                    ListTag disabledAbilitiesList = tag.getList("DisabledAbilities", Tag.TAG_COMPOUND);
                    DisabledAbilitiesComponent disabledComponent = player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT);
                    for (int i = 0; i < disabledAbilitiesList.size(); i++) {
                        disabledComponent.disableSpecificAbilityForTime(disabledAbilitiesList.getCompound(i).getString("AbilityName"), "theft_", 30 * 20);
                    }
                }
                if (tag.contains("sanity")) {
                    player.getData(ModAttachments.SANITY_COMPONENT).setSanity(specificInfo.originalBeforeBorrowing().getFloat("sanity"));
                }
            } else if (specificInfo.type() == HistoricalVoidSummoningAbility.SummonType.SEQUENCE) {
                BeyonderData.setPathway(player, specificInfo.originalBeforeBorrowing().getString("pathway"));
                BeyonderData.setSequence(player, specificInfo.originalBeforeBorrowing().getInt("sequence"));
            }
            data.activeSummonTimes.remove(borrowTime);
        }
    }


    private static int getHistoricalBorrowingCount(ServerPlayer player) {
        HistoricalVoidComponent data = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT.get());
        return data.historicalBorrowingCount;
    }

    private static int getMaxHistoricalBorrowingCount(ServerPlayer serverPlayer){
        return switch (BeyonderData.getSequence(serverPlayer)){
            case 0 -> 50;
            case 1 -> 20;
            case 2 -> 10;
            default -> 5;
        };
    }

    private static int getMaxHistoricalBorrowingDurationTicks(ServerPlayer serverPlayer){
        return switch (BeyonderData.getSequence(serverPlayer)){
            case 0 -> 60 * 60 * 20;
            case 1 -> 10 * 60 * 20;
            case 2 -> 4 * 20;
            default -> 60 * 20;
        };
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        MobEffectInstance newEffect = event.getEffectInstance();

        if (newEffect.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) {
            return;
        }

        ResourceLocation effectId = newEffect.getEffect().unwrapKey()
                .map(ResourceKey::location)
                .orElse(null);

        HistoricalVoidComponent historicalVoidComponentDataBigName = player.getData(ModAttachments.HISTORICAL_VOID_COMPONENT);

        int amplifier = Math.min(newEffect.getAmplifier(), 5);
        int duration = newEffect.getDuration();

        // check if effect already exists in saved list
        HistoricalVoidComponent.SavedEffect existingEffect = null;
        int existingIndex = -1;

        for (int i = 0; i < historicalVoidComponentDataBigName.getSavedEffects().size(); i++) {
            HistoricalVoidComponent.SavedEffect saved = historicalVoidComponentDataBigName.getSavedEffects().get(i);
            if (saved.effectId().equals(effectId)) {
                existingEffect = saved;
                existingIndex = i;
                break;
            }
        }

        if (existingEffect != null) {
            // update existing effect
            int updatedAmplifier = Math.max(existingEffect.amplifier(), amplifier);
            int updatedDuration = Math.max(existingEffect.duration(), duration);

            historicalVoidComponentDataBigName.getSavedEffects().set(
                    existingIndex,
                    new HistoricalVoidComponent.SavedEffect(effectId, updatedAmplifier, updatedDuration)
            );
        } else {
            historicalVoidComponentDataBigName.addSavedEffect(effectId, amplifier, duration);
        }
    }
}