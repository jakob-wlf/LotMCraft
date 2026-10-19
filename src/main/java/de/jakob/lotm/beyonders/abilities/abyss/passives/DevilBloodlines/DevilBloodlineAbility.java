package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.PassiveAbility;
import de.jakob.lotm.attachments.ModAttachments;
import de.jakob.lotm.util.BeyonderData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.machinezoo.noexception.Exceptions.log;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class DevilBloodlineAbility extends PassiveAbility {

    public enum Bloodline { SHADOW_SHIFTER, PHANTOM, SHAPESHIFTER, CURSELING, CORRODER, FLAME_DEVIL }
    private static final int ALL_BLOODLINES_SEQ = 0; // at this sequence the user has every bloodline
    public DevilBloodlineAbility(String id) {
        super(id);
    }
    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    public static void getBloodlineFor(LivingEntity entity, ServerLevel level) {
        assignBloodlineIfMissing(entity, level);
        var component = entity.getData(ModAttachments.BLOODLINE_COMPONENT.get());
        String storedBloodline = component.getBloodline();
        if (!component.hasBloodline()) return;
        try {
            Bloodline.valueOf(storedBloodline);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    //only seq0 
    public static boolean hasAllBloodlines(LivingEntity entity) {
        return BeyonderData.isBeyonder(entity) && "abyss".equals(BeyonderData.getPathway(entity)) && BeyonderData.getSequence(entity) <= ALL_BLOODLINES_SEQ;
    }

    //used by each bloodline
    public static boolean hasBloodline(LivingEntity entity, Bloodline required) {
        String pathway = BeyonderData.getPathway(entity);
        int seq = BeyonderData.getSequence(entity);
        if (!"abyss".equals(pathway)) return false;
        if (hasAllBloodlines(entity)) return true;
        var component = entity.getData(ModAttachments.BLOODLINE_COMPONENT.get());
        if (!component.hasBloodline()) return false;
        return required.name().equals(component.getBloodline());
    }
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            getBloodlineFor(player, level);
        }
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        var component = player.getData(ModAttachments.BLOODLINE_COMPONENT.get());
        if (component.hasBloodline()) return;
        assignBloodlineIfMissing(player, serverLevel);
    }
    @Override
    public void onPassiveAbilityGained(LivingEntity entity, ServerLevel serverLevel) {
        getBloodlineFor(entity, serverLevel); // lock in the bloodline the moment it's gained
    }
    public static void assignBloodlineIfMissing(LivingEntity entity, ServerLevel level) {
        if (!"abyss".equals(BeyonderData.getPathway(entity)))return;
        var component = entity.getData(ModAttachments.BLOODLINE_COMPONENT.get());
        if (component.hasBloodline()) return;
        long seed = level.getSeed() ^ entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
        Random random = new Random(seed);
        Bloodline bloodline = Bloodline.values()[random.nextInt(Bloodline.values().length)];
        component.setBloodline(bloodline.name());
    }

    /*
     * Design notes for the bloodline abilities that still need their own classes:
     *
     * Phantom / Wraith:
     *   Base: toggleable ghost form (wall-phase, no ability use, no world interaction, damageable only
     *   by beyonder damage). Needs its own ToggleAbility.
     *   Upgrade: can use abilities in ghost form, invisible to normal spirit vision but seen by
     *   special spirit vision (spectator/justicar/error).
     *
     * SKINSHIFTER / Spiritshifter:
     *   Done: BloodlineShapeshifterAbility (toggle) + ShapeshifterEvents (drops) + SkinMaskItem.
     *   Upgrade: spirit vision shows the user as the disguised person; special spirit vision exposes
     *   it and inflicts Losing Control.
     *
     * CURSELING / Desire Symbiosis:
     *   Base: curse through blood, Demoness Blood Drop passive, 1.5x damage/duration on Language of Foulness.
     *   Upgrade: hold 5s in 5-10 block range to link a target's desire; while linked and attacked by
     *   that target, cannot drop below 0.5 hearts.
     *
     * CORRODER / Mind Rotter:
     *   Base: green poisonous flame (burn + Poison II), 10-block toxic vapor AoE.
     *   Upgrade: poison retaliation against mind invasion, Mental Plague immunity, +25% poison damage.
     *
     * FLAME_DEVIL / Magma Demon:
     *   Base: sulfur fireballs (3 fast uses then long cooldown), unlosable lava sword, Flame Cage,
     *   Volcanic Eruption.
     *   Upgrade: burn anyone invading your body, +25% fire damage.
     */
}