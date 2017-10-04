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
package com.trilogy.app.crm.dunning.visitor.accountprocessing;

import java.util.Date;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.dunning.DunningConstants;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningAccountProcessor;
import com.trilogy.app.crm.dunning.visitor.AbstractDunningReportRecordProcessingVisitor;
import com.trilogy.app.crm.dunning.visitor.AccountVisitor;
import com.trilogy.app.crm.support.LicensingSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.SeverityEnum;


/**
 * Visitor responsible to process accounts during dunning process.
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningProcessingAccountVisitor extends AccountVisitor 
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    /**
     * Creates a new DunningProcessingAccountVisitor visitor.
     * 
     * @param report
     */
    public DunningProcessingAccountVisitor(final Date runningDate)
    {
        super(runningDate);
    }


    /**
     * Retrieves the process name.
     * 
     * @return
     */
    public static String getVisitorProcessName()
    {
        return "Account Dunning Processing";
    }


    /**
     * {@inheritDoc}
     */
    protected void executeOnActionRequired(final Context context, final Account account,
            final Account responsibleAccount, final DunningReportRecord dunningReportRecord)
            throws DunningProcessException
    {
//        if (AccountStateEnum.ACTIVE.equals(dunningReportRecord.getForecastedState()) || !LicensingSupportHelper.get(context).isLicensed(context, LicenseConstants.DUNNING_REPORT_SUPPORT))
//        {
//            context.put(DunningConstants.DUNNING_ACTION_KEY, dunningReportRecord.getForecastedState());
//            //modifyAccountState(context, dunningReportRecord.getForecastedState(), responsibleAccount, dunningReportRecord);
//        }
//        else if (LogSupport.isEnabled(context, SeverityEnum.INFO))
//        {
//            StringBuilder sb = new StringBuilder();
//            sb.append("Not modifying state for account '");
//            sb.append(account.getBAN());
//            sb.append("' to '");
//            sb.append(dunningReportRecord.getForecastedState().getDescription());
//            sb.append("' because Dunning Report support is enabled. All state changes (but to ACTIVE) should be made through Dunning Report processing.");
//            LogSupport.info(context, this, sb.toString());
//        }
//        
    }
    
    /**
     * {@inheritDoc}
     */
    
    public String getProcessName()
    {
        return getVisitorProcessName();
    }

    private Account account_;
}
