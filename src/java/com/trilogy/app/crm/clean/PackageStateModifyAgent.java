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
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.clean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.GSMPackageXInfo;
import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.PackageStateEnum;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.support.CalendarSupportHelper;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent which goes to database and sets the state of  
 * Package Number from "IN_USE" to "AVAIABLE" after configurable 
 * quarantine time period.
 * 
 */
public class PackageStateModifyAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    /**
     * rattapattu
     * 
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     * 
     * The Logic in this method is as follows. We walk through all the spids  using a visitor and in each spid we querry the package table
     * with the spid specific criteria for the clean up  and then walk through all the selected packages using a visitor to
     * put them back to avialable.
     */
    public void execute(Context ctx) throws AgentException
    {
        final Home simCardHome  = (Home)ctx.get(GSMPackageHome.class);
        
        if ( simCardHome == null )
        {
            throw new AgentException("System error: simCardHome not found in context");
        }
        
        Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
        if ( spidHome == null )
        {
            throw new AgentException("System error: CRMSpidHome not found in context");
        }
        
        try
        {
            
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
                CRMSpid crmSp = (CRMSpid)iter.next();
                
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, - crmSp.getIMSINumberHeldDays());
                /*
                 * We want a behaviour consistent around boundary of a day
                 * Now by clearing time of day we ensure that a new day begins at 00:00 hrs
                 * In the logic below; if any MSISDN whose expiry-date falls on any time today would be cleared from held state
                 */
                calendar = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(calendar);
                
                final Date expiryDate = calendar.getTime();
                
                checkGSMPackage(ctx,crmSp.getSpid(),expiryDate);
                checkTDMAPackage(ctx,crmSp.getSpid(),expiryDate);
            }   
            
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        } 
       
    }
    
    private void checkTDMAPackage(Context ctx,int spid,Date expiryDate)
    {
        Home tdmaPkgHome = (Home)ctx.get(TDMAPackageHome.class);
        try
        {
            tdmaPkgHome.where(ctx, new EQ(TDMAPackageXInfo.STATE,PackageStateEnum.HELD))
            .where(ctx,new EQ(TDMAPackageXInfo.SPID, Integer.valueOf(spid)))
            .where(ctx,new LT(TDMAPackageXInfo.LAST_MODIFIED,(expiryDate)))
            .forEach(ctx, new CloneingVisitor(new HomeVisitor(tdmaPkgHome)
                    {

                        public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                        {
                            TDMAPackage pkg = (TDMAPackage)obj;
                            pkg.setState(PackageStateEnum.AVAILABLE);
                            // http://jira01.ber.office.redknee.com/jira/browse/BSS-1811 While package moving to available state from held state
                            // It should remove external MSID as well.
                            pkg.setExternalMSID(TDMAPackage.DEFAULT_EXTERNALMSID);
                            try
                            {
                                getHome().store(ctx,pkg);
                            }
                            catch (Exception e)
                            {
                                LogSupport.minor(ctx,this,"Couldn't update package " + pkg.getPackId() + " to AVAILABLE state",e);
                            }
                        }
                
                    }
                    
            
            ));
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx,this,"Error selecting packages's in HELD state",e);
        }
    }
    
    private void checkGSMPackage(Context ctx,int spid,Date expiryDate)
    {
        Home gsmPkgHome = (Home)ctx.get(GSMPackageHome.class);
        try
        {
            gsmPkgHome.where(ctx, new EQ(GSMPackageXInfo.STATE,PackageStateEnum.HELD))
            .where(ctx,new EQ(GSMPackageXInfo.SPID, Integer.valueOf(spid)))
            .where(ctx,new LT(GSMPackageXInfo.LAST_MODIFIED,expiryDate))
            .forEach(ctx, new CloneingVisitor(new HomeVisitor(gsmPkgHome)
                    {

                        public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                        {
                            GSMPackage pkg = (GSMPackage)obj;
                            pkg.setState(PackageStateEnum.AVAILABLE);
                            try
                            {
                                getHome().store(ctx,pkg);
                            }
                            catch (Exception e)
                            {
                                LogSupport.minor(ctx,this,"Couldn't update package " + pkg.getPackId() + " to AVAILABLE state",e);
                            }
                        }
                
                    }
                    
            
            ));
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx,this,"Error selecting packages's in HELD state",e);
        }
    }
    
    
}
