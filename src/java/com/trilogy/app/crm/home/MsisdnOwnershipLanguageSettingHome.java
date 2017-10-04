package com.trilogy.app.crm.home;

import java.util.Collection;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.SubscriberLanguageException;
import com.trilogy.app.crm.support.MultiLanguageSupport;

/**
 * This home handles provisioning of the language associated with each msisdn (if Multi Language is supported in the system)
 * @author ltang
 *
 */
public class MsisdnOwnershipLanguageSettingHome extends HomeProxy
{

    public MsisdnOwnershipLanguageSettingHome(Context ctx, Home delegate)
    {
        super(ctx, delegate);
    }
    
    @Override
    public Object find(Context ctx, Object obj) throws HomeException, HomeInternalException
    { 
        MsisdnOwnership ownership = (MsisdnOwnership) getDelegate(ctx).find(ctx, obj);
        return unadapt(ctx, ownership);        
    }

    @Override
    public Collection select(Context ctx, Object obj) throws HomeException, HomeInternalException
    {       
        List<MsisdnOwnership> list = (List<MsisdnOwnership>)getDelegate(ctx).select(ctx, obj);

        for (MsisdnOwnership ownership : list)
        {
            ownership = unadapt(ctx, ownership);
        }
        
        return list;    
    }

    private MsisdnOwnership unadapt(Context ctx, MsisdnOwnership ownership) throws HomeException
    {
        if(ownership==null)
        {
            return null;
        }
        try
        {
            String lang = MultiLanguageSupport.getSubscriberLanguage(ctx, ownership.getSpid(), ownership.getMsisdn());
            ownership.setLanguage(lang);
        }
        catch (ProvisioningHomeException e)
        {
            //ownership.setLanguage("Unknown");
			String errorMsg =
			    SubscriberLanguageException.getVerboseResult(ctx,
			        e.getResultCode());
            new MinorLogMsg(this, "Error encountered attempting to set language preferences for msisdn " + ownership.getOriginalMsisdn() + ": " + errorMsg, e).log(ctx);
        }   
        catch (HomeException e)
        {
            //ownership.setLanguage("Unknown");
            new MinorLogMsg(this, "HomeException encountered attempting to set language preferenes for msisdn " + ownership.getOriginalMsisdn(), e).log(ctx);
        }  
        return ownership;
    }
    
}
