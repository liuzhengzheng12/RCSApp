package net.floodlightcontroller.egp.routing;

import java.io.Serializable;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Created by wangxuan on 15/5/10.
 */
public class RoutingPriorityQueue implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2682311940544668570L;



    PriorityQueue<RoutingTableEntry> queue = new PriorityQueue<RoutingTableEntry>(50, new RoutingEntryComparator());


    // Attention!!
    public RoutingPriorityQueue() {
    	
    }
    
    
    public RoutingPriorityQueue(PriorityQueue<RoutingTableEntry> queue) {
    	this.queue = queue;
    }
    
    public PriorityQueue<RoutingTableEntry> getQueue() {
    	return queue;
    }
    
    public synchronized boolean update(RoutingTableEntry entry) {
        this.remove(entry.getNextHop());
        if (entry.isEmpty()) return false;
        queue.add(entry);
        return false;
    }

    public synchronized boolean remove(HopSwitch hopSwitch) {
        Iterator<RoutingTableEntry> iterator = queue.iterator();
        if (queue.isEmpty()) return false;
        boolean ret = false;
        while (iterator.hasNext()) {
            RoutingTableEntry e = iterator.next();
            if (e.getNextHop().equals(hopSwitch)) {
                RoutingTableEntry e2 = this.getTop();
                if (e.getNextHop().equals(e2.getNextHop())) ret = true;
                queue.remove(e);
                break;
            }
        }
        return ret;
    }
    
    //返回优先级队列的头部路由表项（即path路径长度最短的entry）
    public synchronized RoutingTableEntry getTop() {
        return queue.peek();
    }
    
    public synchronized RoutingTableEntry getPoll() {
    	return queue.poll();
    }
    
    
    //打印RoutingPriorityQueue中的RoutingTableEntry
    public void printAll() {
        System.out.println("RoutingPriorityQueue size:" + queue.size());
        Iterator<RoutingTableEntry> iterator = queue.iterator();
        while (iterator.hasNext()) {
            RoutingTableEntry e = iterator.next();
            System.out.println(e.toString());
        }
    }
}
