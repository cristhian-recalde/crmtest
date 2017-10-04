/*
 * Created on Jun 24, 2005
 */
package com.trilogy.app.crm.subscriber.provision.ipc;

import java.util.Set;

import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.OMLogMsg;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.ipc.IpcProvConfig;
import com.trilogy.app.crm.client.ClientException;
import com.trilogy.app.crm.client.ipcg.IpcgClient;
import com.trilogy.app.crm.client.ipcg.IpcgClientFactory;
import com.trilogy.app.crm.client.ipcg.IpcgClientProxy;
import com.trilogy.app.crm.client.ngrc.AppNGRCClient;
import com.trilogy.app.crm.subscriber.provision.SubscriberProvisionResultCode;
import com.trilogy.app.crm.support.BillCycleSupport;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;

/**
 * @author rattapattu
 */
public class SubscriberIpcProfileUpdateHome extends HomeProxy
{

    public SubscriberIpcProfileUpdateHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[store].....");
    	Subscriber newSub = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);

       if (isIpcProfileExitis(ctx, newSub))
        {
           if (needsToSwapIMSI(ctx, oldSub, newSub))
           {
               swapIMSI(ctx, oldSub, newSub);
           }
           
            if (needsToUpdateIpcProfile(ctx, oldSub, newSub))
            {
                updateIpcProfile(ctx, oldSub, newSub);
            }
        }
        
        return super.store(ctx, obj);
    }
    
    private boolean needsToSwapIMSI(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
        return !SafetyUtil.safeEquals(oldSub.getIMSI(), newSub.getIMSI());
    }
    
    private void swapIMSI(Context ctx, Subscriber oldSub, Subscriber newSub)
    {
        AppNGRCClient client = (AppNGRCClient)ctx.get(AppNGRCClient.class);
        
        try
        {
            client.updateImsi(ctx, oldSub.getIMSI(), newSub.getIMSI());
        }
        catch (ClientException e)
        {
            new MinorLogMsg(this, "Unable to swap IMSI using AppNGRCClient for subscriber=" + newSub.getId(), e).log(ctx);
        }
    }

  
    private boolean isIpcProfileExitis(Context ctx, Subscriber subscriber)
    {
        Set svcSet = subscriber.getServices();
        if (IpcgClientProxy.hasDataService(ctx, svcSet))
        {
            IpcgClient ipcgClient = IpcgClientFactory.locateClient(ctx, subscriber.getTechnology());
            try
            {
                return ipcgClient.isSubscriberProfileAvailable(ctx, subscriber);
            }
            catch (Exception e)
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    private void updateIpcProfile(Context ctx, Subscriber oldSub,
            Subscriber newSub) throws HomeException
    {          
        Set oldSvcSet = oldSub.getServices(ctx);
        Set newSvcSet = newSub.getServices(ctx);
        
        ////if old or new subscriber has no data services then no need to call addChangeSub()
        if (!(IpcgClientProxy.hasDataService(ctx, oldSvcSet) || IpcgClientProxy.hasDataService(ctx, newSvcSet)))
        {
            return;
        }

        IpcProvConfig config = (IpcProvConfig)ctx.get(IpcProvConfig.class);
        int serviceGrade = newSub.getSubscriberType() == SubscriberTypeEnum.POSTPAID ? config.getPostpaidServiceGrade() : config.getPrepaidServiceGrade();
        
        final PricePlan pricePlan = newSub.getRawPricePlanVersion(ctx).getPricePlan(ctx);
        final String ratePlan;
        if(config.isSupportsPriceToRatePlanMapping())
        {
            // TODO - Bad we do this lossy cast...it would go away when we move Data to URCS
            ratePlan = Long.toString(pricePlan.getId());
        }
        else
        {
            new DebugLogMsg(this, "Support of Price Plan to Rate Plan mapping is disabled. Using Rate Plan ID for Subscriber Data rate plan association.", null).log(ctx);
            ratePlan = pricePlan.getDataRatePlan();
        }
        
        int billCycleDay = BillCycleSupport.getBillCycleForBan(ctx, newSub.getBAN()).getDayOfMonth(); 
            
        // This will need to be updated if we support the change of subscriber
        // from one technology type to another.
        IpcgClient ipcgClient =
            IpcgClientFactory.locateClient(ctx, newSub.getTechnology());

        int result = -1;

        try
        {
            result = ipcgClient.addChangeSub(ctx, newSub, (short)billCycleDay, Integer.parseInt(ratePlan), serviceGrade);
        }
        catch (Exception e)
        {
            result = -1;
            SubscriberProvisionResultCode.addException(ctx,
                    "Failed to add or update subscription profile with MSISDN '" + newSub.getMSISDN() + "' to NGRC -> " + e.getMessage(), e,
                    oldSub, newSub);
        }

        if (result != 0)
        {
            String module = Common.OM_IPC_ERROR;
            new OMLogMsg(Common.OM_MODULE, module).log(ctx);
            SubscriberProvisionResultCode.setProvisionIpcErrorCode(ctx, result);

        }
    }

    private boolean needsToUpdateIpcProfile(Context ctx, Subscriber oldSub,
            Subscriber newSub) throws HomeException
    {
        boolean needsToUpdate = false;

        //Check if the state has changed
        if ( ! EnumStateSupportHelper.get(ctx).stateEquals(oldSub, newSub) ) 
        {
            needsToUpdate = true;

            /*
             * For prepaid sub, we only need to update ipcg if sub state changed to 
             * Barred/Locked or Deactivated
             * OR sub state goes to Active  from either Barred/Locked or Available state
             */
            if ( newSub.getSubscriberType() == SubscriberTypeEnum.PREPAID ) 
            {
                int [] prepaidLeavingStates = {
                    SubscriberStateEnum.AVAILABLE_INDEX,
                    SubscriberStateEnum.LOCKED_INDEX
                    };

                if ( (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.LOCKED )) && 
                   (! EnumStateSupportHelper.get(ctx).stateEquals(newSub,SubscriberStateEnum.INACTIVE )) && 
                   (! EnumStateSupportHelper.get(ctx).isTransition(oldSub,newSub,prepaidLeavingStates,
                   SubscriberStateEnum.ACTIVE_INDEX)))
                {
                    needsToUpdate = false;
                }
            }
        }

        if (!needsToUpdate 
                && oldSub.getRawPricePlanVersion(ctx).getPricePlan(ctx).getDataRatePlan() != newSub.getRawPricePlanVersion(ctx).getPricePlan(ctx).getDataRatePlan())
        {
            needsToUpdate = true;
        }

        if (!needsToUpdate 
                && oldSub.getSubscriberType().getIndex() != newSub.getSubscriberType().getIndex())
        {
            needsToUpdate = true;
        }

        return needsToUpdate;
    }

}
