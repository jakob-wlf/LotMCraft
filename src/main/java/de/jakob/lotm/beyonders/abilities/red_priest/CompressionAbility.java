package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.entity.custom.projectiles.FireballEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.DamageLookup;
import de.jakob.lotm.util.helper.VectorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;

public class CompressionAbility extends SelectableAbility {

    public static int CHARGES = 0;
    public static int[] LIMITS = new int[]{3,6,9,12};
    public static int MAX;
    public CompressionAbility(String id) {
        super(id, 1.0f, "burning");
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{
                "ability.lotmcraft.pyrokinesis.compress",
                "ability.lotmcraft.pyrokinesis.release",
        };
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        switch (selectedAbility) {
            case 0 -> compress(level, entity);
            case 1 -> fireball(level, entity);
        }
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of(
                "red_priest", 7
        ));
    }

    @Override
    protected float getSpiritualityCost() {
        return 0;
    }

    @Override
    public void onHold(Level level, LivingEntity entity) {
        if(!(entity instanceof ServerPlayer player)) return;
        if(BeyonderData.getSequence(entity) <= 2 && CHARGES==0) CHARGES=3;
        player.displayClientMessage(Component.translatable("lotm.charges").append(": " + CHARGES).withStyle(ChatFormatting.DARK_RED), true);
    }

    public void compress(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        int Seq = BeyonderData.getSequence(entity);
        MAX = (Seq <= 1) ? LIMITS[3] : (Seq <= 3) ? LIMITS[2] : (Seq <= 5) ? LIMITS[1] : LIMITS[0];
//        entity.sendSystemMessage(Component.literal(Seq + " " + MAX + " " + CHARGES));
        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));

        level.playSound(null, startPos.x,startPos.y,startPos.z, SoundEvents.BLAZE_HURT, entity.getSoundSource(), 1.0f, 1.0f);

        if(CHARGES >= MAX) {
            entity.sendSystemMessage(Component.literal("You have the maximum amount of charges").withStyle(ChatFormatting.RED));
            return;
        }
        CHARGES++;
        if(CHARGES == 3) entity.sendSystemMessage(Component.literal("You have unlocked the giant fireball").withStyle(ChatFormatting.YELLOW));
        if(CHARGES == 6) entity.sendSystemMessage(Component.literal("Giant fireball have been enhanced").withStyle(ChatFormatting.GOLD));
        if(CHARGES == 9) entity.sendSystemMessage(Component.literal("Giant fireball is getting greater").withStyle(ChatFormatting.RED));
        if(CHARGES == 12) entity.sendSystemMessage(Component.literal("Giant fireball has reached its max potential").withStyle(ChatFormatting.DARK_RED));
        BeyonderData.reduceSpirituality(entity, 20);

    }

    private void fireball(Level level, LivingEntity entity) {
        if(level.isClientSide)
            return;
        int Seq = BeyonderData.getSequence(entity);
        MAX = (Seq < 1) ? LIMITS[3] : (Seq < 3) ? LIMITS[2] : (Seq < 5) ? LIMITS[1] : LIMITS[0];
        if( CHARGES == 0 ) {
            if (BeyonderData.getSequence(entity) <= 5) CHARGES+=1;
            else {
                entity.sendSystemMessage(Component.literal("You have no charges").withStyle(ChatFormatting.RED));
                return;
            }
        }

        Vec3 startPos = VectorUtil.getRelativePosition(entity.getEyePosition().add(entity.getLookAngle().normalize()), entity.getLookAngle().normalize(), 0, random.nextDouble(1, 2.85f), random.nextDouble(-.1, .6));
        Vec3 direction = AbilityUtil.getTargetLocation(entity, (int) (50 * multiplier(entity)), 1.4f).subtract(startPos).normalize();
        level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.BLAZE_SHOOT, entity.getSoundSource(), 1.0f, 1.0f);

        if(!ArrayUtils.contains(LIMITS, CHARGES)) {
            FireballEntity fireball = new FireballEntity(level, entity, DamageLookup.lookupDamage(7, 0.8) * multiplier(entity), BeyonderData.isGriefingEnabled(entity));
            fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
            fireball.shoot(direction.x, direction.y, direction.z, 1.85f*multiplier(entity), 0);
            level.addFreshEntity(fireball);
            CHARGES--;
        } else {
            int Scale = ArrayUtils.indexOf(LIMITS, CHARGES)+1;
            FireballEntity fireball = new FireballEntity(level, entity, DamageLookup.lookupDamage(7, Scale*3.2) * multiplier(entity), BeyonderData.isGriefingEnabled(entity), Scale*5.5f);
            fireball.setPos(startPos.x, startPos.y, startPos.z); // Set initial position
            fireball.shoot(direction.x, direction.y, direction.z, 2.35f * multiplier(entity), 0);
            level.addFreshEntity(fireball);
//            if (CHARGES == MAX) {
            CHARGES = 0;
//            } else
//            CHARGES-=3;
        }

        BeyonderData.reduceSpirituality(entity, 30);

    }


}
