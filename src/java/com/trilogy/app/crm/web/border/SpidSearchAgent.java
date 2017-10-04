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

import com.trilogy.app.crm.bean.OickMappingSearch;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.filter.SpidPredicate;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * @author jchen
 */
public class SpidSearchAgent extends ContextAgentProxy
{

    
		public SpidSearchAgent(SearchBorder border)
		{
			searchBorder_ = border;
		}
		
	public void execute(final Context ctx)
    throws AgentException
{
        final SpidAware criteria = (SpidAware)SearchBorder.getCriteria(ctx);

        if (criteria.getSpid() != -1)
        {
        	SpidPredicate spidPredicate =  new SpidPredicate(criteria.getSpid());
        	SearchBorder.doSelect(
                ctx,
               spidPredicate);
                    //"IDIDENTIFIER = '" + XDBSupport.cleanSQLText(criteria.getBAN()) + "'"));
        }
        delegate(ctx);
    }
	
	SearchBorder getSearchBorder()
	{
		return searchBorder_;
	}
	SearchBorder searchBorder_ = null;
}
