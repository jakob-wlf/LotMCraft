package de.jakob.lotm.util;

import de.jakob.lotm.damage.ModDamageTypes;
import net.minecraft.resources.ResourceKey;
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
        visionary.put(ModDamageTypes.MIND_BASED, List.of(0f, 0.3f, 0.5f, 0.8f, 0.9f));
        visionary.put(ModDamageTypes.UNLUCK, List.of(0.85f, 0.9f));
        resistances.put("visionary",visionary);

        Map<ResourceKey<DamageType>, List<Float>> tyrant = new HashMap<>();
        tyrant.put(ModDamageTypes.WATER, List.of(0f, 0.5f, 0.6f, 0.7f, 0.9f));
        tyrant.put(ModDamageTypes.LIGHTNING, List.of(0f, 0.3f, 0.4f, 0.7f, 0.9f));
        tyrant.put(ModDamageTypes.WIND, List.of(0f, 0.5f, 0.6f, 0.7f, 0.9f));
        tyrant.put(ModDamageTypes.IMPACT, List.of(0.7f, 0.75f, 0.8f, 0.85f, 0.9f));
        tyrant.put(ModDamageTypes.SPACE_DESTRUCTION, List.of(0.8f, 0.85f, 0.9f));
        visionary.put(ModDamageTypes.UNLUCK, List.of(0.5f, 0.7f, 0.8f));
        resistances.put("tyrant", tyrant);

        Map<ResourceKey<DamageType>, List<Float>> wof = new HashMap<>();
        wof.put(ModDamageTypes.WATER, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.LIGHTNING, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.WIND, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.IMPACT, List.of(1.0f, 1.1f, 1.1f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.UNLUCK, List.of(0f, 0.3f, 0.5f, 0.7f, 0.8f));
        wof.put(ModDamageTypes.SPIRITUAL, List.of(0.2f, 0.4f, 0.55f, 0.7f, 0.8f));
        wof.put(ModDamageTypes.LOOSING_CONTROL, List.of(0.5f, 0.7f, 0.8f, 0.9f, 0.9f));
        resistances.put("wheel_of_fortune", wof);

        Map<ResourceKey<DamageType>, List<Float>> sun = new HashMap<>();
        sun.put(ModDamageTypes.LIGHT, List.of(0f, 0.5f, 0.6f, 0.7f, 0.9f));
        sun.put(ModDamageTypes.PURIFICATION, List.of(0f, 0.5f, 0.6f, 0.7f, 0.9f));
        sun.put(ModDamageTypes.FAITH, List.of(0.3f, 0.6f, 0.7f, 0.8f, 0.9f));
        sun.put(ModDamageTypes.ORDER, List.of(0.5f, 0.7f, 0.75f, 0.8f, 0.9f));
        sun.put(ModDamageTypes.EVIL_BASED, List.of(0.4f, 0.6f, 0.7f, 0.8f, 0.9f));
        sun.put(ModDamageTypes.HOLY_BASED, List.of(0.4f, 0.6f, 0.7f, 0.8f, 0.9f));
        resistances.put("sun", sun);
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

            List<Float> buff = null;

            if (source.is(ModDamageTypes.HOLY)) {
               buff = pathRes.get(ModDamageTypes.HOLY_BASED);
            } else if (source.is(ModDamageTypes.EVIL)) {
                buff = pathRes.get(ModDamageTypes.EVIL_BASED);
            }

            if(listRes == null)
                listRes = buff;

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
