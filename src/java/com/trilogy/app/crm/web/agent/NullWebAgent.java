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
package com.trilogy.app.crm.web.agent;

import com.trilogy.framework.xhome.beans.XCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.web.agent.WebAgent;

/**
 * Provides a WebAgent that does nothing.
 *
 * @author gary.anderson@redknee.com
 */
public final class NullWebAgent
    implements WebAgent, XCloneable
{
    /**
     * {@inheritDoc}
     */
    public void execute(final Context ctx)
    {
        // EMPTY
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        return INSTANCE;
    }

    /**
     * Gets a singleton instance of this class.
     *
     * @return A singleton instance of this class.
     */
    public static NullWebAgent instance()
    {
        return INSTANCE;
    }

    private static final NullWebAgent INSTANCE = new NullWebAgent();
}
