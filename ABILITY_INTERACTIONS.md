# Ability Interactions — Update Design Notes

---

## 1. Universal Systems

### 1.1 Local Time
A mechanic that can be applied to a defined area with a set radius and time-scale value. All time-dependent abilities (projectiles, delayed effects, channelled casts, etc.) that enter or originate within that area check the local time scale and adjust their behaviour accordingly — moving faster, slower, or freezing entirely depending on the value set.

**Key points:**
- Defined by a radius and a time-scale multiplier
- Any ability that "ticks" over time respects the local time scale
- Can be used offensively (slow enemy projectiles) or as a utility zone

---

### 1.2 Mid-Cast Cancellation
Abilities that support interruption will periodically check their surroundings for an entity carrying a specific cancellation tag. If such an entity is detected within proximity, the ability cancels itself cleanly.

**Key points:**
- Cancellation is triggered by a tagged entity spawned near the caster or target
- Abilities opt in to this system — not all abilities are interruptible
- Provides a consistent, extensible hook for any future cancel mechanic

---

## 2. Pathway Interactions

### 2.1 Demoness Pathway
The Demoness pathway's petrification can freeze and halt certain active abilities mid-flight or mid-cast, and can even neutralise persistent field abilities.

| Target Ability | Effect |
|---|---|
| Fireball (and similar projectiles) | Projectile is petrified and stopped in place |
| Black Hole (Door Pathway) | Petrification can halt and remove the black hole entirely |
| Tornado entities (Tyrant / Red Priest / Demoness) | Petrification freezes and halts active tornado entities |
| Tsunami (Tyrant Pathway) | Petrification converts the tsunami wave into solid stone, stopping it |
| Lightning Branch (Tyrant Pathway) | Petrification can freeze a lightning branch entity in place |
| Frost Spear (Demoness Pathway) | Petrification converts the frost spear to stone mid-flight, stopping it |
| Meteor (Demoness Pathway) | Petrification can halt a falling meteor and turn it to stone |
| Spear of Light / Unshadowed Spear (Sun Pathway) | Petrification encases the spear, neutralising it |
| Electromagnetic Tornado (Tyrant Pathway) | Petrification freezes the tornado entity |
| Exile Doors (Door Pathway) | Petrification can seal and neutralise active exile door entities |
| Puppet Soldiers (Red Priest Pathway) | Petrification can freeze puppet soldier NPCs in place |
| Marionettes (Fool Pathway) | Petrification can freeze marionettes, severing the controller's link |
| Avatar Entity (Visionary Pathway) | Petrification can petrify an identity avatar, disabling it |
| Golem (Mother Pathway) | Petrification turns a golem into inert stone |
| War Banner (Red Priest Pathway) | Petrification can petrify a war banner, nullifying its buff aura |
| Fire Ravens (Red Priest Pathway) | Petrification can freeze fire ravens mid-flight |
| Space-Time Storm (Door Pathway) | Petrification halts the storm, freezing its expansion |

**Demoness – Frost interactions:**

| Target Ability | Effect |
|---|---|
| Torrential Downpour (Tyrant Pathway) | Frost can freeze the rain into hail or ice, altering the effect |
| Water Manipulation projectiles (Tyrant Pathway) | Frost freezes water projectiles mid-flight |
| Water Wall (Tyrant Pathway) | Frost converts a water wall into an ice wall |
| Flooding (Tyrant Pathway) | Frost can flash-freeze placed water blocks |
| Tsunami (Tyrant Pathway) | Frost can freeze the tsunami into a wall of ice |

**Demoness – Black Flame interactions:**

| Target Ability | Effect |
|---|---|
| Thread Cocoon (Demoness Pathway) | Black Flame can burn through a thread cocoon, breaking it |
| Healing (Mother Pathway) | Black Flame on a target can counteract healing, reducing its effectiveness |
| Wall of Light (Sun Pathway) | Black Flame colliding with a Wall of Light causes both to partially cancel out |

**Demoness – Charm interactions:**

| Target Ability | Effect |
|---|---|
| Battle Hypnosis (Visionary Pathway) | Charm and Battle Hypnosis applied to the same target conflict — the Beyonder with the lower (more powerful) sequence number's effect prevails |
| Instigation (Demoness Pathway) | Charm overrides Instigation on the same target |
| Frenzy (Visionary Pathway) | Charm can temporarily suppress a frenzied target |
| Puppet Soldiers (Red Priest Pathway) | Charm can break puppet soldier loyalty, turning them neutral |
| Marionettes (Fool Pathway) | Charm can contest the marionette link — if the charm is stronger, the puppet is freed |

**Demoness – Curse interactions:**

| Target Ability | Effect |
|---|---|
| Blessing (WoF Pathway) | Curse and Blessing on the same entity conflict — the more recent effect suppresses the other |
| Holy Oath (Sun Pathway) | Holy Oath grants resistance to curses, reducing their duration |
| Spiritual Baptism (WoF Pathway) | Spiritual Baptism can cleanse an active curse |

**Demoness – Disease / Plague interactions:**

| Target Ability | Effect |
|---|---|
| Cleansing (Mother Pathway) | Cleansing removes disease and plague effects from the target |
| Purification Halo (Sun Pathway) | Purification Halo can suppress disease and plague within its radius |
| Life Aura (Mother Pathway) | Life Aura passively reduces disease/plague tick damage within its radius |
| Blooming Area (Mother Pathway) | Blooming Area's nature energy conflicts with plague/disease, weakening both |

**Demoness – Invisibility / Shadow Concealment interactions:**

| Target Ability | Effect |
|---|---|
| Spirit Vision (Common) | Spirit Vision can detect invisible Demoness users |
| Spectating (Visionary Pathway) | Spectating can reveal invisible entities |
| Cull (Red Priest Pathway) | Cull's highlighting can pierce Demoness invisibility |
| Illuminate (Sun Pathway) | Illuminate placed near an invisible entity can reveal them |
| Unshadowed Domain (Sun Pathway) | Unshadowed Domain's light dispels invisibility and shadow concealment within its area |
| Night Domain (Darkness Pathway) | Night Domain's darkness enhances invisibility, making detection harder |
| Horror Aura (Darkness Pathway) | Horror Aura's darkness boosts shadow concealment duration |

---

### 2.2 Sun Pathway
Sun abilities carry a purifying nature that interacts with corruption, desire-based effects, and concealment.

