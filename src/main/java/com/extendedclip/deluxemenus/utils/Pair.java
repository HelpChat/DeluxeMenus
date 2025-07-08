package com.extendedclip.deluxemenus.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Pair<K, V> {

    private final K key;
    private final V value;

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

    public @NotNull K getKey() {
        return key;
    }

    public @Nullable V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Pair{" + "key=" + key + ", value=" + key + '}';
    }
}
