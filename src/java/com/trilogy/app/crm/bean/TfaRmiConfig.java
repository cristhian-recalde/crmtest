/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.auth.AESEncryption;
import com.trilogy.app.crm.bean.core.SubscriptionType;


/**
 * 
 *
 * @author aaron.gourley@redknee.com
 * @since 8.2
 */
public class TfaRmiConfig extends AbstractTfaRmiConfig
{
    /**
     * {@inheritDoc}
     */
    public SubscriptionType getSubscriptionType(Context ctx)
    {
        return SubscriptionType.getSubscriptionType(ctx, getSubscriptionType());
    }
    

    /**
     * {@inheritDoc}
     */
    public long getSubscriptionType()
    {
        return super.getOperatorSubscriptionTypeID();
    }


    /**
     * {@inheritDoc}
     */
    public void setSubscriptionType(long subscriptionTypeId)
    {
        setOperatorSubscriptionTypeID(subscriptionTypeId);
    }
    
    	/**
    	   /**
    	    * Encode and update the password if it is not already encoded.
    	    *
    	    * @param String clear text password to be encyprted and stored.
    	    * @since 6.0
    	    */
    	   public void setPassword(String password)
    	   {

    	       
    	       String decoded = AESEncryption.decrypt(password);

    	       if (AESEncryption.NOT_ENCRYPTED.equals(decoded))
    	       {
    	           super.setPassword(AESEncryption.encrypt(password));
    	       }
    	       else
    	       {
    	           super.setPassword(password);
    	       }
    	   }

    	   /**
    	    * Get the decoded password
    	    *
    	    * @param Context ctx
    	    * @return String decoded password to used in connection requests.
    	    * @since 6.0
    	    */
    	   public String getPassword(Context ctx)
    	   {
    	       // password may or may not be encoded. If the XDB configuration
    	       // has never been updated from the default or since the
    	       // upgrade with this feature then the password will still
    	       // be stored in clear text.
    	       String decoded = AESEncryption.decrypt(super.getPassword());

    	       if (AESEncryption.NOT_ENCRYPTED.equals(decoded))
    	       {
    	           return super.getPassword();
    	       }
    	       else
    	       {
    	           return decoded;
    	       }
    	   } 

}