| Target | Effect |
|---|---|
| Avatar of Desire (Abyss Pathway) | Sun abilities cancel or suppress the avatar due to their purifying nature |
| Defiling Seed (on an entity) | Sun abilities can cleanse and remove the seed from the afflicted entity |
| Invisibility abilities (Demoness Pathway) | Sun abilities can pierce or dispel Demoness invisibility effects |
| Nightmare (Darkness Pathway) | Sun abilities can erode or dispel a Nightmare realm — light counters dark |
| Night Domain (Darkness Pathway) | Sun domain abilities (Unshadowed Domain, Divine Kingdom) can counteract and shrink a Night Domain |
| Surge of Darkness (Darkness Pathway) | Sun abilities fired into a Surge of Darkness weaken it, reducing its radius |
| Horror Aura (Darkness Pathway) | Sun's Holy Light or Pure White Light can suppress Horror Aura, removing debuffs within the light's range |
| Shadow Concealment (Demoness Pathway) | Flaring Sun or Pure White Light automatically reveals shadow-concealed entities |
| Psychological Invisibility (Visionary Pathway) | Sun abilities are unable to reveal Psychological Invisibility — it is mental, not physical |
| Fog of War (Red Priest Pathway) | Sun light-based abilities can partially pierce Fog of War, reducing its blindness effect |
| Toxic Smoke (Abyss Pathway) | Fire of Light or Flaring Sun can burn away toxic smoke, clearing the area |
| Poisonous Flame (Abyss Pathway) | Sun's purifying fire overpowers poisonous flame, neutralising the poison component |
| Disease / Plague (Demoness Pathway) | Sun abilities can purify disease and plague effects on afflicted entities |
| Curse (Demoness Pathway) | Sun purification can weaken or remove an active curse |
| Loosing Control effect | Holy Song or Placate-like Sun effects can suppress Loosing Control |
| Mental Plague (Visionary Pathway) | Sun purification can cleanse Mental Plague from a target |
| Sword of Darkness (Darkness Pathway) | A clash between Sword of Justice and Sword of Darkness results in both being partially cancelled |
| Cocoon (Demoness Pathway) | Sun fire abilities can burn through a thread cocoon |

**Sun – Light vs. Darkness thematic counters:**

| Sun Ability | Darkness Ability | Interaction |
|---|---|---|
| Unshadowed Domain | Night Domain | The two domains contest each other — the overlap region shrinks the weaker one |
| Pure White Light | Surge of Darkness | Opposing expanding spheres cancel where they overlap |
| Flaring Sun | Midnight Poem (Wilt) | Flaring Sun in the wilt area reduces the wilt damage |
| Holy Light Summoning | Horror Aura | Holy Light descending within Horror Aura dispels the aura locally |
| Wall of Light | Sword of Darkness | Wall of Light can block or weaken Sword of Darkness slash |
| Wings of Light | Concealment (Darkness) | Wings of Light glow prevents the user from entering concealment |
| Solar Envoy | Requiem | Solar Envoy's radiant presence resists Requiem's pacification |

---

### 2.3 Spectator / Visionary Pathway

| Target | Effect |
|---|---|
| Defiling Seed (on an entity) | **Placate** can remove the defiling seed from a target |
| Curse (Demoness Pathway) | **Placate** can soothe and remove a curse from an afflicted entity |
| Loosing Control (any source) | **Placate** removes Loosing Control and restores sanity |
| Frenzy (Visionary Pathway) | Placate can calm a frenzied entity, ending the frenzy |
| Petrification (Demoness Pathway) | **Battle Hypnosis (Freeze)** stacks with petrification — the combined hold is harder to break |
| Charm (Demoness Pathway) | **Battle Hypnosis** conflicts with Charm — the Beyonder with the lower (more powerful) sequence number's effect prevails |
| Hair Entanglement (Darkness Pathway) | **Battle Hypnosis (Freeze)** stacks with Hair Entanglement for a longer disable |
| Invisibility (Demoness Pathway) | **Spectating** can reveal invisible Demoness users by reading their entity data |
| Shapeshifted Beyonder (Fool Pathway) | **Spectating** or **Telepathy** reveals that an entity is shapeshifted |
| Night Domain (Darkness Pathway) | **Psychological Invisibility** works normally within Night Domain — it is mental, not visual |
| Unshadowed Domain (Sun Pathway) | **Psychological Invisibility** is unaffected by Unshadowed Domain — light cannot reveal a mental effect |
| Sleep Inducement + Nightmare Spectator | **Sleep Inducement** into **Nightmare Spectator** combo: put target to sleep, then attack their dreams |
| Dream Traversal + Sleep Inducement | Use **Sleep Inducement** on a distant target, then **Dream Traversal** to teleport to them |
| Awe + Conquering (Red Priest) | **Awe** debuff stacking with **Conquering** can make the target unable to act |
| Mental Plague + Disease (Demoness) | **Mental Plague** stacks with Disease — the target suffers both physical and mental deterioration |
| Frenzy + Instigation (Demoness) | **Frenzy** on a target already under **Instigation** amplifies the chaotic behaviour |
| Marionette Controlling (Fool) | **Battle Hypnosis (Disable Abilities)** on a marionette controller severs their marionette link |
| Puppet Soldiers (Red Priest) | **Mind Invasion** can potentially hijack control of puppet soldiers from their creator |

---

### 2.4 Error Pathway
Error abilities revolve around loopholes, theft, and subverting the rules other pathways rely on.

