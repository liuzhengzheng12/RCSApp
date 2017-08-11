package util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RouteTable implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3625520147283135979L;
	//路由表：表项为目的IP地址与路径的映射
	private Map<String, String> routeTable = new HashMap<String, String>();
	
	public synchronized void printTable() {
		for (Map.Entry<String, String> entry: routeTable.entrySet()) {
			System.out.printf("dstip: %s, path: %s\n", entry.getKey(), entry.getValue());
		}
	}
	
	public synchronized void clear() {
		routeTable.clear();
	}
	
	public synchronized RouteTable clone() {
		RouteTable t = new RouteTable();
		for (Map.Entry<String, String> entry: routeTable.entrySet()) {
			t.setPath(entry.getKey(), entry.getValue());
		}
		return t;
	}
	public synchronized String getPath(String dstIp) {
		return routeTable.get(dstIp);
	}
	
	public synchronized Set<Entry<String, String> > getEntrySet() {
		return routeTable.entrySet();
	}
	
	public synchronized void setPath(String dstIp, String path) {
		routeTable.put(dstIp, path);
	}
}
