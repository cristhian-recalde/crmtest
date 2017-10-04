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
package com.trilogy.app.crm.subscriber.cron;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.filter.SubscriberFutureDeactivationPredicate;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.elang.Or;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.visitor.CloneingVisitor;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.xdb.SimpleXStatement;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * @author lxia
 */
public class SubscriberFutureActiveOrDeactiveAgent  implements ContextAgent 
{

    /* (non-Javadoc)
     * @see com.redknee.framework.xhome.context.ContextAgent#execute(com.redknee.framework.xhome.context.Context)
     */
    public void execute(Context ctx) throws AgentException 
    {
        try 
        {
            Home spidHome = (Home) ctx.get(CRMSpidHome.class);
            
            Collection spids = spidHome.selectAll();
            
            final Iterator iter = spids.iterator();
            
            //for each spid
            while (iter.hasNext())
            {
                CRMSpid crmSp = (CRMSpid)iter.next();
                try
                {
                    activateSubscribers(ctx, crmSp);
                    deactivateSubscribers(ctx, crmSp);
                }
                catch (Exception e)
                {
                    LogSupport.debug(ctx,this,"Error in SubscriberFutureActiveOrDeactiveAgent",e);
                }
            }
            closeWriters(); 
        }
        catch(Exception e)
        {
            throw new AgentException("SubscriberFutureActivation error:" + e, e);
        }

    }
    
