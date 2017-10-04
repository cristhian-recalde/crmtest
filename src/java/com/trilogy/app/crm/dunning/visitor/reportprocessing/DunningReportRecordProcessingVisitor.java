/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.dunning.visitor.reportprocessing;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportRecordProcessingVisitor;
import com.trilogy.app.crm.log.CoreERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * Visitor responsible to process accounts during dunning report processing.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportRecordProcessingVisitor extends AbstractDunningReportRecordProcessingVisitor
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningReportRecordProcessingAccountVisitor visitor.
     * 
     * @param report
     */
    public DunningReportRecordProcessingVisitor(final DunningReport report)
    {
        super(report);
    }


    /**
     * Retrieves the process name.
     * 
     * @return
     */
    public static String getVisitorProcessName()
    {
        return "Dunning Report Processing";
    }


    /**
     * {@inheritDoc}
     */
    public String getProcessName()
    {
        return getVisitorProcessName();
    }


        
    protected boolean checkAndMoveToPtp(Context context,Account account , DunningReportRecord drr) 
    {
    	boolean movedToPtp = false;
    	if(drr.getMoveToPTP())
    	{
    		if (LogSupport.isDebugEnabled(context))
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Setting account '");
                sb.append(drr.getBAN());
                sb.append("' to promise to pay with PTP expiry date '");
                sb.append(CoreERLogger.formatERDateDayOnly(drr.getPtpExpiryDate()));
                sb.append("'");
                LogSupport.debug(context, this, sb.toString());
            }
    		try{
	    		account.setState(AccountStateEnum.PROMISE_TO_PAY);
	            account.setPromiseToPayDate(drr.getPtpExpiryDate());
	            HomeSupportHelper.get(context).storeBean(context, account);
	            movedToPtp = true;
    		}catch(HomeException e)
    		{
    			LogSupport.minor(context, this, "Unable to set PTP on account :,"+drr.getBAN(), e);               
    		}
    	}
    	return movedToPtp;
    }


    
}
