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
package com.trilogy.app.crm.home;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.util.OldBeanLookupHome;


/**
 * Provices a HomeProxy for automatically looking-up the old version of a
 * GSMPackage bean, and for placing that old bean in the operating context for
 * down-stream Homes. This should be the first Home in the Home pipeline.
 *
 * @author gary.anderson@redknee.com
 */
public
class OldGSMPackageLookupHome
    extends OldBeanLookupHome
{
    /**
     * Gets the old GSMPackage bean from the given context.
     *
     * @param context The operating context.
     * @return The old GSMPackage bean from the given context.
     */
    public static GSMPackage getOldGSMPackage(final Context context)
    {
        return (GSMPackage)context.get(CONTEXT_KEY);
    }


    /**
     * Creates a new OldGSMPackageLookupHome for the given delegate.
     *
     * @param context The operating context.
     * @param delegate The Home to which this proxy updates.
     */
    public OldGSMPackageLookupHome(final Context context, final Home delegate)
    {
        super(context, delegate, GSMPackage.class);
    }


    /**
     * {@inheritDoc}
     */
    protected Object getKey()
    {
        return CONTEXT_KEY;
    }


    /**
     * The key used to reference the old bean in an operating context.
     */
    private static final String CONTEXT_KEY =
        OldGSMPackageLookupHome.class.getName() + ".CONTEXT_KEY";

} // class
