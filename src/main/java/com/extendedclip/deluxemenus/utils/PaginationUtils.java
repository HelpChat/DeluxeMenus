package com.extendedclip.deluxemenus.utils;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

public class PaginationUtils {
    private PaginationUtils() {
        throw new AssertionError("Util classes should not be initialized");
    }

    /**
     * Loose parsing of a page number. If the provided argument is not a number or is less than 1, 1 is returned.
     * If the provided page number is greater than the maximum number of pages, the maximum number of pages is returned.
     * @param itemsPerPage The number of items per page
     * @param itemsCount The total number of items
     * @param pages The maximum number of pages
     * @param argument The argument to parse as a page number
     * @return The parsed page number
     */
    public static int parsePage(final int itemsPerPage, final int itemsCount, @Nullable final Integer pages,
                                @Nullable final String argument) {
        if (itemsCount <= itemsPerPage || argument == null) {
            return 1;
        }

        final Integer page = Ints.tryParse(argument);

        if (page == null || page < 1) {
            return 1;
        }

        final int maxPages = pages != null ? pages : getPagesCount(itemsPerPage, itemsCount);

        if (page > maxPages) {
            return maxPages;
        }

        return page;
    }

    public static int getPagesCount(final int itemsPerPage, final int itemsCount) {
        return (int) Math.ceil((double) itemsCount / itemsPerPage);
    }
}
