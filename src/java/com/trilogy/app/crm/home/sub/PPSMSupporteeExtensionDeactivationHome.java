package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.app.crm.support.EnumStateSupportHelper;
import com.trilogy.app.crm.support.Lookup;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;


/**
 * This home is necessary to make sure that the PPSM Supportee Extension is kept in the context when
 * the subscriber is being deactivated, so that the refunds can still go to the old supporter.
 * 
 * @author Marcio Marques
 *
 */
public class PPSMSupporteeExtensionDeactivationHome extends HomeProxy
{
    public PPSMSupporteeExtensionDeactivationHome(Context context, Home delegate)
    {
        super(context, delegate);
    }

    @Override
    public Object store(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Subscriber subscriber = (Subscriber) obj;
        Subscriber oldSub = (Subscriber) ctx.get(Lookup.OLDSUBSCRIBER);
        Context subCtx = ctx;
        
        if (EnumStateSupportHelper.get(ctx).isEnteringState(oldSub, subscriber, subscriber.getFinalStates()))
        {
            for (Extension extension : subscriber.getExtensions())
            {
                if (extension instanceof PPSMSupporteeSubExtension)
                {
                    subCtx = subCtx.createSubContext();
                    subCtx.put(DEACTIVATED_SUPPORTEE_EXTENSION, extension);
                    break;
                }
            }
        }
        
        Subscriber result = (Subscriber) super.store(subCtx, obj);
        
        return result;
    }
    
    public static String DEACTIVATED_SUPPORTEE_EXTENSION = "PPSMSupporteeSubExtension.DeactivatedSuporteeExtension";

}
