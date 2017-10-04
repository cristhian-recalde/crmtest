/*
 * Copyright (c) 1999-2003, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller;

import com.trilogy.framework.xhome.context.Context;

import com.trilogy.app.crm.poller.event.URCSPromotionProvisioningProcessor;
import com.trilogy.app.crm.poller.event.URSFirstCallActivationProcessor;
import com.trilogy.app.crm.poller.event.URSUnifiedBillingProcessor;
import com.trilogy.app.crm.support.SystemSupport;
import com.trilogy.service.poller.nbio.PollerConfig;
import com.trilogy.service.poller.nbio.PollerLogger;

/**
 * Instance of Poller for URS (a.k.a. URCS). Implements the loadERHandlers.
 * @author psperneac
 */
public class URSPoller
{
    public static final int URS_FIRST_CALL_ACTIVATION_ER_IDENTIFIER = 909;
    public static final int URS_UNIFIED_BILLING_ER_IDENTIFIER = 501;

    public static final int URS_PROMOTION_STATUS_ER_IDENTIFIER = 7002;
    public static final int URS_PROMOTION_PURGE_COUNTER_ER_IDENTIFIER = 7003;
    public static final int URS_PROMOTION_PROVISIONING_ER_IDENTIFIER = 7004;
    public static final int URS_PROMOTION_TRIGGERED_ACTION_ER_IDENTIFIER = 7005;
    public static final int URS_PROMOTION_COUNTER_UPDATE_ER_IDENTIFIER = 7006;
    
}
