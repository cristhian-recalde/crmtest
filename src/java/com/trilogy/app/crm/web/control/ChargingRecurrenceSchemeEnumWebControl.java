package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ui.BundleProfile;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;

public class ChargingRecurrenceSchemeEnumWebControl extends ServicePeriodEnumWebControl
{
    public ChargingRecurrenceSchemeEnumWebControl()
    {
        this(true);
    }
    
    public ChargingRecurrenceSchemeEnumWebControl(boolean autoPreview)
    {
        super(autoPreview);
    }

    // Hardcoded cached collections.  These must be sorted by index in ascending order in order
    // for EnumCollection.getByIndex() to work properly because it uses binary search.
    private static EnumCollection BUNDLE_CHARGING_RECURRENCE_SCHEMES = new EnumCollection(new Enum[]
    {
            ServicePeriodEnum.MONTHLY,
            ServicePeriodEnum.ONE_TIME,
            ServicePeriodEnum.MULTIMONTHLY,
            ServicePeriodEnum.DAILY,
            ServicePeriodEnum.MULTIDAY
    });
    
    @Override
    public EnumCollection getEnumCollection(Context ctx)
    {
        if (ctx.get(AbstractWebControl.BEAN) instanceof BundleProfile)
        {
            return BUNDLE_CHARGING_RECURRENCE_SCHEMES;
        }
        
        return super.getEnumCollection(ctx);
    }
    
}
