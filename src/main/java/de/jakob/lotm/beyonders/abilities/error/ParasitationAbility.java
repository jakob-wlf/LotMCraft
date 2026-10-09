package de.jakob.lotm.beyonders.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.error.handler.TheftHandler;
import de.jakob.lotm.beyonders.acting.ActingEventHandler;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.attachments.TimeWormReturnData;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.AbilityWheelHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ParasitationAbility extends SelectableAbility {

    private static final HashMap<UUID, UUID> concealedMap = new HashMap<>();
    private static final HashMap<UUID, UUID> controllingMap = new HashMap<>();
    private static final HashMap<UUID, Integer> controllingTimer = new HashMap<>();
    private static final HashMap<UUID, Boolean> controllingLowerSeq = new HashMap<>();
    private static final Set<UUID> releaseHostNormally = new HashSet<>();

    public ParasitationAbility(String id) {
        super(id, 5f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
        cannotBeStolen = true;
        canBeUsedWhileControlling = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("error", 4));
    }

    @Override
    public float getSpiritualityCost() {
        return 4000;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.parasitation.controlling",
                "ability.lotmcraft.parasitation.concealed"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        switch (abilityIndex) {
            case 0 -> controlling(level, entity);
            case 1 -> concealed(level, entity);
        }
    }

    private void controlling(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        // Already controlling — exit
        if (controllingMap.containsKey(player.getUUID())) {
            exitControl(serverLevel, player);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(player, 8, 2);
        if (target == null) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.parasitation.no_target").withColor(0x3240bf));
            return;
        };

        // If currently concealed, cancel concealment
        if (concealedMap.containsKey(player.getUUID())) {
            cancelConcealed(serverLevel, player);
        }

        attemptControl(serverLevel, player, target);
    }

    private void attemptControl(ServerLevel serverLevel, ServerPlayer player, LivingEntity target) {
        // 0.05 heart to trigger substitutes/swaps
        target.hurt(ModDamageTypes.source(serverLevel, ModDamageTypes.BEYONDER_GENERIC, player), 0.1f);

        if (!target.isAlive() || target.isRemoved()) {
            AbilityUtil.sendActionBar(player, Component.literal("§cTarget evaded!"));
            return;
        }

        int userSeq = BeyonderData.getSequence(player);
        int targetSeq = BeyonderData.isBeyonder(target) ? BeyonderData.getSequence(target) : 10;
        boolean lowerSeq = targetSeq > userSeq;

        // 55% vs lower seq, 15% against same, 0% chance against higher sequence
        float chance = lowerSeq ? 0.55f : 0.15f;
        if (random.nextFloat() >= chance || userSeq > targetSeq) {
            AbilityUtil.sendActionBar(player, Component.literal(lowerSeq
                    ? "§cControl failed!"
                    : "§cControl failed — resistance too strong!"));
            return;
        }

        startControl(serverLevel, player, target, lowerSeq);
    }

    private void startControl(ServerLevel serverLevel, ServerPlayer player, LivingEntity target, boolean lowerSeq) {
        controllingMap.put(player.getUUID(), target.getUUID());
        controllingLowerSeq.put(player.getUUID(), lowerSeq);

        if (!lowerSeq) {
            controllingTimer.put(player.getUUID(), 100);
        }

        ParasitationComponent pc = target.getData(ModAttachments.PARASITE_COMPONENT);
        pc.setParasited(true);
        pc.setParasiteUUID(player.getUUID());

        if (!ControllingUtils.startControlling(player, target, true, false)) {
            controllingMap.remove(player.getUUID());
            controllingLowerSeq.remove(player.getUUID());
            pc.setParasited(false);
            pc.setParasiteUUID(null);
            return;
        }

        // The wheel switches to the host's abilities while controlling it. Keep
        // the parasite's host commands available until control ends.
        AbilityWheelHelper.addAbility(player, "host_controlling_ability:-1");
        ActingEventHandler.recordParasitationAction(player);
    }

    public static void exitControl(ServerLevel serverLevel, ServerPlayer player) {
        exitControl(serverLevel, player, true);
    }

    private static void exitControl(ServerLevel serverLevel, ServerPlayer player, boolean returnHost) {
        if (!controllingMap.containsKey(player.getUUID())) return;
        boolean lowerSeq = controllingLowerSeq.getOrDefault(player.getUUID(), false);
        boolean releasedTimeWormHost = releaseHostNormally.remove(player.getUUID());
        UUID hostUUID = controllingMap.get(player.getUUID());
        LivingEntity controlledHost = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getControlledEntity();
        ArrayList<String> hostWheelAbilities = controlledHost == null ? null
                : new ArrayList<>(controlledHost.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getAbilities());
        int hostSelectedAbility = controlledHost == null ? 0
                : controlledHost.getData(ModAttachments.ABILITY_WHEEL_COMPONENT).getSelectedAbility();
        if (controlledHost != null) {
            ParasitationComponent hostParasitation = controlledHost.getData(ModAttachments.PARASITE_COMPONENT);
            if (hostParasitation.hasTimeWorm()) {
                for (String abilityId : hostParasitation.getTimeWormAddedAbilityIds()) {
                    if (!hostWheelAbilities.contains(abilityId)) {
                        hostWheelAbilities.add(abilityId);
                    }
                }
            }
        }

        controllingMap.remove(player.getUUID());
        controllingTimer.remove(player.getUUID());
        controllingLowerSeq.remove(player.getUUID());

        ControllingUtils.cancel(player, 0, returnHost, false);

        if (controlledHost != null && hostWheelAbilities != null) {
            var hostWheel = controlledHost.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            hostWheel.setAbilities(hostWheelAbilities);
            hostWheel.setSelectedAbility(hostWheelAbilities.isEmpty() ? 0
                    : Math.clamp(hostSelectedAbility, 0, hostWheelAbilities.size() - 1));
        }

        Entity hostEntity = controlledHost != null ? controlledHost : serverLevel.getEntity(hostUUID);
        if (hostEntity instanceof LivingEntity host) {
            if (returnHost && host.isRemoved() && host.isAlive()) {
                host.unsetRemoved();
            }
            ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
            pc.setParasited(false);
            pc.setParasiteUUID(null);

            if (pc.hasTimeWorm()) {
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.host_controlling.time_worm_host_released").withColor(0x3240bf));
                return;
            }

            if (!lowerSeq && !releasedTimeWormHost && host.isAlive()) {
                performExitSteal(serverLevel, player, host);
            }
        }
    }

    public static void toggleTimeWorm(ServerLevel serverLevel, LivingEntity parasite, LivingEntity host) {
        if (!(parasite instanceof ServerPlayer player) || !isControlling(player.getUUID())) {
            AbilityUtil.sendActionBar(parasite, Component.translatable("ability.lotmcraft.host_controlling.no_host").withColor(0x3240bf));
            return;
        }

        if (host instanceof Player) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.host_controlling.time_worm_player_host").withColor(0xbf3232));
            return;
        }
        ParasitationComponent component = host.getData(ModAttachments.PARASITE_COMPONENT);
        if (component.hasTimeWorm()) {
            if (!player.getUUID().equals(component.getTimeWormOwnerUUID())) {
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.host_controlling.time_worm_owned_by_other").withColor(0xbf3232));
                return;
            }
            int current = BeyonderData.getCowardWormAmount(player);
            int parasiteSequence = BeyonderData.getSequence(player, false, true);
            boolean reserveFull = current >= BeyonderData.getMaxWormAmount(parasiteSequence);
            var wheel = host.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
            ArrayList<String> retainedAbilities = new ArrayList<>(wheel.getAbilities());
            retainedAbilities.removeAll(component.getTimeWormAddedAbilityIds());
            wheel.setAbilities(retainedAbilities);
            component.clearTimeWormData();
            BeyonderData.returnWormAmount(player, 1);
            releaseHostNormally.add(player.getUUID());
            String messageKey = reserveFull
                    ? "ability.lotmcraft.host_controlling.time_worm_removed_reserve_full"
                    : "ability.lotmcraft.host_controlling.time_worm_removed";
            AbilityUtil.sendActionBar(player, Component.translatable(messageKey).withColor(0x3240bf));
            return;
        }

        if (BeyonderData.getCowardWormAmount(player) <= 0) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.host_controlling.no_time_worm").withColor(0xbf3232));
            return;
        }

        int sequence = BeyonderData.getSequence(player);
        var wheel = host.getData(ModAttachments.ABILITY_WHEEL_COMPONENT);
        LinkedHashSet<String> wheelAbilities = new LinkedHashSet<>(wheel.getAbilities());
        ArrayList<String> addedAbilities = new ArrayList<>();
        LOTMCraft.abilityHandler.getByPathwayAndSequence("error", sequence).stream()
                .sorted(Comparator.comparing(Ability::getId))
                .map(ability -> ability.getId() + ":-1")
                .forEach(id -> { if (wheelAbilities.add(id)) addedAbilities.add(id); });
        wheel.setAbilities(new ArrayList<>(wheelAbilities));
        component.setTimeWormOwner(player.getUUID(), sequence);
        component.setTimeWormAddedAbilityIds(addedAbilities);
        component.setHasTimeWorm(true);
        BeyonderData.incrementWormAmount(player, -1);
        AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.host_controlling.time_worm_added").withColor(0x3240bf));
    }

    public static void drainControlledHost(ServerLevel level, ServerPlayer parasite, LivingEntity host, float amount) {
        float hostHealth = host.getHealth();
        float drained = Math.min(Math.max(0, amount), hostHealth);
        if (drained <= 0) return;

        LivingEntity bodyDouble = parasite.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getBodyDouble();
        if (bodyDouble != null && bodyDouble.isAlive()) {
            bodyDouble.heal(drained);
        }

        if (drained >= hostHealth) {
            killControlledHost(level, parasite, host);
            return;
        }

        // The host is discarded while possessed, so hurt() is ignored. Keep
        // its saved health and the controlled player proxy in sync directly.
        host.setHealth(hostHealth - drained);
        parasite.setHealth(Math.max(1, parasite.getHealth() - drained));
    }

    public static void killControlledHost(ServerLevel level, ServerPlayer parasite, LivingEntity host) {
        if (host.isRemoved()) {
            host.unsetRemoved();
            host.setPos(parasite.position());
            if (!level.addFreshEntity(host)) return;
        }

        host.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
        if (host.isAlive()) {
            // Some NPC implementations reject damage even from a kill source.
            // Clear the parasitism state ourselves if that fallback is needed.
            host.kill();
            ParasitationComponent component = host.getData(ModAttachments.PARASITE_COMPONENT);
            if (component.hasTimeWorm()) returnTimeWormToParasite(level, component);
            component.setParasited(false);
            component.setParasiteUUID(null);
            exitControl(level, parasite, false);
        }
    }

    private static void performExitSteal(ServerLevel serverLevel, ServerPlayer player, LivingEntity host) {
        Random random = new Random();
        float roll = random.nextFloat();

        Ability instance = LOTMCraft.abilityHandler.getById("parasitation_ability");

        if (roll < 0.50f) {
            stealArmor(player, host);
        } else if (roll < 0.75f) {
            TheftHandler.stealItemsFromEntity(host, player, instance);
        } else if (roll < 0.90f) {
            TheftHandler.performAbilityTheft(serverLevel, player, host, random, true, instance);
        } else {
            // Health drain — the host dies normally if the stolen life is fatal.
            float drain = host.getMaxHealth() * 0.2f;
            host.setHealth(host.getHealth() - drain);
            if (host.getHealth() <= 0) {
                host.kill();
            }
        }
    }

    private static void stealArmor(ServerPlayer player, LivingEntity host) {
        Ability instance = LOTMCraft.abilityHandler.getById("parasitation_ability");

        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack armor = host.getItemBySlot(slot);
            if (!armor.isEmpty()) {
                if (!player.getInventory().add(armor.copy())) {
                    player.drop(armor.copy(), false);
                }
                host.setItemSlot(slot, ItemStack.EMPTY);
                return;
            }
        }
        TheftHandler.stealItemsFromEntity(host, player, instance);
    }

    @SubscribeEvent
    public static void onHostDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        LivingEntity host = event.getEntity();
        ParasitationComponent component = host.getData(ModAttachments.PARASITE_COMPONENT);
        UUID parasiteUUID = component.getParasiteUUID();
        if (component.hasTimeWorm()) returnTimeWormToParasite(level, component);
        component.setParasited(false);
        component.setParasiteUUID(null);

        if (parasiteUUID != null && host.getUUID().equals(controllingMap.get(parasiteUUID))) {
            level.getServer().execute(() -> {
                ServerPlayer parasite = level.getServer().getPlayerList().getPlayer(parasiteUUID);
                if (parasite != null && isControlling(parasiteUUID)) {
                    exitControl(level, parasite, false);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onHostRemoved(EntityLeaveLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof LivingEntity host)) return;
        Entity.RemovalReason reason = host.getRemovalReason();
        if (reason != Entity.RemovalReason.DISCARDED && reason != Entity.RemovalReason.KILLED) return;

        ParasitationComponent component = host.getData(ModAttachments.PARASITE_COMPONENT);
        if (!component.hasTimeWorm()) return;
        UUID activeParasite = component.getParasiteUUID();
        if (activeParasite != null && host.getUUID().equals(controllingMap.get(activeParasite))) return;
        returnTimeWormToParasite(level, component);
        component.setParasited(false);
        component.setParasiteUUID(null);
    }

    @SubscribeEvent
    public static void onParasiteLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int pendingReturns = TimeWormReturnData.get(player.server).claim(player.getUUID());
        if (pendingReturns > 0) {
            BeyonderData.returnWormAmount(player, pendingReturns);
        }
    }

    private static void returnTimeWormToParasite(ServerLevel level, ParasitationComponent component) {
        UUID ownerUUID = component.getTimeWormOwnerUUID();
        component.clearTimeWormData();
        if (ownerUUID == null) return;

        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null) {
            BeyonderData.returnWormAmount(owner, 1);
        } else {
            TimeWormReturnData.get(level.getServer()).add(ownerUUID, 1);
        }
    }


    // conceal mode
    private void concealed(Level level, LivingEntity entity) {
        if (level.isClientSide) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        // first reset concealment in all cases
        if (concealedMap.containsKey(player.getUUID())) {
            cancelConcealed(serverLevel, player);
            return;
        }

        LivingEntity target = AbilityUtil.getTargetEntity(player, 8, 2);

        if (target == null) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.parasitation.no_target").withColor(0x3240bf));
            return;
        }

        if (!isValidConcealedTarget(player, target)) {
            AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            return;
        }

        // If currently controlling, switch to concealed
        if (controllingMap.containsKey(entity.getUUID())) {
            exitControl(serverLevel, player);
            LivingEntity currentHost = resolveHost(serverLevel, controllingMap.get(player.getUUID()));
            LivingEntity newHost = ((currentHost == null || !target.getUUID().equals(currentHost.getUUID()))) ? target : currentHost;

            if (isValidConcealedTarget(player, newHost)) {
                startConcealed(serverLevel, player, newHost);
                return;
            } else {
                AbilityUtil.sendActionBar(player, Component.translatable("ability.lotmcraft.parasitation.target_too_strong").withColor(0xbf3232));
            }
        }

        startConcealed(serverLevel, player, target);
    }

    private boolean isValidConcealedTarget(LivingEntity entity, LivingEntity target) {
        if (!BeyonderData.isBeyonder(target)) return true;
        return BeyonderData.getSequence(target) > BeyonderData.getSequence(entity);
    }

    private void startConcealed(ServerLevel serverLevel, ServerPlayer serverPlayer, LivingEntity host) {
        concealedMap.put(serverPlayer.getUUID(), host.getUUID());

        ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
        pc.setParasited(true);
        pc.setParasiteUUID(serverPlayer.getUUID());

        serverPlayer.setGameMode(GameType.SPECTATOR);
        serverPlayer.setCamera(host);
        ActingEventHandler.recordParasitationAction(serverPlayer);
    }

    private void cancelConcealed(ServerLevel serverLevel, ServerPlayer serverPlayer) {
        if (concealedMap.containsKey(serverPlayer.getUUID())) {
            Entity hostEntity = serverLevel.getEntity(concealedMap.get(serverPlayer.getUUID()));
            if (hostEntity instanceof LivingEntity host) {
                ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
                pc.setParasited(false);
                pc.setParasiteUUID(null);
            }
        }
        concealedMap.remove(serverPlayer.getUUID());

        serverPlayer.setGameMode(GameType.SURVIVAL);
        serverPlayer.setCamera(null);
    }

    // to set the player as spectator when in concealment mode
    @SubscribeEvent
    public static void onPlayerTargetTick(PlayerTickEvent.Post event) {
        Player target = event.getEntity();

        if (!(target instanceof ServerPlayer serverTarget)) return;

        if (!isConcealed(serverTarget.getUUID())) return;

        UUID currentHostUUID = concealedMap.get(serverTarget.getUUID());

        Entity host = serverTarget.serverLevel().getEntity(currentHostUUID);

        if (isConcealed(serverTarget.getUUID())) {
            if (host != null) {
                serverTarget.setGameMode(GameType.SPECTATOR);
                serverTarget.setCamera(host);
            } else {
                serverTarget.setGameMode(GameType.SURVIVAL);
                serverTarget.setCamera(serverTarget);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (serverPlayer.level().isClientSide) return;
        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return;

        if (!ControllingUtils.isControlling(serverPlayer)) {
            // Finish the parasitism lifecycle if another system ended host control.
            if (controllingMap.containsKey(serverPlayer.getUUID())) {
                exitControl(serverLevel, serverPlayer);
            } else {
                controllingTimer.remove(serverPlayer.getUUID());
                controllingLowerSeq.remove(serverPlayer.getUUID());
            }
            return;
        }

        // Tick down timer for same/higher seq
        boolean lowerSeq = controllingLowerSeq.getOrDefault(serverPlayer.getUUID(), false);
        if (!lowerSeq) {
            int ticks = controllingTimer.getOrDefault(serverPlayer.getUUID(), 0) - 1;
            if (ticks <= 0) {
                serverLevel.getServer().execute(() -> exitControl(serverLevel, serverPlayer));
                return;
            }
            controllingTimer.put(serverPlayer.getUUID(), ticks);
        }
    }

    private static LivingEntity resolveHost(ServerLevel serverLevel, UUID uuid) {
        if (uuid == null) return null;
        Entity entity = serverLevel.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    public static LivingEntity getHostForEntity(ServerLevel serverLevel, LivingEntity parasite) {
        UUID hostUUID = controllingMap.get(parasite.getUUID());
        if (hostUUID != null && parasite instanceof ServerPlayer player) {
            LivingEntity controlledHost = player.getData(ModAttachments.ENTITY_CONTROLLING_COMPONENT).getControlledEntity();
            if (controlledHost != null) return controlledHost;
        }
        if (hostUUID == null) hostUUID = concealedMap.get(parasite.getUUID());
        if (hostUUID == null) return null;
        Entity host = serverLevel.getEntity(hostUUID);
        return host instanceof LivingEntity living ? living : null;
    }

    public static boolean isConcealed(UUID uuid) {
        return concealedMap.containsKey(uuid);
    }

    public static boolean isControlling(UUID uuid) {
        return controllingMap.containsKey(uuid);
    }
}
