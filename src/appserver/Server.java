package appserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import crypt.MyRSA;
import util.IdRouteTable;

public class Server implements Runnable {
	private int port;
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream oos ;
	private ObjectInputStream ois;
	private SecretKeySpec keySpec;
	//AS号与IP地址的映射
	private Map<Integer, String> As2Ip = new HashMap<Integer, String>();
	//AS号与正在使用的路由表的映射
	private Map<Integer, IdRouteTable> As2Table = new HashMap<Integer, IdRouteTable>();
	//AS号与次优路径表的映射
	private Map<Integer, IdRouteTable> As2SubTable = new HashMap<Integer, IdRouteTable>();
	//指示是否有断路事件产生
	private Boolean isLinkDown;
	
	public Server(Boolean isLinkDown, Map<Integer, String> As2Ip, Map<Integer, IdRouteTable> As2Table, Map<Integer, IdRouteTable> As2SubTable, int port) {
		this.isLinkDown = isLinkDown;
		this.port = port;
		this.As2Ip = As2Ip;
		this.As2Table = As2Table;
		this.As2SubTable = As2SubTable;
	}
	
	
	private void sendRSAPublic() {
		try {
			oos.writeObject(MyRSA.getRSAPublic());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void recvAES() {
		byte[] cipherKey;
		byte[] strkey;
		try {
			cipherKey = (byte[]) ois.readObject();
			strkey = MyRSA.decryptByPrivate(MyRSA.getRSAPrivate(), cipherKey);
			keySpec = new SecretKeySpec(strkey, "AES");
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | ClassNotFoundException | IOException e) {
			//e.printStackTrace();
		}  
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				socket = serverSocket.accept();
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());
				
				MyRSA.generateKeyPair();
				sendRSAPublic();
				recvAES();
				
				new Thread(new ServerSendThread(isLinkDown, keySpec, oos)).start();
				new Thread(new ServerRecvThread(isLinkDown, As2Ip, As2Table, As2SubTable, keySpec, ois)).start();
			}
		} catch (IOException | NoSuchAlgorithmException e) {
		}
	}
}
