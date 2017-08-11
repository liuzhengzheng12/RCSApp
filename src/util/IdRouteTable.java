package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IdRouteTable {
	//路由表：表项为目的AS号与路径的映射
	private Map<String, String> routeTable = new HashMap<String, String>();
	
	public String getPath(String dstId) {
		return routeTable.get(dstId);
	}
	
	public Set<Entry<String, String> > getEntrySet() {
		return routeTable.entrySet();
	}
	
	public void setPath(String dstId, String path) {
		routeTable.put(dstId, path);
	}
}
