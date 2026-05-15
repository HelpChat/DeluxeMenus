package com.extendedclip.deluxemenus.command.subcommand;

import com.extendedclip.deluxemenus.DeluxeMenus;
import com.extendedclip.deluxemenus.commandpanels.CommandPanelsConversionResult;
import com.extendedclip.deluxemenus.commandpanels.CommandPanelsConverter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;

public class ConvertCommandPanelsCommand extends SubCommand {

    private static final String CONVERT_PERMISSION = "deluxemenus.convertcommandpanels";

    public ConvertCommandPanelsCommand(final @NotNull DeluxeMenus plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getName() {
        return "convertcommandpanels";
    }

    @Override
    public void execute(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(CONVERT_PERMISSION)) {
            plugin.sms(sender, com.extendedclip.deluxemenus.utils.Messages.NO_PERMISSION);
            return;
        }

        final ParsedArguments parsedArguments = parseArguments(arguments);

        try {
            final CommandPanelsConverter converter = new CommandPanelsConverter(plugin);
            final CommandPanelsConversionResult result = parsedArguments.sourcePath.isBlank()
                    ? converter.convertDefault(parsedArguments.outputFolder)
                    : converter.convert(new File(stripQuotes(parsedArguments.sourcePath)), parsedArguments.outputFolder);

            plugin.sms(sender, text("Converted ", NamedTextColor.GREEN)
                    .append(text(result.menusConverted(), NamedTextColor.WHITE))
                    .append(text(" CommandPanels menus from ", NamedTextColor.GREEN))
                    .append(text(result.filesRead(), NamedTextColor.WHITE))
                    .append(text(" files.", NamedTextColor.GREEN)));

            if (result.menusSkipped() > 0) {
                plugin.sms(sender, text("Skipped " + result.menusSkipped() + " menus. Check console for details.", NamedTextColor.YELLOW));
            }

            for (final String warning : result.warnings()) {
                plugin.getLogger().warning("[CommandPanels converter] " + warning);
            }
        } catch (final IOException exception) {
            plugin.sms(sender, Component.text("CommandPanels conversion failed: " + exception.getMessage(), NamedTextColor.RED));
        } catch (final RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE, "Unexpected error while converting CommandPanels menus.", exception);
            plugin.sms(sender, Component.text("CommandPanels conversion failed. Check console for details.", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull List<String> arguments) {
        if (!sender.hasPermission(CONVERT_PERMISSION)) {
            return null;
        }

        if (arguments.isEmpty()) {
            return List.of(getName());
        }

        if (arguments.size() == 1 && getName().startsWith(arguments.get(0).toLowerCase())) {
            return List.of(getName());
        }

        if (arguments.size() >= 2 && getName().equalsIgnoreCase(arguments.get(0)) && "--output".startsWith(arguments.get(arguments.size() - 1).toLowerCase())) {
            return List.of("--output");
        }

        return null;
    }

    private @NotNull ParsedArguments parseArguments(final @NotNull List<String> arguments) {
        String outputFolder = null;
        final StringBuilder sourcePath = new StringBuilder();

        for (int i = 0; i < arguments.size(); i++) {
            final String argument = arguments.get(i);

            if (argument.equalsIgnoreCase("--output")) {
                if (i + 1 < arguments.size()) {
                    outputFolder = arguments.get(i + 1);
                }
                i++;
                continue;
            }

            if (sourcePath.length() > 0) {
                sourcePath.append(' ');
            }
            sourcePath.append(argument);
        }

        return new ParsedArguments(sourcePath.toString(), outputFolder);
    }

    private @NotNull String stripQuotes(final @NotNull String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static class ParsedArguments {
        private final String sourcePath;
        private final String outputFolder;

        private ParsedArguments(final @NotNull String sourcePath, final @Nullable String outputFolder) {
            this.sourcePath = sourcePath;
            this.outputFolder = outputFolder;
        }
    }
}
