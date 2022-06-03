package simplyrestful.api.framework.test.legacy;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import simplyrestful.api.framework.MediaTypeUtils;
import simplyrestful.api.framework.legacy.LegacyCollectionGet;
import simplyrestful.api.framework.queryparams.SortOrder;
import simplyrestful.api.framework.test.implementation.TestResource;

@SuppressWarnings("deprecation")
@Path("testresources")
@Produces(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
@Consumes(MediaTypeUtils.APPLICATION_HAL_JSON + "; profile=\"" + TestResource.PROFILE_STRING+ "\"")
public class LegacyWebResource implements LegacyCollectionGet<TestResource>{
	public static final List<TestResource> TEST_RESOURCES = new ArrayList<>();

	@Override
	public List<TestResource> list(int pageStart, int pageSize, List<String> fields, String query, List<SortOrder> sort) {
		return TEST_RESOURCES;
	}

	@Override
	public int count(String query) {
	    return TEST_RESOURCES.size();
	}
}
