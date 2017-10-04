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

import com.trilogy.framework.xhome.web.search.SearchBorder;

import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.app.crm.bean.Province;
import com.trilogy.app.crm.bean.ProvinceSearch;
import com.trilogy.app.crm.bean.ProvinceSearchWebControl;
import com.trilogy.app.crm.bean.ProvinceSearchXInfo;
import com.trilogy.app.crm.bean.ProvinceXInfo;

/**
 * @author ali
 *
 * Search Border for Province (a.k.a. "Regions" in the GUI)
 */
public class ProvinceSearchBorder extends SearchBorder {

	public ProvinceSearchBorder(Context ctx)
	   {
	      super(ctx,Province.class,new ProvinceSearchWebControl());

	      // spid
	      addAgent(new ContextAgentProxy()
	            {
	               public void execute(Context ctx)
	                  throws AgentException
	               {
	                  ProvinceSearch criteria = (ProvinceSearch)getCriteria(ctx);
	                  if (criteria.getSpid() != -1)
	                  {
	                     doSelect(
	                        ctx,
	                        new EQ(ProvinceXInfo.SPID, Integer.valueOf(criteria.getSpid())));
	                  }
	                  delegate(ctx);
	               }
	            }
	      );
	      
	      // name
	      addAgent(new WildcardSelectSearchAgent(ProvinceXInfo.NAME, ProvinceSearchXInfo.NAME, true));
	   }
	
	
}
