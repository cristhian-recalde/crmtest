package com.trilogy.app.crm.clean;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.GeneralConfigSupport;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.PortingTypeEnum;
import com.trilogy.app.crm.numbermgn.MsisdnManagement;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.False;
import com.trilogy.framework.xhome.elang.LT;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * ContextAgent which goes to database and sets the state of MSISDN 
 * Number from "HELD" to "AVAIABLE" after configurable time period.
 * @author lzou
 * @date   Nov 14, 2003
 * 
 */
public class MSISDNStateModifyAgent implements ContextAgent
{
    final static long DAY_IN_MILLS = 24*60*60*1000L;
    
    
    /**
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException
    {       
        final Home msisdnHome  = (Home)ctx.get(MsisdnHome.class);
        if ( msisdnHome == null )
        {
            throw new AgentException("System error: MsisdnHome not found in context");
        }
        
        Home spidHome = ( Home ) ctx.get(CRMSpidHome.class);
        if ( spidHome == null )
        {
            throw new AgentException("System error: CRMSpidHome not found in context");
        }
        
        /**
         * The startergy is to walk through all the spids and get msisdn's for that spid 
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
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, - crmSp.getMobileNumberHeldDays());
                /*
                 * We want a behaviour consistent around boundary of a day
                 * Now by clearing time of day we ensure that a new day begins at 00:00 hrs
                 * In the logic below; if any MSISDN whose expiry-date falls on any time today would be cleared from held state
                 */
                calendar = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(calendar);
                final And condition = new And();
                condition.add(new EQ(MsisdnXInfo.STATE,MsisdnStateEnum.HELD));
                
                // Share pool condition 
                Predicate p = GeneralConfigSupport.isAllowedSharedMsisdnAcrossSpids(ctx) ?
                        new LT(MsisdnXInfo.SPID, Integer.valueOf(0)) :
                            False.instance();
                final Or or = new Or();
                or.add(new EQ(MsisdnXInfo.SPID, Integer.valueOf(crmSp.getSpid())));
                or.add(p);
                condition.add(or);
                
                new DebugLogMsg(this, "About to process Internal MSISDN's in the HELD state for [spid=" + crmSp.getSpid() + "]", null).log(ctx);
                try
                {
                    condition.add(new LT(MsisdnXInfo.LAST_MODIFIED,calendar.getTime()));
                    condition.add(new EQ(MsisdnXInfo.EXTERNAL,false));

                    msisdnHome.where(ctx, condition)
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(msisdnHome)
                            {
                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Msisdn msisdn = (Msisdn)obj;
                                    if(msisdn.getPortingType().getIndex() == PortingTypeEnum.OUT_INDEX)
                                    {
                                        LogSupport.debug(ctx,this,"Couldn't updated msisdn " + msisdn.getMsisdn() + " as this is PORT_OUT msisdn ");
                                    	return;
                                    }
                                    msisdn.setState(MsisdnStateEnum.AVAILABLE);
                                    msisdn.setBAN(null);
                                    try
                                    {
                                        getHome().store(ctx,msisdn);
                                        new DebugLogMsg(this, "Successfully moved Msisdn record to AVAILABLE for [msisdn=" + msisdn.getMsisdn() + "]", null).log(ctx);
                                        MsisdnManagement.removeMsisdnFromContractGroup(ctx, msisdn.getMsisdn());
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't updated msisdn " + msisdn.getMsisdn() + " to AVAILABLE state",e);
                                    }
                                }
                            }
                    ));
                    
                }
                catch (Exception e)
                {
                    LogSupport.minor(ctx,this,"Error selecting Internal MSISDN's in HELD state",e);
                }
                
                long extExpiryDate = new Date().getTime() - crmSp.getExternalMobileNumberHeldDays()*DAY_IN_MILLS;
                
                new DebugLogMsg(this, "About to process External MSISDN's in the HELD state for [spid=" + crmSp.getSpid() + "]", null).log(ctx);
                try
                {
                    condition.add(new LT(MsisdnXInfo.LAST_MODIFIED,new Date(extExpiryDate)));
                    condition.add(new LT(MsisdnXInfo.EXTERNAL,false));
                    
                    msisdnHome.where(ctx, condition)
                    .forEach(ctx, new CloneingVisitor(new HomeVisitor(msisdnHome)
                            {

                                public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                                {
                                    Msisdn msisdn = (Msisdn)obj;
                                    if(msisdn.getPortingType().getIndex() == PortingTypeEnum.OUT_INDEX)
                                    {
                                        LogSupport.debug(ctx,this,"Couldn't updated msisdn " + msisdn.getMsisdn() + " as this is PORT_OUT msisdn ");
                                    	return;
                                    }
                                    msisdn.setState(MsisdnStateEnum.AVAILABLE);
                                    msisdn.setBAN(null);
                                    try
                                    {
                                        getHome().store(ctx,msisdn);
                                        new DebugLogMsg(this, "Successfully moved Msisdn record to AVAILABLE for [msisdn=" + msisdn.getMsisdn() + "]", null).log(ctx);
                                        MsisdnManagement.removeMsisdnFromContractGroup(ctx, msisdn.getMsisdn());
                                    }
                                    catch (Exception e)
                                    {
                                        LogSupport.minor(ctx,this,"Couldn't updated msisdn " + msisdn.getMsisdn() + " to AVAILABLE state",e);
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
