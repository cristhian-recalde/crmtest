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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.home.sub;

import java.util.Date;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryXInfo;
import com.trilogy.app.crm.numbermgn.MsisdnMgmtHistoryXInfo;
import com.trilogy.app.crm.numbermgn.ToggleLatestSubscriberVisitor;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;


/**
 * This home wrapper updates the IMSI history on subscriber create/update/remove.
 * 
 * @author Aaron Gourley
 * @since 7.5
 */
public class ImsiChangeAppendNumberMgmtHistoryHome extends AppendNumberMgmtHistoryHome
{

    public ImsiChangeAppendNumberMgmtHistoryHome(Home delegate)
    {
        super(delegate, ImsiMgmtHistoryHome.class);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber sub = (Subscriber) obj;

        // HLD OID 38604: An entry in the IMSI History is created with the SubID, StartDate
        HistoryEventSupport support = (HistoryEventSupport)ctx.get(HistoryEventSupport.class);
        appendImsiHistory(ctx, sub.getIMSI(), sub.getId(), support.getSubIdModificationEvent(ctx), "IMSI assigned to Subscriber");
        
        return super.create(ctx, obj);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber sub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        
        if( EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, sub, SubscriberStateEnum.INACTIVE) )
        {
            // Subscriber deactivation
            // HLD OID 38603: The current entry in the IMSI History has its EndTimestamp set
            unassignImsiFromSubscriber(ctx, oldSub);
        }
        else if( EnumStateSupportHelper.get(ctx).isLeavingState(oldSub, sub, SubscriberStateEnum.INACTIVE) )
        {
            // Subscriber reactivation
            // HLD OID 38604: An entry in the IMSI History is created with the SubID, StartDate
            HistoryEventSupport support = (HistoryEventSupport)ctx.get(HistoryEventSupport.class);
            appendImsiHistory(ctx, sub.getIMSI(), sub.getId(), support.getSubIdModificationEvent(ctx), "IMSI assigned to Subscriber during reactivation");
        }
        else if( oldSub != null && !SafetyUtil.safeEquals(sub.getIMSI(), oldSub.getIMSI()) )
        {
            // Subscriber IMSI change
            
            // HLD OID 38602: IMSI/SubID mapping is updated when the IMSI/Package ID needs to be
            // assigned to a new subscriber.  All other subscriber modification events don't have
            // their EndTimestamp's set.
            
            // HLD OID 38603: The current entry in the IMSI History has its EndTimestamp set
            unassignImsiFromSubscriber(ctx, oldSub);

            // HLD OID 38604: The current entry in the IMSI History is updated with the new SubID, StartDate
            HistoryEventSupport support = (HistoryEventSupport)ctx.get(HistoryEventSupport.class);
            appendImsiHistory(ctx, sub.getIMSI(), sub.getId(), support.getSubIdModificationEvent(ctx), "New IMSI assigned to Subscriber");   
        }
        
        return super.store(ctx, obj);
    }

    @Override
    public void remove(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[remove].....");
    	Subscriber sub = (Subscriber) obj;

        // HLD OID 38603: The current entry in the IMSI History has its EndTimestamp set
        unassignImsiFromSubscriber(ctx, sub);
        
        super.remove(ctx, obj);
    }


    /**
     * Sets the end timestamp for the latest IMSI history record, and flips the latest field to false.
     * There will be no 'latest' entry after this update because no subscriber owns the IMSI anymore. 
     */
    private void unassignImsiFromSubscriber(Context ctx, Subscriber sub) throws HomeException, HomeInternalException
    {
        Date curDate = new Date();
        final And filter = new And();
        filter.add(new EQ(ImsiMgmtHistoryXInfo.TERMINAL_ID, sub.getIMSI()));
        filter.add(new EQ(ImsiMgmtHistoryXInfo.SUBSCRIBER_ID, sub.getId()));
        filter.add(new LTE(ImsiMgmtHistoryXInfo.TIMESTAMP, curDate));
        filter.add(new GT(ImsiMgmtHistoryXInfo.END_TIMESTAMP, curDate));

        
        Home imsiHistoryHome = (Home) ctx.get(ImsiMgmtHistoryHome.class);
        Home filteredHome = imsiHistoryHome.where(ctx, filter);
        filteredHome.forEach(
                ctx, 
                new ToggleLatestSubscriberVisitor(
                        false,
                        sub.getId(),
                        new Date(),
                        imsiHistoryHome));
    }
    
}
