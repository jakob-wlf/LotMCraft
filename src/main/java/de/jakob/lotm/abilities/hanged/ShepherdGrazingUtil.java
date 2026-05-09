package de.jakob.lotm.abilities.hanged;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.core.ToggleAbility;
import de.jakob.lotm.attachments.CopiedAbilityComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ShepherdGrazingComponent;
import de.jakob.lotm.attachments.SanityComponent;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import de.jakob.lotm.util.helper.CopiedAbilityHelper;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ShepherdGrazingUtil {
    public static final String GRAZED_COPY_TYPE = "grazed";
    private static final float HIGHER_SEQUENCE_SANITY_CAP = 0.5f;
    private static final int MAX_CHOOSABLE_ABILITIES = 3;

    private ShepherdGrazingUtil() {
    }

    public static boolean isEligibleShepherd(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity)
                && HangedPathwayConstants.PATHWAY_ID.equals(BeyonderData.getPathway(entity))
                && BeyonderData.getSequence(entity) <= HangedPathwayConstants.SEQUENCE_SHEPHERD;
    }

    public static int getMaxSouls(ServerPlayer player) {
        float digestion = Math.max(0.0f, Math.min(1.0f, BeyonderData.getDigestionProgress(player)));
        return Math.max(1, Math.min(7, 1 + (int) Math.floor(digestion * 6.0f)));
    }

    public static void openGrazePrompt(ServerPlayer player, LivingEntity target) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        if (component.getSouls().size() >= getMaxSouls(player)) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.full")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        List<Ability> candidates = getCandidateAbilities(target);
        GrazeTargetData targetData = new GrazeTargetData(
                target.getName().getString(),
                BeyonderData.getPathway(target),
                BeyonderData.getSequence(target),
                BeyonderData.getSequence(target) <= HangedPathwayConstants.SEQUENCE_SHEPHERD - 1
                        && !(target instanceof net.minecraft.world.entity.player.Player),
                createSoulTag(target)
        );
        if (candidates.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.no_abilities")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        SimpleContainer container = createLockedContainer(27);
        fillContainer(container, createFiller(Items.BLACK_STAINED_GLASS_PANE));
        container.setItem(11, createMenuItem(
                Items.NETHER_STAR,
                Component.translatable("ability.lotmcraft.grazing.prompt_graze")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.prompt_graze_lore").withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.prompt_graze_warning").withStyle(ChatFormatting.DARK_RED)
                ),
                tagWith("Action", "graze"),
                true
        ));
        container.setItem(13, createMenuItem(
                targetData.manifestable() ? Items.NETHER_STAR : Items.SOUL_LANTERN,
                Component.literal(targetData.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                buildGrazePromptLore(targetData, candidates.size(), component.getSouls().size(), getMaxSouls(player), BeyonderData.getSequence(player)),
                null,
                targetData.manifestable()
        ));
        container.setItem(15, createMenuItem(
                Items.BARRIER,
                Component.translatable("ability.lotmcraft.grazing.prompt_skip")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.prompt_skip_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "skip"),
                false
        ));

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x3, id, inv, container, 3) {
                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, net.minecraft.world.entity.player.Player clickPlayer) {
                        if (slotId < 0 || slotId >= container.getContainerSize()) {
                            return;
                        }
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) {
                            return;
                        }
                        CustomData data = clicked.get(DataComponents.CUSTOM_DATA);
                        if (data == null) {
                            return;
                        }

                        String action = data.copyTag().getString("Action");
                        clickPlayer.closeContainer();
                        if ("graze".equals(action)) {
                            openAbilitySelectionMenu(player, targetData, candidates);
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.grazing.prompt_title")
        ));

        HangedEffectUtil.spawnFleshBurst(serverLevel, target.position().add(0, target.getBbHeight() * 0.45, 0), 0.9, 28);
        HangedEffectUtil.playFleshCast(serverLevel, target.position());
    }

    public static void openSoulArchive(ServerPlayer player) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        if (component.getSouls().isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.no_souls")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        SimpleContainer container = createLockedContainer(54);
        fillSoulArchive(container, player, component, false);

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6) {
                    private boolean releaseMode = false;

                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, net.minecraft.world.entity.player.Player clickPlayer) {
                        if (slotId < 0 || slotId >= container.getContainerSize()) {
                            return;
                        }
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) {
                            return;
                        }
                        CustomData data = clicked.get(DataComponents.CUSTOM_DATA);
                        if (data == null) {
                            return;
                        }
                        CompoundTag tag = data.copyTag();
                        String action = tag.getString("Action");

                        if ("toggle_release".equals(action)) {
                            releaseMode = !releaseMode;
                            fillSoulArchive(container, player, component, releaseMode);
                            broadcastChanges();
                            return;
                        }

                        if ("open_wheel".equals(action)) {
                            clickPlayer.closeContainer();
                            openActiveSoulWheel(player);
                            return;
                        }

                        if ("manifest".equals(action)) {
                            clickPlayer.closeContainer();
                            manifestOrRetrieveActiveSoul(player);
                            return;
                        }

                        if (!"soul".equals(action)) {
                            return;
                        }

                        String soulId = tag.getString("SoulId");
                        if (releaseMode) {
                            if (soulId.equals(component.getManifestedSoulId())) {
                                retrieveManifestedSoul(player, component);
                            }
                            component.removeSoul(soulId);
                            syncActiveSoul(player);
                            fillSoulArchive(container, player, component, true);
                            broadcastChanges();
                            return;
                        }

                        component.setActiveSoulId(soulId);
                        syncActiveSoul(player);
                        fillSoulArchive(container, player, component, false);
                        broadcastChanges();
                        if (component.getActiveSoul() != null) {
                            player.displayClientMessage(Component.translatable("ability.lotmcraft.grazing.active_soul",
                                    component.getActiveSoul().displayName()).withStyle(ChatFormatting.DARK_RED), true);
                        }
                    }
                },
                Component.translatable("ability.lotmcraft.grazing.archive_title")
        ));

        HangedEffectUtil.playShadowPulse(player.serverLevel(), player.position(), 0.8f);
    }

    public static void openActiveSoulWheel(ServerPlayer player) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        if (component.hasManifestedSoul()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.soul_manifested")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }
        if (component.getActiveSoul() == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.no_active_soul")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        syncActiveSoul(player);
        CopiedAbilityHelper.openCopiedAbilityWheel(player);
    }

    public static void manifestOrRetrieveActiveSoul(ServerPlayer player) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        ShepherdGrazingComponent.GrazedSoulData activeSoul = component.getActiveSoul();
        if (activeSoul == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.no_active_soul")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        if (component.hasManifestedSoul()) {
            retrieveManifestedSoul(player, component);
            return;
        }

        if (!activeSoul.manifestable()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.cannot_manifest")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        spawnManifestedSoul(player, component, activeSoul);
    }

    public static void tickPlayer(ServerPlayer player) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        if (component.getSouls().isEmpty() && component.getPermanentSanityCap() >= 1.0f) {
            return;
        }

        if (!isEligibleShepherd(player) && !component.getSouls().isEmpty()) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(de.jakob.lotm.effect.ModEffects.LOOSING_CONTROL, 40, 2, false, false, false));
            player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.01f, player);
        }

        boolean changed = false;
        UUID manifestedEntityUuid = component.getManifestedEntityUuid();
        Entity manifestedEntity = manifestedEntityUuid == null ? null : player.serverLevel().getEntity(manifestedEntityUuid);
        if (component.hasManifestedSoul() && (!(manifestedEntity instanceof LivingEntity living) || !living.isAlive())) {
            component.removeSoul(component.getManifestedSoulId());
            changed = true;
        }

        SanityComponent sanityComponent = player.getData(ModAttachments.SANITY_COMPONENT);
        if (sanityComponent.getSanity() > component.getPermanentSanityCap()) {
            sanityComponent.setSanityAndSync(component.getPermanentSanityCap(), player);
        }

        if (changed) {
            syncActiveSoul(player);
        }
    }

    public static boolean tryUseGrazedAbility(ServerPlayer player, CopiedAbilityComponent.CopiedAbilityData copiedData, Ability ability) {
        return tryUseGrazedAbility(player, copiedData.originalOwnerUUID(), ability);
    }

    public static boolean tryUseGrazedAbility(ServerPlayer player, String soulId, Ability ability) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        if (component.hasManifestedSoul()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.soul_manifested")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return false;
        }

        ShepherdGrazingComponent.GrazedSoulData soul = component.getSoul(soulId);
        if (soul == null || component.getActiveSoul() == null || !soul.soulId().equals(component.getActiveSoul().soulId())) {
            syncActiveSoul(player);
            return false;
        }

        if (!ability.canUse(player, false, true)) {
            return false;
        }

        ability.useAbility(player.serverLevel(), player, true, false, false);
        HangedEffectUtil.spawnShadowBurst(player.serverLevel(), player.position().add(0, player.getBbHeight() * 0.55, 0), 0.75, 22);
        HangedEffectUtil.playShadowPulse(player.serverLevel(), player.position(), 0.9f);
        return true;
    }

    public static ArrayList<String> getAbilityWheelAbilities(ServerPlayer player) {
        LinkedHashSet<String> merged = new LinkedHashSet<>(player.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
        ShepherdGrazingComponent.GrazedSoulData activeSoul = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT).getActiveSoul();
        if (activeSoul != null && !player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT).hasManifestedSoul()) {
            merged.addAll(activeSoul.abilityIds());
        }
        return new ArrayList<>(merged);
    }

    public static boolean isActiveGrazedAbility(ServerPlayer player, String abilityId) {
        ShepherdGrazingComponent.GrazedSoulData activeSoul = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT).getActiveSoul();
        return activeSoul != null
                && !player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT).hasManifestedSoul()
                && activeSoul.abilityIds().contains(abilityId);
    }

    public static void syncActiveSoul(ServerPlayer player) {
        ShepherdGrazingComponent grazingComponent = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        CopiedAbilityComponent copiedComponent = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);

        copiedComponent.getAbilities().removeIf(data -> GRAZED_COPY_TYPE.equals(data.copyType()));

        ShepherdGrazingComponent.GrazedSoulData activeSoul = grazingComponent.hasManifestedSoul() ? null : grazingComponent.getActiveSoul();
        if (activeSoul != null) {
            for (String abilityId : activeSoul.abilityIds()) {
                copiedComponent.addAbility(new CopiedAbilityComponent.CopiedAbilityData(
                        abilityId,
                        GRAZED_COPY_TYPE,
                        -1,
                        activeSoul.soulId()
                ));
            }
        }

        CopiedAbilityHelper.syncToClient(player);
        AbilityWheelHelper.syncToClient(player);
    }

    private static void fillSoulArchive(SimpleContainer container, ServerPlayer player, ShepherdGrazingComponent component, boolean releaseMode) {
        fillContainer(container, createFiller(Items.GRAY_STAINED_GLASS_PANE));

        ShepherdGrazingComponent.GrazedSoulData activeSoul = component.getActiveSoul();
        int maxSouls = getMaxSouls(player);
        container.setItem(0, createMenuItem(
                Items.WRITTEN_BOOK,
                Component.translatable("ability.lotmcraft.grazing.archive_summary").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.archive_soul_count", component.getSouls().size(), maxSouls).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_active_name",
                                activeSoul == null ? Component.translatable("ability.lotmcraft.grazing.none") : Component.literal(activeSoul.displayName()))
                                .withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_sanity_cap",
                                Math.round(component.getPermanentSanityCap() * 100.0f)).withStyle(ChatFormatting.GRAY),
                        Component.translatable(releaseMode
                                ? "ability.lotmcraft.grazing.archive_release_instruction"
                                : "ability.lotmcraft.grazing.archive_attune_instruction").withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                false
        ));
        container.setItem(2, createMenuItem(
                Items.ENDER_EYE,
                Component.translatable("ability.lotmcraft.grazing.open_wheel")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.archive_open_wheel_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "open_wheel"),
                activeSoul != null
        ));
        container.setItem(4, createMenuItem(
                component.hasManifestedSoul() ? Items.SOUL_LANTERN : Items.WITHER_ROSE,
                Component.translatable("ability.lotmcraft.grazing.manifest")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                List.of(Component.translatable(component.hasManifestedSoul()
                        ? "ability.lotmcraft.grazing.archive_retrieve_lore"
                        : "ability.lotmcraft.grazing.archive_manifest_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "manifest"),
                component.hasManifestedSoul()
        ));
        container.setItem(6, createMenuItem(
                releaseMode ? Items.REDSTONE_BLOCK : Items.BARRIER,
                Component.translatable("ability.lotmcraft.grazing.release_mode")
                        .withStyle(releaseMode ? ChatFormatting.RED : ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(Component.translatable(releaseMode
                        ? "ability.lotmcraft.grazing.archive_release_enabled"
                        : "ability.lotmcraft.grazing.archive_release_disabled").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "toggle_release"),
                releaseMode
        ));
        container.setItem(8, createMenuItem(
                activeSoul == null ? Items.STRUCTURE_VOID : Items.ECHO_SHARD,
                Component.translatable("ability.lotmcraft.grazing.archive_active_card")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                buildActiveSoulCardLore(component),
                null,
                activeSoul != null
        ));

        List<ShepherdGrazingComponent.GrazedSoulData> souls = component.getSouls();
        for (int i = 0; i < Math.min(45, souls.size()); i++) {
            ShepherdGrazingComponent.GrazedSoulData soul = souls.get(i);
            CompoundTag tag = tagWith("Action", "soul");
            tag.putString("SoulId", soul.soulId());
            container.setItem(i + 9, createMenuItem(
                    soul.soulId().equals(component.getManifestedSoulId()) ? Items.WITHER_ROSE
                            : soul.soulId().equals(component.getActiveSoulId()) ? Items.ECHO_SHARD : Items.PLAYER_HEAD,
                    Component.literal(soul.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    buildSoulArchiveLore(soul, component, releaseMode),
                    tag,
                    soul.soulId().equals(component.getActiveSoulId())
            ));
        }
    }

    private static void openAbilitySelectionMenu(ServerPlayer player, GrazeTargetData targetData, List<Ability> candidates) {
        SimpleContainer container = createLockedContainer(54);
        ArrayList<String> selectedAbilityIds = new ArrayList<>();
        int playerSequence = BeyonderData.getSequence(player);
        fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence);

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6) {
                    @Override
                    public void clicked(int slotId, int button, ClickType clickType, net.minecraft.world.entity.player.Player clickPlayer) {
                        if (slotId < 0 || slotId >= container.getContainerSize()) {
                            return;
                        }
                        ItemStack clicked = container.getItem(slotId);
                        if (clicked.isEmpty()) {
                            return;
                        }
                        CustomData data = clicked.get(DataComponents.CUSTOM_DATA);
                        if (data == null) {
                            return;
                        }
                        CompoundTag tag = data.copyTag();
                        String action = tag.getString("Action");

                        if ("confirm".equals(action)) {
                            int needed = Math.min(MAX_CHOOSABLE_ABILITIES, candidates.size());
                            if (selectedAbilityIds.size() != needed) {
                                return;
                            }
                            clickPlayer.closeContainer();
                            grazeSoul(player, targetData, selectedAbilityIds);
                            return;
                        }
                        if ("cancel".equals(action)) {
                            clickPlayer.closeContainer();
                            return;
                        }
                        if (!"ability".equals(action)) {
                            return;
                        }

                        String abilityId = tag.getString("AbilityId");
                        if (selectedAbilityIds.contains(abilityId)) {
                            selectedAbilityIds.remove(abilityId);
                        } else if (selectedAbilityIds.size() < Math.min(MAX_CHOOSABLE_ABILITIES, candidates.size())) {
                            selectedAbilityIds.add(abilityId);
                        }

                        fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence);
                        broadcastChanges();
                    }
                },
                Component.translatable("ability.lotmcraft.grazing.select_title", targetData.displayName())
        ));

        HangedEffectUtil.playFleshCast(player.serverLevel(), player.position());
    }

    private static void fillAbilitySelectionMenu(SimpleContainer container, GrazeTargetData targetData, List<Ability> candidates,
                                                 List<String> selectedAbilityIds, int playerSequence) {
        fillContainer(container, createFiller(Items.BLACK_STAINED_GLASS_PANE));

        int needed = Math.min(MAX_CHOOSABLE_ABILITIES, candidates.size());

        container.setItem(0, createMenuItem(
                targetData.manifestable() ? Items.NETHER_STAR : Items.SOUL_LANTERN,
                Component.literal(targetData.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                buildGrazePromptLore(targetData, candidates.size(), 0, 0, playerSequence),
                null,
                targetData.manifestable()
        ));
        container.setItem(4, createMenuItem(
                Items.WRITABLE_BOOK,
                Component.translatable("ability.lotmcraft.grazing.selection_status").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.selection_count", selectedAbilityIds.size(), needed).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.selection_locking").withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                false
        ));
        container.setItem(6, createMenuItem(
                Items.EMERALD_BLOCK,
                Component.translatable("ability.lotmcraft.grazing.confirm",
                        selectedAbilityIds.size(), needed).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.selection_confirm_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "confirm"),
                selectedAbilityIds.size() == needed
        ));
        container.setItem(8, createMenuItem(
                Items.BARRIER,
                Component.translatable("ability.lotmcraft.grazing.cancel")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.selection_cancel_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "cancel"),
                false
        ));

        for (int i = 0; i < Math.min(45, candidates.size()); i++) {
            Ability ability = candidates.get(i);
            boolean selected = selectedAbilityIds.contains(ability.getId());
            CompoundTag tag = tagWith("Action", "ability");
            tag.putString("AbilityId", ability.getId());
            container.setItem(i + 9, createMenuItem(
                    selected ? Items.ENCHANTED_BOOK : Items.BOOK,
                    ability.getNameFormatted().copy().withStyle(selected ? ChatFormatting.GOLD : ChatFormatting.GRAY),
                    buildAbilitySelectionLore(ability, selected),
                    tag,
                    selected
            ));
        }
    }

    private static void grazeSoul(ServerPlayer player, GrazeTargetData targetData, List<String> selectedAbilityIds) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        float maxSpirituality = BeyonderData.getMaxSpirituality(targetData.pathway(), targetData.sequence());
        ShepherdGrazingComponent.GrazedSoulData soul = new ShepherdGrazingComponent.GrazedSoulData(
                UUID.randomUUID().toString(),
                targetData.displayName(),
                targetData.pathway(),
                targetData.sequence(),
                selectedAbilityIds,
                maxSpirituality,
                maxSpirituality,
                targetData.manifestable(),
                targetData.soulTag()
        );

        component.addSoul(soul);
        component.setActiveSoulId(soul.soulId());
        if (targetData.sequence() < BeyonderData.getSequence(player)) {
            component.setPermanentSanityCap(Math.min(component.getPermanentSanityCap(), HIGHER_SEQUENCE_SANITY_CAP));
            player.getData(ModAttachments.SANITY_COMPONENT).setSanityAndSync(
                    Math.min(player.getData(ModAttachments.SANITY_COMPONENT).getSanity(), component.getPermanentSanityCap()), player);
        }

        syncActiveSoul(player);
        Vec3 center = player.position().add(player.getLookAngle().normalize().scale(1.2)).add(0, player.getBbHeight() * 0.35, 0);
        HangedEffectUtil.spawnFleshBurst(player.serverLevel(), center, 1.0, 34);
        HangedEffectUtil.playFleshCast(player.serverLevel(), center);
        HangedEffectUtil.spawnShadowBurst(player.serverLevel(), player.position().add(0, player.getBbHeight() * 0.55, 0), 0.85, 26);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 0.6f);
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.success", targetData.displayName())
                .withColor(HangedPathwayConstants.pathwayColor()));
    }

    private static CompoundTag createSoulTag(LivingEntity target) {
        CompoundTag soulTag = new CompoundTag();
        soulTag.putString("EntityType", EntityType.getKey(target.getType()).toString());
        soulTag.putString("DisplayName", target.getName().getString());
        soulTag.putString("BeyonderPathway", BeyonderData.getPathway(target));
        soulTag.putInt("BeyonderSequence", BeyonderData.getSequence(target));
        soulTag.putBoolean("IsPlayerSoul", target instanceof net.minecraft.world.entity.player.Player);

        CompoundTag entityNbt = new CompoundTag();
        target.save(entityNbt);
        entityNbt.remove("UUID");
        entityNbt.remove("Health");
        if (entityNbt.contains("neoforge:attachments")) {
            entityNbt.getCompound("neoforge:attachments").remove("lotmcraft:copied_inventory");
        }
        soulTag.put("EntityNBT", entityNbt);

        if (target instanceof BeyonderNPCEntity beyonderNPC) {
            soulTag.putBoolean("IsBeyonderNPC", true);
            soulTag.putString("NPCPathway", beyonderNPC.getPathway());
            soulTag.putInt("NPCSequence", beyonderNPC.getSequence());
            soulTag.putString("NPCSkin", beyonderNPC.getSkinName());
            soulTag.putBoolean("NPCHostile", beyonderNPC.isHostile());
        } else {
            soulTag.putBoolean("IsBeyonderNPC", false);
        }

        return soulTag;
    }

    private static void spawnManifestedSoul(ServerPlayer player, ShepherdGrazingComponent component, ShepherdGrazingComponent.GrazedSoulData soul) {
        CompoundTag soulData = soul.soulData();
        try {
            Optional<EntityType<?>> optionalType = EntityType.byString(soulData.getString("EntityType"));
            if (optionalType.isEmpty()) {
                return;
            }

            Entity entity;
            EntityType<?> entityType = optionalType.get();
            if (soulData.getBoolean("IsBeyonderNPC")) {
                entity = new BeyonderNPCEntity((EntityType<? extends BeyonderNPCEntity>) entityType, player.serverLevel(),
                        soulData.getBoolean("NPCHostile"),
                        soulData.getString("NPCSkin"),
                        soulData.getString("NPCPathway"),
                        soulData.getInt("NPCSequence"));
                entity.getPersistentData().putBoolean("Initialized", true);
            } else {
                entity = entityType.create(player.serverLevel());
            }

            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            if (soulData.contains("EntityNBT")) {
                CompoundTag nbt = soulData.getCompound("EntityNBT").copy();
                nbt.remove("UUID");
                entity.load(nbt);
            }

            Vec3 spawnPos = player.position().add(player.getLookAngle().normalize().scale(2.5));
            entity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, player.getYRot(), 0.0f);
            entity.setUUID(UUID.randomUUID());
            if (player.serverLevel().addFreshEntity(entity)) {
                SubordinateUtils.turnEntityIntoSubordinate(livingEntity, player, false);
                component.setManifestedSoulId(soul.soulId());
                component.setManifestedEntityUuid(entity.getUUID());
                syncActiveSoul(player);
                HangedEffectUtil.spawnShadowBurst(player.serverLevel(), spawnPos.add(0, entity.getBbHeight() * 0.5, 0), 1.2, 36);
                HangedEffectUtil.playShadowCast(player.serverLevel(), spawnPos);
            }
        } catch (Exception ignored) {
        }
    }

    private static void retrieveManifestedSoul(ServerPlayer player, ShepherdGrazingComponent component) {
        UUID manifestedUuid = component.getManifestedEntityUuid();
        Entity entity = manifestedUuid == null ? null : player.serverLevel().getEntity(manifestedUuid);
        if (entity != null) {
            HangedEffectUtil.spawnShadowBurst(player.serverLevel(), entity.position().add(0, entity.getBbHeight() * 0.5, 0), 1.0, 26);
            entity.discard();
        }
        component.setManifestedSoulId("");
        component.setManifestedEntityUuid(null);
        syncActiveSoul(player);
    }

    private static List<Ability> getCandidateAbilities(LivingEntity target) {
        return LOTMCraft.abilityHandler.getAbilities().stream()
                .filter(ability -> ability.canBeCopied)
                .filter(ability -> ability.hasAbility(target))
                .sorted(Comparator.comparingInt(Ability::lowestSequenceUsable)
                        .thenComparing(Ability::getId))
                .limit(45)
                .toList();
    }

    private static CompoundTag tagWith(String key, String value) {
        CompoundTag tag = new CompoundTag();
        tag.putString(key, value);
        return tag;
    }

    private static SimpleContainer createLockedContainer(int size) {
        return new SimpleContainer(size) {
            @Override
            public boolean canTakeItem(Container targetContainer, int slot, ItemStack stack) {
                return false;
            }
        };
    }

    private static void fillContainer(SimpleContainer container, ItemStack filler) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            container.setItem(i, filler.copy());
        }
    }

    private static ItemStack createFiller(Item item) {
        return createMenuItem(item, Component.literal(" "), List.of(), null, false);
    }

    private static ItemStack createMenuItem(Item item, Component name, List<Component> lore, CompoundTag tag, boolean glint) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, plain(name));
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore.stream().map(ShepherdGrazingUtil::plain).toList()));
        }
        if (tag != null) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
        if (glint) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return stack;
    }

    private static Component plain(Component component) {
        return component.copy().withStyle(style -> style.withItalic(false));
    }

    private static List<Component> buildGrazePromptLore(GrazeTargetData targetData, int candidateCount, int currentSouls, int maxSouls, int playerSequence) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("lotm.pathway").append(Component.literal(": " + targetData.pathway())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("lotm.sequence").append(Component.literal(": " + targetData.sequence())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("ability.lotmcraft.grazing.prompt_power_count", candidateCount).withStyle(ChatFormatting.GRAY));
        if (maxSouls > 0) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.archive_soul_count", currentSouls, maxSouls).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (targetData.manifestable()) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.prompt_manifestable").withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (targetData.sequence() < playerSequence) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.prompt_dangerous").withStyle(ChatFormatting.DARK_RED));
        }
        return lore;
    }

    private static List<Component> buildActiveSoulCardLore(ShepherdGrazingComponent component) {
        ShepherdGrazingComponent.GrazedSoulData activeSoul = component.getActiveSoul();
        if (activeSoul == null) {
            return List.of(Component.translatable("ability.lotmcraft.grazing.no_active_soul").withStyle(ChatFormatting.GRAY));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal(activeSoul.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE));
        lore.add(Component.translatable("lotm.pathway").append(Component.literal(": " + activeSoul.pathway())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("lotm.sequence").append(Component.literal(": " + activeSoul.sequence())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("ability.lotmcraft.grazing.soul_power_count", activeSoul.abilityIds().size()).withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.translatable(component.hasManifestedSoul() && activeSoul.soulId().equals(component.getManifestedSoulId())
                ? "ability.lotmcraft.grazing.currently_manifested"
                : "ability.lotmcraft.grazing.currently_active").withStyle(component.hasManifestedSoul()
                && activeSoul.soulId().equals(component.getManifestedSoulId()) ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_RED));
        return lore;
    }

    private static List<Component> buildSoulArchiveLore(ShepherdGrazingComponent.GrazedSoulData soul,
                                                        ShepherdGrazingComponent component,
                                                        boolean releaseMode) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("lotm.pathway").append(Component.literal(": " + soul.pathway())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("lotm.sequence").append(Component.literal(": " + soul.sequence())).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable("ability.lotmcraft.grazing.soul_power_count", soul.abilityIds().size()).withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.translatable(soul.manifestable()
                ? "ability.lotmcraft.grazing.soul_manifestable"
                : "ability.lotmcraft.grazing.soul_power_only").withStyle(ChatFormatting.GRAY));
        if (soul.soulId().equals(component.getActiveSoulId())) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.currently_active").withStyle(ChatFormatting.DARK_RED));
        }
        if (soul.soulId().equals(component.getManifestedSoulId())) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.currently_manifested").withStyle(ChatFormatting.DARK_PURPLE));
        }
        lore.add(Component.translatable(releaseMode
                ? "ability.lotmcraft.grazing.soul_release_lore"
                : "ability.lotmcraft.grazing.soul_attune_lore").withStyle(releaseMode ? ChatFormatting.RED : ChatFormatting.GRAY));
        return lore;
    }

    private static List<Component> buildAbilitySelectionLore(Ability ability, boolean selected) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("ability.lotmcraft.grazing.selection_required_sequence", ability.lowestSequenceUsable())
                .withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.translatable("ability.lotmcraft.grazing.selection_type", getAbilityTypeLabel(ability))
                .withStyle(ChatFormatting.DARK_GRAY));
        lore.add(Component.literal(ability.getId()).withStyle(ChatFormatting.GRAY));
        lore.add(Component.translatable(selected
                ? "ability.lotmcraft.grazing.selection_remove"
                : "ability.lotmcraft.grazing.select_ability_lore").withStyle(selected ? ChatFormatting.GOLD : ChatFormatting.GRAY));
        return lore;
    }

    private static Component getAbilityTypeLabel(Ability ability) {
        if (ability instanceof ToggleAbility) {
            return Component.translatable("lotm.toggleable");
        }
        if (ability instanceof SelectableAbility) {
            return Component.translatable("lotm.selectable");
        }
        return Component.translatable("lotm.ability");
    }

    private record GrazeTargetData(String displayName, String pathway, int sequence, boolean manifestable,
                                   CompoundTag soulTag) {
    }
}
