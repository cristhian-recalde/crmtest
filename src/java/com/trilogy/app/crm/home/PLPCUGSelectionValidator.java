package com.trilogy.app.crm.home;

import com.trilogy.app.crm.bean.AuxiliaryService;
import com.trilogy.app.crm.bean.AuxiliaryServiceHome;
import com.trilogy.app.crm.bean.AuxiliaryServiceSelection;
import com.trilogy.app.crm.bean.AuxiliaryServiceTypeEnum;
import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAwareSupport;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;


public class PLPCUGSelectionValidator extends ContextAwareSupport implements Validator
{

    public PLPCUGSelectionValidator(final Context ctx)
    {
        setContext(ctx);
    }


    // INHERIT
    public void validate(Context ctx, Object obj) throws IllegalStateException
    {
        final AuxiliaryServiceSelection selection = (AuxiliaryServiceSelection) obj;
        AuxiliaryService auxSvx = getRelatedAuxiliaryService(ctx, selection.getSelectionIdentifier());
        if (auxSvx != null && auxSvx.getType() != null && auxSvx.getType() == AuxiliaryServiceTypeEnum.CallingGroup)
            throw new IllegalStateException(
                    "You can not add PLP/CUG auxiliary Services while creating the subscriber, please try adding PLP/CUG auxiliary services once subscriber is created");
    }


    private AuxiliaryService getRelatedAuxiliaryService(Context ctx, long auxServiceId)
    {
        AuxiliaryService auxService = null;
        try
        {
            Home auxServiceHome = (Home) ctx.get(AuxiliaryServiceHome.class);
            auxService = (AuxiliaryService) auxServiceHome.find(ctx, Long.valueOf(auxServiceId));
        }
        catch (final HomeException exception)
        {
            final String message = "Homezone creation falied:Auxiliary Service with ID [" + auxServiceId
                    + "] couldn't be found";
            new MajorLogMsg(this, message, exception).log(ctx);
        }
        return auxService;
    }
}
