package de.jakob.lotm.util;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.abyss.LanguageOfFoulnessAbility;
import de.jakob.lotm.beyonders.abilities.core.Ability;
import de.jakob.lotm.damage.ModDamageTypes;
import de.jakob.lotm.util.helper.AbilityUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthorityResistanceManager {
    private static final Map<String, Map<ResourceKey<DamageType>, List<Float>>> resistances = new HashMap(22);

    private static Map<UUID, Integer> sequenceBuffer = new ConcurrentHashMap<>();

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
        tyrant.put(ModDamageTypes.FIRE, List.of(0.6f, 0.7f, 0.8f));
        tyrant.put(ModDamageTypes.IMPACT, List.of(0.7f, 0.75f, 0.8f, 0.85f, 0.9f));
        tyrant.put(ModDamageTypes.SPACE_DESTRUCTION, List.of(0.8f, 0.85f, 0.9f));
        visionary.put(ModDamageTypes.UNLUCK, List.of(0.5f, 0.7f, 0.8f));
        resistances.put("tyrant", tyrant);

        Map<ResourceKey<DamageType>, List<Float>> wof = new HashMap<>();
        wof.put(ModDamageTypes.WATER, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.LIGHTNING, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.WIND, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
        wof.put(ModDamageTypes.FIRE, List.of(1.2f, 1.2f, 1.2f, 1.2f, 1.2f));
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

        Map<ResourceKey<DamageType>, List<Float>> hunter = new HashMap<>();
        hunter.put(ModDamageTypes.MIND_BASED, List.of(0.65f, 0.70f, 0.75f, 0.85f, 0.9f));
        hunter.put(ModDamageTypes.IMAGINATION, List.of(1f, 1f, 1f, 1f, 1f));
        hunter.put(ModDamageTypes.FIRE, List.of(0f, 0.3f, 0.4f, 0.6f, 0.7f, 0.85f, 0.9f, 0.95f));
        hunter.put(ModDamageTypes.SOUL_FIRE, List.of(0f, 0.4f, 0.5f, 0.7f, 0.9f));
        hunter.put(ModDamageTypes.BLACK_FLAME, List.of(0.f, 0.4f, 0.5f, 0.7f, 0.9f));
        hunter.put(ModDamageTypes.PROVOCATION, List.of(0f, 0.4f, 0.5f, 0.7f, 0.9f));
        hunter.put(ModDamageTypes.WATER, List.of(0.6f, 0.7f, 0.8f));
        hunter.put(ModDamageTypes.LIGHTNING, List.of(0.6f, 0.7f, 0.8f));
        hunter.put(ModDamageTypes.WIND, List.of(0.6f, 0.7f, 0.8f));
        hunter.put(ModDamageTypes.SPIRITUAL, List.of(1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f));
        hunter.put(ModDamageTypes.CHAOS, List.of(0.5f, 0.7f));
        resistances.put("red_priest", hunter);

        Map<ResourceKey<DamageType>, List<Float>> mother = new HashMap<>();
        mother.put(ModDamageTypes.NATURE_BASED, List.of(0.0f, 0.5f));
        mother.put(ModDamageTypes.PHYSICAL_BASED, List.of(0.3f, 0.5f));
        mother.put(ModDamageTypes.SOUL_BASED, List.of(0.3f, 0.5f));
        mother.put(ModDamageTypes.NATURE_WRATH, List.of(0.0f, 0.5f, 0.6f, 0.7f, 0.9f));
        mother.put(ModDamageTypes.TRIAL_OF_DEATH, List.of(0.0f, 0.6f, 0.7f, 0.9f));
        mother.put(ModDamageTypes.TRIAL_OF_MADNESS, List.of(0.0f, 0.6f, 0.7f));
        mother.put(ModDamageTypes.RETURN_TO_EARTH, List.of(0.0f, 0.6f, 0.7f, 0.9f));
        mother.put(ModDamageTypes.LOOSING_CONTROL, List.of(0.5f, 0.7f, 0.9f));
        mother.put(ModDamageTypes.LIFE_DEPRIVATION, List.of(0.0f, 0.5f, 0.7f, 0.9f));
        mother.put(ModDamageTypes.MIND_BASED, List.of(1.75f, 1.5f, 1.25f));
        resistances.put("mother", mother);

        Map<ResourceKey<DamageType>, List<Float>> fool = new HashMap<>();
        fool.put(ModDamageTypes.SPIRITUAL, List.of(0.2f, 0.4f, 0.55f, 0.7f, 0.8f));
        fool.put(ModDamageTypes.PHYSICAL_BASED, List.of(1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f));
        fool.put(ModDamageTypes.MIND_BASED, List.of(0.8f, 0.85f, 0.85f, 0.9f, 0.9f));
        fool.put(ModDamageTypes.SOUL_BASED, List.of(0.8f, 0.85f, 0.85f, 0.9f, 0.9f));
        resistances.put("fool", fool);

        Map<ResourceKey<DamageType>, List<Float>> error = new HashMap<>();
        error.put(ModDamageTypes.SPIRITUAL, List.of(0.5f, 0.6f, 0.7f, 0.8f, 0.9f));
        error.put(ModDamageTypes.PHYSICAL_BASED, List.of(1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f));
        error.put(ModDamageTypes.MIND_BASED, List.of(0.8f, 0.85f, 0.85f, 0.9f, 0.9f));
        error.put(ModDamageTypes.SOUL_BASED, List.of(0.9f, 0.9f, 0.9f, 0.95f, 0.95f));
        resistances.put("error", error);

        Map<ResourceKey<DamageType>, List<Float>> door = new HashMap<>();
        error.put(ModDamageTypes.SPIRITUAL, List.of(0.5f, 0.6f, 0.7f, 0.8f, 0.9f));
        error.put(ModDamageTypes.PHYSICAL_BASED, List.of(1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f, 1.25f));
        error.put(ModDamageTypes.MIND_BASED, List.of(0.8f, 0.85f, 0.85f, 0.9f, 0.9f));
        error.put(ModDamageTypes.SOUL_BASED, List.of(0.85f, 0.88f, 0.9f, 0.95f, 0.95f));
        resistances.put("door", door);

        Map<ResourceKey<DamageType>, List<Float>> demoness = new HashMap<>();
        demoness.put(ModDamageTypes.SOUL_FIRE, List.of(0.2f, 0.4f, 0.5f, 0.7f, 0.9f));
        demoness.put(ModDamageTypes.CHAOS, List.of(0.5f, 0.7f));
        demoness.put(ModDamageTypes.FIRE, List.of(0.3f, 0.5f, 0.6f, 0.8f, 0.9f));
        demoness.put(ModDamageTypes.PROVOCATION, List.of(0f, 0.4f, 0.5f, 0.7f, 0.9f));
        demoness.put(ModDamageTypes.HOLY_BASED, List.of(2.0f, 1.9f, 1.8f, 1.7f, 1.6f, 1.5f, 1.25f, 1.1f));
        demoness.put(ModDamageTypes.BLACK_FLAME, List.of(0f, 0.4f, 0.5f, 0.7f, 0.9f));
        demoness.put(ModDamageTypes.CURSE, List.of(0f, 0.4f, 0.5f, 0.7f, 0.9f));
        resistances.put("demoness", demoness);



        Map<ResourceKey<DamageType>, List<Float>> justiciar = new HashMap<>();
        justiciar.put(ModDamageTypes.MIND_BASED, List.of(0.8f, 0.85f, 0.85f, 0.9f, 0.9f, 0.95f));
        resistances.put("justiciar", justiciar);

        Map<ResourceKey<DamageType>, List<Float>> death = new HashMap<>();
        death.put(ModDamageTypes.PHYSICAL_BASED, List.of(0.3f, 0.5f, 0.7f, 0.9f));
        death.put(ModDamageTypes.MIND_BASED, List.of(1.75f, 1.5f, 1.25f, 1.1f));
        death.put(ModDamageTypes.MUTATION, List.of(1f, 1f, 1f, 1f));
        death.put(ModDamageTypes.TRIAL_OF_DEATH, List.of(2.0f, 1.75f, 1.6f, 1.5f, 1.25f));
        death.put(ModDamageTypes.TRIAL_OF_MADNESS, List.of(2.0f, 1.75f, 1.6f, 1.5f, 1.25f));
        death.put(ModDamageTypes.RETURN_TO_EARTH, List.of(4.0f, 3.25f, 3.0f, 2.5f, 2.0f, 2.0f));
        death.put(ModDamageTypes.LIFE_DEPRIVATION, List.of(2.0f, 1.75f, 1.6f, 1.5f, 1.25f));
        death.put(ModDamageTypes.HOLY_BASED, List.of(2.5f, 2.25f, 2.25f, 2f, 2.0f, 2.0f, 1.5f, 1.25f, 1.1f));
        resistances.put("death", death);
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
            }else if(source.is(ModDamageTypes.NATURE)){
                buff = pathRes.get(ModDamageTypes.NATURE_BASED);
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

    public static void addToBuffer(LivingEntity entity, Integer seq){
        sequenceBuffer.put(entity.getUUID(), seq);
    }

    public static void removeFromBuffer(LivingEntity entity){
        sequenceBuffer.remove(entity.getUUID());
    }

    public static int getFromBuffer(LivingEntity entity){
        if(!sequenceBuffer.containsKey(entity.getUUID()))
            return BeyonderData.getSequence(entity);

        return sequenceBuffer.get(entity.getUUID());
    }
}
