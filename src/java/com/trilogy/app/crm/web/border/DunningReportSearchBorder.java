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

import com.trilogy.app.crm.dunning.DunningReport;
import com.trilogy.app.crm.dunning.DunningReportSearch;
import com.trilogy.app.crm.dunning.DunningReportSearchWebControl;
import com.trilogy.app.crm.dunning.DunningReportSearchXInfo;
import com.trilogy.app.crm.dunning.DunningReportXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GTE;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.web.search.SearchBorder;


/**
 * SearchBorder for the Dunning Report bean
 * 
 * @author Marcio Marques
 * @since 9.0
 */
public class DunningReportSearchBorder extends SearchBorder
{

    /**
     * Create a new DunningReportSearchBorder object.
     * 
     * @param context
     */
    public DunningReportSearchBorder(Context context)
    {
        super(context, DunningReport.class, new DunningReportSearchWebControl());

        addAgent(new ContextAgentProxy()
        {

            /**
             * 
             */
            private static final long serialVersionUID = 53585166770404132L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                DunningReportSearch criteria = (DunningReportSearch) getCriteria(ctx);
                if (criteria.getSpid() >=0 )
                {
                    doSelect(ctx, new EQ(DunningReportXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                }
                delegate(ctx);
            }
        });

        addAgent(new ContextAgentProxy()
        {
            /**
             * 
             */
            private static final long serialVersionUID = 5751728058336737279L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                DunningReportSearch criteria = (DunningReportSearch) getCriteria(ctx);

                if (criteria.getReportStartDate() != null)
                {
                    doSelect(ctx, new GTE(DunningReportXInfo.REPORT_DATE, criteria.getReportStartDate()));
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
                DunningReportSearch criteria = (DunningReportSearch) getCriteria(ctx);

                if (criteria.getReportEndDate() != null)
                {
                    doSelect(ctx, new LTE(DunningReportXInfo.REPORT_DATE, criteria.getReportEndDate()));
                }
                delegate(ctx);
            }
        });

        addAgent(new ContextAgentProxy()
        {

            /**
             * 
             */
            private static final long serialVersionUID = 7653292221369795508L;


            @Override
            public void execute(Context ctx) throws AgentException
            {
                DunningReportSearch criteria = (DunningReportSearch) getCriteria(ctx);
                if (criteria.getNextDunningLevel() >= 0)
                {
                    doSelect(ctx,new EQ(DunningReportXInfo.NEXT_LEVEL, Integer.valueOf(criteria.getNextDunningLevel())));                    
                }
                delegate(ctx);                
            }
        });        
                
    }
}
