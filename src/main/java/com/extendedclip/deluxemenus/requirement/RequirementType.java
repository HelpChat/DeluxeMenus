package com.extendedclip.deluxemenus.requirement;

import java.util.List;

public enum RequirementType {
    HAS_META(List.of("has meta", "meta"),
            "Checks if a player has a certain metadata value",
            "key", "value"),
    DOES_NOT_HAVE_META(List.of("!has meta", "!meta"),
            "Checks if a player does not have a certain metadata value",
            "key", "value"),
    IS_NEAR(List.of("is near", "near"),
            "Checks if a player is within a certain distance of a specific location",
            "location", "distance"),
    IS_NOT_NEAR(List.of("!is near", "!near"),
            "Checks if a player is not within a certain distance of a specific location",
            "location", "distance"),
    JAVASCRIPT(List.of("javascript", "js"),
            "Evaluates a javascript expression that must return true or false",
            "expression"),
    HAS_ITEM(List.of("has item", "item", "hasitem"),
            "Checks if a player has a specific item",
            "material", "amount", "data", "name", "lore"),
    DOES_NOT_HAVE_ITEM(List.of("!has item", "!item", "!hasitem", "does not have item"),
            "Checks if a player does not have specific item",
            "material", "amount", "data", "name", "lore"),
    HAS_MONEY(List.of("has money", "hasmoney", "money"),
            "Checks if a player has enough money (Vault required)",
            "amount", "placeholder"),
    DOES_NOT_HAVE_MONEY(List.of("!has money", "!hasmoney", "!money"),
            "Checks if a player does not have enough money (Vault required)",
            "amount", "placeholder"),
    HAS_EXP(List.of("has exp", "hasexp", "exp"),
            "Checks if a player has enough exp",
            "amount"),
    DOES_NOT_HAVE_EXP(List.of("!has exp", "!hasexp", "!exp"),
            "Checks if a player has enough exp",
            "amount"),
    HAS_PERMISSION(List.of("has permission", "has perm", "haspermission", "hasperm", "perm"),
            "Checks if a player has a specific permission",
            "permission"),
    DOES_NOT_HAVE_PERMISSION(
            List.of("!has permission", "!has perm", "!haspermission", "!hasperm", "!perm"),
            "Checks if a player does not have a specific permission",
            "permission"),
    STRING_CONTAINS(List.of("string contains", "stringcontains", "contains"),
            "Checks if a string contains another string",
            "input", "output"),
    STRING_DOES_NOT_CONTAIN(List.of("!string contains", "!stringcontains", "!contains"),
            "Checks if a string does not contain another string",
            "input", "output"),
    STRING_EQUALS(List.of("string equals", "stringequals", "equals"),
            "Checks if a string equals another string",
            "input", "output"),
    STRING_DOES_NOT_EQUAL(List.of("!string equals", "!stringequals", "!equals"),
            "Checks if a string does not equal another string",
            "input", "output"),
    STRING_EQUALS_IGNORECASE(
            List.of("stringequalsignorecase", "string equals ignorecase", "equalsignorecase"),
            "Checks if a string equals another string ignoring case",
            "input", "output"),
    STRING_DOES_NOT_EQUAL_IGNORECASE(
            List.of("!stringequalsignorecase", "!string equals ignorecase", "!equalsignorecase"),
            "Checks if a string does not equal another string ignoring case",
            "input", "output"),
    GREATER_THAN(List.of(">", "greater than", "greaterthan"),
            "Checks if a number is greater than another number",
            "input", "output"),
    GREATER_THAN_EQUAL_TO(List.of(">=", "greater than or equal to", "greaterthanorequalto"),
            "Checks if a number is greater than or equal to another number",
            "input", "output"),
    EQUAL_TO(List.of("==", "equal to", "equalto"),
            "Checks if a number is equal to another number",
            "input", "output"),
    NOT_EQUAL_TO(List.of("!=", "not equal to", "notequalto"),
            "Checks if a number is not equal to another number",
            "input", "output"),
    LESS_THAN_EQUAL_TO(List.of("<=", "less than or equal to", "lessthanorequalto"),
            "Checks if a number is less than or equal to another number",
            "input", "output"),
    LESS_THAN(List.of("<", "less than", "lessthan"),
            "Checks if a number is less than another number", "input", "output"),
    REGEX_MATCHES(List.of("regex matches", "regex"),
            "Checks if a placeholder parsed string matches a regex pattern",
            "input", "regex"),
    REGEX_DOES_NOT_MATCH(List.of("!regex matches", "!regex"),
            "Checks if a placeholder parsed string does not match a regex pattern",
            "input", "regex");

    private final List<String> identifier;
    private final String description;
    private final List<String> configOptions;

    RequirementType(List<String> identifier, String description, String... options) {
        this.identifier = identifier;
        this.description = description;
        configOptions = List.of(options);
    }

    public static RequirementType getType(String s) {
        for (RequirementType type : values()) {
            for (String id : type.getIdentifiers()) {
                if (s.equalsIgnoreCase(id)) {
                    return type;
                }
            }
        }
        return null;
    }

    public List<String> getIdentifiers() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getConfigOptions() {
        return configOptions;
    }
}
