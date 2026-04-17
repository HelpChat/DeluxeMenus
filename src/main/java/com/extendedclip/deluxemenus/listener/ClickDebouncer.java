package com.extendedclip.deluxemenus.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

final class ClickDebouncer {

    private static final long CLICK_DEBOUNCE_MILLIS = 75L;
    private static final long SHIFT_CLICK_DEBOUNCE_MILLIS = 200L;

    private final Cache<UUID, Long> clickDebounceCache = CacheBuilder.newBuilder()
            .expireAfterWrite(CLICK_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
            .build();

    private final Cache<UUID, Long> shiftClickDebounceCache = CacheBuilder.newBuilder()
            .expireAfterWrite(SHIFT_CLICK_DEBOUNCE_MILLIS, TimeUnit.MILLISECONDS)
            .build();

    boolean shouldIgnoreClick(@NotNull final Player player) {
        final UUID uniqueId = player.getUniqueId();
        return clickDebounceCache.getIfPresent(uniqueId) != null
                || shiftClickDebounceCache.getIfPresent(uniqueId) != null;
    }

    void markClick(@NotNull final Player player) {
        clickDebounceCache.put(player.getUniqueId(), System.currentTimeMillis());
    }

    void markShiftClick(@NotNull final Player player) {
        shiftClickDebounceCache.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
