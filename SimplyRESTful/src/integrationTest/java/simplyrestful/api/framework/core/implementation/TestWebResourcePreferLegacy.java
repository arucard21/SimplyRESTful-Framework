package simplyrestful.api.framework.core.implementation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.api.webresource.DefaultCollectionGetPreferLegacy;
import simplyrestful.api.framework.test.implementation.TestResource;

@Path("testresources")
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class TestWebResourcePreferLegacy implements DefaultCollectionGetPreferLegacy<TestResource>{
    @Override
    public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, Map<String, Boolean> sort) {
        return Collections.emptyList();
    }

    @Override
    public int count(String query) {
        return 0;
    }

}
