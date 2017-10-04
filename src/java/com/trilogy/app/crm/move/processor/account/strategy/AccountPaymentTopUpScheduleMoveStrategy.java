/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.move.processor.account.strategy;

import java.util.Collection;
import java.util.HashSet;

import com.trilogy.app.crm.bean.TopUpSchedule;
import com.trilogy.app.crm.bean.TopUpScheduleXInfo;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.MoveWarningException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.AccountMoveRequest;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * This class will move following business entities from old Account to new account.
 * 1. TopupSchedule
 * 
 * @author sgaidhani
 * @since 9.5.1
 */
public class AccountPaymentTopUpScheduleMoveStrategy<AMR extends AccountMoveRequest> extends CopyMoveStrategyProxy<AMR> 
{
	public static final String TOP_UP_SCHEDULE_OPERATION = "TopupSchedule";
	
	public AccountPaymentTopUpScheduleMoveStrategy(CopyMoveStrategy<AMR> delegate)
    {
        super(delegate);
    }
	
	@Override
	public void initialize(Context ctx, AMR request) {
		super.initialize(ctx, request);
	}

	@Override
	public void validate(Context ctx, AMR request) throws IllegalStateException {
		super.validate(ctx, request);
	}

	@Override
	public void createNewEntity(Context ctx, AMR request) throws MoveException 
	{
        final String  oldIdentifier = ((AccountMoveRequest) request).getOldAccount(ctx).getBAN();
        final String  newIdentifier = ((AccountMoveRequest) request).getNewAccount(ctx).getBAN();
        
        movePaymentScheduleRecords(ctx, request, oldIdentifier, newIdentifier);
        
        //Move to next delegate.
        super.createNewEntity(ctx, request);
	}

	@Override
	public void removeOldEntity(Context ctx, AMR request) throws MoveException {
		super.removeOldEntity(ctx, request);
	}
	
	private void movePaymentScheduleRecords(Context ctx, AMR request,
			final String oldIdentifier, final String newIdentifier) {
		int size = 0;
		
		Collection<MoveWarningException> warnings = new HashSet<MoveWarningException>();
		try
        {
            Collection<TopUpSchedule> data = HomeSupportHelper.get(ctx).getBeans(ctx, TopUpSchedule.class, new EQ(TopUpScheduleXInfo.BAN, oldIdentifier));
            
            for (TopUpSchedule scheduleRecord : data)
            {
            	size++;
            	scheduleRecord.setBan(newIdentifier);
            	scheduleRecord.setSubscriptionId("");
                try
                {
                    HomeSupportHelper.get(ctx).storeBean(ctx, scheduleRecord);
                }
                catch (HomeException e)
                {
                	String warningMesssage = "Error occured while trying to move an TopUpSchedule Record "
                            + "for BAN :'" + oldIdentifier
                            + "' and id '" + scheduleRecord.ID() 
                            + " to new BAN :" + newIdentifier;
                	
                	LogSupport.minor(ctx, this, warningMesssage, e);
                	
                	warnings.add(
                            new MoveWarningException(request, warningMesssage, e));
                }
            }
    
        }
        catch (HomeException e)
        {
        	String warningMesssage = "Error occured while trying to move all TopUpSchedule Records for BAN : " + oldIdentifier
            		+ " to new BAN :" + newIdentifier;
        	
        	LogSupport.minor(ctx, this, warningMesssage, e);
        	
        	warnings.add(
                    new MoveWarningException(request, warningMesssage , e));
        }
		
		if(size == 0)
		{
			request.reportStatusMessages(ctx,TOP_UP_SCHEDULE_OPERATION + " "+ MOVE_NOT_APPLICABLE);
		}
		else
		{
			if(warnings.size() > 0)
			{
				request.reportStatusMessages(ctx,TOP_UP_SCHEDULE_OPERATION + " "+ COPY_FAILED);
				for(MoveWarningException warning : warnings)
				{
					request.reportWarning(ctx, warning);
				}
			}
			else
			{
				request.reportStatusMessages(ctx,TOP_UP_SCHEDULE_OPERATION + " "+ COPY_SUCCESS);
			}
		}
	}
}
