package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.persistentmeta.DataAction;
import com.extendedclip.deluxemenus.persistentmeta.DataType;
import com.extendedclip.deluxemenus.persistentmeta.PersistentMetaHandler;
import com.extendedclip.deluxemenus.utils.Messages;
import com.extendedclip.deluxemenus.utils.PaginationUtils;
import com.extendedclip.deluxemenus.utils.StringUtils;
import com.extendedclip.deluxemenus.utils.VersionHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class MetaCommand extends SubCommand {

    private static final List<String> SUB_COMMANDS = List.of("list", "show", "set", "remove", "add", "subtract", "switch");

    public MetaCommand(@NotNull final DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "meta";
    }

    @Override
    public void execute(@NotNull final CommandSender sender, @NotNull final List<String> arguments) {
        if (!sender.isOp()) {
            plugin.sms(sender, Messages.NO_PERMISSION);
            return;
        }

        if (!VersionHelper.IS_PDC_VERSION || plugin.getPersistentMetaHandler() == null) {
            plugin.sms(sender, Messages.META_NOT_SUPPORTED);
            return;
        }

        if (arguments.size() < 2) {
            sendWrongUsageMessage(sender, null);
            return;
        }

        final Player target = Bukkit.getPlayerExact(arguments.get(0));

        if (target == null) {
            plugin.sms(sender, Messages.PLAYER_IS_NOT_ONLINE.message().replaceText(PLAYER_REPLACER_BUILDER.replacement(arguments.get(0)).build()));
            return;
        }

        final String actionName = StringUtils.replacePlaceholders(arguments.get(1), target);
        final DataAction action = DataAction.getActionByName(actionName);

        if (action == null) {
            if (actionName.equalsIgnoreCase("list")) {
                handleListMeta(sender, target, arguments.subList(2, arguments.size()));
                return;
            }

            if (actionName.equalsIgnoreCase("show")) {
                handleShowMeta(sender, target, arguments.subList(2, arguments.size()));
                return;
            }

            sendWrongUsageMessage(sender, null);
            return;
        }

        if (arguments.size() < 3) {
            sendWrongUsageMessage(sender, action);
            return;
        }

        final String keyName = StringUtils.replacePlaceholders(arguments.get(2), target);
        final NamespacedKey namespacedKey = plugin.getPersistentMetaHandler().getKey(keyName);
        if (namespacedKey == null) {
            plugin.sms(sender, Messages.META_KEY_INVALID.message().replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build()));
            return;
        }

        final Map<ContextKeys, String> context = new HashMap<>();
        context.put(ContextKeys.KEY_NAME, keyName);

        if (action == DataAction.SWITCH) {
            handleSwitchMeta(sender, target, namespacedKey, context);
            return;
        }

        if (arguments.size() < 4) {
            sendWrongUsageMessage(sender, action);
            return;
        }

        final String typeName = StringUtils.replacePlaceholders(arguments.get(3), target).toUpperCase(Locale.ROOT);
        final DataType<?, ?> type = DataType.getSupportedTypeByName(typeName);
        if (type == null) {
            plugin.sms(sender, Messages.META_TYPE_UNSUPPORTED.message().replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build()));
            return;
        }

        context.put(ContextKeys.TYPE_NAME, typeName);

        if (action == DataAction.REMOVE) {
            handleRemoveMeta(sender, target, namespacedKey, type, context);
            return;
        }

        if (arguments.size() < 5) {
            sendWrongUsageMessage(sender, action);
            return;
        }

        final String value = StringUtils.replacePlaceholders(String.join(" ", arguments.subList(4, arguments.size())), target);

        if (action == DataAction.SET) {
            handleSetMeta(sender, target, namespacedKey, type, value, context);
            return;
        }

        if (action == DataAction.ADD) {
            handleAddMeta(sender, target, namespacedKey, type, value, context);
            return;
        }

        if (action == DataAction.SUBTRACT) {
            handleSubtractMeta(sender, target, namespacedKey, type, value, context);
            return;
        }

        sendWrongUsageMessage(sender, action);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final List<String> arguments) {
        if (!sender.isOp() || !VersionHelper.IS_PDC_VERSION || plugin.getPersistentMetaHandler() == null) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() > 5) {
            return null;
        }

        if (arguments.size() == 1) {
            final String firstArgument = arguments.get(0).toLowerCase();
            if (firstArgument.isEmpty()) {
                return List.of(getName());
            }

            if (getName().startsWith(firstArgument)) {
                return List.of(getName());
            }

            return null;
        }

        final String firstArgument = arguments.get(0).toLowerCase();

        if (!getName().equals(firstArgument)) {
            return null;
        }

        if (arguments.size() == 2) {
            return getPlayerNameCompletion(arguments.get(1));
        }

        if (arguments.size() == 3) {
            final String thirdArgument = arguments.get(2).toLowerCase();
            if (thirdArgument.isEmpty()) {
                return SUB_COMMANDS;
            }

            return SUB_COMMANDS.stream()
                    .filter(action -> action.startsWith(thirdArgument))
                    .collect(Collectors.toList());
        }

        if (arguments.size() == 4) {
            final String action = arguments.get(2).toLowerCase();
            if (!action.equalsIgnoreCase("list")) {
                return null;
            }

            final String fourthArgument = arguments.get(3);

            if (fourthArgument.isEmpty()) {
                return new ArrayList<>(DataType.getSupportedTypeNames());
            }

            return DataType.getSupportedTypeNames().stream()
                    .filter(type -> type.startsWith(fourthArgument.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        if (arguments.size() == 5) {
            final String actionName = arguments.get(2).toLowerCase();
            final DataAction action = DataAction.getActionByName(actionName);

            if (actionName.equalsIgnoreCase("list") || action == DataAction.SWITCH) {
                return null;
            }

            final String fifthArgument = arguments.get(4);

            if (fifthArgument.isEmpty()) {
                return new ArrayList<>(DataType.getSupportedTypeNames());
            }

            return DataType.getSupportedTypeNames().stream()
                    .filter(type -> type.startsWith(fifthArgument.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return null;
    }

    private void handleListMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                                @NotNull final List<String> arguments) {
        if (arguments.isEmpty()) {
            plugin.sms(sender, Messages.WRONG_USAGE_META_LIST_COMMAND);
            return;
        }

        final String typeName = arguments.get(0).toUpperCase(Locale.ROOT);
        final DataType<?, ?> type = DataType.getSupportedTypeByName(typeName);
        if (type == null) {
            plugin.sms(sender, Messages.META_TYPE_UNSUPPORTED.message().replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build()));
            return;
        }

        final Map<String, Object> metas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        metas.putAll(plugin.getPersistentMetaHandler().getMetaValues(target, type));

        if (metas.isEmpty()) {
            plugin.sms(sender, Messages.NO_META_VALUES.message()
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build()));
            return;
        }

        final int itemsPerPage = plugin.getGeneralConfig().metasListPageSize();
        final int itemsCount = metas.size();
        final int pagesCount = PaginationUtils.getPagesCount(itemsPerPage, itemsCount);

        final int page = PaginationUtils.parsePage(itemsPerPage, itemsCount, pagesCount, arguments.size() < 2 ? null : arguments.get(1));

        final Map<String, Object> pageItems = new LinkedHashMap<>();

        final int start = (page - 1) * itemsPerPage;
        final int end = start + itemsPerPage;

        int index = 0;
        for (final Map.Entry<String, Object> entry : metas.entrySet()) {
            if (index >= end) {
                break;
            }

            if (index < start) {
                index++;
                continue;
            }

            pageItems.put(entry.getKey(), entry.getValue());
            index++;
        }

        final int pageItemsCount = pageItems.size();

        final TextComponent.Builder list = text()
                .append(text("Page " + page + "/" + pagesCount + " - " + pageItemsCount + " pairs:", NamedTextColor.GOLD))
                .append(newline())
                .append(newline())
                .append(text("Key (String) - Value (" + typeName + ")", NamedTextColor.GRAY))
                .append(newline());

        final var pairsList = pageItems.entrySet().stream()
                .map(entry -> text(entry.getKey(), NamedTextColor.DARK_AQUA)
                        .append(text(" - ", NamedTextColor.GRAY))
                        .append(text(String.valueOf(entry.getValue()), NamedTextColor.GREEN))
                        .append(newline()))
                .collect(Component.toComponent());

        list.append(newline())
                .append(pairsList)
                .append(newline())
                .append(text("Use /dm meta list " + typeName + " <page> to view more values of this type", NamedTextColor.GRAY));
        plugin.sms(sender, list.build());
    }

    private void handleShowMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                                @NotNull final List<String> arguments) {

        if (arguments.size() < 2) {
            plugin.sms(sender, Messages.WRONG_USAGE_META_SHOW_COMMAND);
            return;
        }

        final String keyName = arguments.get(0);
        final NamespacedKey namespacedKey = plugin.getPersistentMetaHandler().getKey(keyName);
        if (namespacedKey == null) {
            plugin.sms(sender, Messages.META_KEY_INVALID.message().replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build()));
            return;
        }

        final String typeName = arguments.get(1).toUpperCase(Locale.ROOT);
        final DataType<?, ?> type = DataType.getSupportedTypeByName(typeName);
        if (type == null) {
            plugin.sms(sender, Messages.META_TYPE_UNSUPPORTED.message().replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build()));
            return;
        }

        final Object value = plugin.getPersistentMetaHandler().getMetaValue(target, namespacedKey, type);

        if (value == null) {
            plugin.sms(sender, Messages.NO_META_VALUE.message()
                    .replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build())
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
                    .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
            );
            return;
        }

        plugin.sms(sender, Messages.META_VALUE_FOUND.message()
                .replaceText(KEY_REPLACER_BUILDER.replacement(keyName).build())
                .replaceText(TYPE_REPLACER_BUILDER.replacement(typeName).build())
                .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(value)).build())
                .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
        );
    }

    private void handleSetMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                               @NotNull final NamespacedKey namespacedKey,
                               @NotNull final DataType<?, ?> type,
                               @NotNull final String value,
                               @NotNull final Map<ContextKeys, String> context) {
        final Object parsedValue = plugin.getPersistentMetaHandler().parseValueByType(type, value);
        if (parsedValue == null) {
            plugin.sms(sender, Messages.META_VALUE_TYPE_MISMATCH.message()
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(value).build())
                    .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build()));
            return;
        }

        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().setMetaValue(target, namespacedKey, type, parsedValue);
        switch (result) {
            case SUCCESS:
                plugin.sms(sender, Messages.META_VALUE_SET.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build())
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            case NEW_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_VALUE_TYPE_MISMATCH.message()
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build()));
                return;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_EXISTENT_VALUE_WRONG_TYPE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build()));
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SET_COMMAND);
        }
    }

    private void handleRemoveMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                                  @NotNull final NamespacedKey namespacedKey,
                                  @NotNull final DataType<?, ?> type,
                                  @NotNull final Map<ContextKeys, String> context) {
        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().removeMetaValue(target, namespacedKey, type);
        switch (result) {
            case SUCCESS:
                plugin.sms(sender, Messages.META_VALUE_REMOVED.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_EXISTENT_VALUE_WRONG_TYPE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build()));
                return;
            case VALUE_NOT_FOUND:
                plugin.sms(sender, Messages.NO_META_VALUE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_REMOVE_COMMAND);
        }
    }

    private void handleSwitchMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                                  @NotNull final NamespacedKey namespacedKey, @NotNull final Map<ContextKeys, String> context) {
        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().switchMetaValue(target, namespacedKey);

        switch (result) {
            case SUCCESS:
                final Object newValue = plugin.getPersistentMetaHandler().getMetaValue(target, namespacedKey, DataType.BOOLEAN);

                plugin.sms(sender, Messages.META_VALUE_SWITCHED.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(String.valueOf(newValue)).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_EXISTENT_VALUE_WRONG_TYPE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build()));
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SWITCH_COMMAND);
        }
    }

    private void handleAddMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                               @NotNull final NamespacedKey namespacedKey,
                               @NotNull final DataType<?, ?> type,
                               @NotNull final String value,
                               @NotNull final Map<ContextKeys, String> context) {
        final Object parsedValue = plugin.getPersistentMetaHandler().parseValueByType(type, value);

        if (!(parsedValue instanceof Number)) {
            plugin.sms(sender, Messages.META_ADD_TYPE_MISMATCH.message()
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(value)).build()));
            return;
        }

        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().addMetaValue(target, namespacedKey, type, (Number) parsedValue);

        switch (result) {
            case SUCCESS:
                final Object newValue = plugin.getPersistentMetaHandler().getMetaValue(target, namespacedKey, type);

                plugin.sms(sender, Messages.META_VALUE_ADDED.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build())
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build())
                        .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(String.valueOf(newValue)).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            case INVALID_TYPE:
                plugin.sms(sender, Messages.META_ADD_TYPE_MISMATCH.message()
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build()));
                return;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_EXISTENT_VALUE_WRONG_TYPE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build()));
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_ADD_COMMAND);
        }
    }

    private void handleSubtractMeta(@NotNull final CommandSender sender, @NotNull final Player target,
                                    @NotNull final NamespacedKey namespacedKey,
                                    @NotNull final DataType<?, ?> type,
                                    @NotNull final String value,
                                    @NotNull final Map<ContextKeys, String> context) {
        final Object parsedValue = plugin.getPersistentMetaHandler().parseValueByType(type, value);

        if (!(parsedValue instanceof Number)) {
            plugin.sms(sender, Messages.META_SUBTRACT_TYPE_MISMATCH.message()
                    .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(value)).build()));
            return;
        }

        final PersistentMetaHandler.OperationResult result = plugin.getPersistentMetaHandler().subtractMetaValue(target, namespacedKey, type, (Number) parsedValue);

        switch (result) {
            case SUCCESS:
                final Object newValue = plugin.getPersistentMetaHandler().getMetaValue(target, namespacedKey, type);

                plugin.sms(sender, Messages.META_VALUE_SUBTRACTED.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build())
                        .replaceText(TYPE_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.TYPE_NAME, type.getComplexType().getSimpleName())).build())
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build())
                        .replaceText(NEW_VALUE_REPLACER_BUILDER.replacement(String.valueOf(newValue)).build())
                        .replaceText(PLAYER_REPLACER_BUILDER.replacement(target.getName()).build())
                );
                return;
            case INVALID_TYPE:
                plugin.sms(sender, Messages.META_SUBTRACT_TYPE_MISMATCH.message()
                        .replaceText(VALUE_REPLACER_BUILDER.replacement(String.valueOf(parsedValue)).build()));
                return;
            case EXISTENT_VALUE_IS_DIFFERENT_TYPE:
                plugin.sms(sender, Messages.META_EXISTENT_VALUE_WRONG_TYPE.message()
                        .replaceText(KEY_REPLACER_BUILDER.replacement(context.getOrDefault(ContextKeys.KEY_NAME, namespacedKey.toString())).build()));
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SUBTRACT_COMMAND);
        }
    }

    private void sendWrongUsageMessage(@NotNull final CommandSender sender, @Nullable final DataAction action) {
        if (action == null) {
            plugin.sms(sender, Messages.WRONG_USAGE_META_COMMAND);
            return;
        }

        switch (action) {
            case SET:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SET_COMMAND);
                return;
            case REMOVE:
                plugin.sms(sender, Messages.WRONG_USAGE_META_REMOVE_COMMAND);
                return;
            case ADD:
                plugin.sms(sender, Messages.WRONG_USAGE_META_ADD_COMMAND);
                return;
            case SUBTRACT:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SUBTRACT_COMMAND);
                return;
            case SWITCH:
                plugin.sms(sender, Messages.WRONG_USAGE_META_SWITCH_COMMAND);
                return;
            default:
                plugin.sms(sender, Messages.WRONG_USAGE_META_COMMAND);
        }
    }

    public enum ContextKeys {
        KEY_NAME,
        TYPE_NAME,
        VALUE,
    }
}