    /**
     * @param ctx
     * @param serviceProvider
     * @throws HomeException
     * @throws UnsupportedOperationException
     */
    private void activateSubscribers(Context ctx, CRMSpid serviceProvider) 
    throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no SubscriberHome found in context.");
        }
        Date today = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(new Date());
        
        
        Predicate predicate = getSubscribersToActivatePredicate(ctx, serviceProvider, today);

        //System.out.println("Activating subscriber ----");
        home.where(ctx,predicate)
        .forEach(new CloneingVisitor(new Visitor()
                {
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        Subscriber sub = (Subscriber)obj;
                        //System.out.println(" ----"+sub.getMSISDN());
                        updateSubscriber( ctx, sub, SubscriberStateEnum.ACTIVE);
                    }
                }
           ));
        
        new InfoLogMsg(
            this,
            "Attempting to update states for those subscribers (in spid=" + serviceProvider.getId() +
            ") whose Available Timers need to be active",
            null).log(ctx);
        
    }

    /**
     * return sql for retriving subscribers need to activate
     * @param ctx
     * @param serviceProvider
     * @param today
     * @return
     */
    protected Predicate getSubscribersToActivatePredicate(Context ctx, CRMSpid serviceProvider, Date today)
    {
        And predicate = new And();
        predicate.add(new EQ(SubscriberXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())));
        predicate.add(new LTE(SubscriberXInfo.START_DATE, today));
        predicate.add(new GT(SubscriberXInfo.START_DATE, new Date(0)));
        
        Or or = new Or();

        if (!SystemSupport.supportsPrepaidCreationInActiveState(ctx))
        {
            And availablePrepaid = new And();
            availablePrepaid.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.AVAILABLE));
            availablePrepaid.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID));
            or.add(availablePrepaid);
        }
            
        if (SystemSupport.supportsPrepaidPendingState(ctx))
        {
            or.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.PENDING));
        }
        else
        {
            And pendingPostpaid = new And();
            pendingPostpaid.add(new EQ(SubscriberXInfo.STATE, SubscriberStateEnum.PENDING));
            pendingPostpaid.add(new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.POSTPAID));
            or.add(pendingPostpaid);
        }
        
        predicate.add(or);
        
        return predicate;
    }  
     

       /**
     * @param ctx
     * @param serviceProvider
     * @throws HomeException
     * @throws UnsupportedOperationException
     */
    private void deactivateSubscribers(Context ctx, CRMSpid serviceProvider) 
    throws HomeException
    {
        final Home home = (Home) ctx.get(SubscriberHome.class);

        if (home == null)
        {
            throw new HomeException("System error: no SubscriberHome found in context.");
        }
        Date today = CalendarSupportHelper.get(ctx).getDateWithLastSecondofDay(new Date());
        
        final String sqlQuery =  
            " state NOT in " + SubscriberFutureDeactivationPredicate.getValidateStates() +  
            " AND spid = " + serviceProvider.getId() +
            " AND enddate <= " + today.getTime() +
            " AND enddate > 0";
        
        /*home.where(ctx,new Not(new In(SubscriberXInfo.STATE,SubscriberFutureDeactivationPredicate.getValidateStatesAsSet())))
        .where(ctx,new EQ(SubscriberXInfo.SUBSCRIBER_TYPE, SubscriberTypeEnum.PREPAID))
        .where(ctx, new EQ(SubscriberXInfo.SPID, Integer.valueOf(serviceProvider.getSpid())))
        .where(ctx, new LTE(SubscriberXInfo.END_DATE,new Date(today.getTime())))
        //.where(ctx, new GT(SubscriberXInfo.END_DATE, Long.valueOf(0)))*/
        home.where(ctx, new SimpleXStatement(sqlQuery))
        .forEach(new CloneingVisitor(new Visitor()
                {
                    public void visit(Context ctx, Object obj) throws AgentException, AbortVisitException
                    {
                        Subscriber sub = (Subscriber)obj;
                        updateSubscriber( ctx, sub, SubscriberStateEnum.INACTIVE);
                    }
                }
           ));
        
        new InfoLogMsg(
            this,
            "Attempting to update states for those subscribers (in spid=" + serviceProvider.getId() +
            ") whose Available Timers need to be deactivated",
            null).log(ctx);
        
    }    
    
    private void updateSubscriber(Context context, Subscriber sub, SubscriberStateEnum state)
    {
        final Home home = (Home) context.get(SubscriberHome.class);
        try 
        {
            // it is a long process, must refress subscriber before updating. 
            final Subscriber subscriber =  SubscriberSupport.lookupSubscriberForSubId( context, sub.getId() );
            
            // Do state change for subscriber.
            subscriber.setState(state);
            home.store(context,subscriber);
            if (getSuccessWriter(context) != null)
            {
                getSuccessWriter(context).println("Subscriber " + sub.getId() +  " was set to state " + state.getDescription() + "successfully");
            } 
        } 
        catch (Exception e )
        {
            new MinorLogMsg(this, "fail to set subscriber" + sub.getId() + " to state of " + state.getDescription(), e).log(context); 
            if ( getFailWriter(context)!= null)
            {
                getFailWriter(context).println("fail to set subscriber" + sub.getId() + " to state of " + state.getDescription());
            } 
        }
    }

    private PrintWriter getSuccessWriter(Context ctx)
    {
        if ( successWriter == null)
        {
            successWriter = getWriter(ctx, SUCEED_SUBS_FILE_NAME);
        }
        return successWriter; 
     }

    private PrintWriter getFailWriter(Context ctx)
    {
        if ( failWriter != null)
        {
            failWriter = getWriter(ctx, FAIL_SUBS_FILE_NAME);
        }
        return failWriter; 
       
   }
    
    
        
    private PrintWriter getWriter(Context ctx, String filename) 
    {
 
        StringBuilder buff = new StringBuilder();
        
        buff.append(ctx.get("Platform.ProjectHome"));
        File homeDir = new File(buff.toString());
        if(!homeDir.exists()){
            new MinorLogMsg(this,"Project HOme is not exist in Context!", null).log(ctx);
            return null;
        }
        buff.append(File.separator);
        buff.append("SubRecovery");
        
        File dir = new File(buff.toString());
        if(!dir.exists()){
            dir.mkdirs();
        }
        
        buff.append(File.separator);
               buff.append(filename);
        Date file_date = new Date();    
        
        synchronized ( formatter )
        {
             buff.append(formatter.format(file_date));
        }       
        File dataFile = new File(buff.toString()) ;
        
        try
        {
             return new PrintWriter(new FileWriter(dataFile));
       }  
        catch(IOException e)
        {
            return null;   
        }
      }
    
    public void closeWriters() {
        if ( failWriter != null )
        {
            failWriter.close();
        } 
        if ( successWriter != null)
        {
            successWriter.close();
        } 
    }
    
    
   
    private PrintWriter failWriter = null;        
    private PrintWriter successWriter = null;        
    public static String SUCEED_SUBS_FILE_NAME = "ActivatedSubscriberList_";
    public static String FAIL_SUBS_FILE_NAME   = "FailToActivatedSubscriberList_";
    protected static final SimpleDateFormat formatter =  new SimpleDateFormat("yyyyMMdd_HHmmss");
 
}

