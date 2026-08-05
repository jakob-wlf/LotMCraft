# Sefirot Systems

This document is the canonical reference for Sefirot ownership, rituals, authority, dimensions, and Great Old One transformation.

## Implementation Status

| Sefirot ID | Domain pathways | Dimension entry | Accommodation/claim ritual | Special authority |
|---|---|---:|---:|---|
| `sefirah_castle` | Fool, Error, Door | Implemented | Implemented | Gatherings, Grey Fog, divination protection |
| `river_of_eternal_darkness` | Darkness, Death, Twilight Giant | Implemented | Implemented | Blessings, concealment, audience, River systems |
| `chaos_sea` | Sun, Tyrant, Visionary, Hanged Man, White Tower | Implemented | Implemented | Blessed/gathering access and Chaos Sea authority UI |
| `key_of_light` | Wheel of Fortune | Implemented | Implemented | Probability Manipulation, fate/divination protection |
| `brood_hive` | Mother, Moon | Dimension route present | Not implemented | Generic authority mapping only |
| `city_of_calamity` | Red Priest, Demoness | Dimension route present | Not implemented | Generic authority mapping only |
| `nation_of_disorder` | Black Emperor, Justiciar | Dimension route present | Not implemented | Generic authority mapping only |
| `tenebrous_world` | Abyss, Chained | Dimension route present | Not implemented | Generic authority mapping only |
| `knowledge_moor` | Hermit, Paragon | Dimension route present | Not implemented | Generic authority mapping only |

A dimension route does not mean the Sefirot has a survival acquisition path. The five entries marked "Not implemented" have no accommodation ritual in their event handlers.

## Ownership

`SefirotData` is world SavedData stored through the Overworld data storage. It enforces:

- One Sefirot per player.
- One current owner per Sefirot.
- Current holder lookup.
- First-ever owner tracking.
- Mental imprint progress and reclaim state.
- Return locations and whether a player is currently inside a Sefirot.
- The generated Key of Light shrine position.

`SefirahHandler` owns claim, unclaim, transfer, authority ability assignment, and dimension teleportation. Claiming grants `sefirot_authority_ability`; unclaiming removes it and clears authority state, River blessings, audiences, and Sefirot-owned ability seals.

## Entry and Exit

The default key is **U**. `TeleportToSefirotPacket` validates ownership before calling `SefirahHandler.teleportToSefirot`.

On entry, the previous dimension and coordinates are stored. Pressing the key again returns the owner to that location, with an Overworld-spawn fallback. Implemented routes include all nine registered Sefirot dimensions, although only four have survival claim rituals.

Active invasions may authorize non-owner entry through `SefrotInvasionManager`. Individual Sefirot add their own audience or gathering exceptions.

## Shared Authority Ability

Every owner receives the Sefirot Authority ability. Its screen lists eligible abilities from neighboring pathways and lets the owner enable or disable them.

The owner's own pathway is excluded from borrowed-path lists. These common abilities are never offered:

- Angel Authority
- Spirit Vision
- Ally
- Cogitation
- Mythical Creature Form
- Divination

### Sequence Scaling

| Owner Sequence | Borrowed abilities |
|---:|---|
| 3-9 | None |
| 2 | Neighboring pathway abilities available at Sequence 4 or weaker |
| 1 | Neighboring pathway abilities available at Sequence 2 or weaker |
| 0 | Neighboring pathway abilities available at Sequence 1 or weaker |
| Great Old One (-1) | All neighboring pathway abilities, including Sequence 0 |

Selections persist on the player attachment. Invalid selections are removed after Sequence or ownership changes.

## Mental Imprint

The first owner of a Sefirot is permanently recorded. Mental imprint grows for that owner and can burden later owners. `SefirotImprintEventHandler` manages online/offline progression, ownership-change corruption, death handling, and pending reclaim behavior. The current imprint percentage appears in authority screens.

## Accommodation Rituals

### Sefirah Castle

