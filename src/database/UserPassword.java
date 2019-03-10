package database;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Modifications made from: https://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
public class UserPassword {
	
	private String username;
	private String password;	
	private int derivedKeyLength = 160; // SHA-1 generates 160-bit hashes
	
	public UserPassword(String username, String password) {
		this.username = username;
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	
	public boolean authenticate(String attemptedPassword, byte[] encryptedStoredPasswordSalt) throws NoSuchAlgorithmException, 
																							InvalidKeySpecException {
		byte[] encryptedStoredPassword = Arrays.copyOfRange(encryptedStoredPasswordSalt, 0, derivedKeyLength/8);					
		byte[] salt = Arrays.copyOfRange(encryptedStoredPasswordSalt, derivedKeyLength/8, derivedKeyLength/8 + 8); 
		
		// Encrypt the plain-text password the user entered using the same salt that was used to encrypt the original password
		byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

		// Authentication succeeds if encrypted password that the user entered is equal to the stored hash
		return Arrays.equals(encryptedStoredPassword, encryptedAttemptedPassword);
	}
	
	 public byte[] getEncryptedPassword(String password, byte[] salt) throws NoSuchAlgorithmException,
			 																InvalidKeySpecException {
		 // PBKDF2 with SHA-1 as the hashing algorithm
		 String algorithm = "PBKDF2WithHmacSHA1";
		 
		 // A minimum iteration count of 1000 is recommended by the National Institute of Standards and Technology
		 int iterations = 1000;
		 
		 KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);		 
		 SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);		 
		 SecretKey secretKey = skf.generateSecret(spec);
		 			 
		 return secretKey.getEncoded();
	 }
	 
	 public byte[] generateSalt() throws NoSuchAlgorithmException {		 
		 SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		 // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		 byte[] salt = new byte[8];
		 random.nextBytes(salt);

		 return salt;
	 }

	 public byte[] appendSalt(byte[] encryptedPassword, byte[] salt) {
		 int totalLength = encryptedPassword.length + salt.length;
		 // create new array to contain password and salt 
		 byte[] pwdSalt = new byte[totalLength];
		 
		 // insert the password in the array
		 for(int i = 0; i < encryptedPassword.length; i++) {
			 pwdSalt[i] = encryptedPassword[i];
		 }
		 // then insert the salt in the array
		 for(int i = encryptedPassword.length, j = 0; i < totalLength && j < salt.length; i++,j++) {
			 pwdSalt[i] = salt[j];
		 }
		 return pwdSalt;
	 }
	 
	 //(original key length: 20bytes, salt: 8bytes -> produced hash length: 40 + 16 = 56)
	 public String bytesToHex(byte[] arr) {
		 final StringBuilder builder = new StringBuilder();
		 for(byte b : arr) {
			 builder.append(String.format("%02x", b));
		 }
		 return builder.toString();
	 }
	 
	 public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
		 UserPassword user1 = new UserPassword("cxm881", "pass");
		 byte[] salt1 = user1.generateSalt();		 
		 		 
		 // password (encrypted)
		 byte[] encryptedPassword1 = user1.getEncryptedPassword(user1.getPassword(), salt1);		 
		 byte[] pwdSalt1 = user1.appendSalt(encryptedPassword1, salt1);
		 
		 System.out.println(user1.bytesToHex(pwdSalt1));
		 System.out.println(user1.authenticate("pass", pwdSalt1));				 	
		 System.out.println(user1.authenticate("wrongPass", pwdSalt1));				 	
	}	
}


