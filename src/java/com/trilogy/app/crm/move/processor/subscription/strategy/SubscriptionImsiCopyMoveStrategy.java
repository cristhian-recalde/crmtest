package com.trilogy.app.crm.move.processor.subscription.strategy;

import java.util.Date;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.move.MoveException;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategy;
import com.trilogy.app.crm.move.processor.strategy.CopyMoveStrategyProxy;
import com.trilogy.app.crm.move.request.SubscriptionMoveRequest;
import com.trilogy.app.crm.numbermgn.AppendNumberMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.HistoryEventSupport;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryHome;
import com.trilogy.app.crm.numbermgn.ImsiMgmtHistoryXInfo;
import com.trilogy.app.crm.numbermgn.ToggleLatestSubscriberVisitor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.GT;
import com.trilogy.framework.xhome.elang.LTE;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * 
 * @author kabhay
 *
 */
public class SubscriptionImsiCopyMoveStrategy<SMR extends SubscriptionMoveRequest> extends CopyMoveStrategyProxy<SMR> 
{
	

	public SubscriptionImsiCopyMoveStrategy(CopyMoveStrategy<SMR> delegate) 
	{
		super(delegate);
	}

	@Override
	public void createNewEntity(Context ctx, SMR request) throws MoveException 
	{
		super.createNewEntity(ctx, request);
		
		Subscriber oldSubscription = request.getOldSubscription(ctx);
		Subscriber newSubscription = request.getNewSubscription(ctx);
		
		if(!oldSubscription.getId().equals(newSubscription.getId()))
		{
			/*
			 * update IMSI history as well.
			 */
			try {
				unassignImsiFromSubscriber(ctx, oldSubscription);
				HistoryEventSupport support = (HistoryEventSupport)ctx.get(HistoryEventSupport.class);
				getAppendNumberMgmtHistory(ctx).appendImsiHistory(ctx, newSubscription.getIMSI(), newSubscription.getId(),new Date(), support.getSubIdModificationEvent(ctx), true,"IMSI assigned to Subscriber during Subscription Move");
				
			} catch (HomeException e) {
				String msg = "Error occurred while updating IMSI History during subscription move, MSISDN " + newSubscription.getMsisdn();
                new MajorLogMsg(this, msg + ": " + e.getMessage(), null).log(ctx);
                throw new MoveException(request, msg, e);
			}
			
			
		}
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
    
    private AppendNumberMgmtHistoryHome getAppendNumberMgmtHistory(Context ctx)
    {
        if (appendNumberMgmtHistory_==null)
        {
            appendNumberMgmtHistory_ = new AppendNumberMgmtHistoryHome(ctx, NullHome.instance(), ImsiMgmtHistoryHome.class){}; 
        }
        return appendNumberMgmtHistory_;
    }
    
    private AppendNumberMgmtHistoryHome appendNumberMgmtHistory_ = null;
    
	
	
	
}
