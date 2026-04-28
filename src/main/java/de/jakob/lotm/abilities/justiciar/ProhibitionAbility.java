package de.jakob.lotm.abilities.justiciar;

import de.jakob.lotm.abilities.core.AbilityUsedEvent;
import de.jakob.lotm.abilities.core.SelectableAbility;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.toServer.AbilitySelectionPacket;
import de.jakob.lotm.rendering.effectRendering.EffectManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProhibitionAbility extends SelectableAbility {

    public static final List<ProhibitionZone> ACTIVE_ZONES = new CopyOnWriteArrayList<>();
    public static final Map<UUID, Integer> FAIL_COUNT_BY_ENTITY = new ConcurrentHashMap<>();

    private static final double ZONE_RADIUS = 80.0;

    public ProhibitionAbility(String id) {
        super(id, 15f, "prohibition");
        interactionRadius = 40;
        hasOptimalDistance = false;
        postsUsedAbilityEventManually = true;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("justiciar", 6));
    }

    @Override
    protected float getSpiritualityCost() {
        return 800;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.prohibition.beyonder_abilities",
                "ability.lotmcraft.prohibition.combat",
                "ability.lotmcraft.prohibition.flying",
                "ability.lotmcraft.prohibition.item_use",
                "ability.lotmcraft.prohibition.players",
                "ability.lotmcraft.prohibition.outside_world",
                "ability.lotmcraft.prohibition.stand_ins",
                "ability.lotmcraft.prohibition.marionette_interchange",
                "ability.lotmcraft.prohibition.theft"
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;

        int casterSeq = BeyonderData.getSequence(entity);
        int ZONE_DURATION = 3600*(int) Math.max(multiplier(entity)/4,1);
        int MAX_ZONES_PER_TYPE = 3*(int) Math.max(multiplier(entity)/4,1);
        // Check for resistance from same-or-higher-rank beyonders in range
        double failChance = casterSeq <= 4 ? 0.15 : 0.4;

        Optional<LivingEntity> resistor = AbilityUtil.getNearbyEntities(entity, serverLevel, entity.position(), (int) ZONE_RADIUS)
                .stream()
                .filter(e -> e != entity && BeyonderData.isBeyonder(e))
                .filter(target -> BeyonderData.getSequence(target) <= casterSeq && random.nextDouble() < failChance)
                .findFirst();

        if (resistor.isPresent()) {
            FAIL_COUNT_BY_ENTITY.merge(resistor.get().getUUID(), 1, Integer::sum);
            if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
                sp.sendSystemMessage(Component.translatable("ability.lotmcraft.prohibition.verdict_failed").withStyle(ChatFormatting.RED));
            }
            return;
        }

        ProhibitionType type = ProhibitionType.values()[abilityIndex];
        long expiryTick = serverLevel.getGameTime() + ZONE_DURATION;

        // Remove oldest zone of this type if at cap
        List<ProhibitionZone> ownZones = new ArrayList<>(ACTIVE_ZONES.stream()
                .filter(z -> z.ownerId.equals(entity.getUUID()) && z.type == type)
                .sorted(Comparator.comparingLong(z -> z.expiryTick))
                .toList());
        while (ownZones.size() >= MAX_ZONES_PER_TYPE) {
            ACTIVE_ZONES.remove(ownZones.remove(0));
        }

        // Remove expired zones
        long now = serverLevel.getGameTime();
        ACTIVE_ZONES.removeIf(z -> z.expiryTick < now);

        ProhibitionZone newZone = new ProhibitionZone(entity.getUUID(), type, entity.position(), serverLevel, expiryTick);
        ACTIVE_ZONES.add(newZone);

        // Play the VFX immediately, then re-send every 140 ticks (effect lasts 160t) for the full zone duration
        final double px = entity.getX(), py = entity.getY(), pz = entity.getZ();
        EffectManager.playEffect(EffectManager.Effect.PROHIBITION, px, py, pz, serverLevel);
        ServerScheduler.scheduleForDuration(140, 140, ZONE_DURATION, () -> {
            if (!newZone.isActive()) return;
            EffectManager.playEffect(EffectManager.Effect.PROHIBITION, px, py, pz, serverLevel);
        }, null, serverLevel);

        String typeName = type.displayName;
        Component message = Component.translatable("ability.lotmcraft.prohibition.zone_prohibited", typeName)
                .withStyle(ChatFormatting.GOLD);

        serverLevel.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (p.level().equals(serverLevel) && p.distanceTo(entity) <= ZONE_RADIUS) {
                p.sendSystemMessage(message);
            }
        });

        NeoForge.EVENT_BUS.post(new AbilityUsedEvent(serverLevel, entity.position(), entity, this, interactionFlags, ZONE_RADIUS, 20 * 2));
    }

    @Override
    public void nextAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        selectedAbility++;
        if(selectedAbility >= getAbilityNames().length) {
            selectedAbility = 0;
        }

        if((entitySeq > 6 && selectedAbility >= 2)){
            selectedAbility = 0;
        }

        if((entitySeq > 4 && selectedAbility >= 3)){
            selectedAbility = 0;
        }
        if((entitySeq > 3 && selectedAbility >= 4)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    @Override
    public void previousAbility(LivingEntity entity){
        if(getAbilityNames().length == 0)
            return;

        if(!selectedAbilities.containsKey(entity.getUUID())) {
            selectedAbilities.put(entity.getUUID(), 0);
        }

        int selectedAbility = selectedAbilities.get(entity.getUUID());
        selectedAbility--;
        if(selectedAbility <= -1) {
            selectedAbility = getAbilityNames().length - 1;
        }

        int entitySeq = AbilityUtil.getSeqWithArt(entity, this);

        if((entitySeq > 6 && selectedAbility >= 2)){
            selectedAbility = 0;
        }

        if((entitySeq > 4 && selectedAbility >= 3)){
            selectedAbility = 0;
        }
        if((entitySeq > 3 && selectedAbility >= 4)){
            selectedAbility = 0;
        }

        selectedAbilities.put(entity.getUUID(), selectedAbility);
        PacketHandler.sendToServer(new AbilitySelectionPacket(getId(), selectedAbility));
    }

    public enum ProhibitionType {
        BEYONDER_ABILITIES("Beyonder Abilities"),
        COMBAT("Combat"),
        FLYING("Flying"),
        ITEM_USE("Item Use"),
        PLAYERS("Players"),
        OUTSIDE_WORLD("Outside World"),
        STAND_INS("Stand-ins"),
        MARIONETTE_INTERCHANGE("Marionette Interchange"),
        THEFT("Theft");

        public final String displayName;

        ProhibitionType(String name) {
            this.displayName = name;
        }
    }

    public static class ProhibitionZone {
        public final UUID ownerId;
        public final ProhibitionType type;
        public final Vec3 center;
        public final ServerLevel level;
        public final long expiryTick;

        public ProhibitionZone(UUID ownerId, ProhibitionType type, Vec3 center, ServerLevel level, long expiryTick) {
            this.ownerId = ownerId;
            this.type = type;
            this.center = center;
            this.level = level;
            this.expiryTick = expiryTick;
        }

        public boolean isActive() {
            return level.getGameTime() < expiryTick;
        }

        public boolean isInZone(Vec3 pos, ServerLevel lvl) {
            return lvl.equals(level) && pos.distanceTo(center) <= ZONE_RADIUS;
        }
    }
}
