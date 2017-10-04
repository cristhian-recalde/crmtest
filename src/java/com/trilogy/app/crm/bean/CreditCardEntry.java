package com.trilogy.app.crm.bean;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.util.cipher.Encrypted;
import com.trilogy.framework.xhome.beans.Validatable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;



public class CreditCardEntry 
extends AbstractCreditCardEntry 
implements Validatable
{

	  /**
     * {@inheritDoc}
     */
    public void validate(Context ctx) throws IllegalStateException
    {
    	if (this.getCardNumber().startsWith(Encrypted.ENCRYPTED_MASK_PREFIX))
    	{
    		// not need if no new update. 
    		return; 
    		
    	}
    	
    	
    	try
    	{
    		CreditCardType cardType = HomeSupportHelper.get(ctx).findBean(ctx, CreditCardType.class, new EQ(CreditCardTypeXInfo.CARD_TYPE, Integer.valueOf(this.getCardTypeId())));
        
    		if (cardType == null)
    		{
    			throw new IllegalStateException("Could not find card type " + this.getCardTypeId() + ".");
    		}
        
    		Pattern p = Pattern.compile(cardType.getNumberregex());
    		Matcher m = p.matcher(this.getCardNumber());
    		if (!m.matches())
    		{
    			throw new IllegalStateException("Card number did not match number format of the selected card type.");
    		}
    	} catch (Exception e)
    	{
    		throw new IllegalStateException("Creditcard number fail to match the pattern defined in credit card type"); 
    	}
    }

	public String getDecodedCreditCardNumber() {
		return decodedCreditCardNumber;
	}

	public void setDecodedCreditCardNumber(String decodedCreditCardNumber) {
		this.decodedCreditCardNumber = decodedCreditCardNumber;
	}

	private String decodedCreditCardNumber = ""; 
}