- **Eligible pathways:** Fool, Error, Door.
- **Trigger:** Right-click a synchronized Mysterious Tablet.
- **Duration:** 5 minutes (6,000 ticks).
- **Start:** The tablet is consumed and a moving sky beam begins.
- **During ritual:** Ability use is disabled, particles use all three domain colors, progress is synchronized, and Spirit World travel is globally locked.
- **Interrupted by:** Invalid eligibility/ownership state, death, or logout. The tablet is returned/dropped.
- **Success:** Claims the Castle and teleports the owner into its dimension.
- **Dimension rules:** Griefing is disabled. Non-owners have ability use suppressed.
- **Authority UI:** Gatherings and Grey Fog actions.

### River of Eternal Darkness

- **Eligible pathways:** Darkness, Death, Twilight Giant.
- **Trigger:** Drink a Bottle of Eternal Darkness River Water in the Overworld.
- **Duration:** 5 minutes (6,000 ticks), starting at 0%.
- **During ritual:** A River sky beam and domain-color particles appear, progress is synchronized, and Spirit World travel is globally locked.
- **Interrupted by:** Invalid eligibility/ownership state, leaving the Overworld, death, or logout. The bottle is returned/dropped.
- **Success:** Claims the River. The ritual does not automatically use the common teleport call at completion.
- **Dimension access:** Owner, trapped River's Call victims, invited audience, and authorized invaders.
- **Environment:** Dark water deals 5 damage and 0.05 sanity loss per second to unprotected entities.

#### River Blessings

Blessing designations persist. Only the first Sequence-limited slots receive active effects:

| Owner Sequence | Active blessing slots |
|---:|---:|
| 0 or stronger | 3 |
| 1 | 2 |
| 2 | 1 |
| 3-9 | 0 |

Active blessings provide River-specific protections, including sleep immunity. The owner is also immune to sleep. River systems additionally include audience management, concealment checks, death imprints, and River's Call.

### Chaos Sea

- **Eligible pathways:** Tyrant, Sun, Visionary, White Tower, Hanged Man.
- **Trigger:** Right-click a synchronized Blasphemy Slate.
- **Duration:** 5 minutes (6,000 ticks).
- **Start:** The slate is consumed and a sky beam begins.
- **During ritual:** Sun/Tyrant/Visionary particle colors are used, progress is synchronized, and Spirit World travel is globally locked.
- **Interrupted by:** Invalid eligibility/ownership state, death, or logout. The slate is returned/dropped.
- **Success:** Claims the Chaos Sea and teleports the owner into its dimension.
- **Dimension access:** Owner, authorized invaders, and gathering members associated with the owner. Nature-dimension entry is explicitly rejected.

### Key of Light

The Key of Light uses alignment rather than the five-minute item ritual.

- **Temple:** Generated in the Overworld on server start, up to 5,000 blocks along one cardinal direction from spawn, preferring high terrain. Its position is persisted.
- **Trigger:** Interact with the protected Key of Light statue.
- **Eligible pathway:** Wheel of Fortune only.
- **Duration:** 10 minutes (12,000 ticks).
- **Boundary:** The player must remain within a 50-block horizontal radius. A world-height ring marks the active area and refreshes throughout alignment.
- **Interruption:** Leaving the boundary or Overworld, losing Wheel eligibility, gaining another Sefirot, the Key of Light becoming claimed, death, or logout cancels alignment without rolling for success.
- **Awareness:** Other online Wheel of Fortune Beyonders are notified at the start and every 10% of progress with the temple's direction and distance.
- **Intruders:** Once per second, each non-Wheel Beyonder inside the horizontal boundary gains 0.05 corruption. Non-Beyonders and Wheel of Fortune Beyonders are unaffected.
- **Completed failure:** Finishing the 10 minutes and failing the Sequence-based roll applies maximum misfortune. Non-Wheel interaction with the statue also applies maximum misfortune immediately.

Success chance is based on the aligning player's Sequence:

| Sequence | Success chance |
|---:|---:|
| 0 | 100% |
| 1 | 85% |
| 2 | 65% |
| 3 | 40% |
| 4 | 20% |
| 5 | 10% |
| 6 | 5% |
| 7 | 2% |
| 8 | 0.5% |
| 9 | 0.1% |

The temple footprint is protected from block breaking, placement, and explosions.
Active alignments are held in server memory and are not resumed after a server restart.

