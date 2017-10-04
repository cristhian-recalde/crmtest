/*
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.core.locale.Currency;
import com.trilogy.framework.core.locale.CurrencyXInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xhome.webcontrol.WebController;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.support.HomeSupport;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.util.snippet.log.Logger;


/**
 * Creates a sub-Context and adds the proper Currency object to the context.
 * Proper Currency is the one referenced by the Service Provider which the current bean belongs to.
 *
 * @author victor.stratan@redknee.com
 */
public class CurrencyContextSetupWebControl extends ProxyWebControl
{
    /**
     * @param delegate
     */
    public CurrencyContextSetupWebControl(WebControl delegate)
    {
        super(delegate);
    }

    @Override
    public Object fromWeb(Context ctx, ServletRequest p2, String p3)
    {
        Context subCtx = setupSubContext(ctx, null);
        return super.fromWeb(subCtx, p2, p3);
    }

    @Override
    public void fromWeb(Context ctx, Object p1, ServletRequest p2, String p3)
    {
        Context subCtx = setupSubContext(ctx, p1);
        super.fromWeb(subCtx, p1, p2, p3);
    }

    @Override
    public void toWeb(Context ctx, PrintWriter p1, String p2, Object p3)
    {
        Context subCtx = setupSubContext(ctx, p3);
        super.toWeb(subCtx, p1, p2, p3);
    }

    /**
     * {@inheritDoc}
     */
    public Context setupSubContext(final Context ctx, final Object param)
    {
        Object bean = ctx.get(AbstractWebControl.BEAN);
        if (bean == null || !(bean instanceof SpidAware))
        {
            bean = param;
        }
        if (bean == null || !(bean instanceof SpidAware))
        {
            bean = ctx.get(WebController.BEAN_CPROPERTY);
        }
        if (bean != null && bean instanceof SpidAware)
        {
            final SpidAware spidAware = (SpidAware) bean;
            int spid = spidAware.getSpid();
            if (spid > 0)
            {
                HomeSupport hs = HomeSupportHelper.get(ctx);
                CRMSpid sp = null;
                try
                {
                    final EQ condition = new EQ(CRMSpidXInfo.ID, Integer.valueOf(spid));
                    sp = hs.findBean(ctx, CRMSpid.class, condition);
                }
                catch (HomeException e)
                {
                    Logger.minor(ctx, this, "Error occurred while retrieving SP: " + e.getMessage(), e);
                }

                Currency currency = null;
                if (sp != null)
                {
                    final EQ condition = new EQ(CurrencyXInfo.CODE, sp.getCurrency());
                    try
                    {
                        currency = hs.findBean(ctx, Currency.class, condition);
                    }
                    catch (HomeException e)
                    {
                        Logger.minor(ctx, this, "Error occurred while retrieving Currency: " + e.getMessage(), e);
                    }
                }

                if (currency != null)
                {
                    final Context subCtx = ctx.createSubContext();
                    subCtx.put(Currency.class, currency);
                    return subCtx;
                }
            }
        }

        return ctx;
    }
}
