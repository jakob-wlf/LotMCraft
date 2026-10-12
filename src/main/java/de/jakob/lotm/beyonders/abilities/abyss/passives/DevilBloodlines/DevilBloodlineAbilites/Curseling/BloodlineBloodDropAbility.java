package de.jakob.lotm.beyonders.abilities.abyss.passives.DevilBloodlines.DevilBloodlineAbilites.Curseling;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.data.ModDataComponents;
import de.jakob.lotm.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class BloodlineBloodDropAbility extends ToggleAbility {

    public static final String ACTIVE_TAG = "lotm_curseling_blood_drop_active";

    private static final double DROP_CHANCE = .4;
    private static final double DROP_RANGE = 2;

    private static final Random RANDOM = new Random();

    public BloodlineBloodDropAbility(String id) {
        super(id);
        canBeUsedByNPC = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("abyss", 6));
    }
    @Override
    protected float getSpiritualityCost() {
        return 0;
    }
    @Override
    public boolean meetsBloodlineRequirement(LivingEntity entity) {return BloodlineCurselingAbility.isValidCurseling(entity);}
    @Override
    public void start(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        entity.getPersistentData().putBoolean(ACTIVE_TAG, true);
        if (entity instanceof ServerPlayer player) {player.displayClientMessage(Component.literal("Blood drop: ON"), true);}
    }
    @Override
    public void tick(Level level, LivingEntity entity) {
    }
    @Override
    public void stop(Level level, LivingEntity entity) {
        if (level.isClientSide()) return;
        entity.getPersistentData().remove(ACTIVE_TAG);
        if (entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal("Blood drop: OFF"), true);
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (attacker.level().isClientSide()) return;
        if (!attacker.getPersistentData().getBoolean(ACTIVE_TAG)) return;
        if (!BloodlineCurselingAbility.isValidCurseling(attacker)) return;
        LivingEntity victim = event.getEntity();
        if (RANDOM.nextDouble() < DROP_CHANCE && attacker.distanceTo(victim) < DROP_RANGE) {
            ItemStack blood = new ItemStack(ModItems.BLOOD.get());
            blood.set(ModDataComponents.BLOOD_OWNER, victim.getUUID().toString());
            victim.spawnAtLocation(blood);
        }
    }
}