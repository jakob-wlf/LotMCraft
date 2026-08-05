# LOTMCraft Commands

Commands are divided into two access tiers:
- **Player** — available to any connected player.
- **OP** — requires permission level 2 (operator).

Hidden internal commands (prefixed `lotm_`) are triggered automatically by clickable chat messages and are not intended for direct use.

---

## Player Commands

---

### `/ally`

Manage your ally list. Allies are not harmed by your own abilities.

| Subcommand | Description |
|---|---|
| `/ally remove <player>` | Removes a player from your ally list. If they are online, removes the alliance on both sides immediately. If offline, clears your side only (their side is cleaned up on next login). |
| `/ally list` | Displays all current allies, with online players shown in green and offline players in grey. |

---

### `/rteam`

Manage a Red Priest subordinate team. Only **Red Priest Beyonders at Sequence 3 or stronger** can lead a team. Max team size scales with the leader's sequence.

| Subcommand | Description |
|---|---|
| `/rteam add <player>` | Sends a team invite to a player. If they have already invited you, both sides are joined automatically. Invite expires after 30 seconds. |
| `/rteam remove <player>` | Removes a member from your team (leader only). |
| `/rteam leave` | Leave the team you are currently a member of. |
| `/rteam disband` | Disband your entire team, removing all members (leader only). |

---

## OP Commands

---

### `/beyonder <pathway> <sequence> [target]`

Sets a living entity to the specified pathway and sequence. Requires OP level 2.

- Without a `target`, applies to the command executor.
- With a `target`, applies to the specified entity.
- Will fail if the target player does not have enough characteristics to advance to that sequence.
- Every successful use is recorded in the audit log (see `/setbeyonderlog`).

**Example:** `/beyonder visionary 5 Jakob`

---

### `/beyondermap`

Debug/maintenance tool for the server-side BeyonderMap database. Requires OP level 2.

| Subcommand | Description |
|---|---|
| `/beyondermap help` | Shows available subcommands. |
| `/beyondermap all` | Lists all stored players sorted by sequence with short info. |
| `/beyondermap add <target>` | Adds a Beyonder entity to the map. |
| `/beyondermap delete <target>` | Removes a player from the map. |
| `/beyondermap delete all` | Wipes the entire database. |
| `/beyondermap get <target>` | Prints full stored info for a player. |
| `/beyondermap edit <json>` | Edits a player's stored pathway/sequence directly. JSON format: `{"name":"PlayerName","path":"pathway","seq":5}` |

---

### `/characteristicstack`

Manage the characteristic stack for a Beyonder entity. The stack tracks how many characteristics a player has consumed at each sequence, gating their ability to advance. Requires OP level 2.

| Subcommand | Description |
|---|---|
| `/characteristicstack set <target> <seq> <stack>` | Sets the characteristic stack count for the given sequence (0–9) on the target. |
| `/characteristicstack delete <target> <seq>` | Zeroes the stack for a specific sequence. |
| `/characteristicstack delete <target> all` | Clears all stack data for the target. |
| `/characteristicstack delete <target> modifiers` | Removes all applied characteristic stack damage modifiers. |
| `/characteristicstack recalculate <target>` | Recalculates and reapplies all modifier bonuses from the current stack. |

---

### `/digest <target> <amount>`

Sets a player's digestion progress to the given value (0.0–1.0). Digestion represents how fully a Beyonder has processed their current potion. At 0%, the Sun pathway can begin regressing sequences. Requires OP level 2.

---

### `/disableability <ability_id>`

Globally disables a specific ability by ID for all players on the server. Tab-completion lists all registered ability IDs. Fails if the ability is already disabled. Requires OP level 2.

---

### `/enableability <ability_id>`

Re-enables a globally disabled ability. Fails if the ability is not currently disabled. Requires OP level 2.

---

### `/killcount <target> <amount>`

Sets a player's kill count to the specified value. Kill count is used by the Red Priest `Sacrifice` ability (requires 500 kills to activate). Requires OP level 2.

---

### `/luck <player> [value]`

Reads or sets a player's current Luck. Requires OP level 2.

