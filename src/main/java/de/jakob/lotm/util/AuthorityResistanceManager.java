package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthorityResistanceManager {
    private static final Map<String, Map<ResourceKey<DamageType>, List<Float>>> resistances = new HashMap(22);

    static{
        Map<ResourceKey<DamageType>, List<Float>> visionary = new HashMap<>();
        visionary.put(ModDamageTypes.LOOSING_CONTROL, List.of(0.5f, 0.7f, 0.8f, 0.9f, 0.9f));
        visionary.put(ModDamageTypes.IMAGINATION, List.of(0f, 0.5f, 0.8f, 0.9f));
        visionary.put(ModDamageTypes.MIND_BASED, List.of(0.1f, 0.5f, 0.7f));

        resistances.put("visionary",visionary);
    }

    public static float getResistance(DamageSource source, String path, int seq){
        var pathRes = resistances.get(path);
        if(pathRes == null){
            return 1f;
        }

        var listRes = pathRes.get(source.typeHolder().unwrapKey().orElseThrow());
        if(listRes == null){

            if (source.is(ModDamageTypes.MIND)) {
                listRes = pathRes.get(ModDamageTypes.MIND_BASED);
            } else if (source.is(ModDamageTypes.PHYSICAL)) {
                listRes = pathRes.get(ModDamageTypes.PHYSICAL_BASED);
            } else if (source.is(ModDamageTypes.SOUL)) {
                listRes = pathRes.get(ModDamageTypes.SOUL_BASED);
            }

            if(listRes == null){
                return 1f;
            }
        }

        if(seq + 1 > listRes.size()){
            return 1f;
        }

        return listRes.get(seq);
    }

}
