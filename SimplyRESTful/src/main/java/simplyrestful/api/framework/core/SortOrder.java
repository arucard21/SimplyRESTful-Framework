package simplyrestful.api.framework.core;

public class SortOrder {
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
}