**Theft**
| Target | Stolen Resource |
|---|---|
| WoF Pathway | Luck (reduces target's accumulated luck, adds to caster) |
| Sun Pathway | Buff effects from Holy Oath or Holy Song can be stolen |
| Red Priest Pathway | War Banner buff effects can be stolen from entities within its range |
| Tyrant Pathway | Energy Transformation's flight can be temporarily stolen |
| Darkness Pathway | Night Domain's caster buff (1.35 modifier) can be stolen |
| Mother Pathway | Life Aura's healing effect can be siphoned for the Error user |
| Visionary Pathway | Dragon Scales' damage resistance can be stolen |
| Demoness Pathway | Charm effect on a target can be redirected to serve the Error user |
| Common | Mythical Creature Form's damage multiplier buff can be stolen |
| *(more to be added)* | *(more to be added)* |

**Ability Theft — specific interactions:**
| Target Ability | Interaction |
|---|---|
| Recording (Door Pathway) | Ability Theft can steal a recorded ability from a Door Beyonder's book |
| Miracle Creation (Fool Pathway) | Ability Theft can copy a miracle in progress |
| Any toggled ability | Ability Theft can forcibly deactivate an enemy's active toggle ability |
| Puppet Soldiers (Red Priest) | Ability Theft can sever the bond between creator and puppet soldier |
| Marionettes (Fool Pathway) | Ability Theft can steal control of a marionette |

**Loophole**
| Target Ability / Effect | Interaction |
|---|---|
| Space Distortion (Door Pathway) | Loophole can cancel the distortion outright |
| Distortion Field (Door Pathway) | Loophole can grant temporary personal immunity to the field |
| Petrification (Demoness Pathway) | Loophole can exploit a gap in petrification to break free early |
| Night Domain (Darkness Pathway) | Loophole can create a bubble of normalcy within Night Domain, negating debuffs locally |
| Horror Aura (Darkness Pathway) | Loophole can negate the sanity damage from Horror Aura |
| Sealing (Door Pathway) | Loophole can break a seal placed by a Door Beyonder |
| Black Hole (Door Pathway) | Loophole can create safe passage through a Black Hole's pull |
| Nightmare (Darkness Pathway) | Loophole can find an exit from a Nightmare realm |
| Fog of War (Red Priest Pathway) | Loophole can pierce Fog of War for the caster, seeing through the blindness |
| Trap (Red Priest Pathway) | Loophole can detect and disarm a hidden trap |
| Wall of Light (Sun Pathway) | Loophole can bypass a Wall of Light without taking damage |
| Exile (Door Pathway) | Loophole can escape from Exile prematurely |
| Conquering (Red Priest Pathway) | Loophole can nullify the Conquered debuff |
| Misfortune Field (WoF Pathway) | Loophole can negate the unluck effect within a Misfortune Field |
| Chain of Command (Red Priest Pathway) | Loophole can sever the damage-sharing link of Chain of Command |

**Decryption**
| Target | Effect |
|---|---|
| Shapeshifted Beyonder | Decryption can reveal whether a Beyonder is currently shapeshifted |
| Invisible entity (Demoness / Visionary) | Decryption can reveal hidden or invisible entities |
| Concealment dimension (Darkness Pathway) | Decryption can detect the presence of a concealment area |
| Pocket Dimension entrance (Door Pathway) | Decryption can locate a hidden pocket dimension entrance |
| Mirror World (Demoness Pathway) | Decryption can detect a Demoness user traversing the mirror world |
| Trap (Red Priest Pathway) | Decryption can reveal a hidden trap's location and trigger radius |
| Disguised puppet (Fool Pathway) | Decryption reveals whether a mob is actually a marionette |
| Dream Divination (Common) | Decryption can intercept or decode another Beyonder's divination results |
| Anti-Divination (Common) | Decryption can partially bypass Anti-Divination wards |

**Parasitation interactions:**
| Target | Effect |
|---|---|
| Puppet Soldiers (Red Priest Pathway) | Parasitation can hijack a puppet soldier, overriding the creator's control |
| Marionettes (Fool Pathway) | Parasitation on a marionette gives the Error user dual control |
| Golem (Mother Pathway) | Parasitation can infect and control a golem |
| Avatar Entity (Visionary Pathway) | Parasitation can compromise an identity avatar |

**Mental Disruption interactions:**
| Target | Effect |
|---|---|
| Charm (Demoness Pathway) | Mental Disruption breaks an active charm on a target |
| Battle Hypnosis (Visionary Pathway) | Mental Disruption can interrupt an active hypnosis effect |
| Sleep Inducement (Visionary Pathway) | Mental Disruption can wake a sleeping target |
| Marionette link (Fool Pathway) | Mental Disruption on a marionette controller disrupts their puppeteering |

**Time Manipulation interactions:**
| Target | Effect |
|---|---|
| Projectiles (any pathway) | Time Manipulation can slow or freeze projectiles in a local area |
| Delayed effects (any pathway) | Time Manipulation can accelerate or delay ticking effects |
| Cooldowns (any pathway) | Time Manipulation can potentially reset or extend enemy ability cooldowns |
| Defiling Seed ticks (Abyss Pathway) | Time Manipulation can accelerate the seed's damage ticks |
| Disease / Plague duration (Demoness Pathway) | Time Manipulation can speed up or slow down disease progression |
| Nightmare duration (Darkness Pathway) | Time Manipulation can extend or shorten a Nightmare realm's lifespan |

---

### 2.5 Door Pathway
Door abilities involve space, dimensions, and teleportation — they interact with abilities that create physical effects, zones, or entities.

**Black Hole interactions:**
| Target Ability | Effect |
|---|---|
| Projectiles (any pathway) | Black Hole pulls in and absorbs nearby projectiles — fireballs, spears, lightning branches, etc. |
| Tornado entities (Tyrant / Red Priest) | Black Hole can pull in and destroy tornado entities |
| Tsunami (Tyrant Pathway) | Black Hole absorbs the tsunami wave |
| Fire Ravens (Red Priest Pathway) | Black Hole pulls in and destroys fire ravens |
| Meteor (Demoness Pathway) | Black Hole's gravity captures and absorbs a meteor |
| War Banner (Red Priest Pathway) | Black Hole can pull in and destroy a war banner entity |
| Puppet Soldiers (Red Priest Pathway) | Black Hole pulls in puppet soldiers, damaging or destroying them |
| Toxic Smoke (Abyss Pathway) | Black Hole pulls in and condenses toxic smoke, clearing the area |
| Falling blocks (Earthquake, Roar, etc.) | Black Hole absorbs falling block entities from area attacks |

**Distortion Field interactions:**
| Target Ability | Effect |
|---|---|
| Teleportation (Blink, Door, Waypoint) | Distortion Field scrambles teleportation destinations for enemies within |
| Projectile trajectories | Distortion Field warps incoming projectile paths, causing them to miss |
| Lightning (Tyrant Pathway) | Distortion Field can redirect lightning strikes to random locations |
| Divination (Common) | Distortion Field blocks divination attempts targeting entities within |
| Spectating / Telepathy (Visionary) | Distortion Field interferes with mental abilities targeting entities inside |

**Space Tearing interactions:**
| Target Ability | Effect |
|---|---|
| Wall of Light (Sun Pathway) | Space Tearing can cut through a Wall of Light |
| Water Wall (Tyrant Pathway) | Space Tearing can slice through a Water Wall |
| Thread Cocoon (Demoness Pathway) | Space Tearing can rip open a thread cocoon |
| Nightmare realm boundary (Darkness) | Space Tearing can cut an exit out of a Nightmare realm |
| Barrier blocks (any source) | Space Tearing can remove barrier blocks created by abilities |

**Blink / Teleportation interactions:**
| Target Ability | Effect |
|---|---|
| Petrification (Demoness Pathway) | Blink can escape petrification if cast before the effect fully applies |
| Hair Entanglement (Darkness Pathway) | Blink can escape hair entanglement |
| Thread Binding (Demoness Pathway) | Blink can escape thread binding |
| Steel Chains (Red Priest Pathway) | Blink can break free from steel chains |
| Wind Binding (Tyrant Pathway) | Blink can escape wind binding |
| Conquering (Red Priest Pathway) | Blink cannot escape the Conquered debuff — it is not spatial |

**Sealing interactions:**
| Target Ability | Effect |
|---|---|
| Avatar of Desire (Abyss Pathway) | Sealing can suppress the Avatar transformation |
| Mythical Creature Form (Common) | Sealing can force a Beyonder out of their mythical creature form |
| Devil Transformation (Abyss Pathway) | Sealing can lock a devil transformation, preventing activation or forcing reversion |
| Toggle abilities (any pathway) | Sealing can lock an enemy's active toggle ability, preventing them from deactivating it or from activating new ones |
| Parasitation (Error Pathway) | Sealing can contain and remove a parasite |

**Exile interactions:**
| Target Ability | Effect |
|---|---|
| Summoned entities (any pathway) | Exile can banish puppet soldiers, golems, fire ravens, avatars, etc. to another dimension |
| Nightmare realm (Darkness Pathway) | Exile cast within a Nightmare can collapse the realm |
| Toxic Smoke / Disease area (Abyss / Demoness) | Exile can banish a persistent area effect by exiling the air within |

**Recording / Replicating interactions:**
| Target Ability | Effect |
|---|---|
| Any visible ability | Recording can copy any ability cast within its range, storing it for later use |
| Miracle Creation (Fool Pathway) | Recording a miracle allows replaying it |
| Prophecy (WoF Pathway) | Recording a prophecy captures its random outcome for replay |

**Space Concealment interactions:**
| Target Ability | Effect |
|---|---|
| Divination (Common) | Space Concealment blocks divination attempts targeting the concealed area |
| Spirit Vision (Common) | Spirit Vision cannot see through Space Concealment |
| Decryption (Error Pathway) | Decryption can partially penetrate Space Concealment |

---

### 2.6 Darkness Pathway
Darkness abilities create domains of shadow and control that interact with light, fire, and mental effects.

**Night Domain interactions:**
| Target Ability | Effect |
|---|---|
| Sun abilities (all light-based) | Sun abilities are weakened within Night Domain — reduced damage and range |
| Fire-based abilities (Abyss, Red Priest) | Fire provides partial illumination, slightly weakening Night Domain locally |
| Invisibility (Demoness / Visionary) | Night Domain enhances all forms of invisibility within its area |
| Fog of War (Red Priest Pathway) | Night Domain stacks with Fog of War — the combined area has extreme blindness |
| Spirit Vision (Common) | Spirit Vision's detection range is reduced within Night Domain |

**Concealment (dimension) interactions:**
| Target Ability | Effect |
|---|---|
| Pocket Dimension (Door Pathway) | Concealment dimension and Pocket Dimension are separate — entities cannot cross between them |
| Mirror World (Demoness Pathway) | Concealment and Mirror World are separate dimensional layers |
| Divination (Common) | Divination cannot locate entities hidden in the Concealment dimension |
| Exile (Door Pathway) | Exile can pull an entity out of the Concealment dimension |

**Nightmare realm interactions:**
| Target Ability | Effect |
|---|---|
| Dream Traversal (Visionary Pathway) | Dream Traversal can enter an active Nightmare realm |
| Sleep Inducement (Visionary Pathway) | Entities put to sleep inside a Nightmare realm are fully trapped |
| Psychological Invisibility (Visionary) | Psychological Invisibility works within a Nightmare — the caster is mentally hidden |
| Placate (Visionary Pathway) | Placate can weaken a Nightmare realm's hold on a trapped entity |
| Miracle Creation (Fool Pathway) | A miracle can potentially override and collapse a Nightmare |

**Horror Aura interactions:**
| Target Ability | Effect |
|---|---|
| War Cry / War Song (Red Priest Pathway) | War Cry / War Song buffs can partially resist Horror Aura's debuffs |
| Awe (Visionary Pathway) | Horror Aura and Awe stack, creating an overwhelming zone of debuffs |
| Frenzy (Visionary Pathway) | Horror Aura's fear conflicts with Frenzy's rage — one may override the other |

**Midnight Poem (Lullaby) interactions:**
| Target Ability | Effect |
|---|---|
| Sleep Inducement (Visionary Pathway) | Lullaby and Sleep Inducement stack — targets fall asleep faster and the effect is harder to break |
| Nightmare Spectator (Visionary Pathway) | Lullaby puts targets to sleep, making them vulnerable to Nightmare Spectator attacks |
| Siren Song - Dazing (Tyrant Pathway) | Lullaby stacking with Siren Song Daze creates near-total incapacitation |

**Requiem / Hair Entanglement interactions:**
| Target Ability | Effect |
|---|---|
| Petrification (Demoness Pathway) | Pacification stacks with petrification — target is immobilized by both effects |
| Steel Chains (Red Priest Pathway) | Requiem stacking with Steel Chains creates a very long disable |
| Thread Binding (Demoness Pathway) | Requiem stacking with Thread Binding extends the total disable duration |
| Conquering (Red Priest Pathway) | Requiem stacking with Conquering creates layered crowd control |
| Blink (Door Pathway) | Blink can escape Requiem / Hair Entanglement |

---

### 2.7 Tyrant Pathway
Tyrant abilities are elemental forces — lightning, water, wind, and weather — that interact broadly with environmental and elemental abilities.

**Lightning interactions:**
| Target Ability | Effect |
|---|---|
| Water Manipulation (Tyrant Pathway) | Lightning striking a water area deals increased damage to entities in the water |
| Torrential Downpour (Tyrant Pathway) | Lightning during rain has increased chain count |
| Steel Mastery - Steel Skin (Red Priest) | Steel Skin attracts lightning, increasing damage taken from lightning abilities |
| Frost area (Demoness Pathway) | Lightning shatters frozen blocks created by Frost abilities |
| Water Wall (Tyrant Pathway) | Lightning conducted through a Water Wall damages all entities behind it |
| Flooding (Tyrant Pathway) | Lightning in flooded areas chains to all entities standing in water |
| Tsunami (Tyrant Pathway) | Lightning striking a Tsunami electrifies the wave |

**Weather Manipulation interactions:**
| Target Ability | Effect |
|---|---|
| Drought (Red Priest / Tyrant) | A Drought effect counters Torrential Downpour — the two cancel each other |
| Snow Storm (Red Priest / Tyrant) | A Snow Storm combines with Lightning Storm for a devastating blizzard-thunder event |
| Flaring Sun (Sun Pathway) | Flaring Sun during a storm can clear the weather effect |
| Blooming Area (Mother Pathway) | Torrential Downpour enhances Blooming Area's growth effects |
| Disease / Plague (Demoness Pathway) | Rain from Torrential Downpour can spread disease/plague further |

**Tornado interactions:**
| Target Ability | Effect |
|---|---|
| Projectiles (any pathway) | Tornados deflect or absorb projectiles that enter their area |
| Toxic Smoke (Abyss Pathway) | Tornados scatter and disperse toxic smoke clouds |
| Disease area (Demoness Pathway) | Tornados spread disease clouds over a wider area |
| Fire abilities (Abyss / Red Priest / Sun) | Fire abilities can create a fire tornado, combining damage types |
| Poisonous Flame (Abyss Pathway) | A tornado absorbing Poisonous Flame creates a poison fire vortex |

**Earthquake interactions:**
| Target Ability | Effect |
|---|---|
| Structural Collapse (Demoness Pathway) | Earthquake combined with Structural Collapse causes catastrophic terrain destruction |
| Nightmare realm (Darkness Pathway) | Earthquake can destabilise a Nightmare realm's captured terrain |
| Trap (Red Priest Pathway) | Earthquake can trigger buried traps prematurely |
| Pocket Dimension (Door Pathway) | Earthquake in a pocket dimension damages its structure |

**Tsunami interactions:**
| Target Ability | Effect |
|---|---|
| Wall of Light (Sun Pathway) | Tsunami can break through a Wall of Light |
| Frost (Demoness Pathway) | Frost meeting a Tsunami creates a frozen wave barrier |
| Black Hole (Door Pathway) | Black Hole absorbs the tsunami |
| Fire abilities (any pathway) | Tsunami extinguishes fire effects in its path |
| Blooming Area (Mother Pathway) | Tsunami can wash away a Blooming Area |

**Wind Manipulation interactions:**
| Target Ability | Effect |
|---|---|
| Toxic Smoke (Abyss Pathway) | Wind can blow toxic smoke away from allies or toward enemies |
| Disease cloud (Demoness Pathway) | Wind can redirect disease/plague clouds |
| Projectiles (any pathway) | Wind Blade or Wind Boost can deflect lighter projectiles |
| Fog of War (Red Priest Pathway) | Strong wind can partially clear Fog of War |
| Air Bullet (Fool Pathway) | Wind Manipulation can amplify Air Bullet's power and range |

---

### 2.8 Red Priest Pathway
Red Priest abilities focus on war, fire, and command — they interact with other combat and support abilities.

**Fog of War interactions:**
| Target Ability | Effect |
|---|---|
| Night Domain (Darkness Pathway) | Fog of War stacks with Night Domain for total sensory deprivation |
| Sun abilities (Sun Pathway) | Sun light abilities can partially penetrate Fog of War |
| Spirit Vision (Common) | Spirit Vision's range is reduced within Fog of War |
| Spectating / Telepathy (Visionary) | Mental abilities bypass Fog of War — it only blocks physical senses |
| Wind Manipulation (Tyrant Pathway) | Wind can thin Fog of War, reducing its blindness effect |
| Divination (Common) | Divination is blocked within Fog of War |

**War Banner interactions:**
| Target Ability | Effect |
|---|---|
| Horror Aura (Darkness Pathway) | War Banner buffs can partially resist Horror Aura debuffs for allies within range |
| Night Domain (Darkness Pathway) | War Banner buffs counter Night Domain debuffs for nearby allies |
| Awe (Visionary Pathway) | War Banner's morale boost partially resists Awe's suppression |
| Misfortune Field (WoF Pathway) | War Banner cannot counter Misfortune Field — luck is not morale |
| Petrification (Demoness Pathway) | Petrification can freeze the war banner, disabling its buff |

**Fire abilities (Pyrokinesis, Flame Authority, Flame Mastery) interactions:**
| Target Ability | Effect |
|---|---|
| Frost (Demoness Pathway) | Fire and Frost create steam when they clash, briefly obscuring the area |
| Water abilities (Tyrant Pathway) | Fire and Water partially cancel — fire creates steam, water extinguishes fire |
| Ice blocks (Demoness Frost) | Fire melts ice blocks, restoring the original terrain |
| Blooming Area (Mother Pathway) | Fire can burn a Blooming Area, destroying the plant growth |
| Thread Cocoon (Demoness Pathway) | Fire can burn through a thread cocoon |
| Tornado (Tyrant / Demoness) | Fire entering a tornado creates a fire tornado |
| Black Flame (Demoness Pathway) | Black Flame overpowers normal fire — Black Flame wins in a direct clash |
| Toxic Smoke (Abyss Pathway) | Fire igniting toxic smoke causes an explosion |

**Steel Chains interactions:**
| Target Ability | Effect |
|---|---|
| Blink (Door Pathway) | Blink can escape Steel Chains |
| Shapeshifting (Fool Pathway) | Shapeshifting into a smaller form can slip free of Steel Chains |
| Space Tearing (Door Pathway) | Space Tearing can cut through Steel Chains |
| Loophole (Error Pathway) | Loophole can exploit a gap in the chains to break free |
| Lightning (Tyrant Pathway) | Lightning conducted through Steel Chains electrocutes the chained target |

**Conquering interactions:**
| Target Ability | Effect |
|---|---|
| Awe (Visionary Pathway) | Conquering and Awe stack, creating overwhelming suppression |
| Battle Hypnosis (Visionary Pathway) | Conquering + Battle Hypnosis Freeze creates a very long disable |
| Placate (Visionary Pathway) | Placate can soothe a Conquered entity, removing the debuff |
| Holy Oath (Sun Pathway) | Holy Oath's buffs partially resist the Conquered debuff |
| Spiritual Baptism (WoF Pathway) | Spiritual Baptism cleanses the Conquered debuff |

**Puppet Soldier interactions:**
| Target Ability | Effect |
|---|---|
| Charm (Demoness Pathway) | Charm can break puppet soldier loyalty |
| Parasitation (Error Pathway) | Parasitation can hijack puppet soldier control |
| Petrification (Demoness Pathway) | Petrification freezes puppet soldiers |
| Frenzy (Visionary Pathway) | Frenzy on puppet soldiers causes them to attack indiscriminately |
| Instigation (Demoness Pathway) | Instigation can turn puppet soldiers against each other |
| Black Hole (Door Pathway) | Black Hole pulls in and destroys puppet soldiers |
| Exile (Door Pathway) | Exile can banish puppet soldiers to another dimension |

---

### 2.9 Mother Pathway
Mother abilities focus on life, nature, and creation — they interact with corruption, destruction, and elemental effects.

**Healing interactions:**
| Target Ability | Effect |
|---|---|
| Defiling Seed (Abyss Pathway) | Healing can reduce Defiling Seed's damage, but cannot remove the seed itself |
| Poisonous Flame (Abyss Pathway) | Healing counteracts poison damage, but the flame must be separately extinguished |
| Disease / Plague (Demoness Pathway) | Healing reduces the damage tick rate of disease and plague |
| Black Flame (Demoness Pathway) | Healing cannot heal damage while Black Flame is actively burning the target |
| Wither effect (Language of Foulness) | Healing reduces Wither damage but cannot fully negate it |
| Mental Plague (Visionary Pathway) | Healing does not affect Mental Plague — it is a mental affliction, not physical |

**Cleansing interactions:**
| Target Ability | Effect |
|---|---|
| Defiling Seed (Abyss Pathway) | Cleansing can remove a Defiling Seed from the target |
| Curse (Demoness Pathway) | Cleansing can lift a curse from the afflicted entity |
| Disease / Plague (Demoness Pathway) | Cleansing removes disease and plague effects |
| Loosing Control (any source) | Cleansing can calm a target suffering from Loosing Control |
| Conquered debuff (Red Priest Pathway) | Cleansing can remove the Conquered debuff |
| Charm (Demoness Pathway) | Cleansing can break a charm on a target |
| Petrification (Demoness Pathway) | Cleansing cannot remove petrification — it is a physical transformation, not a debuff |
| Mental Plague (Visionary Pathway) | Cleansing can reduce Mental Plague duration |

**Life Aura interactions:**
| Target Ability | Effect |
|---|---|
| Horror Aura (Darkness Pathway) | Life Aura contests Horror Aura — their overlap creates a neutral zone |
| Disease / Plague (Demoness Pathway) | Life Aura slows disease/plague progression within its radius |
| Toxic Smoke (Abyss Pathway) | Life Aura's regeneration counteracts toxic smoke damage |
| Night Domain (Darkness Pathway) | Life Aura continues to function within Night Domain |
| Misfortune Field (WoF Pathway) | Life Aura is not affected by Misfortune Field — healing is not luck-based |

**Blooming Area interactions:**
| Target Ability | Effect |
|---|---|
| Fire abilities (any pathway) | Fire burns a Blooming Area |
| Frost (Demoness Pathway) | Frost kills plant growth in a Blooming Area |
| Torrential Downpour (Tyrant Pathway) | Rain enhances a Blooming Area, increasing its radius and effects |
| Earthquake (Tyrant Pathway) | Earthquake disrupts a Blooming Area's terrain |
| Apocalypse (Demoness Pathway) | Apocalypse completely destroys a Blooming Area |
| Drought (Red Priest / Tyrant) | Drought withers and kills a Blooming Area |

**Golem interactions:**
| Target Ability | Effect |
|---|---|
| Petrification (Demoness Pathway) | Stone golems are partially resistant to petrification since they are already stone-like |
| Parasitation (Error Pathway) | Parasitation can infect and control a golem |
| Charm (Demoness Pathway) | Golems are immune to charm — they lack emotions |
| Black Hole (Door Pathway) | Black Hole can pull in and destroy a golem |
| Lightning (Tyrant Pathway) | Lightning deals normal damage to golems |
| Exile (Door Pathway) | Exile can banish a golem to another dimension |
| Frenzy (Visionary Pathway) | Golems are immune to frenzy — they lack minds |

**Wrath of Nature interactions:**
| Target Ability | Effect |
|---|---|
| Fire abilities (any pathway) | Wrath of Nature can summon rain to extinguish fire effects |
| Drought (Red Priest / Tyrant) | Wrath of Nature directly counters a drought |
| Toxic Smoke (Abyss Pathway) | Wrath of Nature's wind component can disperse toxic smoke |
| Nightmare realm (Darkness) | Wrath of Nature's elemental force can shake a Nightmare realm |

**Crossbreeding / Mutation interactions:**
| Target Ability | Effect |
|---|---|
| Shapeshifting (Fool Pathway) | A crossbred mutation applied to a shapeshifted entity can cause unpredictable results |
| Puppet Soldiers (Red Priest Pathway) | Mutation can be applied to puppet soldiers, enhancing them |
| Golem (Mother Pathway) | Mutation can enhance a golem's abilities |
| Marionettes (Fool Pathway) | Crossbreeding can alter a marionette's physical traits |

---

### 2.10 Wheel of Fortune Pathway
Wheel of Fortune abilities manipulate luck, fate, and misfortune — they interact with probabilistic and debuff systems.

**Blessing / Luck interactions:**
| Target Ability | Effect |
|---|---|
| Curse (Demoness Pathway) | Blessing can counteract a curse — the two effects weaken each other |
| Misfortune Field (WoF Pathway) | Blessing provides immunity to Misfortune Field's unluck effect |
| Prophecy (WoF Pathway) | Higher luck increases the chance of a favourable prophecy outcome |
| Defiling Seed (Abyss Pathway) | Luck reduces the frequency of Defiling Seed's random debuff ticks |
| Trap (Red Priest Pathway) | Lucky entities may avoid triggering traps |
| Calamity Attraction (WoF Pathway) | Blessing's luck can shift Calamity Attraction's random outcome toward less dangerous calamities |

**Misfortune Field / Misfortune Gifting interactions:**
| Target Ability | Effect |
|---|---|
| Holy Oath (Sun Pathway) | Holy Oath partially resists Misfortune — divine protection reduces unluck |
| Blessing (WoF Pathway) | Blessing directly cancels Misfortune Field for the blessed entity |
| War Banner (Red Priest Pathway) | War Banner does not protect against Misfortune — morale cannot fight fate |
| Conquering (Red Priest Pathway) | Misfortune stacking with Conquering weakens a target both in fate and willpower |
| Fog of War (Red Priest Pathway) | Misfortune within Fog of War makes it nearly impossible for enemies to fight effectively |

**Prophecy interactions:**
| Target Ability | Effect |
|---|---|
| Divination (Common) | Prophecy and Divination used together can yield a more detailed fate reading |
| Anti-Divination (Common) | Anti-Divination blocks Prophecy from targeting a protected entity |
| Distortion Field (Door Pathway) | Prophecy is blocked within a Distortion Field |
| Concealment (Darkness Pathway) | Prophecy cannot target entities hidden in the Concealment dimension |
| Decryption (Error Pathway) | Decryption can decode and reveal the full outcome of an enemy's prophecy |

**Spiritual Baptism interactions:**
| Target Ability | Effect |
|---|---|
| Defiling Seed (Abyss Pathway) | Spiritual Baptism can remove a Defiling Seed |
| Curse (Demoness Pathway) | Spiritual Baptism cleanses curses |
| Disease / Plague (Demoness Pathway) | Spiritual Baptism purges all disease and plague effects |
| Loosing Control (any source) | Spiritual Baptism removes Loosing Control and restores sanity |
| Mental Plague (Visionary Pathway) | Spiritual Baptism cleanses Mental Plague |
| Conquered debuff (Red Priest Pathway) | Spiritual Baptism removes the Conquered debuff |
| Charm (Demoness Pathway) | Spiritual Baptism breaks charm effects |
| Petrification (Demoness Pathway) | Spiritual Baptism cannot remove petrification — it requires physical intervention |

**Cycle of Fate interactions:**
| Target Ability | Effect |
|---|---|
| Time Manipulation (Error Pathway) | Time Manipulation can accelerate a Cycle of Fate, triggering it early |
| Miracle Creation (Fool Pathway) | A miracle can alter the outcome of a Cycle of Fate |
| Prophecy (WoF Pathway) | Prophecy can reveal what a Cycle of Fate will do before it triggers |

**Calamity Attraction interactions:**
| Target Ability | Effect |
|---|---|
| Tornado abilities (Tyrant / Red Priest) | A calamity tornado stacks with existing tornado entities |
| Earthquake (Tyrant Pathway) | A calamity earthquake stacks with Tyrant Earthquake for amplified effect |
| Meteor (Demoness Pathway) | A calamity meteor + Demoness Meteor = double impact |
| Night Domain (Darkness Pathway) | Calamities summoned within Night Domain have their visual effects darkened |

---

### 2.11 Fool Pathway
Fool abilities revolve around disguise, puppeteering, fire, and trickery — they interact with detection, control, and identity-based effects.

**Shapeshifting interactions:**
| Target Ability | Effect |
|---|---|
| Decryption (Error Pathway) | Decryption reveals a shapeshifted Beyonder's true identity |
| Spectating / Telepathy (Visionary) | Spectating can reveal that an entity is shapeshifted |
| Spirit Vision (Common) | Spirit Vision can detect that an entity has a Beyonder aura despite appearing as a normal mob |
| Petrification (Demoness Pathway) | Petrification while shapeshifted forces reversion to true form |
| Charm (Demoness Pathway) | Charm on a shapeshifted entity works based on their true form's power level |
| Exile (Door Pathway) | Exile forces a shapeshifted entity to revert |

**Marionette Controlling interactions:**
| Target Ability | Effect |
|---|---|
| Mental Disruption (Error Pathway) | Mental Disruption on the controller severs all marionette links |
| Battle Hypnosis (Visionary Pathway) | Battle Hypnosis (Disable Abilities) on the controller disables marionette control |
| Charm (Demoness Pathway) | Charm on a marionette can contest the marionette link |
| Parasitation (Error Pathway) | Parasitation on a marionette gives the parasite dual control |
| Petrification (Demoness Pathway) | Petrifying the controller does not free marionettes — they simply freeze too |
| Ability Theft (Error Pathway) | Ability Theft can steal control of a marionette |
| Puppeteering (Fool Pathway) | A Fool Beyonder's Puppeteering passive enhances marionette performance |
| Instigation (Demoness Pathway) | Instigation on a marionette cannot override the controller's link |
| Sealing (Door Pathway) | Sealing the controller's abilities severs all marionette links |

**Grafting interactions:**
| Target Ability | Effect |
|---|---|
| Mutation / Crossbreeding (Mother) | Grafting and Mutation applied to the same entity can combine for enhanced modifications |
| Healing (Mother Pathway) | Healing can repair a grafted entity's physical form |
| Cleansing (Mother Pathway) | Cleansing can reverse a harmful graft |

**Paper Figurine Substitute interactions:**
| Target Ability | Effect |
|---|---|
| Black Hole (Door Pathway) | The figurine is consumed by the black hole before teleportation triggers |
| Fire abilities (any pathway) | Fire can destroy paper figurines before they activate |
| Frost (Demoness Pathway) | Frost cannot destroy paper figurines |

**Miracle Creation interactions:**
| Target Ability | Effect |
|---|---|
| Loophole (Error Pathway) | Loophole can find a flaw in a miracle, weakening or cancelling it |
| Anti-Divination (Common) | Anti-Divination cannot block a miracle — miracles transcend divination rules |
| Recording (Door Pathway) | Recording can capture a miracle for replay |
| Cycle of Fate (WoF Pathway) | A miracle can override a Cycle of Fate's outcome |
| Nightmare (Darkness Pathway) | A miracle can collapse a Nightmare realm |
| Distortion Field (Door Pathway) | A miracle is powerful enough to function within a Distortion Field |
| Time Manipulation (Error Pathway) | Time Manipulation cannot affect an active miracle — it is beyond temporal rules |

**Historical Void interactions:**
| Target Ability | Effect |
|---|---|
| Divination (Common) | Hiding in the Historical Void blocks all divination |
| Spectating (Visionary Pathway) | Spectating cannot detect entities in the Historical Void |
| Spirit Vision (Common) | Spirit Vision cannot see into the Historical Void |
| Decryption (Error Pathway) | Decryption cannot decode an entity hidden in the Historical Void |
| Exile (Door Pathway) | Exile cannot reach entities in the Historical Void |

---

### 2.12 Abyss Pathway
Abyss abilities focus on corruption, poison, and dark fire — they interact with purification and protective effects.

**Defiling Seed interactions:**
| Target Ability | Effect |
|---|---|
| Cleansing (Mother Pathway) | Cleansing removes the Defiling Seed |
| Placate (Visionary Pathway) | Placate removes the Defiling Seed |
| Sun abilities (Sun Pathway) | Sun purification can cleanse the Defiling Seed |
| Spiritual Baptism (WoF Pathway) | Spiritual Baptism removes the Defiling Seed |
| Healing (Mother Pathway) | Healing reduces seed damage but cannot remove it |
| Holy Oath (Sun Pathway) | Holy Oath provides partial resistance to the seed's random debuffs |
| Dragon Scales (Visionary Pathway) | Dragon Scales' damage resistance reduces the seed's damage ticks |
| Blessing (WoF Pathway) | Luck from Blessing reduces the frequency of the seed's worst debuff rolls |

**Avatar of Desire interactions:**
| Target Ability | Effect |
|---|---|
| Sun abilities (Sun Pathway) | Sun purification can suppress or cancel the Avatar |
| Sealing (Door Pathway) | Sealing can suppress the Avatar transformation |
| Battle Hypnosis (Visionary Pathway) | Battle Hypnosis can temporarily freeze the Avatar |
| Petrification (Demoness Pathway) | Petrification can petrify the Avatar, halting it |
| Awe (Visionary Pathway) | Awe can suppress the Avatar's Loosing Control aura |
| Holy Oath (Sun Pathway) | Holy Oath grants resistance to the Avatar's Loosing Control effect |
| War Cry (Red Priest Pathway) | War Cry's morale boost can resist the Avatar's control effect |
| Placate (Visionary Pathway) | Placate can remove the Loosing Control caused by the Avatar |

**Toxic Smoke interactions:**
| Target Ability | Effect |
|---|---|
| Wind Manipulation (Tyrant Pathway) | Wind pushes toxic smoke in a chosen direction |
| Tornado (Tyrant / Red Priest / Demoness) | Tornado absorbs and disperses toxic smoke |
| Fire abilities (any pathway) | Intense fire burns away toxic smoke |
| Black Hole (Door Pathway) | Black Hole pulls in and removes toxic smoke |
| Blooming Area (Mother Pathway) | Toxic Smoke kills plant growth within a Blooming Area |
| Life Aura (Mother Pathway) | Life Aura regeneration counteracts toxic smoke damage |

**Poisonous Flame interactions:**
| Target Ability | Effect |
|---|---|
| Sun fire abilities (Sun Pathway) | Sun purifying fire neutralises the poison component of Poisonous Flame |
| Water abilities (Tyrant Pathway) | Water can extinguish Poisonous Flame but leaves poison residue |
| Frost (Demoness Pathway) | Frost can freeze Poisonous Flame, neutralising both fire and poison |
| Healing (Mother Pathway) | Healing counteracts the poison but not the fire damage |
| Wind Manipulation (Tyrant Pathway) | Wind can spread or redirect Poisonous Flame |

**Language of Foulness interactions:**
| Target Ability | Effect |
|---|---|
| Placate (Visionary Pathway) | Placate can resist the Corruption option's Loosing Control |
| Cleansing (Mother Pathway) | Cleansing can remove the Corruption debuffs |
| Holy Song (Sun Pathway) | Holy Song's buffs can counteract Language of Foulness debuffs |
| Spiritual Baptism (WoF Pathway) | Spiritual Baptism cleanses all Language of Foulness effects |
| Battle Hypnosis (Visionary Pathway) | Battle Hypnosis (Disable Abilities) can prevent Language of Foulness from being cast |
| Loophole (Error Pathway) | Loophole can negate a Language of Foulness command |

---

## 3. Cross-Pathway Elemental Clash Table

A summary of how elemental forces interact when abilities from different pathways collide.

| Element A | Element B | Result |
|---|---|---|
| Sun Fire / Holy Light | Darkness / Night Domain | The two cancel in overlapping areas; the Beyonder with the lower (more powerful) sequence number's element dominates |
| Sun Fire / Holy Light | Abyss Corruption | Sun purifies corruption effects |
| Normal Fire (Red Priest / Abyss) | Water (Tyrant) | Steam is created; both effects are weakened |
| Normal Fire (Red Priest / Abyss) | Frost (Demoness) | Steam and partial cancellation |
| Black Flame (Demoness) | Normal Fire (any) | Black Flame overpowers normal fire |
| Black Flame (Demoness) | Water (Tyrant) | Water is less effective against Black Flame — only partially extinguishes |
| Lightning (Tyrant) | Water (Tyrant) | Lightning deals amplified damage in water |
| Lightning (Tyrant) | Steel (Red Priest) | Steel conducts lightning — amplified damage to steel-enhanced targets |
| Wind (Tyrant) | Fire (any) | Wind spreads fire or creates fire tornado |
| Wind (Tyrant) | Poison / Toxic (Abyss) | Wind disperses or redirects poison clouds |
| Nature / Life (Mother) | Corruption (Abyss) | Life energy resists corruption; both are weakened |
| Nature / Life (Mother) | Fire (any) | Fire destroys plant growth |
| Nature / Life (Mother) | Water (Tyrant) | Water enhances plant growth |
| Luck (WoF) | Misfortune (WoF / Error theft) | Luck and Misfortune cancel each other |
| Mental (Visionary) | Physical concealment (Demoness) | Mental effects bypass physical concealment |
| Spatial (Door) | Temporal (Error) | Space abilities within a time-manipulated zone have their range scaled by the local time factor; Time abilities within a space-distorted zone have their duration scaled by the distortion intensity |

---

## 4. Crowd Control Stacking Rules

When multiple crowd control abilities are applied to the same target, the following rules apply:

| CC Type A | CC Type B | Stacking Behaviour |
|---|---|---|
| Petrification (Demoness) | Any other CC | Petrification takes priority — target is fully immobilized |
| Sleep (Visionary / Darkness) | Freeze (Visionary / Demoness) | The more recent effect takes priority; the earlier effect is suspended and resumes with its remaining duration once the newer effect expires |
| Requiem / Hair Entanglement (Darkness) | Thread Binding (Demoness) | Durations stack — total disable time is combined |
| Steel Chains (Red Priest) | Thread Binding (Demoness) | Durations stack — total disable time is combined |
| Conquering (Red Priest) | Awe (Visionary) | Debuffs stack — the target is severely weakened |
| Charm (Demoness) | Frenzy (Visionary) | The two conflict — the Beyonder with the lower (more powerful) sequence number's effect prevails |
| Loosing Control (any source) | Placate (Visionary / WoF) | Placate removes Loosing Control |
| Any CC | Blink (Door) | Blink can escape most physical CC (not mental debuffs) |
| Any CC | Loophole (Error) | Loophole can find an exploit to escape any single CC effect |
| Any CC | Shapeshifting (Fool) | Shapeshifting into a different form can shed some physical CC |

---

## 5. Purification & Cleansing Hierarchy

Multiple abilities can purify or cleanse debuffs. Their effectiveness varies:

| Purification Source | Can Remove |
|---|---|
| Spiritual Baptism (WoF) | All debuffs, curses, disease, plague, Defiling Seed, Loosing Control, Mental Plague, Conquered |
| Cleansing (Mother) | Defiling Seed, Curse, Disease, Plague, Loosing Control, Conquered, Charm, Mental Plague (reduced) |
| Placate (Visionary) | Defiling Seed, Curse, Loosing Control, Frenzy |
| Sun Purification (various) | Defiling Seed, Curse, Disease, Plague, Invisibility (Demoness), Darkness effects, Mental Plague |
| Healing (Mother) | Reduces damage from seeds/disease/plague but cannot remove the source effect |
| Holy Oath (Sun) | Provides resistance (reduced duration/effectiveness) to curses, Conquered, Defiling Seed |
| Blessing (WoF) | Provides luck-based resistance to random debuff effects; counters curses |

---

## 6. Summoned Entity Vulnerability Table

How summoned or created entities from various pathways interact with removal/control abilities:

| Summoned Entity | Petrification | Charm | Parasitation | Black Hole | Exile | Ability Theft |
|---|---|---|---|---|---|---|
| Puppet Soldiers (Red Priest) | Frozen | Loyalty broken | Control hijacked | Destroyed | Banished | Control stolen |
| Marionettes (Fool) | Frozen (controller also frozen) | Contested | Dual control | Destroyed | Banished | Control stolen |
| Golem (Mother) | Partially resistant | Immune | Infected | Destroyed | Banished | N/A |
| Fire Ravens (Red Priest) | Frozen mid-flight | N/A (too simple) | N/A | Destroyed | Banished | N/A |
| Avatar Entity (Visionary) | Petrified | Contested | Compromised | Destroyed | Banished | N/A |
| War Banner (Red Priest) | Frozen (buff nullified) | N/A | N/A | Destroyed | Banished | Buff stolen |
| Tornado entities (any) | Frozen | N/A | N/A | Absorbed | Banished | N/A |
| Tsunami (Tyrant) | Turned to stone | N/A | N/A | Absorbed | N/A | N/A |
| Meteor (Demoness) | Turned to stone | N/A | N/A | Absorbed | N/A | N/A |
| Black Hole (Door) | Halted and removed | N/A | N/A | N/A | Collapsed | N/A |
| Words of Misfortune (WoF) | Frozen | N/A | N/A | Destroyed | Banished | Debuff stolen |

---
