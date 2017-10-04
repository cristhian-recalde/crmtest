package com.trilogy.app.crm.web.border;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.msp.Spid;
import com.trilogy.framework.xhome.session.Session;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;


public class CallDetailSpidComponentLabelBorder<BEAN extends CallDetail> implements Border
{

    public CallDetailSpidComponentLabelBorder(Class<BEAN> beanClass)
    {
        // taking this bean class as an input to enforce compile time adherence
    }


    public void service(Context ctx, HttpServletRequest req, HttpServletResponse res, RequestServicer delegate)
            throws ServletException, IOException
    {
        {
            ctx = ctx.createSubContext();
            Spid spidBean = MSP.getBeanSpid(ctx);
            if (null == spidBean)
            {
                Context session = Session.getSession(ctx);
                Account account = (Account) session.get(Account.class);
                if (null != account)
                {
                    MSP.setBeanSpid(ctx, account.getSpid());
                    spidBean = MSP.getBeanSpid(ctx);
                }
            }
            if (null != spidBean)
            {
                try
                {
                    CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, spidBean.getSpid());
                    if (!crmSpid.isEnableChargingComponents())
                    {
                        ctx = hideProperties(ctx);
                    }
                }
                catch (HomeException e)
                {
                    handleError(ctx, e);
                }
            }
        }
        delegate.service(ctx, req, res);
    }


    private void handleError(Context ctx, HomeException e)
    {
        // TODO Auto-generated method stub
    }


    public Context hideProperties(Context ctx)
    {
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_CHARGE1, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_CHARGE2, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_CHARGE3, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_GLCODE1, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_GLCODE2, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_GLCODE3, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_RATE1, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_RATE2, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_RATE3, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_NAME1, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_NAME2, ViewModeEnum.NONE);
        AbstractWebControl.setMode(ctx, CallDetailXInfo.COMPONENT_NAME3, ViewModeEnum.NONE);
        return ctx;
    }
}
