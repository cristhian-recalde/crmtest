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

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;

import com.trilogy.app.crm.bean.Identification;
import com.trilogy.app.crm.bean.IdentificationSearch;
import com.trilogy.app.crm.bean.IdentificationSearchWebControl;
import com.trilogy.app.crm.bean.IdentificationSearchXInfo;
import com.trilogy.app.crm.bean.IdentificationXInfo;
import com.trilogy.app.crm.web.search.PrePostWildcardSelectSearchAgent;

/**
 * @author amedina
 *
 * Search Border for Identification by Id and description
 */
public class IdentificationSearchBorder extends SearchBorder
{

	   public IdentificationSearchBorder(Context ctx)
	   {
	      super(ctx, Identification.class, new IdentificationSearchWebControl());

	      // ID Type
	      addAgent(new ContextAgentProxy() {
	         @Override
            public void execute(Context ctx)
	            throws AgentException
	         {
	         	IdentificationSearch criteria = (IdentificationSearch)getCriteria(ctx);
	     
               if (criteria.getCode() != -1)
	            {
                  SearchBorder.doSelect(
                     ctx, 
                     new EQ(IdentificationXInfo.CODE, Integer.valueOf(criteria.getCode())));
	            }
	            delegate(ctx);
	         }
	      });

	      addAgent(new PrePostWildcardSelectSearchAgent(IdentificationXInfo.DESC, IdentificationSearchXInfo.DESCRIPTION));
	   }

}
