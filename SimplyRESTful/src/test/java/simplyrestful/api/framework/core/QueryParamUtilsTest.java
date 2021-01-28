package simplyrestful.api.framework.core;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QueryParamUtilsTest {
    @Test
    public void stripHALStructureOnList_shouldStripHALStructureFromEachString() {
        List<String> stripped = QueryParamUtils.stripHALStructure(List.of(
                "_embedded._links.self.type",
                "_links.self.profile",
                "_embedded.description"));
        Assertions.assertEquals(3, stripped.size());
        Assertions.assertEquals("self.type", stripped.get(0));
        Assertions.assertEquals("self.profile", stripped.get(1));
        Assertions.assertEquals("description", stripped.get(2));
    }

    @Test
    public void stripHALStructureOnList_shouldSeparateEntriesDelimitedByCommaAndIgnoreEmptyAndBlankEntries() {
        List<String> stripped = QueryParamUtils.stripHALStructure(List.of(
                "_embedded._links.self.type, _links.self.profile,,,,             ,",
                "_embedded.description"));
        Assertions.assertEquals(3, stripped.size());
        Assertions.assertEquals("self.type", stripped.get(0));
        Assertions.assertEquals("self.profile", stripped.get(1));
        Assertions.assertEquals("description", stripped.get(2));
    }

    @Test
    public void stripHALStructureOnList_shouldRemoveEntriesThatBecomeBlankAfterStrippingHALStructure() {
        List<String> stripped = QueryParamUtils.stripHALStructure(List.of(
                "_embedded._embedded._embedded._links",
                "_links",
                "_embedded"));
        Assertions.assertEquals(0, stripped.size());
    }

    @Test
    public void stripHALStructureOnString_shouldRemoveHALLinksParts() {
        String stripped = QueryParamUtils.stripHALStructure("_links.self.type==application/hal+json");
        Assertions.assertEquals("self.type==application/hal+json", stripped);
    }

    @Test
    public void stripHALStructureOnString_shouldRemoveHALEmbeddedParts() {
        String stripped = QueryParamUtils.stripHALStructure("_embedded.description==something");
        Assertions.assertEquals("description==something", stripped);
    }

    @Test
    public void stripHALStructureOnString_shouldRemoveBothHALLinksAndHALEmbeddedParts() {
        String stripped = QueryParamUtils.stripHALStructure("_embedded._links.self.type==application/hal+json;_links.self.profile==testing;_embedded.description==quite long description");
        Assertions.assertEquals("self.type==application/hal+json;self.profile==testing;description==quite long description", stripped);
    }

    @Test
    public void stripHALStructureOnString_shouldReturnEmptyString_whenProvidedStringOnlyContainsHALStructure() {
        Assertions.assertEquals("", QueryParamUtils.stripHALStructure("_embedded"));
        Assertions.assertEquals("", QueryParamUtils.stripHALStructure("_links"));
        Assertions.assertEquals("", QueryParamUtils.stripHALStructure("_embedded._links"));
        Assertions.assertEquals("", QueryParamUtils.stripHALStructure("_embedded._embedded._links"));
        Assertions.assertEquals("", QueryParamUtils.stripHALStructure("_links._links._embedded"));
    }

    @Test
    public void parseSort_shouldStripHALStructureAndOrderAscendingByDefault() {
        Map<String, Boolean> sortValues = QueryParamUtils.parseSort(List.of(
                "_embedded._links.self.type",
                "_links.self.profile",
                "_embedded.description"));
        Assertions.assertEquals(3, sortValues.size());
        Assertions.assertTrue(sortValues.keySet().contains("self.type"));
        Assertions.assertTrue(sortValues.keySet().contains("self.profile"));
        Assertions.assertTrue(sortValues.keySet().contains("description"));
        sortValues.values().forEach(sortOrder -> Assertions.assertTrue(sortOrder.equals(true)));
    }

    @Test
    public void parseSort_shouldStripHALStructureAndParseSortOrder() {
        Map<String, Boolean> sortValues = QueryParamUtils.parseSort(List.of(
                "_embedded._links.self.type:asc",
                "_links.self.profile",
                "_embedded.description:desc"));
        Assertions.assertEquals(3, sortValues.size());
        Assertions.assertEquals(true, sortValues.get("self.type"));
        Assertions.assertEquals(true, sortValues.get("self.profile"));
        Assertions.assertEquals(false, sortValues.get("description"));
    }
}
