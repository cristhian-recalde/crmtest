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
package com.trilogy.app.crm.client.xmlhttp;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.xhome.home.TotalCachingHome;
import com.trilogy.util.snippet.home.HomeDecorator;


/**
 * @author gary.anderson@redknee.com
 */
public class XMLTranslationConfigurationPipelineDecorator
    implements HomeDecorator
{

    /**
     * {@inheritDoc}
     */
    public Home decorate(final Context context, final Home baseHome)
        throws HomeException
    {
        final Home cache = new XMLTranslationConfigurationTransientHome(context);

        Home delegate = new AdapterHome(baseHome, new XMLTranslationConfigurationBaseAdapter());

        Home home = new TotalCachingHome(context, cache, delegate, true);
        home = new ScriptManagementHome(context, home);
        return home;
    }

}
