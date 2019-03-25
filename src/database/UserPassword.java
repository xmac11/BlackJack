package database;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Class that encrypts users' passwords
 *
 * @author Group21
 *
 */

/* Modifications made from: 
 * https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html */
public class UserPassword {
	
	private static int derivedKeyLength = 256; // SHA-256 generates 256-bit hashes

	public static String getEncryptedPassword(String password, String saltHex) throws NoSuchAlgorithmException,
	InvalidKeySpecException {
		// convert hex to byte array
		byte[] salt = decodeHexString(saltHex);
		
		// PBKDF2 with SHA-256 as the hashing algorithm
		String algorithm = "PBKDF2WithHmacSHA256";

		// A minimum iteration count of 1000 is recommended by the National Institute of Standards and Technology
		int iterations = 1000;

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);		 
		SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);		 
		SecretKey secretKey = skf.generateSecret(spec);

		return bytesToHex(secretKey.getEncoded());
	}

	public static byte[] generateSalt() throws NoSuchAlgorithmException {		 
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		// Generate a 16 byte (128 bit) salt as recommended by RSA PKCS5
		byte[] salt = new byte[16];
		random.nextBytes(salt);

		return salt;
	}

	/* The following are helper methods for converting between hex to byte arrays and vice versa
	 * source:  https://www.baeldung.com/java-byte-arrays-hex-strings */
	public static String bytesToHex(byte[] arr) {
		final StringBuilder builder = new StringBuilder();
		for(byte b : arr) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

	// Hexadecimal String to Byte Array
	public static byte[] decodeHexString(String hexString) {
		if (hexString.length() % 2 == 1) {
			throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
		}

		byte[] bytes = new byte[hexString.length() / 2];
		for (int i = 0; i < hexString.length(); i += 2) {
			bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
		}
		return bytes;
	}

	// Hexadecimal to Byte
	public static byte hexToByte(String hexString) {
		int firstDigit = Character.digit(hexString.charAt(0), 16);
		int secondDigit = Character.digit(hexString.charAt(1), 16);
		return (byte) ((firstDigit << 4) + secondDigit);
	}
}


