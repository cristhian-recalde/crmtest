/*
 * Created on 2004-11-9
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.cltc;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.SubscriberCltcHome;
import com.trilogy.app.crm.bean.SubscriberCltcXInfo;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;


/**
 * @author jchen
 *
 * This class has supports for clean up expired SubscriberCLTC records
 */
public class SubscriberCltcCleanUpAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    /**
     * Each spid may have different CLTC expiry days, so we iterator 
     * each spid, remove cltc records respectively 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {
        
        try
        {    
            Home spidHome = (Home)ctx.get(CRMSpidHome.class);
            final Home subsCltcHome = (Home)ctx.get(SubscriberCltcHome.class);
            
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
                CRMSpid crmSp = (CRMSpid)iter.next();
                
                long expiryDate = new Date().getTime() - crmSp.getSubscriberCltcCleanUpDays()*DAY_IN_MILLS;
                
                try
                {
                    subsCltcHome.where(ctx, new EQ(SubscriberCltcXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    .where(ctx,new LT(MsisdnXInfo.LAST_MODIFIED,new Date(expiryDate)))
                    .removeAll(ctx);
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error removing CLCT records for spid " + crmSp.getSpid() ,e);
                }
            }    
            
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        }   
    }  

}