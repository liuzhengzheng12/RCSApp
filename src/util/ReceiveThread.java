package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.egp.routing.RoutingIndex;
import net.floodlightcontroller.egp.routing.RoutingPriorityQueue;


public class ReceiveThread extends Thread {
	private ObjectInputStream ois;
	private Map<String, String> Port2Isdown = new HashMap<String, String>();
	private Map<String, String> Port2Isup = new HashMap<String, String>();
	private Map<String, String> Swid2As = new HashMap<String, String>();
	private Map<RoutingIndex, RoutingPriorityQueue> routes;
	private volatile Boolean flag = false;
	
	public ReceiveThread(ObjectInputStream ois, Map<String, String> Port2Isdown, Map<String, String> Port2Isup, Map<String, String> Swid2As) {
		this.ois = ois;
		this.Port2Isdown = Port2Isdown;
		this.Port2Isup = Port2Isup;
		this.Swid2As = Swid2As;
	}
	
	public Boolean getFlag() {
		return flag;
	}
	
	public Map<RoutingIndex, RoutingPriorityQueue> getRoutes() {
		flag = false;
		return routes;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		//监听之后的链路断开以及恢复的情况
		try {
			while(true) {
				String cmd = (String)ois.readObject();
				System.out.println(cmd);
				if (cmd.equals("Route")) {
					routes = (Map<RoutingIndex, RoutingPriorityQueue>)(ois.readObject());
					if (routes == null) {
						System.out.println("Error");
					}
					flag = true;
				}
				else {
					String localswid = (String)ois.readObject();
					String localport = (String)ois.readObject();
					if (cmd.equals("LinkDown")) {
						System.out.printf("LinkDown: %s\n", localswid+" "+localport);
						Port2Isdown.put(Swid2As.get(localswid), localport);
					}
					if (cmd.equals("LinkUp")) {
						System.out.printf("LinkUp: %s\n", localswid+" "+localport);
						Port2Isup.put(Swid2As.get(localswid), localport);
					}
					Thread.sleep(1000);
				}
			}
		} catch (ClassNotFoundException | IOException | InterruptedException e) {
		}
	}
}
