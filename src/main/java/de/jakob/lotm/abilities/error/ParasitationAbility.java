package de.jakob.lotm.abilities.error;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.abilities.error.handler.TheftHandler;
import de.jakob.lotm.attachments.ControllingDataComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.ParasitationComponent;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ControllingUtil;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.marionettes.MarionetteUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ParasitationAbility extends SelectableAbility {

    private static final HashMap<UUID, UUID> concealedMap = new HashMap<>();
    private static final HashMap<UUID, UUID> controllingMap = new HashMap<>();
    private static final HashMap<UUID, Integer> controllingTimer = new HashMap<>();
    private static final HashMap<UUID, Boolean> controllingLowerSeq = new HashMap<>();

    public ParasitationAbility(String id) {
        super(id, 5f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeReplicated = false;
        canBeUsedInArtifact = false;
        canBeShared = false;
        cannotBeStolen = true;
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

        ControllingUtil.possess(player, target, false);
    }

    public static void exitControl(ServerLevel serverLevel, ServerPlayer player) {
        if (!controllingMap.containsKey(player.getUUID())) return;
        boolean lowerSeq = controllingLowerSeq.getOrDefault(player.getUUID(), false);
        UUID hostUUID = controllingMap.get(player.getUUID());

        controllingMap.remove(player.getUUID());
        controllingTimer.remove(player.getUUID());
        controllingLowerSeq.remove(player.getUUID());

        ControllingUtil.reset(player, serverLevel, true);

        Entity hostEntity = serverLevel.getEntity(hostUUID);
        if (hostEntity instanceof LivingEntity host) {
            ParasitationComponent pc = host.getData(ModAttachments.PARASITE_COMPONENT);
            pc.setParasited(false);
            pc.setParasiteUUID(null);

            if (!lowerSeq) {
                boolean killedBySteal = performExitSteal(serverLevel, player, host);
                if (killedBySteal) MarionetteUtils.turnEntityIntoMarionette(host, player);
            }
        }
    }

    private static boolean performExitSteal(ServerLevel serverLevel, ServerPlayer player, LivingEntity host) {
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
            // Health drain — check if it kills
            float drain = host.getMaxHealth() * 0.2f;
            host.setHealth(host.getHealth() - drain);
            if (host.getHealth() <= 0) {
                host.kill();
                return true;
            }
        }
        return false;
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


        ControllingDataComponent data = serverPlayer.getData(ModAttachments.CONTROLLING_DATA);
        if (!data.isControlling()) {
            // Ended externally — clean up without calling reset again
            controllingMap.remove(serverPlayer.getUUID());
            controllingTimer.remove(serverPlayer.getUUID());
            controllingLowerSeq.remove(serverPlayer.getUUID());
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
