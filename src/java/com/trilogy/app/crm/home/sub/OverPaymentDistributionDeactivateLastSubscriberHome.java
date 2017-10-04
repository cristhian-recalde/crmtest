/**
 * 
 */
package com.trilogy.app.crm.home.sub;

import java.util.Collection;
import java.util.Date;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextSupport;
import com.trilogy.framework.xhome.elang.NEQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.app.crm.account.AccountConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountOverPaymentHistory;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentTypeEnum;
import com.trilogy.app.crm.bean.GroupTypeEnum;
import com.trilogy.app.crm.bean.PayeeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberXInfo;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.app.crm.support.AccountOverPaymentHistorySupport;
import com.trilogy.app.crm.support.AccountSupport;
import com.trilogy.app.crm.support.AdjustmentTypeSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.app.crm.support.TransactionSupport;


/**
 * @author alok.sohani
 *
 */
public class OverPaymentDistributionDeactivateLastSubscriberHome extends HomeProxy {	
	
	public OverPaymentDistributionDeactivateLastSubscriberHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

	public OverPaymentDistributionDeactivateLastSubscriberHome(Home delegate)
    {
        super(delegate);
    }
		
    @Override
    public Object store(final Context ctx, final Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	final PMLogMsg pmLog = new PMLogMsg(OverPaymentDistributionDeactivateLastSubscriberHome.class, "store()");
    	try
    	{
    	
	    	final Subscriber sub = (Subscriber) obj;
	        final Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);      	
	
	        if(!AccountSupport.getAccount(ctx,sub.getBAN()).isResponsible())
	        {
	        	
	        if (!SubscriberStateEnum.INACTIVE.equals(oldSub.getState()) 
	        		&& SubscriberStateEnum.INACTIVE.equals(sub.getState()))
	        {
	        	if(LogSupport.isDebugEnabled(ctx))
	    		{
	    			LogSupport.debug(ctx, this, "OverPaymentDistributionDeactivateLastSubscriberHome : Deactivating subscriber account " +oldSub.getBAN());
	    		}    	  
	        	
	        	final Account responsibleParentAccount =sub.getResponsibleParentAccount(ctx);
	        	
	        	/* Check that the subscriber which is getting deactivated is last Non Responsible in the hierarchy  */      	
	        	
	        	if(AccountSupport.getNonDeActiveChildrenSubscribers((ContextSupport)ctx.get("app"),responsibleParentAccount).size()==1)
	            {
	        		Long overPaymentBalance=AccountOverPaymentHistorySupport.getOverPaymentBalance(ctx, responsibleParentAccount.getBAN());
	        		
	        		/* Check whether the Responsible BAN for last subscriber has any Overpayment balance */
	        		if (overPaymentBalance!=null 
	        		        && overPaymentBalance!=0)
	        		{   
	        			/* If yes , consume the Overpayment balance */
        			
	        			TransactionSupport.createTransaction(ctx, oldSub, overPaymentBalance,0, AdjustmentTypeSupportHelper.get(ctx).getAdjustmentType(ctx, AdjustmentTypeEnum.OverPaymentCredit), 
	        		            false,false, "", new Date(), new Date(),"", 0, PayeeEnum.Account);
	        		}	        		
	            }
	        }   
	        }
	        
	        final Object ret = super.store(ctx, obj);      
	        return ret;
    	}
    	finally
    	{
            pmLog.log(ctx);
    	}     
       
    }
}