- `/luck <player>` displays the current net Luck value.
- `/luck <player> <value>` sets Luck to the requested value, clamped to the player's cap.
- Wheel of Fortune players use their pathway's larger sequence capacity.
- Other Beyonders use a separate lower cap: Seq 9 **100**, Seq 8 **125**, Seq 7 **150**, Seq 6 **200**, Seq 5 **250**, Seq 4 **400**, Seq 3 **600**, Seq 2 **900**, Seq 1 **1200**, and Seq 0 **2000**.
- Non-Beyonders have a maximum positive Luck of **100**. All entities have a minimum Luck of **-10000**.

See [LUCK_SYSTEM.md](LUCK_SYSTEM.md) for capacities, regeneration, resistance, timed effects, and developer APIs.

---

### `/quest`

Manage active quests for players. Requires OP level 2.

| Subcommand | Description |
|---|---|
| `/quest give <target> <quest_id>` | Assigns the specified quest to the player. Tab-completion lists all registered quest IDs. |
| `/quest discard [target]` | Discards the player's currently active quest. Defaults to the command executor if no target is specified. |
| `/quest finish [target]` | Immediately completes the player's currently active quest and grants its rewards. Defaults to self. |

---

### `/sanity <target> <amount>`

Sets the sanity of a living entity to the given value (0.0–1.0). Sanity affects ability effectiveness and triggers Losing Control at low values. Requires OP level 2.

---

### `/sefirot`

Manages Sefirot ownership, mental imprints, and convergence. Requires OP level 2. All nine Sefirot IDs are accepted for administration and dimension routing; survival accommodation rituals currently exist for `sefirah_castle`, `river_of_eternal_darkness`, `chaos_sea`, and `key_of_light`.

| Subcommand | Description |
|---|---|
| `/sefirot check <sefirot>` | Looks up which player has claimed the given sefirot and prints their full Beyonder info. |
| `/sefirot set <player> <sefirot>` | Assigns a sefirot to a player. Use `empty` to unclaim. One sefirot can only be held by one player at a time. If the player is offline, the assignment applies on next login. |
| `/sefirot clear <sefirot>` | Clears all ownership state for the selected Sefirot. |
| `/sefirot imprint check <sefirot>` | Shows the first owner, current holder, and mental imprint percentage. |
| `/sefirot imprint set <sefirot> <0-100>` | Sets the mental imprint percentage directly. |
| `/sefirot convergence` | Manually attempts convergence using a matching Sefirot piece held by the executor. |

Owners use the U key to enter their Sefirot dimension and return to their saved location. Use `/locate structure key_of_light_temple` to locate the generated Key of Light temple. See [SEFIROT_SYSTEMS.md](SEFIROT_SYSTEMS.md) for rituals, authority scaling, dimensions, and Great Old One requirements.

---

### `/setbeyonderlog [count]`

Prints recent `/beyonder` command usage from the audit log. Defaults to the last 20 entries; up to 100 can be shown. Each entry records the executor, target player, pathway, sequence, full command, and timestamp. Requires OP level 2.

---

### `/uniqueness`

Manage pathway Uniqueness entities. Each pathway has at most one Uniqueness in the world at a time, which can be picked up by players. Requires OP level 2.

| Subcommand | Description |
|---|---|
| `/uniqueness check <pathway>` | Reports whether the pathway's Uniqueness is currently in the world, which player holds it, or if it does not exist. |
| `/uniqueness remove <pathway>` | Removes the Uniqueness from whatever player currently holds it. |
| `/uniqueness spawn <pathway>` | Spawns a Uniqueness entity for the pathway at the executor's feet (2 blocks below). Fails silently if one already exists in the world, a player already holds it, or a Sequence 0 player of that pathway exists. |

---

## Internal Commands (Hidden)

These commands are not for direct use — they are triggered automatically by clickable buttons in chat messages.

| Command | Trigger |
|---|---|
| `/lotm_accept_ally <uuid>` | Clicking "Accept" on an ally request message |
| `/lotm_deny_ally <uuid>` | Clicking "Deny" on an ally request message |
| `/lotm_accept_team <uuid>` | Clicking "Accept" on a team invite message |
| `/lotm_decline_team <uuid>` | Clicking "Decline" on a team invite message |
