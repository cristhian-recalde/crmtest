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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.api.generic.entity.validator;


import com.trilogy.app.crm.api.generic.entity.support.DisputeSupport;
import com.trilogy.app.crm.troubleticket.bean.Dispute;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
* @author monami.pakira@redknee.com
* @since 9.11.4
*  
*/

public class DisputeCreationValidator extends AbstractGenericEntityValidator implements Validator
{

	@Override
	public void validate(Context ctx, Object paramObject) throws IllegalStateException {
		Dispute dispute = (Dispute) paramObject;
		String createMode="createMode";
		if (LogSupport.isDebugEnabled(ctx))
	    {
		  LogSupport.debug(ctx, this, "Dispute Values in creation :: "+dispute);
		}
		CompoundIllegalStateException el = new CompoundIllegalStateException();
		try
		{
			DisputeSupport.validateBan(ctx, dispute);
			DisputeSupport.validateSpid(ctx, dispute);
			DisputeSupport.validateDisputedAmount(ctx, dispute);
			DisputeSupport.validateInvoiceId(ctx, dispute);
			DisputeSupport.validateAdjustmentType(ctx, dispute);
			DisputeSupport.validateState(ctx, dispute, createMode);
			DisputeSupport.validateSubscriberId(ctx, dispute);
			DisputeSupport.validateTroubleTicketId(ctx, dispute);
		}
		catch (HomeException e)
		{
			el.thrown(e);
		}
		finally
		{
			el.throwAll();
		}
	}
	
}