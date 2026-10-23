package de.jakob.lotm.beyonders.abilities.abyss.DevilWorship;

import de.jakob.lotm.LOTMCraft;
import de.jakob.lotm.beyonders.abilities.core.SelectableAbility;
import de.jakob.lotm.beyonders.abilities.fool.marionettes.ControllingUtils;
import de.jakob.lotm.beyonders.potions.BeyonderCharacteristicItemHandler;
import de.jakob.lotm.entity.ModEntities;
import de.jakob.lotm.entity.custom.BeyonderNPCEntity;
import de.jakob.lotm.item.ModIngredients;
import de.jakob.lotm.item.PotionIngredient;
import de.jakob.lotm.util.BeyonderData;
import de.jakob.lotm.util.helper.AbilityUtil;
import de.jakob.lotm.util.helper.subordinates.SubordinateUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static de.jakob.lotm.entity.custom.BeyonderNPCEntity.getRandomBeyonderSkin;

public class DevilWorshipAbility extends SelectableAbility {
    public static final int TARGET_RANGE = 30;

    // Swap cooldown for Select Mob
    public static final long COOLDOWN_TICKS = 20L * 60 * 30;

    public static final int DEFAULT_KILLS_FOR_FULL = 50;//normal 50
    public static final int PLAYER_KILLS_FOR_FULL = 10;
    public static final int NPC_KILLS_FOR_FULL = 25;

    //Devil seq, chance it answers, chance it escapes control
    public static final int[] DEVIL_SEQUENCE = { 6, 5, 4 };
    public static final int[] SUMMON_CHANCE  = { 60, 25, 15 };
    public static final int[] ESCAPE_CHANCE  = { 20, 40, 60 };

    // Percentage points taken off a mission's failure chance by the sequence of the Devil
    private static int successBonus(int sequence) {
        if (sequence == 0) return 60;
        if (sequence <= 2) return 40;
        if (sequence <= 4) return 20;
        return sequence == 5 ? 10 : 0;
    }

    // Delay between choosing a mission and the Devil appearing; gives others time to answer
    public static final int SUMMON_DELAY_TICKS = 20 * 20;
    public static final double PROJECTION_RANGE_PER_SEQ = 1000;
    public static final String ABYSS_DIMENSION_NAME = "abyss";
    public static final float DIGEST_BOOST_MAX_PERCENT = 10f;
    public static final float DIGEST_LOSS_PER_SEQ = 2f;//change
    public static final int KILL_HUNT_TICKS = 20 * 60 * 5;
    public static final int ESCAPED_DEVIL_LIFETIME_TICKS = 20 * 60 * 2;
    public static final String DEVIL_TAG = "DevilProjection";
    public static final String DEVIL_CLEARED_TAG = "DevilProjectionCleared";
    // Seek missions
    public static final int SEEK_MIN_SEQ = 5;
    public static final int SEEK_MAX_SEQ = 9;
    // Failure chance (%) at sequence 9 with any pathway
    public static final int SEEK_BASE_FAILURE = 15;
    // Added for every sequence stronger than 9=
    public static final int SEEK_FAILURE_PER_SEQ = 10;
    // Added when a specific pathway is chosen
    public static final int SEEK_PATHWAY_FAILURE = 10;
    //Time at sequence 9 with any pathway
    public static final int SEEK_BASE_SECONDS = 3 * 60;
    //Time added for every sequence stronger than 9.
    public static final int SEEK_SECONDS_PER_SEQ = 3 * 60;
    // Time is multiplied by this when a specific pathway is chosen
    public static final double SEEK_PATHWAY_TIME_MULT = 3.0;

