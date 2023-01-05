package simplyrestful.api.framework.test;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import simplyrestful.api.framework.QueryParamUtils;
import simplyrestful.api.framework.queryparams.SortOrder;

public class QueryParamUtilsTest {
    @Test
    public void parseSort_shouldOrderEachFieldAscendingByDefault() {
        List<SortOrder> sortValues = QueryParamUtils.parseSort(List.of(
                "self.type",
                "self.profile",
                "description"));
        Assertions.assertEquals(3, sortValues.size());
        List<String> sortFields = sortValues.stream().map(SortOrder::getField).collect(Collectors.toList());
        Assertions.assertTrue(sortFields.contains("self.type"));
        Assertions.assertTrue(sortFields.contains("self.profile"));
        Assertions.assertTrue(sortFields.contains("description"));
        sortValues.stream().map(SortOrder::isAscending).forEach(sortOrder -> Assertions.assertTrue(sortOrder.equals(true)));
    }

    @Test
    public void parseSort_shouldParseSortOrder() {
	List<SortOrder> sortValues = QueryParamUtils.parseSort(List.of(
                "self.type:asc",
                "self.profile",
                "description:desc"));
        Assertions.assertEquals(3, sortValues.size());
        Assertions.assertEquals(true, sortValues.stream()
        	.filter( sortValue -> sortValue.getField().equals("self.type"))
        	.findFirst()
        	.orElseThrow()
        	.isAscending());
        Assertions.assertEquals(true, sortValues.stream()
        	.filter( sortValue -> sortValue.getField().equals("self.profile"))
        	.findFirst()
        	.orElseThrow()
        	.isAscending());
        Assertions.assertEquals(false, sortValues.stream()
        	.filter( sortValue -> sortValue.getField().equals("description"))
        	.findFirst()
        	.orElseThrow()
        	.isAscending());
    }
}
