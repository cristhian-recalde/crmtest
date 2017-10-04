package com.trilogy.app.crm.home.validator;

import java.util.Collection;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.extension.Extension;
import com.trilogy.app.crm.extension.spid.PricePlanSwitchLimitSpidExtension;
import com.trilogy.app.crm.support.PricePlanSwitchLimitSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;

/**
 * Prevents removal of PricePlanSwitchLimitSpidExtension and also any change in
 * Counter Profile ID. 
 * 
 * @author amahmood
 * @since 8.5
 */
public class PricePlanSwitchLimitSpidExtensionValidator implements Validator
{

    @Override
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        CRMSpid spid = (CRMSpid) obj;
        
        final Collection<Extension> newExtensions = spid.getExtensions();
        final PricePlanSwitchLimitSpidExtension oldExt =  PricePlanSwitchLimitSupport.getPricePlanSwitchLimitSpidExtension(ctx, spid.getId());

        if (oldExt == null)
        {
            return;
        }
        else
        {
            PricePlanSwitchLimitSpidExtension newExt =  null;
            boolean valid = true;
            
            if (newExtensions == null)
            {
                valid = false;
            }
            else
            {
                for (Extension newExtension : newExtensions)
                {
                    if (newExtension instanceof PricePlanSwitchLimitSpidExtension)
                    {
                        newExt = (PricePlanSwitchLimitSpidExtension)newExtension; 
                        break;
                    }
                }       
                valid = valid & newExt != null;
            }
            if (!valid)
            {
                exception.thrown(new IllegalPropertyArgumentException(spid.getExtensionHolderProperty(), 
                        "Price Plan Switch Limit spid extension - cannot be removed once configured."));
            }
            
            if (newExt != null)
            {
                if (oldExt.getCounterProfileId() != newExt.getCounterProfileId())
                {
                    exception.thrown(new IllegalPropertyArgumentException(PricePlanSwitchLimitSpidExtension.COUNTERPROFILEID_PROPERTY, 
                            "Price Plan Switch Limit spid extension - Counter Profile ID cannot be modified once assigned."));
                }
            }
            
        }
        
        exception.throwAll();
    }
}
