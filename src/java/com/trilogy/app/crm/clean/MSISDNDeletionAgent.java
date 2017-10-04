package com.trilogy.app.crm.clean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfig;
import com.trilogy.app.crm.grr.GrrGeneratorGeneralConfigHome;
import com.trilogy.app.crm.home.sub.MsisdnBeforeDeletionSpgProvUpdateProxyHome;
import com.trilogy.app.crm.home.sub.MsisdnDeletionOnSubscriberDeactivationHome;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.provision.gateway.ServiceProvisioningGatewaySupport;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent which goes to database and removes the  MSISDN 
 * Number from the system which are in "HELD" for configurable time period.
 * @author sgaidhani
 * @date   11 Mar 2013
 * 
 */
public class MSISDNDeletionAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
   
    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {        
    	Home msisdnHome ;

        
        Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
        if ( spidHome == null )
        {
            throw new AgentException("System error: CRMSpidHome not found in context");
        }
        
        /**
         * The strategy is to walk through all the spids and get msisdn's for that spid 
         * which are expired. This way we have access to spid specific expiry date.
         * 
         * There are only a few spids in a system so the impact is really low.
         * Several small sql querries are better than one massive querry which loads everything into memory
         * 
         * It's certainly better than loading all msisdn's :)
         * 
         *  
         */
        
        try
        {
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
                CRMSpid crmSp = (CRMSpid)iter.next();                
                
                GrrGeneratorGeneralConfig grrGeneratorGeneralConfig = HomeSupportHelper.get(ctx).
                		findBean(ctx, GrrGeneratorGeneralConfig.class, crmSp.getSpid());                   

                if(grrGeneratorGeneralConfig != null && (grrGeneratorGeneralConfig.isUpdateFNR() || grrGeneratorGeneralConfig.isUpdateNPGW()) )
                {
                	if(LogSupport.isDebugEnabled(ctx))
                	{
                		LogSupport.debug(ctx, this, "Update on external system will be initiated during MSISDN deletion, based on found GRR General Configuration.");
                	}
                	msisdnHome = new MsisdnBeforeDeletionSpgProvUpdateProxyHome(ctx, (Home)ctx.get(MsisdnHome.class));
                }
        		else
        		{
            		msisdnHome =  (Home)ctx.get(MsisdnHome.class);
        		}
                if ( msisdnHome == null )
                {
                    throw new AgentException("System error: MsisdnHome not found in context");
                }
                
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, - crmSp.getMobileNumberDeletionAfterHeldDays());          
                

                
                /*
                 * We want a behaviour consistent around boundary of a day
                 * Now by clearing time of day we ensure that a new day begins at 00:00 hrs
                 * In the logic below; if any MSISDN whose expiry-date falls on any time today would be cleared from held state
                 */
                calendar = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(calendar);
                
                new DebugLogMsg(this, "About to process Internal MSISDN's in the HELD state for [spid=" + crmSp.getSpid() + "]", null).log(ctx);
                try
                {

                    msisdnHome.where(ctx, new EQ(MsisdnXInfo.STATE,MsisdnStateEnum.HELD))
                    .where(ctx,new EQ(MsisdnXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    .where(ctx,new LT(MsisdnXInfo.LAST_MODIFIED,calendar.getTime()))
                    .where(ctx,new EQ(MsisdnXInfo.EXTERNAL,false))
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(msisdnHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Msisdn msisdn = (Msisdn)obj;
                                    try
                                    {
                                    	getHome().remove(ctx,msisdn);
                                        new DebugLogMsg(this, "Successfully removed Msisdn from the System [msisdn=" + msisdn.getMsisdn() + "]", null).log(ctx);
                                        MsisdnManagement.removeMsisdnFromContractGroup(ctx, msisdn.getMsisdn());
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't updated msisdn :" + msisdn.getMsisdn() + " to AVAILABLE state",e);
                                    }
                                }
                            }
                    ));
                    
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting Internal MSISDN's in HELD state",e);
                }
                
                
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, - crmSp.getExternalMobileNumberDeletionAfterHeldDays());
                /*
                 * We want a behaviour consistent around boundary of a day
                 * Now by clearing time of day we ensure that a new day begins at 00:00 hrs
                 * In the logic below; if any MSISDN whose expiry-date falls on any time today would be cleared from held state
                 */
                calendar = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(calendar);
                
                new DebugLogMsg(this, "About to process External MSISDN's in the HELD state for [spid=" + crmSp.getSpid() + "]", null).log(ctx);
                try
                {
                    msisdnHome.where(ctx, new EQ(MsisdnXInfo.STATE,MsisdnStateEnum.HELD))
                    .where(ctx,new EQ(MsisdnXInfo.SPID, Integer.valueOf(crmSp.getSpid())))
                    .where(ctx,new LT(MsisdnXInfo.LAST_MODIFIED,calendar.getTime()))
                    .where(ctx,new EQ(MsisdnXInfo.EXTERNAL,true))
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(msisdnHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Msisdn msisdn = (Msisdn)obj;
                                    try
                                    {
                                    	
                                        getHome().remove(ctx,msisdn);
                                        new DebugLogMsg(this, "Successfully removed Msisdn record from system for [msisdn=" + msisdn.getMsisdn() + "]", null).log(ctx);
                                        MsisdnManagement.removeMsisdnFromContractGroup(ctx, msisdn.getMsisdn());
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't remove msisdn :" + msisdn.getMsisdn(),e);
                                    }
                                }
                            }
                    ));
                    
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting External MSISDN's in HELD state",e);
                }
                
            }
            
        }
        catch(HomeException e)
        {
              new MinorLogMsg(this, "Error getting data from spid table", e).log(ctx); 
        }
        
   }
}
