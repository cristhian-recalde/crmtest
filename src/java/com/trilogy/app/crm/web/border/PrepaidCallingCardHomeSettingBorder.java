package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.bean.GeneralConfig;
import com.trilogy.app.crm.bean.calldetail.CallDetailHome;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.bean.core.PrepaidCallingCard;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;

/**
 * This border is used to put the Prepaid Calling Card Call Detail Home in the
 * context under the CallDetailHome.class key, filtered by the MSISDN of the
 * prepaid calling card in the context. This allows reusing the call
 * detail search logic.
 * 
 * @author mcmarques
 * 
 */
public class PrepaidCallingCardHomeSettingBorder implements Border
{

    /**
     * Creates a new PrepaidCallingCardHomeSettingBorder class.
     */
    public PrepaidCallingCardHomeSettingBorder()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void service(
            final Context context,
            final HttpServletRequest request,
            final HttpServletResponse response,
            final RequestServicer delegate)
        throws ServletException, IOException
    {
        final Context subContext = context.createSubContext();
        GeneralConfig config = (GeneralConfig) subContext.get(GeneralConfig.class);

        PrepaidCallingCard pcc = (PrepaidCallingCard) subContext.get(PrepaidCallingCard.class);
        Home home = (Home) subContext.get(Common.PREPAID_CALLING_CARD_CALL_DETAIL_HOME);
        if (pcc!=null)
        {
            home = home.where(subContext, new EQ(CallDetailXInfo.BAN, config.getPrepaidCallingCardPrefix() + pcc.getSerial()));
        }
        
        subContext.put(CallDetailHome.class, home);

        delegate.service(subContext, request, response);
    }

}
