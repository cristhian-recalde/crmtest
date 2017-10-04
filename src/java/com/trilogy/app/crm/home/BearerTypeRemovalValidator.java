package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.BearerType;
import com.trilogy.app.crm.bean.BearerTypeXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.AddMsisdnAuxSvcExtensionXInfo;
import com.trilogy.app.crm.extension.auxiliaryservice.core.AddMsisdnAuxSvcExtension;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

public class BearerTypeRemovalValidator implements Validator
{
    /**
     * {@inheritDoc}
     */
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException el = new CompoundIllegalStateException();
        BearerType bearerType = (BearerType) obj;
        try
        {
            if (HomeSupportHelper.get(ctx).hasBeans(ctx, AddMsisdnAuxSvcExtension.class,
                    new EQ(AddMsisdnAuxSvcExtensionXInfo.BEARER_TYPE, bearerType.getId())))
            {
                el.thrown(new IllegalPropertyArgumentException(BearerTypeXInfo.ID, "Bearer Type '" + bearerType.getId()
                        + "' is in use by some auxiliary services and cannot be removed"));
            }
        }
        catch (HomeException e)
        {
            String msg = "Unable to verify if Bearer Type '" + bearerType.getId()
                    + "' is in use by any auxiliary service";
            LogSupport.minor(ctx, this, msg + ": " + e.getMessage());
            el.thrown(new IllegalStateException(msg, e));
        }
        el.throwAll();
    }

}