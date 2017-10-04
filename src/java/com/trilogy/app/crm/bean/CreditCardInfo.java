package com.trilogy.app.crm.bean;

import com.trilogy.app.crm.util.cipher.CrmCipher;
import com.trilogy.app.crm.util.cipher.CrmEncryptingException;
import com.trilogy.app.crm.util.cipher.Encrypted;
import com.trilogy.app.crm.validator.CreditCardNumberValidator;


public class CreditCardInfo extends AbstractCreditCardInfo
implements Encrypted
{
	synchronized public void encrypt(CrmCipher cipher) throws CrmEncryptingException
	{
		try 
		{
			if (this.getCardNumber()== null && this.getCardNumber().trim().length() < 1)
			{
				throw new Exception("invalid Credit card number"); 
			}
		
			
			if(this.getCardNumber()!=null && !this.getCardNumber().isEmpty() && !this.getCardNumber().startsWith(Encrypted.ENCRYPTED_MASK_PREFIX) )
			{
				this.setEncodedCreditCardNumber(cipher.encode(this.getCardNumber()));
				this.setDecodedCreditCardNumber ( this.getCardNumber()); 
				this.setCardNumber( Encrypted.ENCRYPTED_MASK_PREFIX + this.getDecodedCreditCardNumber().trim().substring(
						this.getDecodedCreditCardNumber().trim().length() -4));
			}
		} catch (Exception e)
		{
			CrmEncryptingException ex =  new CrmEncryptingException();
			ex.setStackTrace(e.getStackTrace());
			throw ex; 
		}
			
	}
	
	synchronized public void decrypt(CrmCipher cipher) throws CrmEncryptingException
	{
		try 
		{
			if(this.getEncodedCreditCardNumber()!=null  && !this.getEncodedCreditCardNumber().isEmpty())
			{
				this.setDecodedCreditCardNumber ( cipher.decode(this.getEncodedCreditCardNumber()));
				this.setCardNumber( Encrypted.ENCRYPTED_MASK_PREFIX + this.getDecodedCreditCardNumber().trim().substring(
					this.getDecodedCreditCardNumber().trim().length() -4));
			
			} 
		} catch (Exception e)
		{
			CrmEncryptingException ex =  new CrmEncryptingException();
			ex.setStackTrace(e.getStackTrace());
			throw ex; 
		}
	
	}

}
