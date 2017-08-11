package crypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MyAES {
	private static final String KEY_ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	
	private static SecretKey key;
	private static SecretKeySpec keySpec;
	
	public static SecretKeySpec getAESKeySpec() {
		return keySpec;
	}
	
	public static SecretKey getAESKey() {
		return key;
	}
	
	public static void generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
		keyGenerator.init(KEY_SIZE);
		key = keyGenerator.generateKey();
		byte[] enCodeFormat = key.getEncoded();
		keySpec = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
	}
	
	public static byte[] encrypt(SecretKeySpec keySpec, byte[] plainText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		return cipher.doFinal(plainText);
	}
	
	public static byte[] decrypt(SecretKeySpec keySpec, byte[] cipherText) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		return cipher.doFinal(cipherText);
	}

	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String plainText = "1234567890qwerqwtqwtqwtq1";
		generateKey();
		byte[] e = MyAES.encrypt(MyAES.getAESKeySpec(), plainText.getBytes());
		byte[] de = MyAES.decrypt(MyAES.getAESKeySpec(), e);
		System.out.println("Cipher Text is "+Utils.bytesToString(e));
		System.out.println("Plain Text is "+Utils.bytesToString(de));
	}
}
