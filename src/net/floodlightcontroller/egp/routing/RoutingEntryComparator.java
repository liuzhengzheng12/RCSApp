package net.floodlightcontroller.egp.routing;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by wangxuan on 15/5/10.
 */
public class RoutingEntryComparator implements Comparator<RoutingTableEntry>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7448048591088921123L;

	//排序方法，按路径长度从小到大；如路径相同，则按nextHop交换机的switchId从小到大排序。
    @Override
    public int compare(RoutingTableEntry entry1, RoutingTableEntry entry2) {
        if (entry1.getPath().size() != entry2.getPath().size())
            return entry1.getPath().size() - entry2.getPath().size();
        else
            return entry1.getNextHop().getSwitchId() .compareTo(entry2.getNextHop().getSwitchId());
    }
}
