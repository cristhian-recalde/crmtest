/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.BlacklistWhitelistMsisdn;
import com.trilogy.app.crm.bean.BlacklistWhitelistTemplate;
import com.trilogy.app.crm.bean.Subscriber;

/**
 * Provides a web control for presenting Blacklist Whitelist MSISDN entries on the subscription profile pane.
 * 
 * @author chandrachud.ingale
 * @since 9.6
 */
public class BlacklistWhitelistEntriesWebControl extends ProxyWebControl
{
    public BlacklistWhitelistEntriesWebControl()
    {}


    public BlacklistWhitelistEntriesWebControl(WebControl delegate)
    {
        super(delegate);
    }


    @Override
    public void toWeb(Context context, final PrintWriter out, final String name, final Object obj)
    {
        final Subscriber subscriber = (Subscriber) context.get(AbstractWebControl.BEAN);
        Map<BlacklistWhitelistTemplate, Set<String>> plpList = subscriber.getBlacklistWhitelistPlanEntries(context);

        if(LogSupport.isDebugEnabled(context))
        {
            LogSupport.debug(context, BlacklistWhitelistEntriesWebControl.class,
                "BlacklistWhitelistEntriesWebControl PLP List size : " + plpList.size());
        }

        if (plpList != null)
        {
            for (Iterator<BlacklistWhitelistTemplate> it = plpList.keySet().iterator(); it.hasNext();)
            {
                BlacklistWhitelistTemplate key = it.next();

                String label = key.getType().toString() + " ( " + " ID : " + key.getIdentifier() + " )";
                out.print("<font size=2>" + label + "</font> <br/>");
                toWeb(context, out, name, key.getIdentifier(), plpList.get(key));
            }
        }
    }


    protected void toWeb(Context context, final PrintWriter out, final String name, final long plpId,
            final Set<String> msisdnSet)
    {
        context = context.createSubContext();
        context.setName(this.getClass().getName());

        final List<BlacklistWhitelistMsisdn> msisdns = new ArrayList<BlacklistWhitelistMsisdn>(msisdnSet.size());
        for (Iterator<String> it = msisdnSet.iterator(); it.hasNext();)
        {
            String msisdn = (String) it.next();
            BlacklistWhitelistMsisdn blacklistWhitelistMsisdn = new BlacklistWhitelistMsisdn();
            blacklistWhitelistMsisdn.setMsisdn(msisdn);

            out.print("<font>" + msisdn + "</font> <br/>");
            msisdns.add(blacklistWhitelistMsisdn);
        }

        out.print("<br/><br/>");
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(final Context context, final ServletRequest request, final String name)
    {
        return null;
    }

} // class
