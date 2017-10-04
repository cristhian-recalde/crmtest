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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.bean.search.TransactionSearchXInfo;
import com.trilogy.app.crm.dunning.DunningHistorySearch;
import com.trilogy.app.crm.dunning.DunningHistorySearchWebControl;
import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportRecord;
import com.trilogy.app.crm.dunning.DunningReportRecordXInfo;
import com.trilogy.app.crm.dunning.DunningReportSearchWebControl;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

/**
 * SearchBorder for the Dunning Report Record bean specific to account
 * 
 * @author chandrachud.ingale
 * @since  10.1.4
 */
public class DunningReportRecordSearchBorder extends SearchBorder
{

    /**
     * Create a new DunningReportSearchBorder object.
     * 
     * @param ctx
     */
    public DunningReportRecordSearchBorder(Context ctx)
    {
        super(ctx, DunningReportRecord.class, new DunningHistorySearchWebControl());

        
//        addAgent(new SelectSearchAgent(DunningReportRecordXInfo.BAN, TransactionSearchXInfo.BAN));
        addAgent(new ContextAgentProxy()
        {
            /**
             * 
             */
            private static final long serialVersionUID = -5555777123715699535L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                Account acct = getSessionAccount(ctx);

                if (acct != null)
                {
                    doSelect(ctx, new EQ(DunningReportRecordXInfo.SPID, acct.getSpid()));
                    doSelect(ctx, new EQ(DunningReportRecordXInfo.BAN, acct.getBAN()));
                }
                delegate(ctx);
            }


            private Account getSessionAccount(Context ctx)
            {
                Account acct = null;
                if (ctx.has(Account.class))
                {
                    acct = (Account) ctx.get(Account.class);
                }
                else
                {
                    Context session = Session.getSession(ctx);
                    if(session != null)
                    {
                        acct = (Account) session.get(Account.class);
                        ctx.put(Account.class, acct);
                    }
                }
                return acct;
            }
        });

        addAgent(new ContextAgentProxy()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 4927116068475981370L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                DunningHistorySearch criteria = (DunningHistorySearch) getCriteria(ctx);

                if (criteria.getReportStartDate() != null)
                {
                    doSelect(ctx, new GTE(DunningReportRecordXInfo.REPORT_DATE, criteria.getReportStartDate()));
                }
                delegate(ctx);
            }
        });

        addAgent(new ContextAgentProxy()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 4564681993336316238L;


            @Override
            public void execute(Context ctx) throws AgentException
            {

                DunningHistorySearch criteria = (DunningHistorySearch) getCriteria(ctx);

                if (criteria.getReportEndDate() != null)
                {
                    doSelect(ctx, new LTE(DunningReportRecordXInfo.REPORT_DATE, criteria.getReportEndDate()));
                }
                delegate(ctx);
            }
        });
    }
}
