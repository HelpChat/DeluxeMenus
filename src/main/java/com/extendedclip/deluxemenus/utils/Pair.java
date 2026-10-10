package com.extendedclip.deluxemenus.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record Pair<K, V>(K key, V value) {

    public static <K, V> Pair<K, V> of(@NotNull final K key, @Nullable final V value) {
        return new Pair<>(key, value);
    }

    public static <K, V> Pair<K, V> of(@NotNull final K key) {
        return new Pair<>(key, null);
    }

    public Pair(@NotNull final K key, @Nullable final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public @NotNull K key() {
        return key;
    }

    @Override
    public @Nullable V value() {
        return value;
    }

    @Override
    public @NonNull String toString() {
        return "Pair{" + "key=" + key + ", value=" + key + '}';
    }
}
