package de.jakob.lotm.beyonders.abilities.fool;

import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.item.ModItems;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.ParticleUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class PaperEquipmentAbility extends SelectableAbility {

    public PaperEquipmentAbility(String id) {
        super(id, 10);

        canBeUsedByNPC = false;
        canBeShared = false;
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 7));
    }

    @Override
    protected float getSpiritualityCost() {
        return 100;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.paper_equipment.sword", "ability.lotmcraft.paper_equipment.pickaxe", "ability.lotmcraft.paper_equipment.shovel", "ability.lotmcraft.paper_equipment.axe"};
    }

    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int selectedAbility) {
        if(!(entity instanceof ServerPlayer serverPlayer)) return;

        int paperIndex = serverPlayer.getInventory().findSlotMatchingItem(new ItemStack(Items.PAPER));
        if(paperIndex == -1) {
            AbilityUtil.sendActionBar(entity, Component.translatable("ability.lotmcraft.paper_equipment.no_paper").withColor(0xa463b8));
            return;
        }

        serverPlayer.getInventory().removeItem(paperIndex, 1);
        ItemStack equipment = switch (selectedAbility) {
            case 0 -> new ItemStack(ModItems.PAPER_SWORD.get());
            case 1 -> new ItemStack(ModItems.PAPER_PICKAXE.get());
            case 2 -> new ItemStack(ModItems.PAPER_SHOVEL.get());
            case 3 -> new ItemStack(ModItems.PAPER_AXE.get());
            default -> new ItemStack(Items.AIR);
        };

        serverPlayer.addItem(equipment);
        ParticleUtil.spawnParticles((ServerLevel) level, ParticleTypes.POOF, serverPlayer.position().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.1);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SNOWBALL_THROW, serverPlayer.getSoundSource(), 1, 1);
    }
}
