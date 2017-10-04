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
package com.trilogy.app.crm.web.message;

import java.util.HashMap;
import java.util.Map;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgrSPI;
import com.trilogy.framework.xhome.language.MessageMgrSPIProxy;
import com.trilogy.framework.xhome.language.Lang;

/**
 * Redirect a query for one mesage key to a value of another key.
 * @author victor.stratan@redknee.com
 */
public class MessageMgrRedirect extends MessageMgrSPIProxy
{
    public MessageMgrRedirect(final MessageMgrSPI delegate)
    {
        super(delegate);
    }

    public void addRedirect(final String sourceKey, final String destinationKey)
    {
        redirect_.put(sourceKey, destinationKey);
    }

    public void setRedirects(final Map newRedirects)
    {
        redirect_ = new HashMap(newRedirects);
    }

    /**
     * {@inheritDoc}
     */
    public String get(final Context ctx, final String key, final Class module, final Lang lang,
            final String defaultValue, final Object[] args)
    {
        String newKey = (String) redirect_.get(key);
        if (newKey == null)
        {
            newKey = key;
        }

        return super.get(ctx, newKey, module, lang, defaultValue, args);
    }

    private Map redirect_ = new HashMap();
}
