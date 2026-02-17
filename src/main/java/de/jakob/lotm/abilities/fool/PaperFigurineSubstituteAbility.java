package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = LOTMCraft.MOD_ID)
public class PaperFigurineSubstituteAbility extends Ability {

    private static final HashMap<UUID, Integer> figurineNumbers = new HashMap<>();

    public PaperFigurineSubstituteAbility(String id) {
        super(id, 5f);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    public float getSpiritualityCost() {
        return 20;
    }

    @Override
    public void onAbilityUse(Level level, LivingEntity entity) {
        if(level.isClientSide) {
            if(entity instanceof Player player)
                player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1, 1);
            return;
        }

        if(figurineNumbers.containsKey(entity.getUUID()) && figurineNumbers.get(entity.getUUID()) >= 5)
            return;

        if(!figurineNumbers.containsKey(entity.getUUID()))
            figurineNumbers.put(entity.getUUID(), 1);
        else
            figurineNumbers.replace(entity.getUUID(), figurineNumbers.get(entity.getUUID()) + 1);
        if(entity instanceof Player player) {
            player.addItem(new ItemStack(ModItems.PAPER_FIGURINE_SUBSTITUTE.get()));
        }
    }

    @SubscribeEvent
    public static void takeDamage(LivingDamageEvent.Pre event) {
        if(!figurineNumbers.containsKey(event.getEntity().getUUID()))
            return;

        if(event.getSource().is(ModDamageTypes.LOOSING_CONTROL)) {
            return;
        }

        int num = figurineNumbers.get(event.getEntity().getUUID());

        if(num <= 0)
            return;

        figurineNumbers.put(event.getEntity().getUUID(), num - 1);
        event.setNewDamage(0);

        LivingEntity entity = event.getEntity();
        Vec3 pos = entity.position();

        Level level = entity.level();

        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.CLOUD, entity.getEyePosition().subtract(0, .4, 0), 35, .3, .8, .3, 0);

        Random r = new Random();
        Vec3 newPos = pos.add(r.nextDouble(-7, 7), r.nextDouble(-1, 3), r.nextDouble(-7, 7));

        for(int i = 0; i < 65; i++) {
            if(level.getBlockState(BlockPos.containing(newPos.x, newPos.y, newPos.z)).isAir())
                break;

            newPos = pos.add(r.nextDouble(-7, 7), r.nextDouble(-1, 3), r.nextDouble(-7, 7));
        }

        entity.teleportTo(newPos.x, newPos.y, newPos.z);

        if(entity instanceof Player player) {
            int index = player.getInventory().findSlotMatchingItem(new ItemStack(ModItems.PAPER_FIGURINE_SUBSTITUTE.get()));
            if(index != -1)
                player.getInventory().removeItem(index, 1);
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ARMOR_STAND_HIT, SoundSource.BLOCKS, 3, 1);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, .6f, 1);

    }
}
