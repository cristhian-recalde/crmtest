package com.trilogy.app.crm.extension.account;

import java.io.IOException;

import com.trilogy.app.crm.bean.LoyaltyCard;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 * Adapt from LoyaltyCard to LoyaltyCardExtension which is a UI bean. LoyaltyCardExtension is simply a 
 * holder for LoyaltyCard. 
 * 
 * @author asim.mahmood@redknee.com
 * @since 9.1
 */
public class LoyaltyCardExtensionAdapter implements Adapter
{

    /**
     * 
     */
    private static final long serialVersionUID = -4014900920748921686L;


    @Override
    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        LoyaltyCard card = (LoyaltyCard)obj;
        
        LoyaltyCardExtension extension = null;
        try
        {
            extension = (LoyaltyCardExtension)XBeans.instantiate(LoyaltyCardExtension.class, ctx);
        }
        catch (Exception e)
        {
            new InfoLogMsg(this, "Unable to instantiate LoyaltyCardExtension using XBeans, attempting manual", e).log(ctx);
            extension = new LoyaltyCardExtension();
        }
        
        extension.setBAN(card == null ? extension.getBAN() : card.getBAN());
        extension.setLoyaltyCard(card);

        return extension;
    }


    @Override
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        return obj == null ? null : ((LoyaltyCardExtension)obj).getLoyaltyCard();
    }
}
