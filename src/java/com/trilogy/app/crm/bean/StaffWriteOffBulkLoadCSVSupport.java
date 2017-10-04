package com.trilogy.app.crm.bean;

import com.trilogy.framework.xhome.csv.AbstractCSVSupport;
import com.trilogy.framework.xhome.support.StringSeperator;

public class StaffWriteOffBulkLoadCSVSupport 
extends AbstractCSVSupport 
{
	  private final static StaffWriteOffBulkLoadCSVSupport instance__ = new StaffWriteOffBulkLoadCSVSupport();
	   public static StaffWriteOffBulkLoadCSVSupport instance()
	   {
	      return instance__;
	   }

	   public StaffWriteOffBulkLoadCSVSupport()
	   {
	   }

	   public Object parse(StringSeperator seperator)
	   {
	      StaffWriteOffBulkLoad bean = new StaffWriteOffBulkLoad();
	      bean.setBAN(parseString(seperator.next()));
	      
	      bean.setAdjustmentType(parseInte(seperator.next()));
	      bean.setExtTransactionId(parseString(seperator.next()));
	      bean.setReasonCode(parseLong(seperator.next()));
	      bean.setCSRInput(parseString(seperator.next()));

	      return bean;

	   }

	   private int parseInte(String strg)
	   {
		   if (strg != null && !strg.trim().equals(""))
		   {
			   return Integer.parseInt(strg.trim()); 
		   }
		   return 0; 
	   }
	
	   private long parseLong(String strg)
	   {
		   if (strg != null && !strg.trim().equals(""))
		   {
			   return Long.parseLong(strg.trim()); 
		   }
		   return 0l; 
	   }

	public StringBuffer append(StringBuffer buf, char delimiter, Object obj) {
		return new StringBuffer("");
	}

}
