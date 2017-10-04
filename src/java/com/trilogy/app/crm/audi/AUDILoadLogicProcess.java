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
 * Will process the client logic before installing the subscriber into CRM
 */
public interface AUDILoadLogicProcess 
{
	/**
	 * Adds logic to the subscriber creation and creates a subscriber to CRM
	 * @param ctx
	 * @param csvObject
	 */
	public void add(Context ctx, Object csvObject);
	
	/**
	 * Print msg to Error Log
	 * @param ctx
	 * @param msg
	 */
	public void printMessageToLog(Context ctx, String msg);

}