    public static int seekFailurePercent(int sequence, boolean pathwayChosen) {
        int steps = SEEK_MAX_SEQ - sequence;
        int failure = SEEK_BASE_FAILURE + steps * SEEK_FAILURE_PER_SEQ + (pathwayChosen ? SEEK_PATHWAY_FAILURE : 0);
        return Math.min(95, failure);
    }
    public static int seekTimerTicks(int sequence, boolean pathwayChosen) {
        double seconds = SEEK_BASE_SECONDS + (SEEK_MAX_SEQ - sequence) * SEEK_SECONDS_PER_SEQ;
        if (pathwayChosen) seconds *= SEEK_PATHWAY_TIME_MULT;
        return (int) Math.round(seconds * 20);
    }
    //ability
    public DevilWorshipAbility(String id) {
        super(id, 600f);
        canBeUsedByNPC = false;
        canBeCopied = false;
        canBeUsedInArtifact = true;
        canBeReplicated = false;
        cannotBeStolen = true;
    }
    @Override
    public Map<String, Integer> getRequirements() { return new HashMap<>(Map.of("abyss", 7)); }
    @Override
    public float getSpiritualityCost() { return 100; }
    @Override
    protected String[] getAbilityNames() {
        return new String[] {
                "ability.lotmcraft.devil_worship.select_mob",
                "ability.lotmcraft.devil_worship.summon_devil"
        };
    }
    @Override
    protected void castSelectedAbility(Level level, LivingEntity entity, int abilityIndex) {
        if (level.isClientSide) return; // the summon menu is opened by a packet from the server
        if (!(entity instanceof ServerPlayer player)) return;
        switch (abilityIndex) {
            case 0 -> selectMob(player);
            case 1 -> openSummonMenu(player);
        }
    }
    //select mob
    private static void selectMob(ServerPlayer player) {
        LivingEntity target = AbilityUtil.getTargetEntity(player, TARGET_RANGE, 1.5f);
        if (target == null) {
            sendStatus(player);
            return;
        }
        long now = player.level().getGameTime();
        long remaining = Data.cooldownRemaining(player, now);
        if (remaining > 0) {
            say(player, "The Devils will not listen yet. Try again in " + formatTicks(remaining) + ".", 0xFF124D);
            return;
        }
        Data.setPreferred(player, typeId(target));
        Data.setPleasure(player, 0f);
        Data.setLastSelect(player, now);
        say(player, "You offer the Devils a new tribute: " + target.getType().getDescription().getString() + ". Their attention vanishes.", 0x9932CC);
    }
    private static void sendStatus(ServerPlayer player) {
        String preferred = Data.preferred(player);
        if (preferred.isEmpty()) {
            say(player, "You have chosen no tribute. Look at a mob and use Select Mob.", 0x9932CC);
            return;
        }
        long remaining = Data.cooldownRemaining(player, player.level().getGameTime());
        say(player, "Tribute: " + displayName(preferred) + " | Devil pleasure: " + Math.round(Data.pleasure(player)) + "% | Swap available " + (remaining > 0 ? "in " + formatTicks(remaining) : "now"), 0x9932CC);
    }
    //summon devil
    private static void openSummonMenu(ServerPlayer player) {
        List<DevilWorshipNetwork.PlayerEntry> players = player.server.getPlayerList().getPlayers().stream().filter(p -> p != player).map(p -> new DevilWorshipNetwork.PlayerEntry(p.getUUID().toString(), p.getGameProfile().getName())).toList();
        String preferred = Data.preferred(player);
        PacketDistributor.sendToPlayer(player, new DevilWorshipNetwork.OpenSummonScreen(Math.round(Data.pleasure(player)), preferred.isEmpty() ? "" : displayName(preferred), players, availablePathways()));
    }
    public static void handleChoice(ServerPlayer player, int missionIndex, String targetUuid, int seekReward, String seekPathway, int seekSequence) {
        MissionDef mission;
        if (missionIndex == SEEK_MISSION_INDEX) {
            Reward[] rewards = Reward.values();
            if (seekReward < 0 || seekReward >= rewards.length) return;
            if (seekSequence < SEEK_MIN_SEQ || seekSequence > SEEK_MAX_SEQ) return;
            String pathway = seekPathway == null || seekPathway.isEmpty() ? null : seekPathway;
            if (pathway != null && !availablePathways().contains(pathway)) return;
            mission = seekMission(rewards[seekReward], pathway, seekSequence);
        } else {
            if (missionIndex < 0 || missionIndex >= MISSIONS.size()) return;
            mission = MISSIONS.get(missionIndex);
        }
        if (Data.pleasure(player) < 100f) {
            say(player, "The Devils are not pleased enough to answer (" + Math.round(Data.pleasure(player)) + "%).", 0xFF124D);
            return;
        }
        if (hasPendingOrActive(player.getUUID())) {
            say(player, "A Devil's projection still lingers. You cannot call another yet.", 0xFF124D);
            return;
        }
        UUID target = null;
        if (mission.needsTarget()) {
            try {
                ServerPlayer t = player.server.getPlayerList().getPlayer(UUID.fromString(targetUuid));
                if (t == null || t == player) { say(player, "That player cannot be found.", 0xFF124D); return; }
                target = t.getUUID();
            } catch (IllegalArgumentException e) {
                say(player, "That player cannot be found.", 0xFF124D);
                return;
            }
        }
        Data.setPleasure(player, 0f); // the offering is spent regardless
        // Roll which Devil answers.
        int roll = player.getRandom().nextInt(100);
        int idx = 0;
        for (int cumulative = 0; idx < SUMMON_CHANCE.length - 1; idx++) {
            cumulative += SUMMON_CHANCE[idx];
            if (roll < cumulative) break;
        }
        boolean escaped = player.getRandom().nextInt(100) < ESCAPE_CHANCE[idx];
        PENDING.put(player.getUUID(), new PendingSummon(player.getUUID(), mission, target, player.server.getTickCount(), DEVIL_SEQUENCE[idx], escaped));
        say(player, "The Devils stir. One will answer in " + (SUMMON_DELAY_TICKS / 20) + " seconds.", 0x9932CC);
        notifyResponders(player);
    }

