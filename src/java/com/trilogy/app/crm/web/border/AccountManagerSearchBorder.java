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
package com.trilogy.app.crm.web.border;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.FindSearchAgent;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;

import com.trilogy.app.crm.bean.account.AccountManager;
import com.trilogy.app.crm.bean.account.AccountManagerSearch;
import com.trilogy.app.crm.bean.account.AccountManagerSearchWebControl;
import com.trilogy.app.crm.bean.account.AccountManagerSearchXInfo;
import com.trilogy.app.crm.bean.account.AccountManagerXInfo;


/**
 * @author aaron.gourley@redknee.com
 */
public class AccountManagerSearchBorder extends SearchBorder
{

    public AccountManagerSearchBorder(Context ctx)
    {
        super(ctx, AccountManager.class, new AccountManagerSearchWebControl());

        addAgent(new FindSearchAgent(AccountManagerXInfo.ACCOUNT_MGR_ID, AccountManagerSearchXInfo.ACCOUNT_MGR_ID));

        addAgent(new ContextAgentProxy()
        {
            @Override
            public void execute(Context ctx) throws AgentException
            {
                AccountManagerSearch criteria = (AccountManagerSearch) getCriteria(ctx);

                if (criteria.getSpid() != -1)
                {
                    doSelect(ctx, new EQ(AccountManagerXInfo.SPID, Integer.valueOf(criteria.getSpid())));
                }

                delegate(ctx);
            }
        });

        addAgent(new WildcardSelectSearchAgent(AccountManagerXInfo.NAME, AccountManagerSearchXInfo.NAME));
    }
}
