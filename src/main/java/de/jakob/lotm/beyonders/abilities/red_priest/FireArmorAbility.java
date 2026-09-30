package de.jakob.lotm.beyonders.abilities.red_priest;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.ToggleAbility;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.ability_entities.red_priest_pathway.FirePlateEntity;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.scheduling.ServerScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class FireArmorAbility extends ToggleAbility {
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("lotmcraft", "fire_armor");
    private static int INTERVAL;
    private static boolean IN_IFRAME = false;
    private static final int IFRAME_DELAY = 2;
    private static final int[] LIMITS = new int[]{3,4,5,6};
    private static final int[] INTERVALS = new int[]{40,30,24,20};
    private static List<FirePlateEntity> plates = new ArrayList<>();
    private static int ARMOR_SHARDS;
    private static LivingEntity player;
    private static int MAX;
    public FireArmorAbility(String id) {
        super(id, "burning_armor");
    }

    @Override
    public void tick(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
//        if(ARMOR_SHARDS) return;
        if (ARMOR_SHARDS <= 0) {
            cancel((ServerLevel) level, entity);
        }
    }

    @Override
    public void start(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        player = entity;
        int Seq = BeyonderData.getSequence(entity);
        ARMOR_SHARDS = (Seq <= 1) ? LIMITS[3] : (Seq <= 3) ? LIMITS[2] : (Seq <= 5) ? LIMITS[1] : LIMITS[0];
        INTERVAL = (Seq <= 1) ? INTERVALS[3] : (Seq <= 3) ? INTERVALS[2] : (Seq <= 5) ? INTERVALS[1] : INTERVALS[0];
        MAX = ARMOR_SHARDS;
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR);
        if (toughnessAttribute != null) {
            if (!toughnessAttribute.hasModifier(TOUGHNESS_MODIFIER_ID)) {
                double toughnessValue = 15D;
                AttributeModifier modifier = new AttributeModifier(
                        TOUGHNESS_MODIFIER_ID,
                        toughnessValue,
                        AttributeModifier.Operation.ADD_VALUE
                );
                toughnessAttribute.addPermanentModifier(modifier);

                BeyonderData.reduceSpirituality(entity, (BeyonderData.getSpirituality(entity)/10));

                AtomicInteger j = new AtomicInteger();
                for (int i = 0;i < MAX; i++) {

                    FirePlateEntity firePlateEntity = new FirePlateEntity(ModEntities.FIRE_PLATE.get(), level, entity.getUUID());
                    firePlateEntity.setPos(entity.getX()+0.5D, entity.getY(), entity.getZ());
                    plates.add(firePlateEntity);
                }
                ServerScheduler.scheduleRepeating(0, INTERVAL, MAX, () -> {
                    FirePlateEntity firePlateEntity = plates.get(j.get());
                    level.addFreshEntity(firePlateEntity);
                    j.getAndIncrement();
                });

                entity.sendSystemMessage(Component.literal("You are now shielded with fire, you are immune to the next " + plates.size() + " attacks").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    @Override
    public void stop(Level level, LivingEntity entity) {
        if(level.isClientSide) return;
        AttributeInstance toughnessAttribute = entity.getAttribute(Attributes.ARMOR);
        if (toughnessAttribute != null) {
            if (toughnessAttribute.hasModifier(TOUGHNESS_MODIFIER_ID)) {
                toughnessAttribute.removeModifier(TOUGHNESS_MODIFIER_ID);
                ARMOR_SHARDS=0;
                entity.sendSystemMessage(Component.literal("Your armor has depleted, stay safe").withStyle(ChatFormatting.DARK_RED));
                if(plates.isEmpty()) return;
                for(FirePlateEntity firePlateEntity : plates) {
                    firePlateEntity.discard();
                }
                player = null;
                INTERVAL = 0;
                MAX = ARMOR_SHARDS;
                plates.clear();
                plates = new ArrayList<>();
            }
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

    private static void negateDamage(LivingIncomingDamageEvent event) {
        ARMOR_SHARDS--;
        IN_IFRAME=true;
        event.getEntity().playSound(SoundEvents.BELL_RESONATE, 1.0F, 1.0F);
        if (ARMOR_SHARDS > 0) event.getEntity().sendSystemMessage(Component.literal("Your fire armor protected you.").withStyle(ChatFormatting.RED));
        event.setCanceled(true);
        ServerScheduler.scheduleDelayed(20*IFRAME_DELAY, () -> IN_IFRAME=false);
        if(plates.isEmpty()) return;
        plates.getLast().discard();
        plates.removeLast();
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer)) return;
        if (player == null) return;
        if(event.getEntity() == null) return;
        if(event.getEntity().level().isClientSide) return;
        if(event.getEntity().equals(player) && ARMOR_SHARDS >= 1) {
            if(event.getSource().getEntity() == null) return;
            if(BeyonderData.isBeyonder((LivingEntity) event.getSource().getEntity())) {
                int playerSeq =  BeyonderData.getSequence(event.getEntity());
                int enemySeq =   BeyonderData.getSequence((LivingEntity) event.getSource().getEntity());
                if(playerSeq >= 6 && enemySeq <= 4) return;
                if(playerSeq >= 5 && enemySeq <= 3) return;
                if(playerSeq >=3 && enemySeq <= 1) return;
                if(IN_IFRAME) {
                    event.setCanceled(true);
                    return;
                }
                if(event.getSource().is(DamageTypes.PLAYER_EXPLOSION) || event.getSource().is(DamageTypes.MOB_ATTACK) || event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
                    event.setCanceled(true);
                    return;
                }

                negateDamage(event);

            }
        }
    }


}
