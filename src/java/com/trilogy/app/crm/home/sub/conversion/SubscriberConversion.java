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
package com.trilogy.app.crm.home.sub.conversion;

import com.trilogy.framework.xhome.beans.Validator;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.bean.Subscriber;

/**
 * Executes the validation and conversion.
 *
 * @author arturo.medina@redknee.com
 */
public interface SubscriberConversion extends Validator
{

    /**
     * Executes the Subscriber conversion.
     *
     * @param ctx
     * @param prevSubscriber
     * @param currentSubscriber
     * @param delegate
     * @return
     * @throws HomeException
     */
    public Subscriber convertSubscriber(final Context ctx, final Subscriber prevSubscriber,
            final Subscriber currentSubscriber, final Home delegate) throws HomeException;
}
