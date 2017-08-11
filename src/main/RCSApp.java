package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.egp.routing.RoutingIndex;
import net.floodlightcontroller.egp.routing.RoutingPriorityQueue;
import util.ReceiveThread;
import util.RouteTable;
import util.IdRouteTable;

public class RCSApp {
	//AS号与IP地址的映射
	private Map<String, String> As2Ip = new HashMap<String, String>();
	//IP地址与AS号的映射
	private Map<String, String> Ip2As = new HashMap<String, String>();
	//AS号与交换机号的映射
	private Map<String, String> As2Swid = new HashMap<String, String>();
	//交换机号与AS号的映射
	private Map<String, String> Swid2As = new HashMap<String, String>();
	//app与各个域中通信输入接口
	private Map<String, ObjectInputStream> As2In = new HashMap<String, ObjectInputStream>();
	//app与各个域中通信输出接口
	private Map<String, ObjectOutputStream> As2Out = new HashMap<String, ObjectOutputStream>();
	//AS号与对应接收线程的映射
	private Map<String, ReceiveThread> As2Recv = new HashMap<String, ReceiveThread>();
	//边及对应的出端口映射
	private Map<String, String> Edge2Port = new HashMap<String, String>();
	//AS号与正在使用的路由表的映射
	private Map<String, IdRouteTable> As2Table = new HashMap<String, IdRouteTable>();
	//AS号与次优路径表的映射
	private Map<String, IdRouteTable> As2SubTable = new HashMap<String, IdRouteTable>();
	//AS号与备用的路由表的映射
	private Map<String, IdRouteTable> As2BackTable = new HashMap<String, IdRouteTable>();
	//down的交换机的端口
	private Map<String, String> Port2Isdown = new HashMap<String, String>();
	//up的交换机的端口
	private Map<String, String> Port2Isup = new HashMap<String, String>();
	//断路时启用的备用路由path
	private Map<String, String> BackRoute = new HashMap<String, String>();

	//建立和各个AS的连接，总计as_num个AS
	private void createComm(Integer as_num) {
		try {
			ServerSocket serverSocket = new ServerSocket(9000);
			Integer as_cnt = as_num;
			while((as_cnt--)!= 0) {
				Socket socket = serverSocket.accept();
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				//接收AS号和对应的IP地址
				String id = (String)ois.readObject();
				String ip = (String)ois.readObject();
				String swid = (String)ois.readObject();
				System.out.printf("%s %s %s\n", id, ip, swid);
				As2Out.put(id, oos);
				As2In.put(id, ois);
				As2Ip.put(id, ip);
				Ip2As.put(ip, id);
				As2Swid.put(id, swid);
				Swid2As.put(swid, id);
				
				//接受边和对应的出端口
				Integer edge_num = (Integer)ois.readObject();
				while((edge_num--) != 0) {
					String remoteid = (String)ois.readObject();
					String outport = (String)ois.readObject();
					Edge2Port.put(id+" "+remoteid, outport);
					System.out.printf("%s %s: %s\n", id, remoteid, outport);
				}
			}
			serverSocket.close();
		} catch (IOException | ClassNotFoundException e) {
		}	
	}
	
	
	//创建并启动各个AS对应的链路状态监听线程
	@SuppressWarnings("unchecked")
	private void createRecvThread() {
		for (Map.Entry<String, ObjectInputStream> item: As2In.entrySet()) {
			//首先接收收敛之后的BGP路由信息
			try {
				String local_id = item.getKey();
				ObjectInputStream ois = item.getValue();
				IdRouteTable routeTable = new IdRouteTable();
				IdRouteTable backRouteTable = new IdRouteTable();
				RouteTable table = new RouteTable();
				RouteTable btable = new RouteTable();
				table = (RouteTable)ois.readObject();
				btable = (RouteTable)ois.readObject();
				
				System.out.println("RouteTable:");
				for (Map.Entry<String, String> entry: table.getEntrySet()) {
					String dstId = Ip2As.get(entry.getKey());
					routeTable.setPath(dstId, entry.getValue());
					System.out.printf("local_id %s , dstId %s : %s\n", local_id, dstId, entry.getValue());
				}
				
				System.out.println("BackRouteTable:");
				for (Map.Entry<String, String> entry: btable.getEntrySet()) {
					String dstId = Ip2As.get(entry.getKey());
					backRouteTable.setPath(dstId, entry.getValue());
					System.out.printf("local_id %s , dstId %s : %s\n", local_id, dstId, entry.getValue());
				} 
					
				As2Table.put(local_id, routeTable);
				As2SubTable.put(local_id, backRouteTable);

				ReceiveThread t = new ReceiveThread(item.getValue(), Port2Isdown, Port2Isup, Swid2As);
				t.start();
				As2Recv.put(local_id, t);
			} catch (ClassNotFoundException | IOException e) {
				//e.printStackTrace();
			}
		}	
	}
	
