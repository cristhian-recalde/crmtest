package com.trilogy.app.crm.web.control;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.LicenseConstants;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.filter.SubscriberTypeEnumPredicate;
import com.trilogy.app.crm.support.LicensingSupportHelper;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.EnumWebControl;
import com.trilogy.framework.xhome.xenum.Enum;
import com.trilogy.framework.xhome.xenum.EnumCollection;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class SubscriberTypeEnumWebControl
extends EnumWebControl
{
    
    public SubscriberTypeEnumWebControl()
    {
        this(true);
    }
    
    public SubscriberTypeEnumWebControl(boolean autoPreview)
    {
        super(SubscriberTypeEnum.COLLECTION, autoPreview);
        setPredicate(new SubscriberTypeEnumPredicate());
    }
    
}
