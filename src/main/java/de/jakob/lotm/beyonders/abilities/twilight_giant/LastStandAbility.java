package de.jakob.lotm.beyonders.abilities.twilight_giant;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.beyonders.abilities.core.AbilityUseEvent;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.ClientBeyonderCache;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class LastStandAbility extends SelectableAbility {

    private static final int KOKOA_TICKS = 20 * 60;
    private static final int LAST_FIGHT_TICKS = 20 * 30;
    private static final int WORLD_FIGHT_TICKS = 20 * 3;
    private static final double SEQUENCE_ZERO = 2.25D;
    private static final String MODIFIER = "hand_of_god_last_stand";
    private static final String STATS = "last_stand";

    private static final Map<UUID, Stand> stands = new HashMap<>();

    public LastStandAbility(String id) {
        super(id, 150);
        canBeCopied = false;
        canBeUsedByNPC = false;
        cannotBeStolen = true;
        canBeReplicated = false;
        hasDynamicSpirituality = true;
        dynamicSpirituality = new LinkedList<>(List.of(28000f, 14000f));
        hasDynamicCooldown = true;
        dynamicCooldown = new LinkedList<>(List.of(80, 150));
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("twilight_giant", 1));
    }

    @Override
    protected float getSpiritualityCost() {
        return 14000;
    }

    @Override
    protected String[] getAbilityNames() {
        if (ClientBeyonderCache.localSequence() <= 0) return lastFightOnly();
        return both();
    }

    @Override
    public String getSelectedAbility(LivingEntity entity) {
        String[] names = namesFor(entity);
        int selected = getSelectedAbilityIndex(entity.getUUID());
        if (selected < 0 || selected >= names.length) selected = 0;
        return names[selected];
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (!(entity instanceof ServerPlayer player)) return;
        boolean lastFight = onlyLastFight(player) || abilityIndex == 1;
        boolean world = lastFight && BeyonderData.getSequence(player) <= 0;
        int duration = !lastFight ? KOKOA_TICKS : world ? WORLD_FIGHT_TICKS : LAST_FIGHT_TICKS;
        cleanup(player);
        if (!lastFight) BeyonderData.setSpirituality(player, 0);
        double target = lastFight ? SEQUENCE_ZERO * 1.5D : SEQUENCE_ZERO;
        double raw = Math.max(1D, BeyonderData.getMultiplierForSequence(BeyonderData.getSequence(player)) / 4D);
        BeyonderData.addModifierWithTimeLimit(player, MODIFIER, target / raw, duration * 50L);
        ProxyAbility.applyAttributes(player, STATS, target - 1D);
        player.setHealth(player.getMaxHealth());
        ServerBossEvent bar = new ServerBossEvent(Component.translatable(lastFight ? "ability.lotmcraft.last_stand.last_fight_bar" : "ability.lotmcraft.last_stand.kokoa_bar", duration / 20), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
        bar.addPlayer(player);
        bar.setProgress(1f);
        stands.put(player.getUUID(), new Stand(level.getGameTime() + duration, duration, lastFight, world, bar));
    }

    public static boolean tramples(LivingEntity source, LivingEntity target) {
        Stand stand = stands.get(source.getUUID());
        return stand != null && stand.lastFight && BeyonderData.getSequence(target) == 0;
    }

    private static void cleanup(ServerPlayer player) {
        Stand stand = stands.remove(player.getUUID());
        if (stand != null) stand.bar.removePlayer(player);
        BeyonderData.removeModifier(player, MODIFIER);
        ProxyAbility.clearAttributes(player, STATS);
    }

    private static void finish(ServerPlayer player, boolean kill) {
        Stand stand = stands.get(player.getUUID());
        boolean world = stand != null && stand.world;
        cleanup(player);
        if (world) shatter(player);
        if (kill) die(player);
    }

    private static void shatter(ServerPlayer caster) {
        List<LivingEntity> victims = new ArrayList<>();
        for (Entity entity : caster.serverLevel().getAllEntities()) {
            if (entity instanceof LivingEntity living && living != caster && living.isAlive()) victims.add(living);
        }
        for (LivingEntity living : victims) {
            if (endures(living)) continue;
            living.invulnerableTime = 0;
            living.setHealth(0f);
            living.die(living.damageSources().genericKill());
        }
    }

    private static boolean endures(LivingEntity living) {
        if (!BeyonderData.isBeyonder(living) || BeyonderData.getSequence(living) != 0) return false;
        String pathway = BeyonderData.getPathway(living);
        return "mother".equals(pathway) || "darkness".equals(pathway);
    }

    private static boolean onlyLastFight(LivingEntity entity) {
        return BeyonderData.getSequence(entity) <= 0;
    }

    private static String[] namesFor(LivingEntity entity) {
        if (onlyLastFight(entity)) return lastFightOnly();
        return both();
    }

    private static String[] lastFightOnly() {
        return new String[]{"ability.lotmcraft.last_stand.last_fight"};
    }

    private static String[] both() {
        return new String[]{
                "ability.lotmcraft.last_stand.kokoa",
                "ability.lotmcraft.last_stand.last_fight"
        };
    }

    private static void die(ServerPlayer player) {
        if (!player.isAlive() || player.isRemoved()) return;
        player.invulnerableTime = 0;
        float strike = player.getHealth() + player.getAbsorptionAmount() + 1f;
        player.hurt(player.damageSources().genericKill(), strike);
        if (player.isAlive() && player.deathTime <= 0) {
            player.invulnerableTime = 0;
            player.setHealth(0f);
            player.die(player.damageSources().genericKill());
        }
    }

    private static boolean patronAbility(ServerPlayer player, Ability ability) {
        UUID patron = ProxyAbility.patronId(player);
        if (patron == null) return false;
        String owner = patron.toString();
        return player.getData(ModAttachments.COPIED_ABILITY_COMPONENT).getAbilities().stream()
                .anyMatch(data -> ability.getId().equals(data.abilityId()) && ProxyAbility.COPY_TYPE.equals(data.copyType()) && owner.equals(data.originalOwnerUUID()));
    }

    @SubscribeEvent
    public static void onAbilityUse(AbilityUseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAbility() == null) return;
        Stand stand = stands.get(player.getUUID());
        if (stand == null || !patronAbility(player, event.getAbility())) return;
        stand.strain++;
        if (stand.strain < 3) return;
        var sanity = player.getData(ModAttachments.SANITY_COMPONENT);
        sanity.setSanityAndSync(sanity.getSanity() - 0.1f, player);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (stands.isEmpty()) return;
        for (UUID id : new ArrayList<>(stands.keySet())) {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(id);
            Stand stand = stands.get(id);
            if (player == null || stand == null) {
                stands.remove(id);
                continue;
            }
            long left = stand.endsAt - player.level().getGameTime();
            if (left <= 0) {
                finish(player, stand.lastFight);
                continue;
            }
            stand.bar.setProgress(left / (float) stand.duration);
            if (player.tickCount % 20 == 0) {
                stand.bar.setName(Component.translatable(stand.lastFight ? "ability.lotmcraft.last_stand.last_fight_bar" : "ability.lotmcraft.last_stand.kokoa_bar", (int) Math.ceil(left / 20D)));
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) cleanup(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Stand stand = stands.get(player.getUUID());
        if (stand == null) return;
        finish(player, stand.lastFight);
    }

    private static final class Stand {
        private final long endsAt;
        private final int duration;
        private final boolean lastFight;
        private final boolean world;
        private final ServerBossEvent bar;
        private int strain;

        private Stand(long endsAt, int duration, boolean lastFight, boolean world, ServerBossEvent bar) {
            this.endsAt = endsAt;
            this.duration = duration;
            this.lastFight = lastFight;
            this.world = world;
            this.bar = bar;
        }
    }
}
