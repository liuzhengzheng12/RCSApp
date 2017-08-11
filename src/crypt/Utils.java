package crypt;

public class Utils {
	public static String bytesToString(byte[] bytes) {
		String result = "";
		for (byte b: bytes) {
			result += (char)b;
		}
		return result;
	}
}
