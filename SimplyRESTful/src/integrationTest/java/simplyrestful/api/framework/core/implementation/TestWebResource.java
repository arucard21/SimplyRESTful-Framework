package simplyrestful.api.framework.core.implementation;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.core.MediaTypeUtils;
import simplyrestful.api.framework.core.SortOrder;
import simplyrestful.api.framework.core.webresource.api.implementation.DefaultCollectionGet;
import simplyrestful.api.framework.test.implementation.TestResource;

@Path("testresources")
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class TestWebResource implements DefaultCollectionGet<TestResource>{
    @Override
    public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
        return Collections.emptyList();
    }

    @Override
    public int count(String query) {
        return 0;
    }

}