#### Probability Manipulation

The Key of Light authority screen has a top-right Probability Manipulation button. The owner can select abilities and assign a global failure chance. Rules persist as world SavedData and are inactive while Key of Light has no owner.

| Owner Sequence | Ability limit | Allowed failure chance |
|---:|---:|---:|
| 0 | 5 | 25-75% |
| 1 | 3 | 15-50% |
| 2 | 2 | 10-35% |
| 3 | 2 | 5-25% |
| 4 | 1 | 3-15% |
| 5 | 1 | 2-10% |
| 6-9 | 1 | 1-5% |

The current owner's Sequence is enforced dynamically. Excess rules are removed and remaining chances are clamped if ownership or Sequence changes.

Sequence 0 abilities are unavailable as probability targets until the Key of Light owner reaches Sequence 0 (Great Old Ones also qualify). Hidden abilities, Cogitation, Ally, Divination, Spirit Vision, Mythical Creature Form, and all authority abilities are blacklisted. Invalid persisted rules are removed automatically.

A rejected cast still:

- consumes spirituality,
- applies sanity-shortfall payment,
- gains normal digestion,
- consumes copied uses,
- enters cooldown.

Only execution, Connection propagation, client use animation, tracking, and the post-use event are suppressed. Sefirot Authority itself cannot be selected or rejected.

## Passive Protections

`SefirotAuthorityManager` refreshes passive sets approximately once per second and on ownership changes.

- Castle, River, and Key of Light owners receive Sefirot divination protection.
- Inside the Castle dimension, owner divination protection is absolute.
- Outside it, a diviner must be at least four Sequences stronger to pierce the standard owner protection.
- River authority also marks passive concealment state.
- Elevated River concealment blocks applicable targeting by casters without a Sefirot.
- Sefirot dimensions are protected targeting contexts for Envisioning checks.

## Great Old One Transcendence

Implemented Great Old One forms:

| Sefirot | Form |
|---|---|
| Sefirah Castle | Lord of Mysteries |
| River of Eternal Darkness | Eternal Darkness |
| Chaos Sea | God Almighty |
| Key of Light | Key of Light |

Requirements:

1. Own one of the four eligible Sefirot above.
2. Be Sequence 0 of the current pathway.
3. Hold one Sequence 1 characteristic from every other pathway in that Sefirot's domain.

The Introspect screen exposes the transcendence action when locally eligible. The ritual lasts **10 minutes (12,000 ticks)**. `ApotheosisTickHandler` manages progression and observer effects; completion sets the special Great Old One Sequence value (-1).

## Commands

- `/sefirot check <sefirot>`: inspect ownership.
- `/sefirot set <player> <sefirot>`: assign ownership.
- `/sefirot clear <sefirot>`: clear ownership.
- `/locate structure key_of_light_temple`: locate the generated Key of Light temple.
- `/greatoldone`: administrative transformation tools.
- `/fragment`, `/blasphemy`, and `/imprint`: related ritual/data administration.

See [COMMANDS.md](COMMANDS.md) for command syntax and permissions.

## Developer Entry Points

| Concern | Class |
|---|---|
| Ownership persistence | `attachments/SefirotData.java` |
| Claim, transfer, entry/exit | `beyonders/sefirah/SefirahHandler.java` |
| Borrowed abilities and protections | `beyonders/sefirah/SefirotAuthorityManager.java` |
| Ritual handlers | `beyonders/sefirah/*EventHandler.java` |
| Probability rules | `beyonders/sefirah/ProbabilityManipulationManager.java` |
| GOO requirements and transformation | `beyonders/sefirah/GreatOldOneManager.java` |
| Transcendence ticking | `events/ApotheosisTickHandler.java` |
| Ritual HUD sync | `SyncSefirotAccommodationPacket`, `ClientAccommodationCache`, `HudProgressBarsRenderer` |

When adding a Sefirot, implement all of these explicitly: ownership ID, domain pathways, dimension route, acquisition ritual, interruption cleanup, authority UI behavior, passive protections, and optional GOO mapping. Do not treat a dimension constant or empty event handler as a complete implementation.
