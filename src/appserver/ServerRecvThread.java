package appserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import crypt.MyAES;
import crypt.Utils;
import util.IdRouteTable;

public class ServerRecvThread implements Runnable {
	//AS号与IP地址的映射
	private Map<Integer, String> As2Ip = new HashMap<Integer, String>();
	//AS号与正在使用的路由表的映射
	private Map<Integer, IdRouteTable> As2Table = new HashMap<Integer, IdRouteTable>();
	//AS号与次优路径表的映射
	private Map<Integer, IdRouteTable> As2SubTable = new HashMap<Integer, IdRouteTable>();
	//指示是否有断路事件产生
	private Boolean isLinkDown;
	private SecretKeySpec keySpec;
	private ObjectInputStream ois;
	
	public ServerRecvThread(Boolean isLinkDown, Map<Integer, String> As2Ip, Map<Integer, IdRouteTable> As2Table, Map<Integer, IdRouteTable> As2SubTable, SecretKeySpec keySpec, ObjectInputStream ois) {
		this.isLinkDown = isLinkDown;
		this.As2Ip = As2Ip;
		this.As2Table = As2Table;
		this.As2SubTable = As2SubTable;
		this.keySpec = keySpec;
		this.ois = ois;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				String plainText;
				plainText = Utils.bytesToString(MyAES.decrypt(keySpec, (byte[])ois.readObject()));
				System.out.println("server receive: "+plainText);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | IOException | ClassNotFoundException e) {
				//e.printStackTrace();
			}
		}
	}

}