    //missions
    public enum Kind { KILL_PLAYER, PROTECTION, LOOT }
    // One possible reward. percent is rolled separately for every entry if the mission succeeds
    public record Loot(String label, int percent, Supplier<ItemStack> stack) {}
    public record MissionDef(String title, String description, int failurePercent, int timerTicks, Kind kind, List<Loot> loot) {
        public boolean needsTarget() { return kind == Kind.KILL_PLAYER; }
        public String timerLabel() { return DevilWorshipAbility.timerLabel(timerTicks); }
    }
    public static String timerLabel(int ticks) {
        int s = ticks / 20;
        return (s / 60) + ":" + String.format("%02d", s % 60);
    }
    // A faction you can raid: its beyonder team, a church and a cathedral
    public record Faction(String name, String deity, String teamName, MissionDef team, MissionDef church, MissionDef cathedral) {}
    public static final MissionDef KILL_PLAYER_MISSION = new MissionDef("Kill a Player", "The Devil hunts a chosen player until one of you falls.", 0, 5 * 60 * 20, Kind.KILL_PLAYER, List.of());
    public static final MissionDef PROTECTION_MISSION = new MissionDef("Protection", "The Devil guards you for the whole duration.", 0, 10 * 60 * 20, Kind.PROTECTION, List.of());
    // Shown when the faction is "None"
    public static final MissionDef ABANDONED_CATHEDRAL = raid(
            "Raid an Abandoned Cathedral",
            "The Devil searches an abandoned cathedral for anything the faithful left behind.",
            20, 6 * 60, List.of(
                    new Loot("Gold ingots x8", 50, () -> new ItemStack(Items.GOLD_INGOT, 8)),
                    new Loot("Diamonds x2", 30, () -> new ItemStack(Items.DIAMOND, 2)),
                    characteristic(10, any(9, 5)),
                    recipe(5, any(9, 5)),
                    subIngredient(30, any(9, 5))
            ));
    //Factions
    public static final List<Faction> FACTIONS = List.of(
            new Faction("Evernight Church", "the Evernight Goddess", "Nighthawks",
                    raid("Raid the Nighthawks",
                            "The Devil attacks the Nighthawks.",
                            50, 10 * 60, List.of(
                                    new Loot("Diamonds x1", 60, () -> new ItemStack(Items.DIAMOND, 1)),
                                    characteristic(60, pw("darkness", 9, 5), pw("death", 9, 6), pw("fool", 9, 7)),
                                    recipe(10, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    subIngredient(30, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    characteristic(30, any(9, 5))
                            )),
                    raid("Raid a Church of the Evernight Goddess",
                            "The Devil storms a church of the Evernight Goddess and ransacks it.",
                            60, 15 * 60, List.of(
                                    new Loot("Diamonds x2", 60, () -> new ItemStack(Items.DIAMOND, 2)),
                                    characteristic(30, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    recipe(20, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    subIngredient(80, pw("darkness", 9, 5), pw("death", 9, 6), pw("fool", 9, 7)),
                                    subIngredient(80, any(9, 5))
                            )),
                    raid("Raid the Cathedral of the Evernight Goddess",
                            "The Devil storms a cathedral of the Evernight Goddess.",
                            75, 20 * 60,  List.of(
                                    new Loot("Diamonds x4", 60, () -> new ItemStack(Items.DIAMOND, 4)),
                                    characteristic(30, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    recipe(80, pw("darkness", 9, 5), pw("death", 9, 6), pw("fool", 9, 7)),
                                    subIngredient(50, pw("darkness", 9, 6), pw("death", 9, 7), pw("fool", 9, 8)),
                                    recipe(80, any(9, 5))
                            ))
            ),
            new Faction("Church of the Lord of Storms", "the Lord of Storms", "Mandated Punishers",
                    raid("Raid the Mandated Punishers",
                            "The Devil attacks the Mandated Punishers.",
                            50, 15 * 60,  List.of(
                                    new Loot("Iron Ingot x9", 60, () -> new ItemStack(Items.IRON_INGOT, 9)),
                                    characteristic(60, pw("tyrant", 9, 5), pw("visionary", 9, 6)),
                                    recipe(10, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    subIngredient(30, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    characteristic(30, any(9, 5))
                            )),
                    raid("Raid a Church of the Lord of Storms",
                            "The Devil storms a church of the Lord of Storms and ransacks it.",
                            60, 20 * 60,   List.of(
                                    new Loot("Iron Ingot x18", 60, () -> new ItemStack(Items.IRON_INGOT, 18)),
                                    characteristic(30, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    recipe(20, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    subIngredient(80, pw("tyrant", 9, 5), pw("visionary", 9, 6)),
                                    subIngredient(80, any(9, 5))
                            )),
                    raid("Raid the Cathedral of the Lord of Storms",
                            "The Devil storms a cathedral of the Lord of Storms.",
                            75, 25 * 60, List.of(
                                    new Loot("Netherite scrap x2", 60, () -> new ItemStack(Items.NETHERITE_SCRAP, 2)),
                                    characteristic(30, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    recipe(80, pw("tyrant", 9, 5), pw("visionary", 9, 6)),
                                    subIngredient(50, pw("tyrant", 9, 6), pw("visionary", 9, 7)),
                                    recipe(80, any(9, 5))
                            ))
            ),
            new Faction("Church of the Earth Mother", "the Earth Mother", "Fertility Order",
                    raid("Raid the Fertility Order",
                            "The Devil attacks the Fertility Order.",
                            40, 15 * 60,  List.of(
                                    new Loot("Emerald x4", 60, () -> new ItemStack(Items.EMERALD, 4)),
                                    characteristic(60, pw("mother", 9, 5)),
                                    recipe(10, pw("mother", 9, 6)),
                                    subIngredient(30, pw("mother", 9, 6)),
                                    characteristic(30, any(9, 5))
                            )),
                    raid("Raid a Church of the Earth Mother",
                            "The Devil storms a church of the Lord of Storms and ransacks it.",
                            50, 20 * 60,   List.of(
                                    new Loot("Emerald x8", 60, () -> new ItemStack(Items.EMERALD, 8)),
                                    characteristic(30, pw("mother", 9, 6)),
                                    recipe(20, pw("mother", 9, 6)),
                                    subIngredient(80, pw("mother", 9, 5)),
                                    subIngredient(80, any(9, 5))
                            )),
                    raid("Raid the Cathedral of the Earth Mother",
                            "The Devil storms a cathedral of the Lord of Storms.",
                            65, 25 * 60, List.of(
                                    new Loot("Emerald x16", 60, () -> new ItemStack(Items.EMERALD, 16)),
                                    characteristic(30, pw("mother", 9, 6)),
                                    recipe(80, pw("mother", 9, 5)),
                                    subIngredient(50, pw("mother", 9, 6)),
                                    recipe(80, any(9, 5))
                            ))
            ),
            new Faction("Church of the Eternal Blazing Sun", "the Eternal Blazing Sun", "Inquisition",
                    raid("Raid the Inquisition",
                            "The Devil attacks the Inquisition.",
                            60, 10 * 60,  List.of(
                                    new Loot("Gold Ingot x10", 60, () -> new ItemStack(Items.GOLD_INGOT, 10)),
                                    characteristic(60, pw("sun", 9, 5)),
                                    recipe(10, pw("sun", 9, 6)),
                                    subIngredient(30, pw("sun", 9, 6)),
                                    characteristic(30, any(9, 5))
                            )),
                    raid("Raid a Church of the Eternal Blazing Sun",
                            "The Devil storms a church of the Lord of Storms and ransacks it.",
                            70, 15 * 60,   List.of(
                                    new Loot("Gold Ingot x20", 60, () -> new ItemStack(Items.GOLD_INGOT, 20)),
                                    characteristic(30, pw("sun", 9, 6)),
                                    recipe(20, pw("sun", 9, 6)),
                                    subIngredient(80, pw("sun", 9, 5)),
                                    subIngredient(80, any(9, 5))
                            )),
                    raid("Raid the Cathedral of the Eternal Blazing Sun",
                            "The Devil storms a cathedral of the Lord of Storms.",
                            85, 20 * 60, List.of(
                                    new Loot("Gold Ingot x30", 60, () -> new ItemStack(Items.GOLD_INGOT, 30)),
                                    characteristic(30, pw("sun", 9, 6)),
                                    recipe(80, pw("sun", 9, 5)),
                                    subIngredient(50, pw("sun", 9, 6)),
                                    recipe(80, any(9, 5))
                            ))
            ),
            new Faction("Blood Sanctify Sect", "Dark Side of the Universe.", "The Butchers",
                    raid("Raid The Butchers",
                            "The Devil attacks the Butchers.",
                            60, 15 * 60,  List.of(
                                    new Loot("Wither Skull x1", 60, () -> new ItemStack(Items.WITHER_SKELETON_SKULL, 1)),
                                    characteristic(50, pw("abyss", 9, 5)),
                                    recipe(5, pw("abyss", 9, 6)),
                                    subIngredient(20, pw("abyss", 9, 6)),
                                    characteristic(20, any(9, 5))
                            )),
                    raid("Raid a External Blood Sanctify Sub-Sect",
                            "The Devil storms the External Blood Sanctify Sub-Sect and ransacks it.",
                            70, 20 * 60,   List.of(
                                    new Loot("Netherite Ingot x1", 50, () -> new ItemStack(Items.NETHERITE_INGOT, 1)),
                                    characteristic(20, pw("sun", 9, 6)),
                                    recipe(10, pw("sun", 9, 6)),
                                    subIngredient(70, pw("sun", 9, 5)),
                                    subIngredient(70, any(9, 5))
                            )),
                    raid("Raid one of the Main Family homes",
                            "The Devil storms one of the Main Family homes of the Blood Sanctify Sect.",
                            85, 25 * 60, List.of(
                                    new Loot("Template x1", 40, () -> new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1)),
                                    characteristic(20, pw("abyss", 9, 6)),
                                    recipe(70, pw("abyss", 9, 5)),
                                    subIngredient(40, pw("abyss", 9, 6)),
                                    recipe(70, any(9, 5))
                            ))
            )
    );
    public static final List<MissionDef> MISSIONS = buildMissionList();
    private static List<MissionDef> buildMissionList() {
        List<MissionDef> all = new ArrayList<>();
        all.add(KILL_PLAYER_MISSION);
        all.add(PROTECTION_MISSION);
        all.add(ABANDONED_CATHEDRAL);
        for (Faction f : FACTIONS) {
            all.add(f.team());
            all.add(f.church());
            all.add(f.cathedral());
        }
        return List.copyOf(all);
    }
    public static final int SEEK_MISSION_INDEX = -1;
    private static MissionDef raid(String title, String description, int failurePercent, int timerSeconds, List<Loot> loot) {return new MissionDef(title, description, failurePercent, timerSeconds * 20, Kind.LOOT, loot);}
    private static String rewardWord(Reward reward) {
        return switch (reward) {
            case CHARACTERISTIC -> "characteristic";
            case RECIPE -> "recipe";
            case SUB_INGREDIENT -> "sub-ingredient";
        };
    }

    // Builds a Seek mission for the chosen reward, pathway (null = any) and sequence
    private static MissionDef seekMission(Reward reward, String pathway, int sequence) {
        boolean chosen = pathway != null;
        PathwayRange range = chosen ? pw(pathway, sequence, sequence) : any(sequence, sequence);
        String what = rewardWord(reward);
        String title = "Seek: " + (chosen ? prettyPathway(pathway) + " " : "") + what + " (Sequence " + sequence + ")";
        return new MissionDef(title, "The Devil searches for a " + (chosen ? prettyPathway(pathway) + " " : "") + what + " of Sequence " + sequence + ".", seekFailurePercent(sequence, chosen), seekTimerTicks(sequence, chosen), Kind.LOOT, List.of(new Loot(label(what, range), 100, () -> randomReward(reward, range))));
    }
    //loot helpers
    public enum Reward {
        CHARACTERISTIC,
        RECIPE,
        SUB_INGREDIENT
    }
    public record PathwayRange(String pathway, int from, int to) {
        public int weakest() { return Math.max(from, to); }
        public int strongest() { return Math.min(from, to); }
    }
    private static PathwayRange pw(String pathway, int from, int to) {
        return new PathwayRange(pathway, from, to);
    }
    private static PathwayRange any(int from, int to) {
        return new PathwayRange(null, from, to);
    }
    private static Loot characteristic(int percent, PathwayRange... ranges) {return new Loot(label("characteristic", ranges), percent, () -> randomReward(Reward.CHARACTERISTIC, ranges));}
    private static Loot recipe(int percent, PathwayRange... ranges) {return new Loot(label("recipe", ranges), percent, () -> randomReward(Reward.RECIPE, ranges));}

    private static Loot subIngredient(int percent, PathwayRange... ranges) {return new Loot(label("sub-ingredient", ranges), percent, () -> randomReward(Reward.SUB_INGREDIENT, ranges));}
    public static List<String> availablePathways() {return BeyonderData.implementedPathways.stream().sorted().toList();}
    private static ItemStack randomReward(Reward type, PathwayRange... ranges) {
        PathwayRange[] pool = ranges.length == 0 ? new PathwayRange[]{any(9, 5)} : ranges;
        List<String> pathways = availablePathways();
        RandomSource random = RandomSource.create();
        for (int attempt = 0; attempt < 30; attempt++) {
            PathwayRange range = pool[random.nextInt(pool.length)];
            String pathway = range.pathway();
            if (pathway == null) {
                if (pathways.isEmpty()) continue;
                pathway = pathways.get(random.nextInt(pathways.size()));
            }
            int minSequence = range.strongest();
            int maxSequence = range.weakest();
            int sequence = minSequence + random.nextInt(maxSequence - minSequence + 1);
            ItemStack stack = switch (type) {
                case CHARACTERISTIC -> stackOf(BeyonderCharacteristicItemHandler.selectCharacteristicOfPathwayAndSequence(pathway, sequence));
                case RECIPE -> recipeOf(pathway, sequence);
                case SUB_INGREDIENT -> subIngredientOf(pathway, sequence);
            };
            if (stack != null && !stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    private static String label(String what, PathwayRange... ranges) {
        if (ranges.length == 0) ranges = new PathwayRange[]{any(9, 5)};
        if (ranges.length == 1) {
            PathwayRange range = ranges[0];
            return "Random " + (range.pathway() == null ? "" : prettyPathway(range.pathway()) + " ") + what + " (Sequence " + sequenceText(range) + (range.pathway() == null ? ", any pathway" : "") + ")";
        }
        List<String> parts = new ArrayList<>();
        for (PathwayRange range : ranges) {parts.add((range.pathway() == null ? "Any" : prettyPathway(range.pathway())) + " " + sequenceText(range));}
        return "Random " + what + " (" + String.join(", ", parts) + ")";
    }
    private static String sequenceText(PathwayRange range) {return range.weakest() == range.strongest() ? String.valueOf(range.weakest()) : range.weakest() + "-" + range.strongest();}
    public static String prettyPathway(String pathway) {
        StringBuilder result = new StringBuilder();
        for (String word : pathway.split("_")) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
    private static ItemStack stackOf(Item item) {
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
    // Finds the recipe item registered under the sequence name
    private static ItemStack recipeOf(String pathway, int sequence) {
        var info = BeyonderData.pathwayInfos.get(pathway);
        if (info == null) return ItemStack.EMPTY;
        String sequenceName = null;
        for (String name : info.sequenceNames()) {if (info.getSequenceIndex(name) == sequence) { sequenceName = name; break; }}
        if (sequenceName == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(LOTMCraft.MOD_ID, sequenceName)).orElse(null);
        return stackOf(item);
    }
    private static boolean isMainIngredient(PotionIngredient ingredient) {
        return ingredient.isMainIngredient();
    }
    // Returns only sub-ingredients, never main ingredients
    private static ItemStack subIngredientOf(String pathway, int sequence) {
        Random random = new Random();
        for (int attempt = 0; attempt < 20; attempt++) {
            PotionIngredient ingredient = ModIngredients.selectRandomIngredientOfPathwayAndSequence(random, pathway, sequence);
            if (ingredient == null) return ItemStack.EMPTY;
            if (!isMainIngredient(ingredient)) return new ItemStack(ingredient);
        }
        return ItemStack.EMPTY;
    }

    //player data
    public static final class Data {
        private Data() {}
        private static final String ROOT = "lotm_devil_worship";
        private static CompoundTag root(ServerPlayer p) { return p.getPersistentData().getCompound(ROOT); }
        private static void save(ServerPlayer p, CompoundTag root) { p.getPersistentData().put(ROOT, root); }
        public static String preferred(ServerPlayer p) { return root(p).getString("mob"); }
        public static void setPreferred(ServerPlayer p, String typeId) { CompoundTag r = root(p); r.putString("mob", typeId); save(p, r); }
        public static float pleasure(ServerPlayer p) { return root(p).getFloat("pleasure"); }
        public static void setPleasure(ServerPlayer p, float value) { CompoundTag r = root(p); r.putFloat("pleasure", Math.clamp(value, 0f, 100f)); save(p, r); }
        public static void setLastSelect(ServerPlayer p, long tick) { CompoundTag r = root(p); r.putLong("lastSelect", tick); save(p, r); }
        public static long cooldownRemaining(ServerPlayer p, long now) {
            CompoundTag r = root(p);
            if (!r.contains("lastSelect")) return 0;
            return Math.max(0, r.getLong("lastSelect") + COOLDOWN_TICKS - now);
        }
    }

    //in-memory state
    private static final class PendingSummon {
        final UUID owner;
        final MissionDef mission;
        final UUID target;       // Kill Player only
        final long startTick;
        final int rolledSeq;     // the tier rolled when the mission was chosen
        final boolean escaped;
        UUID responder;          // set by castProjection
        PendingSummon(UUID owner, MissionDef mission, UUID target, long startTick, int rolledSeq, boolean escaped) {
            this.owner = owner; this.mission = mission; this.target = target;
            this.startTick = startTick; this.rolledSeq = rolledSeq; this.escaped = escaped;
        }
    }

    private static final class ActiveMission {
        final UUID id = UUID.randomUUID();
        final UUID owner, target, responder;
        final MissionDef mission;
        final boolean escaped;
        final int answerSeq, rolledSeq;   // sequence of whoever answered, and the rolled tier
        UUID devilId;                     // null while the Devil is away
        ResourceKey<Level> dimension;     // dimension of that Devil
        long endTick;
        boolean hunting;                  // Kill Player: the Devil has reached its target
        boolean targetDied;
        boolean responderControlling;
        ActiveMission(UUID owner, UUID devilId, ResourceKey<Level> dimension, MissionDef mission, UUID target, long endTick, boolean escaped, UUID responder, int answerSeq, int rolledSeq) {
            this.owner = owner; this.devilId = devilId; this.dimension = dimension; this.mission = mission;
            this.target = target; this.endTick = endTick; this.escaped = escaped;
            this.responder = responder; this.answerSeq = answerSeq; this.rolledSeq = rolledSeq;
        }
    }
    private static final Map<UUID, PendingSummon> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, ActiveMission> ACTIVE = new ConcurrentHashMap<>();
    private static boolean hasPendingOrActive(UUID owner) {return PENDING.containsKey(owner) || ACTIVE.values().stream().anyMatch(m -> m.owner.equals(owner));}
    //projection
    private static boolean inAbyssDimension(ServerPlayer p) {return p.level().dimension().location().getPath().contains(ABYSS_DIMENSION_NAME);}
    public static double hearingRange(int sequence) {
        if (sequence <= 1) return Double.POSITIVE_INFINITY;
        return (4 - sequence + 1) * PROJECTION_RANGE_PER_SEQ;
    }
    private static boolean canHear(ServerPlayer listener, ServerPlayer owner) {
        if (inAbyssDimension(listener)) return true;// the abyss hears everything
        int seq = BeyonderData.getSequence(listener);
        if (seq <= 0) return true;// Sequence 0 years across dimensions
        if (listener.level() != owner.level()) return false;// everyone else needs the same dimension
        return listener.distanceTo(owner) > hearingRange(seq);
    }
    private static double distanceBetween(ServerPlayer a, ServerPlayer b) {return a.level() == b.level() ? a.distanceTo(b) : Double.MAX_VALUE;}
    private static void notifyResponders(ServerPlayer owner) {
        for (ServerPlayer other : owner.server.getPlayerList().getPlayers()) {
            if (other == owner || canHear(other, owner)) continue;
            String where = other.level() == owner.level() ? "to the " + direction(other, owner) + " (" + Math.round(other.distanceTo(owner)) + " blocks)" : "in another dimension";
            say(other, "You feel a Devil Worship ritual " + where + ". Use Projection within " + (SUMMON_DELAY_TICKS / 20) + " seconds to answer it.", 0xB22222);
        }
    }
    private static String direction(ServerPlayer from, ServerPlayer to) {
        double angle = Math.toDegrees(Math.atan2(to.getZ() - from.getZ(), to.getX() - from.getX())); // 0 = east, 90 = south
        String[] names = { "east", "south-east", "south", "south-west", "west", "north-west", "north", "north-east" };
        return names[Math.floorMod(Math.round(angle / 45.0), 8)];
    }
    private static void tellResponder(MinecraftServer server, UUID responderId, String text) {
        if (responderId == null) return;
        ServerPlayer responder = server.getPlayerList().getPlayer(responderId);
        if (responder != null) say(responder, text, 0xB22222);
    }
    private static ActiveMission activeMissionAnsweredBy(UUID responder) {
        for (ActiveMission m : ACTIVE.values()) {if (responder.equals(m.responder) && !m.escaped && m.mission.kind() != Kind.LOOT) return m;}
        return null;
    }
    private static boolean isControlledByResponder(MinecraftServer server, ActiveMission m) {
        if (!m.responderControlling || m.responder == null) return false;
        ServerPlayer responder = server.getPlayerList().getPlayer(m.responder);
        return responder != null && ControllingUtils.isControlling(responder);
    }
    // Second cast: take over the projection Devil
    private static void toggleTakeover(ServerPlayer responder, ActiveMission m) {
        BeyonderNPCEntity devil = findDevil(responder.server, m);
        if (devil == null) {
            say(responder, "Your projection is still on its way.", 0xB22222);
            return;
        }
        if (isControlledByResponder(responder.server, m)) {
            ControllingUtils.cancel(responder, 0, true, false);
            m.responderControlling = false;
            // The original Devil was discarded by ControllingUtils.
            // Spawn a replacement so the mission can continue.
            BeyonderNPCEntity replacement = respawnControlledDevil(responder.server, m);
            if (replacement == null) {
                say(responder, "You release your projection, but it cannot reform.", 0xFF124D);
            } else {
                say(responder, "You release your projection. It reforms nearby.", 0xB22222);
            }
            return;
        }
        m.responderControlling = false;
        if (ControllingUtils.isControlling(responder)) {
            say(responder, "You are already controlling something else.", 0xFF124D);
            return;
        }
        if (!ControllingUtils.startControlling(responder, devil, false, true)) {
            say(responder, "You cannot take over your projection.", 0xFF124D);
            return;
        }
        m.responderControlling = true;
        say(responder, "You take over your projection. Use Projection again to release it.", 0xFFD700);
    }
    public static void castProjection(ServerPlayer responder) {
        ActiveMission mine = activeMissionAnsweredBy(responder.getUUID());
        if (mine != null) {
            toggleTakeover(responder, mine);
            return;
        }
        for (PendingSummon p : PENDING.values()) {
            if (responder.getUUID().equals(p.responder)) {
                say(responder, "Your projection is already on its way.", 0xB22222);
                return;
            }
        }
        long now = responder.server.getTickCount();
        PendingSummon best = null;
        ServerPlayer bestOwner = null;
        double bestDist = Double.MAX_VALUE;
        for (PendingSummon p : PENDING.values()) {
            if (p.mission.kind() == Kind.LOOT) continue; // Seek and Raid are never heard
            if (p.owner.equals(responder.getUUID()) || p.responder != null) continue;
            ServerPlayer owner = responder.server.getPlayerList().getPlayer(p.owner);
            if (owner == null || canHear(responder, owner)) continue;
            double dist = distanceBetween(responder, owner);
            if (best == null || dist < bestDist) { best = p; bestOwner = owner; bestDist = dist; }
        }
        if (best == null) {
            say(responder, "You hear no Devil Worship ritual that still awaits an answer.", 0xFF124D);
            return;
        }
        best.responder = responder.getUUID();
        long left = Math.max(0, (SUMMON_DELAY_TICKS - (now - best.startTick)) / 20);
        say(responder, "Your projection slips into the ritual. It will answer in " + left + " seconds.", 0xB22222);
        say(bestOwner, "The ritual trembles as another presence reaches for it...", 0x9932CC);
    }
    public static float digestBoostPercent(int answerSeq, int rolledSeq) {return Math.max(0f, DIGEST_BOOST_MAX_PERCENT - Math.abs(answerSeq - rolledSeq) * DIGEST_LOSS_PER_SEQ);}
    private static void addDigestion(ServerPlayer player, float percent) {BeyonderData.digest(player, percent / 100f, false);}
    //server ticking
    private static void resolveSummon(MinecraftServer server, ServerPlayer owner, PendingSummon p) {
        ServerPlayer responder = p.responder == null ? null : server.getPlayerList().getPlayer(p.responder);
        if (responder != null && !responder.isAlive()) responder = null;
        UUID responderId = responder == null ? null : responder.getUUID();
        int seq = responder != null ? BeyonderData.getSequence(responder) : p.rolledSeq;
        long now = server.getTickCount();
        say(owner, "A projection of a Sequence " + seq + " Devil answers your call.", 0x9932CC);
        if (p.escaped) {
            say(owner, "The Devil breaks free of your control and turns on you!", 0xFF124D);
            spawnDevil(server, owner, p, seq, responderId);
            return;
        }
        // Mission failure is rolled here, with the answering Devil's sequence helping.
        int failurePercent = Math.max(0, p.mission.failurePercent() - successBonus(seq));
        if (owner.getRandom().nextInt(100) < failurePercent) {
            say(owner, "The Devil's projection fails and vanishes.", 0xFF124D);
            return;
        }
        say(owner, "The Devil accepts your mission: " + p.mission.title() + ".", 0xFFD700);
        if (p.mission.kind() == Kind.PROTECTION) {
            spawnDevil(server, owner, p, seq, responderId); // guards you in person
            return;
        }
        // LOOT and KILL_PLAYER: the Devil departs and the timer starts.
        ActiveMission m = new ActiveMission(owner.getUUID(), null, null, p.mission, p.target, now + p.mission.timerTicks(), false, responderId, seq, p.rolledSeq);
        ACTIVE.put(m.id, m);
        say(owner, "The Devil departs. It will report back in " + p.mission.timerLabel() + ".", 0x9932CC);
    }
    private static BeyonderNPCEntity createDevil(ServerLevel level, int seq, Vec3 pos, UUID ownerId) {
        BeyonderNPCEntity devil = new BeyonderNPCEntity(ModEntities.BEYONDER_NPC.get(), level, false, getRandomBeyonderSkin(), "abyss", seq, false, false);
        devil.setPos(pos);
        devil.getPersistentData().putBoolean(DEVIL_TAG, true);
        devil.getPersistentData().putUUID("DevilProjectionOwner", ownerId);
        return devil;
    }
    private static Vec3 nearby(LivingEntity entity) {return entity.position().add(entity.getRandom().nextDouble() * 6 - 3, 0, entity.getRandom().nextDouble() * 6 - 3);}
    private static void spawnDevil(MinecraftServer server, ServerPlayer owner, PendingSummon p, int seq, UUID responder) {
        ServerLevel level = owner.serverLevel();
        BeyonderNPCEntity devil = createDevil(level, seq, nearby(owner), owner.getUUID());
        int lifetime = p.escaped ? Math.min(p.mission.timerTicks(), ESCAPED_DEVIL_LIFETIME_TICKS) : p.mission.timerTicks();
        if (!p.escaped) {
            devil.setPuppetWarrior(true);
            devil.setMaxLifetimeIfPuppet(lifetime);
            devil.getPersistentData().putBoolean("PuppetSummoned", true);
            devil.getPersistentData().putUUID("PuppetSummonOwner", owner.getUUID());
            level.addFreshEntity(devil);
            SubordinateUtils.turnEntityIntoSubordinate(devil, owner, true);
        } else {
            level.addFreshEntity(devil);
            devil.setTarget(owner);
        }
        ActiveMission m = new ActiveMission(owner.getUUID(), devil.getUUID(), level.dimension(), p.mission, p.target, server.getTickCount() + lifetime, p.escaped, responder, seq, p.rolledSeq);
        ACTIVE.put(m.id, m);
        if (!p.escaped) tellResponder(server, responder, "Your projection has arrived. Use Projection again to take it over.");
    }
    private static boolean startHunt(MinecraftServer server, ServerPlayer owner, ActiveMission m, long now) {
        ServerPlayer target = m.target == null ? null : server.getPlayerList().getPlayer(m.target);
        if (target == null || !target.isAlive()) return false;
        ServerLevel level = target.serverLevel();
        BeyonderNPCEntity devil = createDevil(level, m.answerSeq, nearby(target), m.owner);
        devil.setTarget(target);
        level.addFreshEntity(devil);
        m.devilId = devil.getUUID();
        m.dimension = level.dimension();
        m.hunting = true;
        m.endTick = now + KILL_HUNT_TICKS;
        if (owner != null) say(owner, "The Devil has found its prey.", 0x9932CC);
        say(target, "A Devil's shadow has found you!", 0xFF124D);
        tellResponder(server, m.responder, "Your projection has found its prey. Use Projection again to take it over.");
        return true;
    }
    private static BeyonderNPCEntity findDevil(MinecraftServer server, ActiveMission m) {
        if (m.devilId == null || m.dimension == null) return null;
        ServerLevel level = server.getLevel(m.dimension);
        Entity found = level == null ? null : level.getEntity(m.devilId);
        if (found instanceof BeyonderNPCEntity npc && npc.isAlive()) {return npc;}
        if (m.responderControlling) {return null;}
        return null;
    }
    private static void tickPending(MinecraftServer server, long now) {
        Iterator<PendingSummon> it = PENDING.values().iterator();
        while (it.hasNext()) {
            PendingSummon p = it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(p.owner);
            if (owner == null || !owner.isAlive()) { it.remove(); continue; }
            if (now - p.startTick < SUMMON_DELAY_TICKS) continue;
            it.remove();
            resolveSummon(server, owner, p);
        }
    }
    private static void tickMissions(MinecraftServer server, long now) {
        Iterator<ActiveMission> it = ACTIVE.values().iterator();
        while (it.hasNext()) {
            ActiveMission m = it.next();
            ServerPlayer owner = server.getPlayerList().getPlayer(m.owner);
            BeyonderNPCEntity devil = findDevil(server, m);
            boolean timeUp = now >= m.endTick;
            Boolean outcome = null; // null = still running
            if (m.responderControlling && devil == null) {
                ServerPlayer responder = m.responder == null ? null : server.getPlayerList().getPlayer(m.responder);
                if (responder != null && ControllingUtils.isControlling(responder)) {
                    continue;
                }
                devil = respawnControlledDevil(server, m);
                m.responderControlling = false;
            }
            if (m.escaped) {
                if (devil == null || timeUp) outcome = false;
                else if (owner != null && owner.isAlive() && owner.level() == devil.level()) devil.setTarget(owner);
            } else switch (m.mission.kind()) {
                case LOOT -> {
                    if (timeUp && owner != null && owner.isAlive()) outcome = true;
                }
                case KILL_PLAYER -> {
                    if (!m.hunting) {
                        if (timeUp && !startHunt(server, owner, m, now)) outcome = false;
                    } else if (m.targetDied) {
                        outcome = true;
                    } else if (devil == null || timeUp) {
                        outcome = false;
                    } else if (!isControlledByResponder(server, m)) {
                        ServerPlayer target = server.getPlayerList().getPlayer(m.target);
                        if (target != null && target.isAlive()) devil.setTarget(target);
                    }
                }
                case PROTECTION -> {
                    if (devil == null || owner == null || !owner.isAlive()) outcome = false;
                    else if (timeUp) outcome = true;
                }
            }
            if (outcome != null) {
                it.remove();
                finishMission(server, owner, m, devil, outcome);
            }
        }
    }
    private static void finishMission(MinecraftServer server, ServerPlayer owner, ActiveMission m, BeyonderNPCEntity devil, boolean success) {
        if (m.responderControlling && m.responder != null) {
            ServerPlayer controller = server.getPlayerList().getPlayer(m.responder);
            if (controller != null && ControllingUtils.isControlling(controller)) {
                ControllingUtils.cancel(controller, 0, true, false);
            }
            m.responderControlling = false;
        }
        if (devil != null) devil.discard(); // its beyonder data is cleared in Events.onDevilLeave
        if (owner != null) {
            if (m.escaped) {
                say(owner, "The rogue Devil's projection fades.", 0x9932CC);
            } else if (success && m.mission.kind() == Kind.LOOT) {
                deliverLoot(owner, m.mission);
            } else {
                say(owner, success ? "The Devil completes its task and withdraws: " + m.mission.title() : "The Devil fails its task and withdraws: " + m.mission.title(), success ? 0xFFD700 : 0xFF124D);
            }
        }
        if (success && m.responder != null) {
            ServerPlayer responder = server.getPlayerList().getPlayer(m.responder);
            if (responder != null) {
                float boost = digestBoostPercent(m.answerSeq, m.rolledSeq);
                if (boost > 0f) {
                    addDigestion(responder, boost);
                    say(responder, "Your projection succeeded. Digestion boost earned: " + String.format("%.1f", boost) + "%.", 0xFFD700);
                }
            }
        }
    }
    private static void deliverLoot(ServerPlayer owner, MissionDef mission) {
        List<String> got = new ArrayList<>();
        for (Loot loot : mission.loot()) {
            if (owner.getRandom().nextInt(100) >= loot.percent()) continue;
            ItemStack stack = loot.stack().get();
            if (stack == null || stack.isEmpty()) continue;
            got.add(stack.getHoverName().getString() + (stack.getCount() > 1 ? " x" + stack.getCount() : ""));
            if (!owner.getInventory().add(stack)) owner.drop(stack, false);
        }
        if (got.isEmpty()) {say(owner, "The Devil returns from " + mission.title() + " empty-handed.", 0xFF124D);}
        else {say(owner, "The Devil returns from " + mission.title() + " with: " + String.join(", ", got) + ".", 0xFFD700);}
    }
    //helpers
    private static void say(ServerPlayer player, String text, int color) { player.sendSystemMessage(Component.literal(text).withColor(color)); }
    private static String typeId(LivingEntity entity) { return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(); }
    private static String displayName(String typeId) {return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(typeId)).map(t -> t.getDescription().getString()).orElse(typeId);}
    private static String formatTicks(long ticks) {
        long totalSeconds = ticks / 20;
        return (totalSeconds / 60) + "m " + (totalSeconds % 60) + "s";
    }
    private static BeyonderNPCEntity respawnControlledDevil(MinecraftServer server, ActiveMission m) {
        ServerPlayer responder = m.responder == null ? null : server.getPlayerList().getPlayer(m.responder);
        ServerLevel level = server.getLevel(m.dimension);
        if (level == null) return null;
        Vec3 pos = responder != null ? responder.position() : Vec3.ZERO;
        BeyonderNPCEntity devil = createDevil(level, m.answerSeq, pos, m.owner);
        devil.setPuppetWarrior(true);
        devil.setMaxLifetimeIfPuppet((int) Math.max(20, m.endTick - server.getTickCount()));
        devil.getPersistentData().putBoolean("PuppetSummoned", true);
        devil.getPersistentData().putUUID("PuppetSummonOwner", m.owner);
        level.addFreshEntity(devil);
        m.devilId = devil.getUUID();
        return devil;
    }

    //events
    @EventBusSubscriber(modid = LOTMCraft.MOD_ID)
    public static class Events {
        private static boolean isDevil(Entity e) { return e.getPersistentData().getBoolean(DEVIL_TAG); }
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            MinecraftServer server = event.getServer();
            long now = server.getTickCount();
            if (now % 20 != 0) return;
            tickPending(server, now);
            tickMissions(server, now);
        }
        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            PENDING.clear();
            ACTIVE.clear();
        }
        @SubscribeEvent
        public static void onDevilDrops(LivingDropsEvent event) { if (isDevil(event.getEntity())) event.setCanceled(true); }
        @SubscribeEvent
        public static void onDevilXp(LivingExperienceDropEvent event) { if (isDevil(event.getEntity())) event.setDroppedExperience(0); }
        @SubscribeEvent
        public static void onDevilLeave(EntityLeaveLevelEvent event) {
            if (event.getLevel().isClientSide()) return;
            if (!(event.getEntity() instanceof LivingEntity devil) || !isDevil(devil)) return;
            if (devil.getPersistentData().getBoolean(DEVIL_CLEARED_TAG)) return;
            devil.getPersistentData().putBoolean(DEVIL_CLEARED_TAG, true);
            BeyonderData.clearBeyonderData(devil);
        }
        @SubscribeEvent
        public static void onTargetDeath(LivingDeathEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer dead)) return;
            Entity killer = event.getSource().getEntity();
            if (killer == null) return;
            for (ActiveMission m : ACTIVE.values()) {
                boolean byDevil = killer.getUUID().equals(m.devilId);
                boolean byController = m.responderControlling && killer.getUUID().equals(m.responder);
                if (m.mission.kind() == Kind.KILL_PLAYER && dead.getUUID().equals(m.target) && (byDevil || byController)) {m.targetDied = true;}
            }
        }
        private static int getKillsForFull(LivingEntity entity) {
            if (entity instanceof Player) return PLAYER_KILLS_FOR_FULL;
            if (entity instanceof BeyonderNPCEntity) return NPC_KILLS_FOR_FULL;
            return DEFAULT_KILLS_FOR_FULL;
        }
        @SubscribeEvent
        public static void onKill(LivingDeathEvent event) {
            LivingEntity dead = event.getEntity();
            if (dead.level().isClientSide) return;
            if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
            if (dead == killer) return;
            if (dead instanceof TamableAnimal pet && pet.isTame()) return;
            if (isDevil(dead)) return; // killing a summoned devil never counts
            String preferred = Data.preferred(killer);
            if (preferred.isEmpty() || !preferred.equals(typeId(dead))) return;
            float before = Data.pleasure(killer);
            float after = Math.min(100f, before + 100f / getKillsForFull(dead));
            Data.setPleasure(killer, after);
            if (after >= 100f && before < 100f) {say(killer, "The Devils have accepted your offering. Open Summon Devil to call one...", 0xFFD700);}
            else if (before < 25f && after >= 25f) {say(killer, "A Devil senses the blood split..", 0xFFa3070c);}
            else if (before < 50f && after >= 50f) {say(killer, "The Devil watches with glee. Continue your offering...", 0xFFa3070c);}
            else if (before < 75f && after >= 75f) {say(killer, "The Devil has seen what you are. It is pleased you no longer hide it...", 0xFFa3070c);}
        }
    }
}