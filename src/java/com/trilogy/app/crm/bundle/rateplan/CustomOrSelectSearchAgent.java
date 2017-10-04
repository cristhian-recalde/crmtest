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
package com.trilogy.app.crm.bundle.rateplan;

import java.util.Iterator;
import java.util.Collection;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.OrSelectSearchAgent;

/**
 * Need to check the validity of the type before attacing the condition.
 *
 * @author victor.stratan@redknee.com
 */
public class CustomOrSelectSearchAgent extends OrSelectSearchAgent
{
    public CustomOrSelectSearchAgent(final PropertyInfo searchPInfo)
    {
        super(searchPInfo);
    }

    public CustomOrSelectSearchAgent(final PropertyInfo searchPInfo, final boolean ignoreCase)
    {
        super(searchPInfo, ignoreCase);
    }

    public CustomOrSelectSearchAgent(final Collection pInfos, final PropertyInfo searchPInfo)
    {
        super(pInfos, searchPInfo);
    }

    public CustomOrSelectSearchAgent(final Collection pInfos, final PropertyInfo searchPInfo, final boolean ignoreCase)
    {
        super(pInfos, searchPInfo, ignoreCase);
    }

    public void execute(final Context ctx) throws AgentException
    {
        Object value = getSearchCriteria(ctx);
        if (value != null && !"".equals(value.toString()))
        {
            final Or or = new Or();

            for (final Iterator iter = getPropertyInfos().iterator(); iter.hasNext();)
            {
                final PropertyInfo pInfo = (PropertyInfo) iter.next();
                if (pInfo.getType().equals(Integer.class))
                {
                    try
                    {
                        value = Integer.parseInt((String) value);
                    }
                    catch (NumberFormatException e)
                    {
                        continue;
                    }
                }
                else if (pInfo.getType().equals(Long.class))
                {
                    try
                    {
                        value = Long.parseLong((String) value);
                    }
                    catch (NumberFormatException e)
                    {
                        continue;
                    }
                }

                if (getIgnoreCase())
                {
                    or.add(getComparatorIC(pInfo, value));
                }
                else
                {
                    or.add(getComparator(pInfo, value));
                }
            }
            SearchBorder.doSelect(ctx, or);
        }
        delegate(ctx);
    }
}
