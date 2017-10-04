/*
 * Created on Jun 1, 2005
 * 
 * Copyright (c) 1999-2005 REDKNEE.com. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of REDKNEE.com.
 * ("Confidential Information"). You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement you entered
 * into with REDKNEE.com.
 * 
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
 * REDKNEE.COM SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.web.border;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidSearch;
import com.trilogy.app.crm.bean.CRMSpidSearchWebControl;
import com.trilogy.app.crm.bean.CRMSpidSearchXInfo;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgentProxy;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.web.search.SearchBorder;
import com.trilogy.framework.xhome.web.search.WildcardSelectSearchAgent;


/**
 * @author jke
 * 
 */
public class ServiceProviderSearchBorder extends SearchBorder
{

    public ServiceProviderSearchBorder(Context ctx)
    {
        super(ctx, CRMSpid.class, new CRMSpidSearchWebControl());
        // spid
        addAgent(new ContextAgentProxy()
        {

            public void execute(Context ctx) throws AgentException
            {
                CRMSpidSearch criteria = (CRMSpidSearch) getCriteria(ctx);
                if (criteria.getId() != -1
                        && null != doFind(ctx, new EQ(CRMSpidXInfo.ID, Integer.valueOf(criteria.getId()))))
                {
                    ContextAgents.doReturn(ctx);
                }
                delegate(ctx);
            }
        });
        // name
        addAgent(new WildcardSelectSearchAgent(CRMSpidXInfo.NAME, CRMSpidSearchXInfo.NAME, true));
    }
}
