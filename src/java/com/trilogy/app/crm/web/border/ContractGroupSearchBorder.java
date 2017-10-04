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
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.transfer.ContractGroup;
import com.trilogy.app.crm.transfer.ContractGroupSearch;
import com.trilogy.app.crm.transfer.ContractGroupSearchWebControl;
import com.trilogy.app.crm.transfer.ContractGroupSearchXInfo;
import com.trilogy.app.crm.transfer.ContractGroupXInfo;
import com.trilogy.app.crm.transfer.GroupPrivacyEnum;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Searches for Contract Groups by Id and owner ID
 * @author arturo.medina@redknee.com
 *
 */
public class ContractGroupSearchBorder extends SearchBorder
{
    
    /**
     * Constructor that build the agents to search for the Transfer contracts
     * @param context
     */
    public ContractGroupSearchBorder(final Context context)
    {   
        super(context, ContractGroup.class, new ContractGroupSearchWebControl());
        
        addAgent(new SelectSearchAgent(ContractGroupXInfo.PRIVACY, ContractGroupSearchXInfo.PRIVACY));
        addAgent(new ContextAgentProxy()
        {

            /**
             * 
             */
            private static final long serialVersionUID = 448517525639181880L;


            public void execute(Context ctx) throws AgentException
            {
                ContractGroupSearch criteria = (ContractGroupSearch) getCriteria(ctx);
                if (GroupPrivacyEnum.PUBLIC_INDEX == criteria.getPrivacy().getIndex())
                {
                    if (LogSupport.isDebugEnabled(ctx))
                    {
                        new DebugLogMsg(this, "PUBLIC: Performing a search with public owner crietrion:", null)
                                .log(ctx);
                    }
                    doSelect(ctx, new EQ(ContractGroupXInfo.OWNER_ID, ""));
                }
                else
                {
                    if ("".equals(criteria.getOwner()))
                    {
                        /**
                         * Do nothing to avoid search for this false criterion. For newer
                         * versions of fw; if we threw an IllegalArgumentException() The
                         * error message would get printed and search will fail gracefully
                         */
                    }
                    else
                    {
                        if (LogSupport.isDebugEnabled(ctx))
                        {
                            new DebugLogMsg(this, "NOT PUBLIC: Performing a search owner as: " + criteria.getOwner(),
                                    null).log(ctx);
                        }
                        doSelect(ctx, new EQ(ContractGroupXInfo.OWNER_ID, criteria.getOwner()));
                    }
                }
                delegate(ctx);
            }
        });


    }
}
