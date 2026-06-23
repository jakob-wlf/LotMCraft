package de.jakob.lotm.events;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.attachments.ApotheosisComponent;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.attachments.UniquenessComponent;
import de.jakob.lotm.network.PacketHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.effectRendering.MovableEffectManager;
import de.jakob.lotm.sefirah.GreatOldOneManager;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.data.EntityLocation;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class ApotheosisTickHandler {

    /** Colors for Lord of Mysteries (Sefirah Castle) transcendence: Fool, Error, Door. */
    private static final int[] LORD_OF_MYSTERIES_COLORS = {
            0x864ec7, // fool
            0x0018b8, // error
            0x89f5f5, // door
    };

    /** Colors for Eternal Darkness (River of Eternal Darkness) transcendence: Darkness, Death, Twilight Giant. */
    private static final int[] ETERNAL_DARKNESS_COLORS = {
            0x3300b5, // darkness
            0x334f23, // death
            0x944b16, // twilight_giant
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ApotheosisComponent component = event.getEntity().getData(ModAttachments.APOTHEOSIS_COMPONENT);
        if (component.getApotheosisTicksLeft() <= 0) return;

        Player player = event.getEntity();

        if (player.level().isClientSide) {
            ClientHandler.applyCameraShakeToPlayersInRadius(4, 20, (ClientLevel) player.level(), player.position(), 1064);
            return;
        }

        player.setDeltaMovement(new Vec3(0, 0.02, 0));
        player.hurtMarked = true;
        player.getData(ModAttachments.DISABLED_ABILITIES_COMPONENT).disableAbilityUsageForTime("apotheosis", 20, player);

        if (component.isTranscendence()) {
            tickTranscendence(player, component);
        } else {
            tickNormalApotheosis(player, component);
        }
    }

    // ── Transcendence (Great Old One ritual) ──────────────────────────────────

    private static void tickTranscendence(Player player, ApotheosisComponent component) {
        ServerLevel level = (ServerLevel) player.level();
        int ticksLeft = component.getApotheosisTicksLeft();

        // Cycle through pathway colors for this player's sefirot group
        String pathway = BeyonderData.getPathway(player);
        int[] colors = GreatOldOneManager.PATHWAY_TO_NAME.getOrDefault(pathway, "").equals("Eternal Darkness")
                ? ETERNAL_DARKNESS_COLORS : LORD_OF_MYSTERIES_COLORS;
        int colorInt = colors[(ticksLeft / 8) % colors.length];
        float red   = ((colorInt >> 16) & 0xFF) / 255.0f;
        float green = ((colorInt >>  8) & 0xFF) / 255.0f;
        float blue  = ( colorInt        & 0xFF) / 255.0f;

        DustParticleOptions dust = new DustParticleOptions(new Vector3f(red, green, blue), 3.0f);
        Vec3 center = player.position().add(0, player.getBbHeight() / 2.0, 0);
        ParticleUtil.spawnSphereParticles(level, dust, center, 2.0, 80);

        if (ticksLeft % 120 == 0) {
            MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.BEAMS_OF_LIGHT,
                    new EntityLocation(player), 120, false, level, player);
        }

        // Every 5 ticks: drain spirit and sanity from nearby players who are looking at the transcending player
        if (ticksLeft % 5 == 0) {
            tickObserverDrain(player, level);
        }

        component.setApotheosisTicksLeftAndSync(ticksLeft - 1, level, player);
        if (component.getApotheosisTicksLeft() == 0) {
            component.setTranscendence(false);
            GreatOldOneManager.transform((ServerPlayer) player);
        }
    }

    /** Drains spirituality and sanity from nearby players who are looking at the transcending player. */
    private static void tickObserverDrain(Player transcending, ServerLevel level) {
        Vec3 transcendingPos = transcending.position().add(0, transcending.getBbHeight() * 0.6, 0);

        for (ServerPlayer watcher : level.players()) {
            if (watcher == transcending) continue;
            if (watcher.distanceTo(transcending) > 60) continue;
            if (!watcher.hasLineOfSight(transcending)) continue;

            // Check if the watcher is looking at the transcending player (dot product on look angle)
            Vec3 dirToTranscending = transcendingPos.subtract(watcher.getEyePosition()).normalize();
            double dot = watcher.getLookAngle().dot(dirToTranscending);
            if (dot < 0.92) continue; // ~22° cone

            // Drain spirituality
            if (BeyonderData.isBeyonder(watcher)) {
                BeyonderData.reduceSpirituality(watcher, 5000f);
                // Drain sanity
                watcher.getData(ModAttachments.SANITY_COMPONENT.get())
                        .increaseSanityAndSync(-0.04f, watcher);
            } else {
                // Non-beyonders get blinded and slowed
                watcher.hurt(level.damageSources().magic(), 3.0f);
            }
        }
    }

    // ── Normal apotheosis ─────────────────────────────────────────────────────

    private static void tickNormalApotheosis(Player player, ApotheosisComponent component) {
        ServerLevel level = (ServerLevel) player.level();
        int ticksLeft = component.getApotheosisTicksLeft();

        int colorInt = BeyonderData.pathwayInfos.get(component.getPathway()).color();
        float red   = ((colorInt >> 16) & 0xFF) / 255.0f;
        float green = ((colorInt >>  8) & 0xFF) / 255.0f;
        float blue  = ( colorInt        & 0xFF) / 255.0f;

        DustParticleOptions dustParticle = new DustParticleOptions(new Vector3f(red, green, blue), 2.5f);

        Vec3 currentCenter = player.position().add(0, player.getBbHeight() / 2, 0);
        ParticleUtil.spawnSphereParticles(level, dustParticle, currentCenter, 1.5, 60);

        if (ticksLeft % 120 == 0) {
            MovableEffectManager.playEffect(MovableEffectManager.MovableEffect.BEAMS_OF_LIGHT,
                    new EntityLocation(player), 120, false, level, player);
        }

        component.setApotheosisTicksLeftAndSync(ticksLeft - 1, level, player);
        if (component.getApotheosisTicksLeft() == 0) {
            if (BeyonderData.getSequence(player) == 1)
                BeyonderData.setBeyonder(player, BeyonderData.getPathway(player), 0);
            else
                BeyonderData.addCharacteristic(player, 0, component.getPathway());

            UniquenessComponent comp = player.getData(ModAttachments.UNIQUENESS_COMPONENT);
            comp.setHasUniqueness(false);
            comp.setUniquenessPathway("");
            comp.resetKillCount();
            BeyonderData.playerMap.setUniqueness(player, "none");

            PacketHandler.syncUniquenessToPlayer((ServerPlayer) player);
        }
    }

    // ── Death / logout cleanup ────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;
        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        comp.setTranscendence(false);
        comp.setApotheosisTicksLeftAndSync(0, (ServerLevel) player.level(), player);
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        comp.setTranscendence(false);
        comp.setApotheosisTicksLeftAndSync(0, (ServerLevel) player.level(), player);
    }

    /**
     * Cancels any active apotheosis or transcendence ritual when the player
     * leaves the Overworld. Both rituals require the Overworld as their foundation.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        // No ritual active — nothing to do
        ApotheosisComponent comp = player.getData(ModAttachments.APOTHEOSIS_COMPONENT);
        if (comp.getApotheosisTicksLeft() <= 0) return;
        // Leaving the overworld cancels the ritual
        if (event.getFrom().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            boolean wasTranscendence = comp.isTranscendence();
            comp.setTranscendence(false);
            comp.setApotheosisTicksLeftAndSync(0, (ServerLevel) player.level(), player);
            String msg = wasTranscendence
                    ? "§8Transcendence ritual interrupted — you must remain in the Overworld."
                    : "§8Apotheosis interrupted — you must remain in the Overworld.";
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(msg));
        }
    }
}
