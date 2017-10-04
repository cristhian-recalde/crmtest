package com.trilogy.app.crm.invoice.config;

import com.trilogy.app.crm.auth.AESEncryption;
import com.trilogy.framework.xhome.context.Context;

public class InvoiceServerRemoteServicerConfig extends AbstractInvoiceServerRemoteServicerConfig
{
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
