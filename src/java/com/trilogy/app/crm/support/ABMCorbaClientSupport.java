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
package com.trilogy.app.crm.support;

import com.trilogy.app.crm.client.AbmResultCode;
import com.trilogy.app.crm.client.ProductABMAccountClient;
import com.trilogy.framework.xhome.context.Context;

/**
 * @author amedina
 *
 * Support class that implements all ABM calls
 */
public class ABMCorbaClientSupport 
{

	public static int debit(Context ctx,
			String msisdn, 
    		int amount, 
			int serviceId)
	{
 		ProductABMAccountClient abmClient = (ProductABMAccountClient) ctx.get(
 				ProductABMAccountClient.class); 

 		int result = AbmResultCode.SUCCESS;
 		
 		result = abmClient.debit(msisdn,amount,"",true,serviceId,false,"");
 		
 		return result;
 		
	}
	public static int credit(Context ctx,
			String msisdn, 
    		int amount, 
			int serviceId)
	{
 		ProductABMAccountClient abmClient = (ProductABMAccountClient) ctx.get(
 				ProductABMAccountClient.class); 

 		int result = AbmResultCode.SUCCESS;
 		
 		result = abmClient.credit(msisdn,amount,"",true,serviceId,"",false,(short)0);
 		
 		return result;
 		
	}


}
