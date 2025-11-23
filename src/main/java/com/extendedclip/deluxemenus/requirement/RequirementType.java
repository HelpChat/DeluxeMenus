package com.extendedclip.deluxemenus.requirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum RequirementType {
  HAS_META(Arrays.asList("has meta", "meta"), "Checks if a player has a certain metadata value",
      Arrays.asList("key", "value")),
  DOES_NOT_HAVE_META(Arrays.asList("!has meta", "!meta"),
      "Checks if a player does not have a certain metadata value", Arrays.asList("key", "value")),
  IS_NEAR(Arrays.asList("is near", "near"),
      "Checks if a player is within a certain distance of a specific location",
      Arrays.asList("location", "distance")),
  IS_NOT_NEAR(Arrays.asList("!is near", "!near"),
      "Checks if a player is not within a certain distance of a specific location",
      Arrays.asList("location", "distance")),
  JAVASCRIPT(Arrays.asList("javascript", "js"),
      "Evaluates a javascript expression that must return true or false",
      Collections.singletonList("expression")),
  HAS_ITEM(Arrays.asList("has item", "item", "hasitem"), "Checks if a player has a specific item",
      Arrays.asList("material", "amount", "data", "name", "lore")),
  DOES_NOT_HAVE_ITEM(Arrays.asList("!has item", "!item", "!hasitem", "does not have item"),
      "Checks if a player does not have specific item",
      Arrays.asList("material", "amount", "data", "name", "lore")),
  HAS_MONEY(Arrays.asList("has money", "hasmoney", "money"),
      "Checks if a player has enough money (Vault required)",
      Arrays.asList("amount", "placeholder")),
  DOES_NOT_HAVE_MONEY(Arrays.asList("!has money", "!hasmoney", "!money"),
      "Checks if a player does not have enough money (Vault required)",
      Arrays.asList("amount", "placeholder")),
  HAS_EXP(Arrays.asList("has exp", "hasexp", "exp"),
      "Checks if a player has enough exp",
      Arrays.asList("amount")),
  DOES_NOT_HAVE_EXP(Arrays.asList("!has exp", "!hasexp", "!exp"),
      "Checks if a player has enough exp",
      Arrays.asList("amount")),
  HAS_PERMISSION(Arrays.asList("has permission", "has perm", "haspermission", "hasperm", "perm"),
      "Checks if a player has a specific permission", Collections.singletonList("permission")),
  DOES_NOT_HAVE_PERMISSION(
      Arrays.asList("!has permission", "!has perm", "!haspermission", "!hasperm", "!perm"),
      "Checks if a player does not have a specific permission",
      Collections.singletonList("permission")),
  HAS_PERMISSIONS(Arrays.asList("has permissions", "has perms", "haspermissions", "hasperms", "perms"),
          "Checks if a player has a set amount of permissions", Collections.singletonList("permissions")),
  DOES_NOT_HAVE_PERMISSIONS(
          Arrays.asList("!has permissions", "!has perms", "!haspermissions", "!hasperms", "!perms"),
          "Checks if a player does not have a set amount of permission",
          Arrays.asList("permissions", "minimum")),
  STRING_CONTAINS(Arrays.asList("string contains", "stringcontains", "contains"),
      "Checks if a string contains another string", Arrays.asList("input", "output")),
  STRING_DOES_NOT_CONTAIN(Arrays.asList("!string contains", "!stringcontains", "!contains"),
      "Checks if a string does not contain another string", Arrays.asList("input", "output")),
  STRING_CONTAINS_IGNORECASE(Arrays.asList("string contains ignorecase", "stringcontainsignorecase", "containsignorecase"),
      "Checks if a string contains another string ignoring case", Arrays.asList("input", "output")),
  STRING_DOES_NOT_CONTAIN_IGNORECASE(Arrays.asList("!string contains ignorecase", "!stringcontainsignorecase", "!containsignorecase"),
      "Checks if a string does not contain another string ignoring case", Arrays.asList("input", "output")),
  STRING_EQUALS(Arrays.asList("string equals", "stringequals", "equals"),
      "Checks if a string equals another string", Arrays.asList("input", "output")),
  STRING_DOES_NOT_EQUAL(Arrays.asList("!string equals", "!stringequals", "!equals"),
      "Checks if a string does not equal another string", Arrays.asList("input", "output")),
  STRING_EQUALS_IGNORECASE(
      Arrays.asList("stringequalsignorecase", "string equals ignorecase", "equalsignorecase"),
      "Checks if a string equals another string ignoring case", Arrays.asList("input", "output")),
  STRING_DOES_NOT_EQUAL_IGNORECASE(
      Arrays.asList("!stringequalsignorecase", "!string equals ignorecase", "!equalsignorecase"),
      "Checks if a string does not equal another string ignoring case",
      Arrays.asList("input", "output")),
  GREATER_THAN(Arrays.asList(">", "greater than", "greaterthan"),
      "Checks if a number is greater than another number", Arrays.asList("input", "output")),
  GREATER_THAN_EQUAL_TO(Arrays.asList(">=", "greater than or equal to", "greaterthanorequalto"),
      "Checks if a number is greater than or equal to another number",
      Arrays.asList("input", "output")),
  EQUAL_TO(Arrays.asList("==", "equal to", "equalto"),
      "Checks if a number is equal to another number", Arrays.asList("input", "output")),
  NOT_EQUAL_TO(Arrays.asList("!=", "not equal to", "notequalto"),
      "Checks if a number is not equal to another number", Arrays.asList("input", "output")),
  LESS_THAN_EQUAL_TO(Arrays.asList("<=", "less than or equal to", "lessthanorequalto"),
      "Checks if a number is less than or equal to another number",
      Arrays.asList("input", "output")),
  LESS_THAN(Arrays.asList("<", "less than", "lessthan"),
      "Checks if a number is less than another number", Arrays.asList("input", "output")),
  REGEX_MATCHES(Arrays.asList("regex matches", "regex"),
      "Checks if a placeholder parsed string matches a regex pattern",
      Arrays.asList("input", "regex")),
  REGEX_DOES_NOT_MATCH(Arrays.asList("!regex matches", "!regex"),
      "Checks if a placeholder parsed string does not match a regex pattern",
      Arrays.asList("input", "regex")),
  STRING_LENGTH(Arrays.asList("string length"),
  "Checks if the given string's length is between the provided minimum and (optionally) maximum.",
          Arrays.asList("input", "min", "max")),
  IS_OBJECT(Arrays.asList("is object"),
          "Checks if the given string can be parsed as a given Java object.",
          Arrays.asList("input", "object"));

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

