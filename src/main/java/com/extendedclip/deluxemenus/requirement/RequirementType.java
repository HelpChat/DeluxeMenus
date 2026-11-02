package com.extendedclip.deluxemenus.requirement;

import java.util.Collections;
import java.util.List;

public enum RequirementType {
  HAS_META(List.of("has meta", "meta"), "Checks if a player has a certain metadata value",
      List.of("key", "value")),
  DOES_NOT_HAVE_META(List.of("!has meta", "!meta"),
      "Checks if a player does not have a certain metadata value", List.of("key", "value")),
  IS_NEAR(List.of("is near", "near"),
      "Checks if a player is within a certain distance of a specific location",
      List.of("location", "distance")),
  IS_NOT_NEAR(List.of("!is near", "!near"),
      "Checks if a player is not within a certain distance of a specific location",
      List.of("location", "distance")),
  JAVASCRIPT(List.of("javascript", "js"),
      "Evaluates a javascript expression that must return true or false",
      Collections.singletonList("expression")),
  HAS_ITEM(List.of("has item", "item", "hasitem"), "Checks if a player has a specific item",
      List.of("material", "amount", "data", "name", "lore")),
  DOES_NOT_HAVE_ITEM(List.of("!has item", "!item", "!hasitem", "does not have item"),
      "Checks if a player does not have specific item",
      List.of("material", "amount", "data", "name", "lore")),
  HAS_MONEY(List.of("has money", "hasmoney", "money"),
      "Checks if a player has enough money (Vault required)",
      List.of("amount", "placeholder")),
  DOES_NOT_HAVE_MONEY(List.of("!has money", "!hasmoney", "!money"),
      "Checks if a player does not have enough money (Vault required)",
      List.of("amount", "placeholder")),
  HAS_EXP(List.of("has exp", "hasexp", "exp"),
      "Checks if a player has enough exp",
      List.of("amount")),
  DOES_NOT_HAVE_EXP(List.of("!has exp", "!hasexp", "!exp"),
      "Checks if a player has enough exp",
      List.of("amount")),
  HAS_PERMISSION(List.of("has permission", "has perm", "haspermission", "hasperm", "perm"),
      "Checks if a player has a specific permission", Collections.singletonList("permission")),
  DOES_NOT_HAVE_PERMISSION(
      List.of("!has permission", "!has perm", "!haspermission", "!hasperm", "!perm"),
      "Checks if a player does not have a specific permission",
      Collections.singletonList("permission")),
  HAS_PERMISSIONS(List.of("has permissions", "has perms", "haspermissions", "hasperms", "perms"),
          "Checks if a player has a set amount of permissions", Collections.singletonList("permissions")),
  DOES_NOT_HAVE_PERMISSIONS(
          List.of("!has permissions", "!has perms", "!haspermissions", "!hasperms", "!perms"),
          "Checks if a player does not have a set amount of permission",
          List.of("permissions", "minimum")),
  STRING_CONTAINS(List.of("string contains", "stringcontains", "contains"),
      "Checks if a string contains another string", List.of("input", "output")),
  STRING_DOES_NOT_CONTAIN(List.of("!string contains", "!stringcontains", "!contains"),
      "Checks if a string does not contain another string", List.of("input", "output")),
  STRING_CONTAINS_IGNORECASE(List.of("string contains ignorecase", "stringcontainsignorecase", "containsignorecase"),
      "Checks if a string contains another string ignoring case", List.of("input", "output")),
  STRING_DOES_NOT_CONTAIN_IGNORECASE(List.of("!string contains ignorecase", "!stringcontainsignorecase", "!containsignorecase"),
      "Checks if a string does not contain another string ignoring case", List.of("input", "output")),
  STRING_EQUALS(List.of("string equals", "stringequals", "equals"),
      "Checks if a string equals another string", List.of("input", "output")),
  STRING_DOES_NOT_EQUAL(List.of("!string equals", "!stringequals", "!equals"),
      "Checks if a string does not equal another string", List.of("input", "output")),
  STRING_EQUALS_IGNORECASE(
      List.of("stringequalsignorecase", "string equals ignorecase", "equalsignorecase"),
      "Checks if a string equals another string ignoring case", List.of("input", "output")),
  STRING_DOES_NOT_EQUAL_IGNORECASE(
      List.of("!stringequalsignorecase", "!string equals ignorecase", "!equalsignorecase"),
      "Checks if a string does not equal another string ignoring case",
      List.of("input", "output")),
  GREATER_THAN(List.of(">", "greater than", "greaterthan"),
      "Checks if a number is greater than another number", List.of("input", "output")),
  GREATER_THAN_EQUAL_TO(List.of(">=", "greater than or equal to", "greaterthanorequalto"),
      "Checks if a number is greater than or equal to another number",
      List.of("input", "output")),
  EQUAL_TO(List.of("==", "equal to", "equalto"),
      "Checks if a number is equal to another number", List.of("input", "output")),
  NOT_EQUAL_TO(List.of("!=", "not equal to", "notequalto"),
      "Checks if a number is not equal to another number", List.of("input", "output")),
  LESS_THAN_EQUAL_TO(List.of("<=", "less than or equal to", "lessthanorequalto"),
      "Checks if a number is less than or equal to another number",
      List.of("input", "output")),
  LESS_THAN(List.of("<", "less than", "lessthan"),
      "Checks if a number is less than another number", List.of("input", "output")),
  REGEX_MATCHES(List.of("regex matches", "regex"),
      "Checks if a placeholder parsed string matches a regex pattern",
      List.of("input", "regex")),
  REGEX_DOES_NOT_MATCH(List.of("!regex matches", "!regex"),
      "Checks if a placeholder parsed string does not match a regex pattern",
      List.of("input", "regex")),
  STRING_LENGTH(List.of("string length"),
  "Checks if the given string's length is between the provided minimum and (optionally) maximum.",
          List.of("input", "min", "max")),
  IS_OBJECT(List.of("is object"),
          "Checks if the given string can be parsed as a given Java object.",
          List.of("input", "object")),
  HAS_EPHEMERAL_COOLDOWN(List.of("has ephemeral cooldown", "hasephemeralcooldown",
          "ephemeral cooldown", "ephemeralcooldown"),
          "Checks if the player is currently on the given ephemeral cooldown.",
          List.of("id")),
  DOES_NOT_HAVE_EPHEMERAL_COOLDOWN(List.of("!has ephemeral cooldown", "!hasephemeralcooldown",
          "!ephemeral cooldown", "!ephemeralcooldown", "does not have ephemeral cooldown"),
          "Checks if the player is not currently on the given ephemeral cooldown.",
          List.of("id"));

  private final List<String> identifier;
  private final String description;
  private final List<String> configOptions;

  RequirementType(List<String> identifier, String description, List<String> options) {
    this.identifier = identifier;
    this.description = description;
    this.configOptions = options;
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

