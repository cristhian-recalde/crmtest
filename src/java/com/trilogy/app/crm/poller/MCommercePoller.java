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

package com.trilogy.app.crm.poller;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.poller.event.MCGAdvancedEventRatingProcessor;
import com.trilogy.app.crm.poller.event.MCommerceRechargeProcessor;
import com.trilogy.service.poller.nbio.PollerConfig;
import com.trilogy.service.poller.nbio.PollerLogger;


/**
 * Instance of Poller for MCommerce. Implements the loadERHandlers.
 *
 * @author danny.ng@redknee.com
 * @since Mar 03, 2006
 */
public class MCommercePoller
{

    /**
     * MCommerce Recharge ER ID.
     */
    public static final int MCOMMERCE_ER_IDENTIFIER = 1251;

    /**
     * MCG Advanced Event Rating ER ID.
     */
    public static final int MCOMMERCE_ADVANCED_EVENT_ER_IDENTIFIER = 511;

}
