package com.trilogy.app.crm.support;

import com.trilogy.app.crm.bean.OcgAdj2CrmAdj;
import com.trilogy.app.crm.bean.OcgAdj2CrmAdjHome;
import com.trilogy.app.crm.bean.OcgAdj2CrmAdjID;
import com.trilogy.app.crm.bean.Spid2DefaultAdj;
import com.trilogy.app.crm.bean.Spid2DefaultAdjHome;
import com.trilogy.app.crm.bean.core.AdjustmentType;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.InfoLogMsg;


public class OcgAdj2CrmAdjSupport
{
    
    /**
     * Retur
     * 
     * @param ctx
     * @param spid
     * @param ocgAdjId
     * 
     * @return AdjustmentType
     * @throws HomeException
     */
    public static AdjustmentType mapOcgAdj2CrmAdjType(final Context ctx, final int spid, final String ocgAdjId) 
        throws HomeException
    {
        
        final OcgAdj2CrmAdj ocgAdjMapping = 
            HomeSupportHelper.get(ctx).findBean(ctx, OcgAdj2CrmAdj.class, new OcgAdj2CrmAdjID(spid, ocgAdjId));

        int adjId = -1;
        if (ocgAdjMapping != null)
        {
            adjId = ocgAdjMapping.getAdjId();
        }
        else
        {
            new InfoLogMsg(OcgAdj2CrmAdjSupport.class,
                "No adjustment type is mapped for OCG adjustment " + ocgAdjId, null).log(ctx);

            final Spid2DefaultAdj defaultAdjMapping = 
                HomeSupportHelper.get(ctx).findBean(ctx,Spid2DefaultAdj.class, Integer.valueOf(spid));

            if (defaultAdjMapping == null)
            {
                throw new HomeException("No default adjustment type is set up for SP " + spid);
            }

            adjId = defaultAdjMapping.getAdjId();
        }

        final AdjustmentType adjType =
            AdjustmentTypeSupportHelper.get(ctx).getAdjustmentTypeForRead(ctx, adjId);

        if (adjType == null)
        {
            throw new HomeException("Adjustment Type " + adjId + " is not found.");
        }

        return adjType;

    }
}
