package simplyrestful.api.framework.queryparams;

public class SortOrder {
    public static final String DELIMITER = ":";
    public static final String ASCENDING = "asc";
    public static final String DESCENDING = "desc";

    private final String field;
    private final boolean ascending;

    public SortOrder(String field, boolean ascending) {
        this.field = field;
        this.ascending = ascending;
    }

    public String getField() {
        return field;
    }

    public boolean isAscending() {
        return ascending;
    }

    @Override
    public String toString() {
        return field + DELIMITER + (ascending ? ASCENDING : DESCENDING);
    }

    public static SortOrder from(String sortOrder) {
        return new SortOrder(parseSortField(sortOrder), parseSortOrder(sortOrder));
    }

    private static String parseSortField(String sortWithOrderDelimeter) {
        if (sortWithOrderDelimeter.contains(SortOrder.DELIMITER)) {
            return sortWithOrderDelimeter.split(SortOrder.DELIMITER)[0];
        }
        else {
            return sortWithOrderDelimeter;
        }
    }

    private static Boolean parseSortOrder(String sortWithOrderDelimeter) {
        if (sortWithOrderDelimeter.contains(SortOrder.DELIMITER)) {
            return !sortWithOrderDelimeter.split(SortOrder.DELIMITER)[1].equalsIgnoreCase(SortOrder.DESCENDING);
        }
        else {
            // No sort order defined, return true (ascending) by default
            return true;
        }
    }
}
