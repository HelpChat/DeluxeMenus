package com.extendedclip.deluxemenus.cooldown;

import com.extendedclip.deluxemenus.DeluxeMenus;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory, per player cooldown store.
 * <p>
 * Cooldowns tracked here are <b>ephemeral</b>: they are never written to disk and are lost when the
 * plugin is disabled or the server stops. They do survive a {@code /dm reload}. Anyone who needs a
 * cooldown to outlive a restart should use a dedicated cooldown plugin or temporary permissions.
 * <p>
 * Every method is safe to call from any thread. Requirements are evaluated off the main thread when
 * a menu is opened or refreshed, actions run on the main thread, and PlaceholderAPI may request a
 * value from either, so the backing maps are concurrent.
 */
public class EphemeralCooldownManager {

    /**
     * How often the sweep task runs, in ticks.
     */
    private static final long SWEEP_INTERVAL = 20L * 300L;

    private final DeluxeMenus plugin;

    /**
     * Player uuid -> cooldown id -> epoch millis at which the cooldown ends.
     */
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public EphemeralCooldownManager(final @NotNull DeluxeMenus plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts a cooldown for a player, replacing any cooldown already running under the same id.
     * <p>
     * A duration of zero or less clears the cooldown instead of storing it.
     *
     * @param uuid           the player the cooldown belongs to
     * @param id             the cooldown id, case insensitive
     * @param durationMillis how long the cooldown should last, in milliseconds
     */
    public void set(final @NotNull UUID uuid, final @NotNull String id, final long durationMillis) {
        final String key = normalize(id);

        if (durationMillis <= 0) {
            clear(uuid, key);
            return;
        }

        cooldowns.computeIfAbsent(uuid, ignored -> new ConcurrentHashMap<>())
                .put(key, System.currentTimeMillis() + durationMillis);
    }

    /**
     * @return true if the player currently has an unexpired cooldown under this id
     */
    public boolean isOnCooldown(final @NotNull UUID uuid, final @NotNull String id) {
        return getRemainingMillis(uuid, id) > 0;
    }

    /**
     * @return the milliseconds left on this cooldown, or 0 if there is none or it has expired
     */
    public long getRemainingMillis(final @NotNull UUID uuid, final @NotNull String id) {
        final Map<String, Long> playerCooldowns = cooldowns.get(uuid);

        if (playerCooldowns == null) {
            return 0L;
        }

        final Long expiry = playerCooldowns.get(normalize(id));

        if (expiry == null) {
            return 0L;
        }

        final long remaining = expiry - System.currentTimeMillis();

        if (remaining <= 0) {
            // Expire lazily so the sweep task is only ever a memory reclaim, never correctness.
            clear(uuid, id);
            return 0L;
        }

        return remaining;
    }

    /**
     * Removes a single cooldown from a player.
     */
    public void clear(final @NotNull UUID uuid, final @NotNull String id) {
        final String key = normalize(id);

        cooldowns.computeIfPresent(uuid, (ignored, playerCooldowns) -> {
            playerCooldowns.remove(key);
            return playerCooldowns.isEmpty() ? null : playerCooldowns;
        });
    }

    /**
     * Removes every cooldown belonging to a player.
     */
    public void clearAll(final @NotNull UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Removes every cooldown of every player.
     */
    public void clearAll() {
        cooldowns.clear();
    }

    /**
     * Schedules the task that drops expired entries. Cancelled along with every other plugin task in
     * {@link DeluxeMenus#onDisable()}.
     */
    public void startSweepTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::sweep, SWEEP_INTERVAL, SWEEP_INTERVAL);
    }

    private void sweep() {
        final long now = System.currentTimeMillis();

        cooldowns.entrySet().removeIf(entry -> {
            entry.getValue().values().removeIf(expiry -> expiry <= now);
            return entry.getValue().isEmpty();
        });
    }

    private @NotNull String normalize(final @NotNull String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }
}
