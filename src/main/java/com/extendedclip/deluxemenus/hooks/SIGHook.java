package com.extendedclip.deluxemenus.hooks;

import com.extendedclip.deluxemenus.cache.SimpleCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ua.valeriishymchuk.simpleitemgenerator.api.SimpleItemGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SIGHook implements ItemHook, SimpleCache {


    private final Map<String, ItemStack> cache = new ConcurrentHashMap<>();

    @Override
    public void clearCache() {
        cache.clear();
    }

    @Override
    public ItemStack getItem(Player holder, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return errorItem("Item arguments are absent.");
        }

        final ItemStack item = cache.computeIfAbsent(arguments[0], id -> {
            return SimpleItemGenerator.get().bakeItem(id, holder).orElse(null);
        });
        return item == null? errorItem("Item %s wasn't found by SIG.", arguments[0]) : item.clone();
    }

    private ItemStack errorItem(String error, Object... args) {
        final ItemStack item = new ItemStack(Material.STONE, 1);
        final ItemMeta meta = item.getItemMeta();
        final Component errorText = MiniMessage.miniMessage()
                .deserialize("<red>" + String.format(error, args) + "</red>");
        // I can't use components, because for some reason Spigot 1.21.4 still doesn't have methods for it, lol
        meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(errorText));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean itemMatchesIdentifiers(@NotNull ItemStack item, @NotNull String... arguments) {
        if (arguments.length == 0) {
            return false;
        }
        return SimpleItemGenerator.get().getCustomItemKey(item)
                .map(s -> arguments[0].equals(s))
                .orElse(false);
    }

    @Override
    public String getPrefix() {
        return "sig-";
    }
}
