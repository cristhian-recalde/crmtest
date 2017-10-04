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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.SelectSearchAgent;

import com.trilogy.app.crm.bean.account.AccountManagerHistory;
import com.trilogy.app.crm.bean.account.AccountManagerHistorySearchWebControl;
import com.trilogy.app.crm.bean.account.AccountManagerHistorySearchXInfo;
import com.trilogy.app.crm.bean.account.AccountManagerHistoryXInfo;
import com.trilogy.app.crm.web.search.DateRangeSearchAgent;


/**
 * @author aaron.gourley@redknee.com
 */
public class AccountManagerHistorySearchBorder extends SearchBorder
{
    public AccountManagerHistorySearchBorder(Context context)
    {
        super(context, AccountManagerHistory.class, new AccountManagerHistorySearchWebControl());

        // Account Manager Id
        addAgent(new SelectSearchAgent(
                AccountManagerHistoryXInfo.ACCOUNT_MGR_ID, 
                AccountManagerHistorySearchXInfo.ACCOUNT_MGR_ID));

        // History Date Range
        addAgent(new DateRangeSearchAgent(
                AccountManagerHistorySearchXInfo.START_DATE, AccountManagerHistorySearchXInfo.END_DATE, 
                AccountManagerHistoryXInfo.HISTORY_DATE));
    }
}
