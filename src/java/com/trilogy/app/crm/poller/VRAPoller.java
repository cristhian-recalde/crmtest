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

import com.trilogy.app.crm.poller.event.VRAERProcessor;
import com.trilogy.app.crm.poller.event.VRAFraudERProcessor;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.service.poller.nbio.PollerConfig;
import com.trilogy.service.poller.nbio.PollerLogger;

/**
 * Instance of Poller for VRA. Implements the loadERHandlers.
 * @author: Lily Zou
 * @date:   Dec.02, 2004
 */
public class VRAPoller
{
    public static final int VRA_ER_IDENTIFIER                    = 1050;
    public static final int VRA_VOUCHERFRAUD_ER_IDENTIFIER       = 1051;    // "Auto-Blocking on Voucher Fraud" RFF [1stSep'06]
    
}
