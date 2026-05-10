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
    private static final int MAX_CHOOSABLE_ABILITIES_SHEPHERD = 3;
    private static final int MAX_CHOOSABLE_ABILITIES_BLACK_KNIGHT = 5;
    private static final int MAX_SOULS_DARK_ANGEL = 22;
    private static final int MAX_SOULS_TRINITY_TEMPLAR = 13;
    private static final int MAX_SOULS_PROFANE_PRESBYTER = 18;
    private static final int MAX_MANIFESTED_SOULS_DEFAULT = 1;
    private static final int MAX_MANIFESTED_SOULS_DARK_ANGEL = 22;
    private static final int MAX_MANIFESTED_SOULS_TRINITY_TEMPLAR = 3;
    private static final int MAX_ACTIVE_SOULS_DEFAULT = 1;
    private static final int MAX_ACTIVE_SOULS_TRINITY_TEMPLAR = 3;
    private static final int MAX_ACTIVE_SOULS_PROFANE_PRESBYTER = 6;
    private static final int GRAZING_SELECTIONS_PER_PAGE = 45;
    private static final ItemStack ARCHIVE_FILLER = createFiller(Items.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack ARCHIVE_ACCENT_FILLER = createFiller(Items.PURPLE_STAINED_GLASS_PANE);
    private static final ItemStack SELECTION_FILLER = createFiller(Items.BLACK_STAINED_GLASS_PANE);
    private static final ItemStack SELECTION_ACCENT_FILLER = createFiller(Items.MAGENTA_STAINED_GLASS_PANE);

    private ShepherdGrazingUtil() {
    }

    public static boolean isEligibleShepherd(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity)
                && HangedPathwayConstants.PATHWAY_ID.equals(BeyonderData.getPathway(entity))
                && BeyonderData.getSequence(entity) <= HangedPathwayConstants.SEQUENCE_SHEPHERD;
    }

    public static int getMaxSouls(ServerPlayer player) {
        float digestion = Math.max(0.0f, Math.min(1.0f, BeyonderData.getDigestionProgress(player)));
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_DARK_ANGEL) {
            return Math.max(1, Math.min(MAX_SOULS_DARK_ANGEL, 1 + (int) Math.floor(digestion * 21.0f)));
        }
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER) {
            return Math.max(1, Math.min(MAX_SOULS_PROFANE_PRESBYTER, 1 + (int) Math.floor(digestion * 17.0f)));
        }
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_TRINITY_TEMPLAR) {
            return Math.max(1, Math.min(MAX_SOULS_TRINITY_TEMPLAR, 1 + (int) Math.floor(digestion * 12.0f)));
        }
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT) {
            return Math.max(1, Math.min(9, 1 + (int) Math.floor(digestion * 8.0f)));
        }
        return Math.max(1, Math.min(7, 1 + (int) Math.floor(digestion * 6.0f)));
    }

    public static int getMaxManifestedSouls(ServerPlayer player) {
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_DARK_ANGEL) {
            return MAX_MANIFESTED_SOULS_DARK_ANGEL;
        }
        return BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_TRINITY_TEMPLAR
                ? MAX_MANIFESTED_SOULS_TRINITY_TEMPLAR
                : MAX_MANIFESTED_SOULS_DEFAULT;
    }

    public static int getMaxActiveSouls(ServerPlayer player) {
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_PROFANE_PRESBYTER) {
            return MAX_ACTIVE_SOULS_PROFANE_PRESBYTER;
        }
        if (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_TRINITY_TEMPLAR) {
            return MAX_ACTIVE_SOULS_TRINITY_TEMPLAR;
        }
        return MAX_ACTIVE_SOULS_DEFAULT;
    }

    public static int getMaxChoosableAbilities(ServerPlayer player) {
        return BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT
                ? MAX_CHOOSABLE_ABILITIES_BLACK_KNIGHT
                : MAX_CHOOSABLE_ABILITIES_SHEPHERD;
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
                !(target instanceof net.minecraft.world.entity.player.Player)
                        && (BeyonderData.getSequence(player) <= HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT
                        || BeyonderData.getSequence(target) <= HangedPathwayConstants.SEQUENCE_SHEPHERD - 1),
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
                            if (component.isSoulManifested(soulId)) {
                                retrieveManifestedSoul(player, component, soulId);
                            }
                            component.removeSoul(soulId);
                            syncActiveSoul(player);
                            fillSoulArchive(container, player, component, true);
                            broadcastChanges();
                            return;
                        }

                        if (clickType == ClickType.PICKUP && button == 1) {
                            boolean wasActive = component.isSoulActive(soulId);
                            if (!wasActive && component.getActiveSoulCount() >= getMaxActiveSouls(player)) {
                                player.displayClientMessage(Component.translatable("ability.lotmcraft.grazing.active_limit",
                                                component.getActiveSoulCount(), getMaxActiveSouls(player))
                                        .withStyle(ChatFormatting.RED), true);
                                return;
                            }
                            if (wasActive && component.getActiveSoulCount() <= 1) {
                                player.displayClientMessage(Component.translatable("ability.lotmcraft.grazing.active_minimum")
                                        .withStyle(ChatFormatting.RED), true);
                                return;
                            }
                            boolean activated = component.toggleActiveSoul(soulId, getMaxActiveSouls(player));
                            fillSoulArchive(container, player, component, false);
                            broadcastChanges();
                            player.displayClientMessage(Component.translatable(activated
                                            ? "ability.lotmcraft.grazing.attuned_soul"
                                            : "ability.lotmcraft.grazing.released_soul",
                                    Component.literal(component.getSoul(soulId) == null ? soulId : component.getSoul(soulId).displayName()))
                                    .withStyle(ChatFormatting.DARK_PURPLE), true);
                            syncActiveSoul(player);
                            return;
                        }

                        component.ensureActiveSoul(soulId, getMaxActiveSouls(player));
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
        ArrayList<ShepherdGrazingComponent.GrazedSoulData> activeSouls = component.getActiveSouls();
        if (activeSouls.isEmpty()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.no_active_soul")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }
        boolean hasUnmanifestedActiveSoul = activeSouls.stream().anyMatch(soul -> !component.isSoulManifested(soul.soulId()));
        if (!hasUnmanifestedActiveSoul) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.soul_manifested")
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

        if (component.isSoulManifested(activeSoul.soulId())) {
            retrieveManifestedSoul(player, component, activeSoul.soulId());
            return;
        }

        if (!activeSoul.manifestable()) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.cannot_manifest")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return;
        }

        if (component.getManifestedSoulCount() >= getMaxManifestedSouls(player)) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.manifest_limit",
                            component.getManifestedSoulCount(), getMaxManifestedSouls(player))
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

        boolean changed = component.trimActiveSouls(getMaxActiveSouls(player));

        if (!isEligibleShepherd(player) && !component.getSouls().isEmpty()) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(de.jakob.lotm.effect.ModEffects.LOOSING_CONTROL, 40, 2, false, false, false));
            player.getData(ModAttachments.SANITY_COMPONENT).decreaseSanityAndSync(0.01f, player);
        }
        for (ShepherdGrazingComponent.ManifestedSoulData manifestedSoul : component.getManifestedSouls()) {
            UUID manifestedEntityUuid = manifestedSoul.entityUuid();
            Entity manifestedEntity = manifestedEntityUuid == null ? null : player.serverLevel().getEntity(manifestedEntityUuid);
            if (!(manifestedEntity instanceof LivingEntity living) || !living.isAlive()) {
                component.removeSoul(manifestedSoul.soulId());
                changed = true;
            }
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
        if (component.isSoulManifested(soulId)) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.grazing.soul_manifested")
                    .withColor(HangedPathwayConstants.pathwayColor()));
            return false;
        }

        ShepherdGrazingComponent.GrazedSoulData soul = component.getSoul(soulId);
        if (soul == null || !component.isSoulActive(soul.soulId())) {
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
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        for (ShepherdGrazingComponent.GrazedSoulData activeSoul : component.getActiveSouls()) {
            if (!component.isSoulManifested(activeSoul.soulId())) {
                merged.addAll(activeSoul.abilityIds());
            }
        }
        return new ArrayList<>(merged);
    }

    public static boolean isActiveGrazedAbility(ServerPlayer player, String abilityId) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        return component.getActiveSouls().stream()
                .anyMatch(activeSoul -> !component.isSoulManifested(activeSoul.soulId()) && activeSoul.abilityIds().contains(abilityId));
    }

    public static String getActiveSoulIdForAbility(ServerPlayer player, String abilityId) {
        ShepherdGrazingComponent component = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        for (ShepherdGrazingComponent.GrazedSoulData activeSoul : component.getActiveSouls()) {
            if (!component.isSoulManifested(activeSoul.soulId()) && activeSoul.abilityIds().contains(abilityId)) {
                return activeSoul.soulId();
            }
        }
        return null;
    }

    public static void syncActiveSoul(ServerPlayer player) {
        ShepherdGrazingComponent grazingComponent = player.getData(ModAttachments.SHEPHERD_GRAZING_COMPONENT);
        CopiedAbilityComponent copiedComponent = player.getData(ModAttachments.COPIED_ABILITY_COMPONENT);

        copiedComponent.getAbilities().removeIf(data -> GRAZED_COPY_TYPE.equals(data.copyType()));

        LinkedHashSet<String> addedAbilityIds = new LinkedHashSet<>();
        for (ShepherdGrazingComponent.GrazedSoulData activeSoul : grazingComponent.getActiveSouls()) {
            if (!grazingComponent.isSoulManifested(activeSoul.soulId())) {
                for (String abilityId : activeSoul.abilityIds()) {
                    if (!addedAbilityIds.add(abilityId)) {
                        continue;
                    }
                    copiedComponent.addAbility(new CopiedAbilityComponent.CopiedAbilityData(
                            abilityId,
                            GRAZED_COPY_TYPE,
                            -1,
                            activeSoul.soulId()
                    ));
                }
            }
        }

        CopiedAbilityHelper.syncToClient(player);
        AbilityWheelHelper.syncToClient(player);
    }

    private static void fillSoulArchive(SimpleContainer container, ServerPlayer player, ShepherdGrazingComponent component, boolean releaseMode) {
        fillContainer(container, ARCHIVE_FILLER);
        setAccentSlots(container, ARCHIVE_ACCENT_FILLER, 1, 3, 5, 7);

        ShepherdGrazingComponent.GrazedSoulData activeSoul = component.getActiveSoul();
        int maxSouls = getMaxSouls(player);
        container.setItem(0, createMenuItem(
                Items.WRITTEN_BOOK,
                Component.translatable("ability.lotmcraft.grazing.archive_summary").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.archive_soul_count", component.getSouls().size(), maxSouls).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_active_count",
                                component.getActiveSoulCount(), getMaxActiveSouls(player)).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_manifested_count",
                                component.getManifestedSoulCount(), getMaxManifestedSouls(player)).withStyle(ChatFormatting.GRAY),
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
        container.setItem(1, createMenuItem(
                Items.AMETHYST_CLUSTER,
                Component.translatable("ability.lotmcraft.grazing.archive_focus").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.archive_primary_click").withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_secondary_click").withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_release_hint").withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                true
        ));
        container.setItem(2, createMenuItem(
                Items.ENDER_EYE,
                Component.translatable("ability.lotmcraft.grazing.open_wheel")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.archive_open_wheel_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "open_wheel"),
                activeSoul != null
        ));
        container.setItem(3, createMenuItem(
                Items.HEART_OF_THE_SEA,
                Component.translatable("ability.lotmcraft.grazing.archive_trinity_state").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.archive_active_count",
                                component.getActiveSoulCount(), getMaxActiveSouls(player)).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_manifested_count",
                                component.getManifestedSoulCount(), getMaxManifestedSouls(player)).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.archive_primary_focus",
                                activeSoul == null ? Component.translatable("ability.lotmcraft.grazing.none") : Component.literal(activeSoul.displayName()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                activeSoul != null
        ));
        boolean activeSoulManifested = activeSoul != null && component.isSoulManifested(activeSoul.soulId());
        boolean canToggleManifest = activeSoul != null
                && (activeSoulManifested || component.getManifestedSoulCount() < getMaxManifestedSouls(player));
        container.setItem(4, createMenuItem(
                activeSoulManifested ? Items.SOUL_LANTERN : Items.WITHER_ROSE,
                Component.translatable("ability.lotmcraft.grazing.manifest")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                List.of(Component.translatable(activeSoulManifested
                        ? "ability.lotmcraft.grazing.archive_retrieve_lore"
                        : "ability.lotmcraft.grazing.archive_manifest_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "manifest"),
                canToggleManifest
        ));
        container.setItem(5, createMenuItem(
                releaseMode ? Items.REDSTONE_TORCH : Items.RECOVERY_COMPASS,
                Component.translatable("ability.lotmcraft.grazing.archive_mode")
                        .withStyle(releaseMode ? ChatFormatting.RED : ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(Component.translatable(releaseMode
                        ? "ability.lotmcraft.grazing.archive_release_enabled"
                        : "ability.lotmcraft.grazing.archive_release_disabled").withStyle(ChatFormatting.GRAY)),
                null,
                releaseMode
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
                    component.isSoulManifested(soul.soulId()) ? Items.WITHER_ROSE
                            : soul.soulId().equals(component.getActiveSoulId()) ? Items.ECHO_SHARD
                            : component.isSoulActive(soul.soulId()) ? Items.AMETHYST_SHARD : Items.PLAYER_HEAD,
                    Component.literal(soul.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    buildSoulArchiveLore(soul, component, releaseMode),
                    tag,
                    component.isSoulActive(soul.soulId())
            ));
        }
    }

    private static void openAbilitySelectionMenu(ServerPlayer player, GrazeTargetData targetData, List<Ability> candidates) {
        SimpleContainer container = createLockedContainer(54);
        ArrayList<String> selectedAbilityIds = new ArrayList<>();
        int playerSequence = BeyonderData.getSequence(player);
        int[] currentPage = {0};
        fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence, currentPage[0]);

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
                            int needed = Math.min(getMaxChoosableAbilities(player), candidates.size());
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
                        if ("previous_page".equals(action)) {
                            if (currentPage[0] > 0) {
                                currentPage[0]--;
                                fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence, currentPage[0]);
                                broadcastChanges();
                            }
                            return;
                        }
                        if ("next_page".equals(action)) {
                            int totalPages = Math.max(1, (int) Math.ceil(candidates.size() / (double) GRAZING_SELECTIONS_PER_PAGE));
                            if (currentPage[0] + 1 < totalPages) {
                                currentPage[0]++;
                                fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence, currentPage[0]);
                                broadcastChanges();
                            }
                            return;
                        }
                        if (!"ability".equals(action)) {
                            return;
                        }

                        String abilityId = tag.getString("AbilityId");
                        if (selectedAbilityIds.contains(abilityId)) {
                            selectedAbilityIds.remove(abilityId);
                        } else if (selectedAbilityIds.size() < Math.min(getMaxChoosableAbilities(player), candidates.size())) {
                            selectedAbilityIds.add(abilityId);
                        }

                        fillAbilitySelectionMenu(container, targetData, candidates, selectedAbilityIds, playerSequence, currentPage[0]);
                        broadcastChanges();
                    }
                },
                Component.translatable("ability.lotmcraft.grazing.select_title", targetData.displayName())
        ));

        HangedEffectUtil.playFleshCast(player.serverLevel(), player.position());
    }

    private static void fillAbilitySelectionMenu(SimpleContainer container, GrazeTargetData targetData, List<Ability> candidates,
                                                 List<String> selectedAbilityIds, int playerSequence, int page) {
        fillContainer(container, SELECTION_FILLER);
        setAccentSlots(container, SELECTION_ACCENT_FILLER, 2, 3, 5);

        int needed = Math.min(playerSequence <= HangedPathwayConstants.SEQUENCE_BLACK_KNIGHT
                ? MAX_CHOOSABLE_ABILITIES_BLACK_KNIGHT
                : MAX_CHOOSABLE_ABILITIES_SHEPHERD, candidates.size());
        int totalPages = Math.max(1, (int) Math.ceil(candidates.size() / (double) GRAZING_SELECTIONS_PER_PAGE));
        int clampedPage = Math.max(0, Math.min(page, totalPages - 1));
        int startIndex = clampedPage * GRAZING_SELECTIONS_PER_PAGE;
        int endIndex = Math.min(candidates.size(), startIndex + GRAZING_SELECTIONS_PER_PAGE);

        container.setItem(0, createMenuItem(
                targetData.manifestable() ? Items.NETHER_STAR : Items.SOUL_LANTERN,
                Component.literal(targetData.displayName()).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                buildGrazePromptLore(targetData, candidates.size(), 0, 0, playerSequence),
                null,
                targetData.manifestable()
        ));
        container.setItem(1, createMenuItem(
                Items.ARROW,
                Component.translatable("ability.lotmcraft.grazing.previous_page").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.page", clampedPage + 1, totalPages).withStyle(ChatFormatting.DARK_GRAY)),
                tagWith("Action", "previous_page"),
                clampedPage > 0
        ));
        container.setItem(2, createMenuItem(
                Items.ENCHANTING_TABLE,
                Component.translatable("ability.lotmcraft.grazing.selection_header").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.selection_locking").withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.selection_header_hint").withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                true
        ));
        container.setItem(3, createMenuItem(
                targetData.sequence() < playerSequence ? Items.WITHER_ROSE : Items.SOUL_TORCH,
                Component.translatable("ability.lotmcraft.grazing.selection_target_state").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("lotm.pathway").append(Component.literal(": " + targetData.pathway())).withStyle(ChatFormatting.GRAY),
                        Component.translatable("lotm.sequence").append(Component.literal(": " + targetData.sequence())).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.prompt_power_count", candidates.size()).withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                targetData.sequence() < playerSequence
        ));
        container.setItem(4, createMenuItem(
                Items.WRITABLE_BOOK,
                Component.translatable("ability.lotmcraft.grazing.selection_status").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.selection_count", selectedAbilityIds.size(), needed).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.selection_locking").withStyle(ChatFormatting.DARK_GRAY),
                        Component.translatable("ability.lotmcraft.grazing.page", clampedPage + 1, totalPages).withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                false
        ));
        container.setItem(5, createMenuItem(
                Items.CHAIN,
                Component.translatable("ability.lotmcraft.grazing.selection_pick_rule").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(
                        Component.translatable("ability.lotmcraft.grazing.selection_count", selectedAbilityIds.size(), needed).withStyle(ChatFormatting.GRAY),
                        Component.translatable("ability.lotmcraft.grazing.selection_pick_rule_lore").withStyle(ChatFormatting.DARK_GRAY)
                ),
                null,
                selectedAbilityIds.size() == needed
        ));
        container.setItem(6, createMenuItem(
                Items.EMERALD_BLOCK,
                Component.translatable("ability.lotmcraft.grazing.confirm",
                        selectedAbilityIds.size(), needed).withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.selection_confirm_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "confirm"),
                selectedAbilityIds.size() == needed
        ));
        container.setItem(7, createMenuItem(
                Items.SPECTRAL_ARROW,
                Component.translatable("ability.lotmcraft.grazing.next_page").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.page", clampedPage + 1, totalPages).withStyle(ChatFormatting.DARK_GRAY)),
                tagWith("Action", "next_page"),
                clampedPage + 1 < totalPages
        ));
        container.setItem(8, createMenuItem(
                Items.BARRIER,
                Component.translatable("ability.lotmcraft.grazing.cancel")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                List.of(Component.translatable("ability.lotmcraft.grazing.selection_cancel_lore").withStyle(ChatFormatting.GRAY)),
                tagWith("Action", "cancel"),
                false
        ));

        for (int i = startIndex; i < endIndex; i++) {
            Ability ability = candidates.get(i);
            boolean selected = selectedAbilityIds.contains(ability.getId());
            CompoundTag tag = tagWith("Action", "ability");
            tag.putString("AbilityId", ability.getId());
            container.setItem((i - startIndex) + 9, createMenuItem(
                    getAbilitySelectionItem(ability, selected),
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
        component.ensureActiveSoul(soul.soulId(), getMaxActiveSouls(player));
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
                component.manifestSoul(soul.soulId(), entity.getUUID());
                syncActiveSoul(player);
                HangedEffectUtil.spawnShadowBurst(player.serverLevel(), spawnPos.add(0, entity.getBbHeight() * 0.5, 0), 1.2, 36);
                HangedEffectUtil.playShadowCast(player.serverLevel(), spawnPos);
            }
        } catch (Exception ignored) {
        }
    }

    private static void retrieveManifestedSoul(ServerPlayer player, ShepherdGrazingComponent component, String soulId) {
        UUID manifestedUuid = component.getManifestedEntityUuid(soulId);
        Entity entity = manifestedUuid == null ? null : player.serverLevel().getEntity(manifestedUuid);
        if (entity != null) {
            HangedEffectUtil.spawnShadowBurst(player.serverLevel(), entity.position().add(0, entity.getBbHeight() * 0.5, 0), 1.0, 26);
            entity.discard();
        }
        component.removeManifestedSoul(soulId);
        syncActiveSoul(player);
    }

    private static List<Ability> getCandidateAbilities(LivingEntity target) {
        return LOTMCraft.abilityHandler.getAbilities().stream()
                .filter(ability -> ability.canBeCopied)
                .filter(ability -> ability.hasAbility(target))
                .sorted(Comparator.comparingInt(Ability::lowestSequenceUsable)
                        .thenComparing(Ability::getId))
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

    private static void setAccentSlots(SimpleContainer container, ItemStack filler, int... slots) {
        for (int slot : slots) {
            if (slot >= 0 && slot < container.getContainerSize()) {
                container.setItem(slot, filler.copy());
            }
        }
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
        boolean activeSoulManifested = component.isSoulManifested(activeSoul.soulId());
        if (component.getActiveSoulCount() > 1) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.archive_active_count",
                    component.getActiveSoulCount(), component.getActiveSoulCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
        lore.add(Component.translatable(activeSoulManifested
                ? "ability.lotmcraft.grazing.currently_manifested"
                : "ability.lotmcraft.grazing.currently_active").withStyle(activeSoulManifested ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_RED));
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
        else if (component.isSoulActive(soul.soulId())) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.currently_attuned").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (component.isSoulManifested(soul.soulId())) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.currently_manifested").withStyle(ChatFormatting.DARK_PURPLE));
        }
        lore.add(Component.translatable(releaseMode
                ? "ability.lotmcraft.grazing.soul_release_lore"
                : "ability.lotmcraft.grazing.soul_attune_lore").withStyle(releaseMode ? ChatFormatting.RED : ChatFormatting.GRAY));
        if (!releaseMode) {
            lore.add(Component.translatable("ability.lotmcraft.grazing.soul_secondary_attune_lore").withStyle(ChatFormatting.DARK_GRAY));
        }
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

    private static Item getAbilitySelectionItem(Ability ability, boolean selected) {
        if (selected) {
            return Items.ENCHANTED_BOOK;
        }
        if (ability instanceof ToggleAbility) {
            return Items.LEVER;
        }
        if (ability instanceof SelectableAbility) {
            return Items.COMPASS;
        }
        return Items.BOOK;
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
