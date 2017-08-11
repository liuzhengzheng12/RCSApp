package crypt;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MyRSA {
	private static final String KEY_ALGORITHM = "RSA";
	private static final int KEY_SIZE = 2048;
	
	private static RSAPublicKey publicKey;
	private static RSAPrivateKey privateKey;
	
	public static RSAPublicKey getRSAPublic() {
		return publicKey;
	}
	
	public static RSAPrivateKey getRSAPrivate() {
		return privateKey;
	}
	
	public static void generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGenerator.initialize(KEY_SIZE);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		publicKey = (RSAPublicKey)keyPair.getPublic();
		privateKey = (RSAPrivateKey)keyPair.getPrivate();
	}
	
	public static byte[] encryptByPublic(RSAPublicKey publicKey, byte[] plainText) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(plainText);
	}
	
	public static byte[] decryptByPrivate(RSAPrivateKey privateKey, byte[] cipherText) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(cipherText);
	}
	
	
	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		String plainText = "1234567890qwerqwtqwtqwtq1";
		generateKeyPair();
		byte[] e = MyRSA.encryptByPublic(MyRSA.getRSAPublic(), plainText.getBytes());
		byte[] de = MyRSA.decryptByPrivate(MyRSA.getRSAPrivate(), e);
		System.out.println("Cipher Text is "+Utils.bytesToString(e));
		System.out.println("Plain Text is "+Utils.bytesToString(de));
	}
}
