package com.trilogy.app.crm.auth;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.trilogy.framework.xhome.support.Base64;

public class AESEncryption
{
	    final static byte[] keyValue = 
                new byte[] {'R', 'e', 'd', 'k', 'n', 'e', 'e', 'P', 'a', 's', 's', 'w', 'o', 'r', 'd', 'E' };
	    public static String NOT_ENCRYPTED = "Not Encrypted";
	    
	    public static String encrypt(String plainText) {   
		  	
	    	try{
		        SecretKeySpec secret = new SecretKeySpec(keyValue, "AES");
		        plainText = "#$%%$#" + plainText ;
		        
		 
		        //encrypt the message
		        Cipher cipher = Cipher.getInstance("AES");
		        cipher.init(Cipher.ENCRYPT_MODE, secret);
		        byte[] encryptedTextBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
				String encryptedValue = Base64.encodeBytes(encryptedTextBytes);
		        return encryptedValue;
	    	}catch(Throwable t){
	    		return plainText;
	    	}
	    }

	    @SuppressWarnings("static-access")
	    public static String decrypt(String encryptedText) 
	    {
	    	try{
		    	byte[] encryptedTextBytes = Base64.decode(encryptedText);
		        SecretKeySpec secret = new SecretKeySpec(keyValue, "AES");
		        // Decrypt the message
		        Cipher cipher = Cipher.getInstance("AES");
		        cipher.init(Cipher.DECRYPT_MODE, secret);
		        byte[] decryptedTextBytes = null;
		        decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
		        String text = new String (decryptedTextBytes);
		        String pad = text.substring(0, 6);
		        if (pad.equals("#$%%$#"))
		        	return new String(text.substring(6,text.length()));
		        else
		        	return "Not Encrypted";
	    	}catch(Throwable t){
	    		return NOT_ENCRYPTED;
	    	}
	    }

	    
	    public static void main(String [] args) throws Throwable{
	    	String text = "Dvb/MjN/Ycp5izICnwALpQ==";
	    	System.out.println(decrypt("Dvb/MjN/Ycp5izICnwALpQ=="));
	    }
}