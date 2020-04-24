package example.jetty.resources.dao;

import javax.ws.rs.ext.Provider;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.message.Message;

@Provider
public class ExampleEntityDAOProvider implements ContextProvider<ExampleEntityDAO>{
	private ExampleEntityDAO dao = new ExampleEntityDAOImpl();

	@Override
	public ExampleEntityDAO createContext(Message message) {
		return dao;
	}
}
