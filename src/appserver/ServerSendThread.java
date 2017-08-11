package appserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import crypt.MyAES;

public class ServerSendThread implements Runnable {
	//指示是否有断路事件产生
	private Boolean isLinkDown;
	private SecretKeySpec keySpec;
	private ObjectOutputStream oos;
	
	public ServerSendThread(Boolean isLinkDown, SecretKeySpec keySpec, ObjectOutputStream oos) {
		this.isLinkDown = isLinkDown;
		this.keySpec = keySpec;
		this.oos = oos;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void run() {
		Scanner input = new Scanner(System.in);
		while(true) {
			String plainText;
			plainText = input.nextLine();
			try {
				oos.writeObject(MyAES.encrypt(keySpec, plainText.getBytes()));
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | IOException e) {
				//e.printStackTrace();
			}
		}
	}

}
