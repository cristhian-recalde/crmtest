package com.trilogy.app.crm.filter;

import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

// Filter out all options except CUSTOM if the subscriber/account is prepaid and the System Feature Configuration indicates that 
public class MsisdnEntryTypePredicate implements Predicate
{
    private PropertyInfo subTypeProperty_;

    public MsisdnEntryTypePredicate(PropertyInfo subTypeProperty)
    {
        subTypeProperty_ = subTypeProperty;
    }

    public boolean f(Context ctx, Object obj) throws AbortVisitException
    {
        final SysFeatureCfg systemConfiguration = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        
        MsisdnEntryTypeEnum entryType = (MsisdnEntryTypeEnum)obj;
        Object obj1 = (Object)ctx.get(AbstractWebControl.BEAN);
        
        if (systemConfiguration.isPrepaidMsisdnSelectionEnabled())
        {
            return true;
        }
        else if (entryType != MsisdnEntryTypeEnum.CUSTOM_ENTRY)
        {

            if (subTypeProperty_ != null)
            {
                SubscriberTypeEnum subType = (SubscriberTypeEnum) subTypeProperty_.get(obj1);
                if(subType == SubscriberTypeEnum.PREPAID)
                {
                    return false;
                }
            }
            return true;
            
        }
        else
        {
            // custom option is always allowed
            return true;
        }
    }
}