	//获取linkup之后重收敛的BGP路由信息
	@SuppressWarnings("unchecked")
	private void updateRoutes() {
		
		try {
			for (String as_id: As2Ip.keySet()) {
				ObjectOutputStream oos = As2Out.get(as_id);
				oos.writeObject("Retransmit");
				Map<RoutingIndex, RoutingPriorityQueue> routes;
				ReceiveThread t = As2Recv.get(as_id);
				while(!t.getFlag()) {
				}
				routes = t.getRoutes();
				IdRouteTable routeTable = new IdRouteTable();
				IdRouteTable backRouteTable = new IdRouteTable();
				for (Map.Entry<RoutingIndex, RoutingPriorityQueue> entry: routes.entrySet()) {
					RoutingPriorityQueue queue = entry.getValue();
					String dstId = Ip2As.get(entry.getKey().getDstIp());
					String now_path = convertToString(as_id, queue.getPoll().getPath());
					String back_path = null;
					if (queue.getTop() != null) {
						back_path = convertToString(as_id, queue.getPoll().getPath());
					}
					routeTable.setPath(dstId, now_path);
					backRouteTable.setPath(dstId, back_path);
					System.out.printf("update: local_id %s , dstId %s : %s  %s\n", as_id, dstId, now_path, back_path);
				}
				As2Table.put(as_id, routeTable);
				As2SubTable.put(as_id, backRouteTable);
			}
		} catch (IOException e) {
				//e.printStackTrace();
		}		
	}
	
	
	private String convertToString(String id, List<String> list) {
		String str = id;
		Integer end = list.size()-1;
		for (int i = 0; i < end; i++) {
			str += (" "+list.get(i));
		}
		
		return str;
	}
	
	//检测路径中是否有环路, true表示有环路
	private boolean loop(String path) {
		Map<String, Integer> Id2Cnt = new HashMap<String, Integer>();
		String[] seg = path.split(" ");
		for (String id: seg) {
			if (Id2Cnt.containsKey(id)) {
				return true;
			}
			else {
				Id2Cnt.put(id, 1);
			}
		}
		return false;
	}
	
	
	//计算两条路径的重复边数
	private Integer cal_repeat(String path, String optPath) {
		Integer repeat = 0;
		String[] seg1 = path.split(" ");
		String[] seg2 = optPath.split(" ");
		int end1 = seg1.length - 1;
		int end2 = seg2.length - 1;
		Map<String, Integer> Edge2Cnt = new HashMap<String, Integer>();
		for (int i = 0; i < end2; i++) {
			Edge2Cnt.put(seg2[i]+" "+seg2[i+1], 1);
		}
		
		for (int i = 0; i < end1; i++) {
			if (Edge2Cnt.get(seg1[i]+" "+seg1[i+1]) != null) {
				repeat++;
			}
		}
		
		return repeat;
	}
	
	
	//计算各个自治域的备份路由表
	void calBackTable() {
		for (String srcId: As2Ip.keySet()) {
			IdRouteTable back_table = new IdRouteTable();
			IdRouteTable as_table = As2Table.get(srcId);
			IdRouteTable as_subtable = As2SubTable.get(srcId);
			for (String dstId: As2Ip.keySet()) {
				if (srcId == dstId) {
					continue;
				}
				Integer repeat = Integer.MAX_VALUE;
				String bgpPath = as_table.getPath(dstId);
				String backPath = null;
				for (String midId: As2Ip.keySet()) {
					if (midId == srcId || midId == dstId) {
						continue;
					}
					Integer t_len = as_table.getPath(midId).length();
					Integer s_len = as_subtable.getPath(midId).length();
					String[] paths = new String[1];   //四种可能的路径组合方案
					//paths[0] = as_table.getPath(midId).substring(0, t_len-1) + As2Table.get(midId).getPath(dstId);
					paths[0] = as_table.getPath(midId).substring(0, t_len-1) + As2SubTable.get(midId).getPath(dstId);
					//paths[2] = as_subtable.getPath(midId).substring(0, s_len-1) + As2Table.get(midId).getPath(dstId);
					//paths[3] = as_subtable.getPath(midId).substring(0, s_len-1) + As2SubTable.get(midId).getPath(dstId);
					
					for (String path: paths) {
						if (loop(path)) {
							continue;
						}
						else {
							Integer t = cal_repeat(path, bgpPath);
							if (repeat > t) {
								repeat = t;
								backPath = path;
							}
						}
					}
				}
				if (backPath != null) {
					System.out.printf("src: %s, dst: %s, path: %s\n", srcId, dstId, backPath);
					back_table.setPath(dstId, backPath);
				}
			}
			As2BackTable.put(srcId, back_table);
		}
	}
	
	
	//处理断路事件
	void handleLinkDownEvent() {
		String linkDownEdge = "";
		for (String as_id: Port2Isdown.keySet()) {
			linkDownEdge += as_id + " ";
		}
		linkDownEdge = linkDownEdge.trim();
		System.out.printf("linkdownEdge is: %s\n", linkDownEdge);
		Port2Isdown.clear();
		for (String srcId: As2Ip.keySet()) {
			IdRouteTable back_table = As2BackTable.get(srcId);
			IdRouteTable as_table = As2Table.get(srcId);
			for (String dstId: As2Ip.keySet()) {
				if (srcId == dstId) {
					continue;
				}
				String optPath = as_table.getPath(dstId);
				//检查正在使用的路径中是否有断开的链路
				if ((optPath.contains(linkDownEdge)) || (optPath.contains(new StringBuilder(linkDownEdge).reverse().toString()))) {
					//需要启用备份路径
					String backPath = back_table.getPath(dstId);
					if (backPath ==  null) {
						continue;
					}
					//检查备份路径中是否有断开的链路
					if ((backPath.contains(linkDownEdge)) || (backPath.contains(new StringBuilder(linkDownEdge).reverse().toString()))) {
						System.out.println("Back route can't be used");
					}
					else {
						BackRoute.put(srcId+" "+dstId, backPath);
						System.out.printf("optPath: %s\n", optPath);
						System.out.printf("backPath: %s\n", backPath);
						createFlowMods(backPath);
					}
				}
			}
		}
	}
	
	
	//处理链路重新连通事件
	void handleLinkUpEvent() {
		Port2Isup.clear();
		for (Map.Entry<String, String> entry: BackRoute.entrySet()) {
			deleteFlowMods(entry.getValue());
			System.out.printf("backPath: %s\n", entry.getValue());
		}
		BackRoute.clear();
	}
	
