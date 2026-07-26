package com.extendedclip.deluxemenus.menu.options;

import com.extendedclip.deluxemenus.action.ClickHandler;
import com.extendedclip.deluxemenus.requirement.RequirementList;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class MenuOptions {

    private final String name;
    private final String title;
    private final InventoryType type;
    private final int size;
    private final int updateInterval;
    private final int refreshInterval;
    private final boolean refresh;
    private final boolean parsePlaceholdersInArguments;
    private final boolean parsePlaceholdersAfterArguments;
    private final boolean enableBypassPerm;

    private final List<String> commands;
    private final List<String> guiCloseCommands;
    private final List<String> guiOpenCommands;
    private final boolean registerCommands;
    private final List<String> arguments;
    private final List<RequirementList> argumentRequirements;
    private final String argumentsUsageMessage;

    private final RequirementList openRequirements;
    private final ClickHandler openHandler;
    private final ClickHandler closeHandler;

    private MenuOptions(final @NotNull MenuOptionsBuilder builder) {
        this.name = builder.name;
        this.title = builder.title;
        this.type = builder.type;
        this.size = builder.size;
        this.updateInterval = builder.updateInterval;
        this.refreshInterval = builder.refreshInterval;
        this.refresh = builder.refresh;
        this.parsePlaceholdersInArguments = builder.parsePlaceholdersInArguments;
        this.parsePlaceholdersAfterArguments = builder.parsePlaceholdersAfterArguments;
        this.enableBypassPerm = builder.enableBypassPerm;

        this.commands = builder.commands;
        this.guiCloseCommands = builder.guiCloseCommands;
        this.guiOpenCommands = builder.guiOpenCommands;
        this.registerCommands = builder.registerCommands;
        this.arguments = builder.arguments;
        this.argumentRequirements = builder.argumentRequirements;
        this.argumentsUsageMessage = builder.argumentsUsageMessage;

        this.openRequirements = builder.openRequirements;
        this.openHandler = builder.openHandler;
        this.closeHandler = builder.closeHandler;
    }

    public static @NotNull MenuOptionsBuilder builder(final @NotNull String name, final @NotNull String title) {
        return new MenuOptionsBuilder(name, title);
    }

    public @NotNull String name() {
        return this.name;
    }

    public @NotNull String title() {
        return this.title;
    }

    public @NotNull InventoryType type() {
        return this.type;
    }

    public int size() {
        return this.size;
    }

    public int updateInterval() {
        return this.updateInterval;
    }

    public int refreshInterval() {
        return this.refreshInterval;
    }

    public boolean refresh() {
        return this.refresh;
    }

    public boolean parsePlaceholdersInArguments() {
        return this.parsePlaceholdersInArguments;
    }

    public boolean parsePlaceholdersAfterArguments() {
        return this.parsePlaceholdersAfterArguments;
    }

    public boolean enableBypassPerm() {
        return this.enableBypassPerm;
    }

    public @NotNull List<@NotNull String> commands() {
        return this.commands;
    }

    public @Nullable List<String> guiCloseCommands() {
        return this.guiCloseCommands;
    }

    public @Nullable List<String> guiOpenCommands() {
        return this.guiOpenCommands;
    }

    public boolean registerCommands() {
        return this.registerCommands;
    }

    public @NotNull List<@NotNull String> arguments() {
        return this.arguments;
    }

    public @NotNull List<@NotNull RequirementList> argumentRequirements() {
        return this.argumentRequirements;
    }

    public @NotNull Optional<String> argumentsUsageMessage() {
        return Optional.ofNullable(this.argumentsUsageMessage);
    }

    public @NotNull Optional<RequirementList> openRequirements() {
        return Optional.ofNullable(this.openRequirements);
    }

    public @NotNull Optional<ClickHandler> openHandler() {
        return Optional.ofNullable(this.openHandler);
    }

    public @NotNull Optional<ClickHandler> closeHandler() {
        return Optional.ofNullable(this.closeHandler);
    }

    public @NotNull MenuOptionsBuilder asBuilder() {
        return MenuOptions.builder(this.name, this.title)
                .type(this.type)
                .size(this.size)
                .updateInterval(this.updateInterval)
                .refreshInterval(this.refreshInterval)
                .refresh(this.refresh)
                .parsePlaceholdersInArguments(this.parsePlaceholdersInArguments)
                .parsePlaceholdersAfterArguments(this.parsePlaceholdersAfterArguments)
                .enableBypassPerm(this.enableBypassPerm)
                .commands(this.commands)
                .guiCloseCommands(this.guiCloseCommands)
                .guiOpenCommands(this.guiOpenCommands)
                .registerCommands(this.registerCommands)
                .arguments(this.arguments)
                .argumentRequirements(this.argumentRequirements)
                .argumentsUsageMessage(this.argumentsUsageMessage)
                .openRequirements(this.openRequirements)
                .openHandler(this.openHandler)
                .closeHandler(this.closeHandler);
    }

    public static class MenuOptionsBuilder {

        private String name;
        private String title;
        private InventoryType type = InventoryType.CHEST;
        private int size = 9;
        private int updateInterval = 10;
        private int refreshInterval = 10;
        private boolean refresh;
        private boolean parsePlaceholdersInArguments = false;
        private boolean parsePlaceholdersAfterArguments = false;
        private boolean enableBypassPerm = false;

        private List<String> commands = List.of();
        private List<String> guiCloseCommands;
        private List<String> guiOpenCommands;
        private boolean registerCommands = false;
        private List<String> arguments = List.of();
        private List<RequirementList> argumentRequirements = List.of();
        private String argumentsUsageMessage;

        private RequirementList openRequirements;
        private ClickHandler openHandler;
        private ClickHandler closeHandler;

        private MenuOptionsBuilder(final @NotNull String name, final @NotNull String title) {
            this.name = name;
            this.title = title;
        }

        public MenuOptionsBuilder name(final @NotNull String name) {
            this.name = name;
            return this;
        }

        public MenuOptionsBuilder title(final @NotNull String title) {
            this.title = title;
            return this;
        }

        public MenuOptionsBuilder type(final @NotNull InventoryType type) {
            this.type = type;
            return this;
        }

        public MenuOptionsBuilder size(final int size) {
            this.size = size;
            return this;
        }

        public MenuOptionsBuilder updateInterval(final int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public MenuOptionsBuilder refreshInterval(final int refreshInterval) {
            this.refreshInterval = refreshInterval;
            return this;
        }

        public MenuOptionsBuilder refresh(final boolean refresh) {
            this.refresh = refresh;
            return this;
        }

        public MenuOptionsBuilder parsePlaceholdersInArguments(final boolean parsePlaceholdersInArguments) {
            this.parsePlaceholdersInArguments = parsePlaceholdersInArguments;
            return this;
        }

        public MenuOptionsBuilder parsePlaceholdersAfterArguments(final boolean parsePlaceholdersAfterArguments) {
            this.parsePlaceholdersAfterArguments = parsePlaceholdersAfterArguments;
            return this;
        }

        public MenuOptionsBuilder enableBypassPerm(final boolean enableBypassPerm) {
            this.enableBypassPerm = enableBypassPerm;
            return this;
        }

        public MenuOptionsBuilder commands(final @NotNull List<@NotNull String> commands) {
            this.commands = commands;
            return this;
        }

        public MenuOptionsBuilder guiCloseCommands(final @Nullable List<String> guiCloseCommands) {
            this.guiCloseCommands = guiCloseCommands;
            return this;
        }

        public MenuOptionsBuilder guiOpenCommands(final @Nullable List<String> guiOpenCommands) {
            this.guiOpenCommands = guiOpenCommands;
            return this;
        }

        public MenuOptionsBuilder registerCommands(final boolean registerCommands) {
            this.registerCommands = registerCommands;
            return this;
        }

        public MenuOptionsBuilder arguments(final @NotNull List<@NotNull String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public MenuOptionsBuilder argumentRequirements(final @NotNull List<@NotNull RequirementList> argumentRequirements) {
            this.argumentRequirements = argumentRequirements;
            return this;
        }

        public MenuOptionsBuilder argumentsUsageMessage(final @Nullable String argumentsUsageMessage) {
            this.argumentsUsageMessage = argumentsUsageMessage;
            return this;
        }

        public MenuOptionsBuilder openRequirements(final @Nullable RequirementList openRequirements) {
            this.openRequirements = openRequirements;
            return this;
        }

        public MenuOptionsBuilder openHandler(final @Nullable ClickHandler openHandler) {
            this.openHandler = openHandler;
            return this;
        }

        public MenuOptionsBuilder closeHandler(final @Nullable ClickHandler closeHandler) {
            this.closeHandler = closeHandler;
            return this;
        }

        public MenuOptions build() {
            return new MenuOptions(this);
        }
    }
}
