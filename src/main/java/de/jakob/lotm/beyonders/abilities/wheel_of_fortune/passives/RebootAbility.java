package de.jakob.lotm.beyonders.abilities.wheel_of_fortune.passives;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.BeyonderComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityHandler;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbilityItem;
import de.jakob.lotm.beyonders.abilities.wheel_of_fortune.ProphecyAbility;
import de.jakob.lotm.events.BeyonderDataTickHandler;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.DivinationUtil;
import de.jakob.lotm.util.playerMap.Characteristic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Map;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class RebootAbility extends PassiveAbilityItem {
    private static final String activeKey = "lotm_reboot_active";
    private static final String onlineTicksKey = "lotm_reboot_online_ticks";
    private static final String restoredHoursKey = "lotm_reboot_restored_hours";
    private static final String originalCharacteristicsKey = "lotm_reboot_original_characteristics";
    private static final int ticksPerHour = 20 * 60 * 60;
    private static final int restorationHours = 8;
    private static final int maximumAntiDivinationPower = 9;

    public RebootAbility(Item.Properties properties) {
        super(properties);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return Map.of("wheel_of_fortune", 1);
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) return;
        if (player.getPersistentData().getBoolean(activeKey)) return;
        if (!((RebootAbility) PassiveAbilityHandler.REBOOT.get()).shouldApplyTo(player)) return;
        if (!"wheel_of_fortune".equals(BeyonderData.getPathway(player))
                || BeyonderData.getSequence(player) != 1) return;

        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        player.deathTime = 0;
        player.hurtTime = 0;
        player.invulnerableTime = 0;

        CompoundTag data = player.getPersistentData();
        data.putBoolean(activeKey, true);
        data.putInt(onlineTicksKey, 0);
        data.putInt(restoredHoursKey, 0);
        data.put(originalCharacteristicsKey, saveCharacteristics(player));

        ProphecyAbility.clearActiveProphecy(player);
        restrictToMonster(player);
        teleportWithinWorldBorder(player);
        player.sendSystemMessage(Component.literal(
                "\u00A7bReboot activated. One characteristic layer will return per online hour."));
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(activeKey)) return;

        int onlineTicks = data.getInt(onlineTicksKey) + 1;
        data.putInt(onlineTicksKey, onlineTicks);
        int restoredHours = data.getInt(restoredHoursKey);
        if (onlineTicks < (restoredHours + 1) * ticksPerHour) return;

        restoredHours++;
        data.putInt(restoredHoursKey, restoredHours);
        if (restoredHours >= restorationHours) {
            restoreOriginalCharacteristics(player);
            DivinationUtil.grantAntiDivination(player, ticksPerHour, maximumAntiDivinationPower);
            data.remove(activeKey);
            data.remove(onlineTicksKey);
            data.remove(restoredHoursKey);
            data.remove(originalCharacteristicsKey);
                player.sendSystemMessage(Component.literal(
                    "\u00A7bReboot complete. Your original state has returned with one hour of anti-divination."));
            return;
        }

        int restoredSequence = 9 - restoredHours;
        BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
        component.getCharacteristicList().stream()
                .filter(characteristic -> characteristic.sequence() == restoredSequence)
                .forEach(characteristic -> characteristic.setEnabled(true));
        component.syncHighest();
        persistAndSync(player);
        player.sendSystemMessage(Component.literal("\u00A7bReboot restored your Sequence "
                + restoredSequence + " characteristic layer."));
    }

    private static ListTag saveCharacteristics(ServerPlayer player) {
        ListTag saved = new ListTag();
        for (Characteristic characteristic : BeyonderData.getCharList(player)) {
            CompoundTag entry = new CompoundTag();
            entry.putString("pathway", characteristic.pathway());
            entry.putInt("sequence", characteristic.sequence());
            entry.putInt("stack", characteristic.stack());
            entry.putInt("disabled", characteristic.getDisabledStacks());
            saved.add(entry);
        }
        return saved;
    }

    private static void restrictToMonster(ServerPlayer player) {
        BeyonderComponent component = player.getData(ModAttachments.BEYONDER_COMPONENT);
        for (Characteristic characteristic : component.getCharacteristicList()) {
            if (characteristic.sequence() == 9 && "wheel_of_fortune".equals(characteristic.pathway())) {
                characteristic.setDisabledStacks(Math.max(0, characteristic.stack() - 1));
            } else {
                characteristic.setEnabled(false);
            }
        }
        component.syncHighest();
        persistAndSync(player);
    }

    private static void restoreOriginalCharacteristics(ServerPlayer player) {
        ListTag saved = player.getPersistentData().getList(originalCharacteristicsKey, Tag.TAG_COMPOUND);
        ArrayList<Characteristic> restored = new ArrayList<>();
        for (Tag tag : saved) {
            CompoundTag entry = (CompoundTag) tag;
            Characteristic characteristic = new Characteristic(
                    entry.getString("pathway"), entry.getInt("stack"), entry.getInt("sequence"));
            characteristic.setDisabledStacks(entry.getInt("disabled"));
            restored.add(characteristic);
        }
        player.getData(ModAttachments.BEYONDER_COMPONENT).setCharacteristicList(restored);
        persistAndSync(player);
    }

    private static void persistAndSync(ServerPlayer player) {
        BeyonderData.playerMap.put(player);
        BeyonderData.recalculateCharStackModifiers(player);
        BeyonderDataTickHandler.invalidateCache(player);
        PacketHandler.syncBeyonderDataToPlayer(player);
    }

    private static void teleportWithinWorldBorder(ServerPlayer player) {
        ServerLevel level = player.getServer().overworld();
        var border = level.getWorldBorder();
        BlockPos origin = player.level() == level ? player.blockPosition() : level.getSharedSpawnPos();
        int searchRadius = 512;
        for (int attempt = 0; attempt < 32; attempt++) {
            int x = origin.getX() + player.getRandom().nextIntBetweenInclusive(-searchRadius, searchRadius);
            int z = origin.getZ() + player.getRandom().nextIntBetweenInclusive(-searchRadius, searchRadius);
            if (!level.hasChunk(x >> 4, z >> 4)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos destination = new BlockPos(x, y, z);
            if (y <= level.getMinBuildHeight() || !border.isWithinBounds(destination)) continue;
            player.teleportTo(level, x + 0.5, y, z + 0.5, player.getYRot(), player.getXRot());
            return;
        }
        BlockPos spawn = level.getSharedSpawnPos();
        player.teleportTo(level, spawn.getX() + 0.5,
            spawn.getY(), spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}