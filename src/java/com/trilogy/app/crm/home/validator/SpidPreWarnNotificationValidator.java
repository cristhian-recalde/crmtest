package com.trilogy.app.crm.home.validator;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeRecurringChargeEnum;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;


public class SpidPreWarnNotificationValidator implements Validator
{
    private SpidPreWarnNotificationValidator()
    {
    }
    
    public static Validator instance()
    {
        if (instance_ == null)
        {
            instance_ = new SpidPreWarnNotificationValidator();
        }
        
        return instance_;
    }
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CRMSpid spid = (CRMSpid) obj;
        
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        int MAX_DAY = MAX_DAYS_MONTHLY;
        
        // If weekly recurring recharge is enabled for SPID, use WEEKLY maximum
        if (SubscriberTypeRecurringChargeEnum.POSTPAID != spid.getRecurChargeSubscriberType())
        {
            MAX_DAY = MAX_DAYS_WEEKLY;
        }
        
        if (spid.getRecurringChargePrepaidNotificationDaysBefore()>MAX_DAY)
        {
            exception.thrown(new IllegalPropertyArgumentException(CRMSpidXInfo.RECURRING_CHARGE_PREPAID_NOTIFICATION_DAYS_BEFORE, "The number of days prior charging for notification should be less than or equal to " + MAX_DAY));
        }
        
        if( spid.getRecChrgPrepdInsufBalNotifDaysBefore() > MAX_DAY )
        {
        	exception.thrown(new IllegalPropertyArgumentException(CRMSpidXInfo.REC_CHRG_PREPD_INSUF_BAL_NOTIF_DAYS_BEFORE , "The number of days prior charging for notification should be less than or equal to " + MAX_DAY ));
        }
        
        exception.throwAll();
        
    }
    
    private static final int MAX_DAYS_WEEKLY = 7;
    private static final int MAX_DAYS_MONTHLY = 28;
    
    private static Validator instance_ = null;
}
