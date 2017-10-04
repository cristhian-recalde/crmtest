/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.audi;

import com.trilogy.framework.xhome.context.Context;

/**
 * @author amedina
 *
 * Process logic before deactivating the subscriber and deactivates the subscriber
 */
public interface AUDIDeleteLogicProcess
{
	/**
	 * Adds logic to the subscriber deactivation and update a subscriber to CRM
	 * @param ctx
	 * @param csvObject
	 */	
	public void delete(Context ctx, Object csvObject);
	
	/**
	 * Print msg to Error Log
	 * @param ctx
	 * @param msg
	 */
	public void printMessageToLog(Context ctx, String msg);
	
}
