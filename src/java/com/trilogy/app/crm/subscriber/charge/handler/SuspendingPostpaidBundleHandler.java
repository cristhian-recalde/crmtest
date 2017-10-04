package com.trilogy.app.crm.subscriber.charge.handler;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.BundleFee;
import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.provision.SuspendEntityActionSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 * 
 * @author sbanerjee
 * @since 9.3.2
 */
public class SuspendingPostpaidBundleHandler
extends GenericHandler
{
    public SuspendingPostpaidBundleHandler(ChargeRefundResultHandler handler)
    {
        super(handler); 
    }

    public SuspendingPostpaidBundleHandler()
    {
        
    }

    public void handleError(Context ctx, ChargableItemResult ret)
    {
        final BundleFee bundleFee = (BundleFee)ret.getChargableObject();
        
        try
        {
            BundleProfile bundle = bundleFee.getBundleProfile(ctx, ret.getSubscriber().getSpid());
            if(!bundle.isRestrictProvisioning())
            {
                return;
            }
                
            switch( ret.getChargeResult())
            {
                case TRANSACTION_FAIL_OCG:
                    //ret.setRunningState(RUNNING_CONTINUE_SUSPEND); 
                    //$FALL-THROUGH$
                case  TRANSACTION_SKIPPED_SUSPEND: 
                    suspendBundle(ctx, ret.getSubscriber(), bundleFee);
            }
        } 
        catch (Exception e)
        {
            if(LogSupport.isDebugEnabled(ctx))
            {
                LogSupport.debug(ctx, this, "Could not fetch BundleProfile for bundle ID: "+ bundleFee.getId(), e);
            }
        }
    }

    
    private int suspendBundle(Context ctx, Subscriber sub, BundleFee fee)
    {
        int ret = ERROR_HANDLE_RESULT_SUCCESS; 
        try{
            if (SuspendEntityActionSupport.suspendBundle(ctx, sub, fee) != 
                SuspendEntityActionSupport.SUCCESS )
            {
                ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;    
            }
        } catch (Exception e)
        {
            new MinorLogMsg(this, "fail to suspend service for postpaid subscriber " +
               sub.getId() + 
               fee.getId(),e).log(ctx);
            ret = ERROR_HANDLE_RESULT_FAIL_SUSPEND;
        }

        return ret; 
    }
}