	//删除路由表项
	private void deleteFlowMods(String path) {
		String id[] = path.split(" ");
		ObjectOutputStream oos;
		int len = id.length;
		String srcIp = As2Ip.get(id[0]);
		String dstIp = As2Ip.get(id[len-1]);
		for (int i= 0; i < len-1; i++){
			oos = As2Out.get(id[i]);
			try {
				oos.writeObject("Delete");
				//发送swid, srcip, dstip, outport
				oos.writeObject(As2Swid.get(id[i]));
				oos.writeObject(srcIp);
				oos.writeObject(dstIp);
				oos.writeObject(Edge2Port.get(id[i]+" "+id[i+1]));
				String loginfo = "DeleteFlowMods:" +
			 			 "\n---swichId: " + As2Swid.get(id[i]) + 
			 			 "\n---srcIp: " + srcIp +
			 			 "\n---dstIp: " + dstIp +
			 			 "\n---outPort:" + Edge2Port.get(id[i]+" "+id[i+1])+"\n";
				System.out.println(loginfo);
			} catch (IOException e) {
				//e.printStackTrace();
			}
			
		}
	}
	
	//下发路由表项
	private void createFlowMods(String path) {
		String id[] = path.split(" ");
		ObjectOutputStream oos;
		int len = id.length;
		String srcIp = As2Ip.get(id[0]);
		String dstIp = As2Ip.get(id[len-1]);
		for (int i= 0; i < len-1; i++){
			oos = As2Out.get(id[i]);
			try {
				oos.writeObject("Create");
				//发送swid, srcip, dstip, outport
				oos.writeObject(As2Swid.get(id[i]));
				oos.writeObject(srcIp);
				oos.writeObject(dstIp);
				oos.writeObject(Edge2Port.get(id[i]+" "+id[i+1]));
				String loginfo = "CreateFlowMods:" +
			 			 "\n---swichId: " + As2Swid.get(id[i]) + 
			 			 "\n---srcIp: " + srcIp +
			 			 "\n---dstIp: " + dstIp +
			 			 "\n---outPort:" + Edge2Port.get(id[i]+" "+id[i+1])+"\n";
				System.out.println(loginfo);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void init(Integer as_num) {
		createComm(as_num);
		createRecvThread();
	}
	
	public void work() {
		while(true) {
			if (Port2Isdown.size() == 2) {
				handleLinkDownEvent();
			}
			if (Port2Isup.size() == 2 ) {
				handleLinkUpEvent();
				//updateRoutes();
				//calBackTable();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}
	
	
	public static void main(String[] args) {
		//从命令行读取要连接的AS数目as_num
		Integer as_num = Integer.parseInt(args[0]);
		RCSApp app = new RCSApp();
		app.init(as_num);
		app.calBackTable();
		app.work();
	}
}
