package com.trilogy.app.crm.home.sub;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.subscriber.BypassURCSAware;
import com.trilogy.app.crm.extension.subscriber.PPSMSupporteeSubExtension;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * This home is necessary to make sure that the BypassURCS Aware Sub Extension is validated before
 * the subscriber bean is created, saved in the database before the charging occurs, but is only 
 * updated on URCS after the subscriber profile is created on URCS.
 * 
 * @author Marcio Marques
 *
 */
public class BypassURCSAwareSubExtensionInstallationHome extends HomeProxy
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public BypassURCSAwareSubExtensionInstallationHome(Context context, Home delegate)
    {
        super(context, delegate);
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
    	LogSupport.debug(ctx, this, "SubscriberPipeline[create].....");
    	Subscriber subscriber = (Subscriber) obj;
        boolean found = false;
        
        for (Extension extension : subscriber.getExtensions())
        {
            if (extension instanceof BypassURCSAware)
            {
                ((BypassURCSAware) extension).bypassURCS();
                found = true;
            }
        }
        
        Subscriber result = (Subscriber) super.create(ctx, obj);
        
        if (found)
        {
            for (Extension extension : result.getExtensions())
            {
                if (extension instanceof BypassURCSAware)
                {
                    ((BypassURCSAware) extension).unBypassURCS();
                }
            }
        }
        
        return result;
    }
    
}
