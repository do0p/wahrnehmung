package at.lws.wnm.server.dao;

import java.util.Iterator;
import java.util.List;

public class AbstractDao {

	public AbstractDao() {
		DaoRegistry.register(this);
	}

	public static String join(List<String> subQueries, String seperator) {
		if(subQueries == null || subQueries.isEmpty())
		{
			return "";
		}
		final StringBuilder result = new StringBuilder();
		final Iterator<String> iterator = subQueries.iterator();
		result.append(iterator.next());
		while(iterator.hasNext())
		{
			result.append(seperator);
			result.append(iterator.next());
		}
		return result.toString();
	}
}
