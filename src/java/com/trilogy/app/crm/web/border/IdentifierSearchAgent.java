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

import com.trilogy.app.crm.bean.IdentifierAware;
import com.trilogy.app.crm.filter.EitherPredicate;
import com.trilogy.app.crm.filter.IdentifierPredicate;
import com.trilogy.app.crm.filter.SpidPredicate;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.web.search.SearchBorder;

/**
 * @author jchen
 */
public class IdentifierSearchAgent extends ContextAgentProxy
{

    
		public IdentifierSearchAgent(SearchBorder border, String fieldName)
		{
			searchBorder_ = border;
			if (fieldName != null)
				setIdentifierColumnName(fieldName);
			
		}
		
	public void execute(final Context ctx)
    throws AgentException
{
        final IdentifierAware criteria = (IdentifierAware)SearchBorder.getCriteria(ctx);

        if (criteria.getIdentifier() != 0)
        {
        	IdentifierPredicate spidPredicate =  new IdentifierPredicate(criteria.getIdentifier());
            if (getIdentifierColumnName() != null)
                spidPredicate.setIdentifierColumnName(getIdentifierColumnName());
            SearchBorder.doSelect(ctx, spidPredicate);
            // "IDIDENTIFIER = '" + XDBSupport.cleanSQLText(criteria.getBAN()) + "'"));
        }
        delegate(ctx);
    }
	
	SearchBorder getSearchBorder()
	{
		return searchBorder_;
	}
	SearchBorder searchBorder_ = null;
	String identifierColumnName = "IDIDENTIFIER";

	/**
	 * @return Returns the identifierColumnName.
	 */
	public String getIdentifierColumnName() {
		return identifierColumnName;
	}
	/**
	 * @param identifierColumnName The identifierColumnName to set.
	 */
	public void setIdentifierColumnName(String identifierColumnName) {
		this.identifierColumnName = identifierColumnName;
	}
}
