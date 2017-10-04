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

import com.trilogy.app.crm.bean.BalanceHistory;
import com.trilogy.app.crm.bean.BalanceHistorySearchWebControl;

/**
 * An Custom SearchBorder for Balance History information.
 *
 * @author victor.stratan@redknee.com
 **/
public class BalanceHistorySearchBorder extends SearchBorder
{

    public BalanceHistorySearchBorder(final Context context)
    {
        super(context, BalanceHistory.class, new BalanceHistorySearchWebControl());

        // no need to run this one, all work done in MergedBalanceHistoryHome
        //addAgent(new BalanceHistoryCustomSearchAgent());
    }
}

