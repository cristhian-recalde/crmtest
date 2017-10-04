package com.trilogy.app.crm.web.control;

import com.trilogy.app.crm.bean.AdditionalMsisdnBean;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;

public class AdditionalMsisdnGroupAvailMsisdnWebControl extends MsisdnGroupAvailMsisdnWebControl
{
    public AdditionalMsisdnGroupAvailMsisdnWebControl(
            final PropertyInfo msisdnGroupProperty,
            final PropertyInfo banProperty,
            final PropertyInfo spidProperty,
            final PropertyInfo subscriberTypeProperty,
            final PropertyInfo technologyProperty,
            final PropertyInfo subscriptionTypeProperty,
            final boolean isOptional)
    {
        super(msisdnGroupProperty, banProperty, spidProperty, subscriberTypeProperty, technologyProperty, subscriptionTypeProperty, isOptional);
    }
    
    private Context createSubContext(final Context context)
    {
        AdditionalMsisdnBean bean = (AdditionalMsisdnBean) context.get(AbstractWebControl.BEAN);
        Context subCtx = context.createSubContext();
        subCtx.put(AbstractWebControl.BEAN, bean.getSubscriber());
        return subCtx;
    }

    protected TechnologyEnum getTechnology(final Context context)
    {
        return super.getTechnology(createSubContext(context));
    }

    protected int getSpid(Context context)
    {
        return super.getSpid(createSubContext(context));
    }
    
    protected SubscriberTypeEnum getSubscriberType(Context context)
    {
        return super.getSubscriberType(createSubContext(context));
    }
}
