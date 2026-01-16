package de.jakob.lotm.abilities.fool;

import de.jakob.lotm.abilities.ClientAbilityWheelHelper;
import de.jakob.lotm.abilities.SelectableAbilityItem;
import de.jakob.lotm.abilities.fool.miracle_creation.MiracleHandler;
import de.jakob.lotm.network.packets.handlers.ClientHandler;
import de.jakob.lotm.rendering.MiracleWheelOverlay;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;


/* IDEAS:
CATEGORIES:
- Structure Summoning
- Calamity Creation
    - Meteor
    - Tornado
    - Earthquake
- Area Manipulation
    - Slow Time
    - Make the ground hot
    - Darkness
    - Forbid Godhood
    - Reverse Gravity
- Teleportation
- Target Manipulation
    - Make Target lost
- Other
 */
public class MiracleCreationAbility extends SelectableAbilityItem {
    public MiracleCreationAbility(Properties properties) {
        super(properties, 5);
    }

    @Override
    public Map<String, Integer> getRequirements() {
        return new HashMap<>(Map.of("fool", 2));
    }

    @Override
    protected float getSpiritualityCost() {
        return 1200;
    }

    @Override
    protected String[] getAbilityNames() {
        return new String[]{"ability.lotmcraft.miracle_creation.summon_structure",
                "ability.lotmcraft.miracle_creation.calamity_creation",
                "ability.lotmcraft.miracle_creation.area_manipulation",
                "ability.lotmcraft.miracle_creation.teleportation",
                "ability.lotmcraft.miracle_creation.target_manipulation"
        };
    }

    @Override
    public void handleLeftClickInAir(Player player) {
        if (player.level().isClientSide && getAbilityNames().length > 0) {
            if((ClientAbilityWheelHelper.isWheelOpen() && ClientAbilityWheelHelper.getCurrentAbilityItem() == this) ||
                    (MiracleWheelOverlay.getInstance().isOpen()) || ((System.currentTimeMillis() - MiracleWheelOverlay.getInstance().lastClosedMs) < 500)) {
                return;
            }
            ClientAbilityWheelHelper.openWheel(this, player);
        }
    }

    @Override
    protected void useAbility(Level level, LivingEntity entity, int abilityIndex) {
        if(!(entity instanceof Player player)) return; // Will be handled later
        if(!level.isClientSide) return; // Client-side only for opening the wheel
        switch (abilityIndex) {
            case 0 -> MiracleWheelOverlay.getInstance().open(player, "summon_village", "summon_end_city", "summon_pillager_outpost", "summon_desert_temple", "summon_evernight_church");
            case 1 -> MiracleWheelOverlay.getInstance().open(player, "summon_meteor", "summon_tornados", "summon_volcano");
            case 2 -> MiracleWheelOverlay.getInstance().open(player, "reverse_gravity", "slow_time", "make_ground_hot", "darkness", "forbid_godhood");
            case 3 -> ClientHandler.openCoordinateScreen(player, "teleportation");
        }
    }
}
