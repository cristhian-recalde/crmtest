//INSPECTED: 19/09/2003 CANDYWONG
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
package com.trilogy.app.crm.poller.event;

import com.trilogy.app.crm.poller.agent.SMSBUnifiedBillingAgent;
import com.trilogy.framework.xhome.context.Context;

/**
 * This class is called to process every one of the ER311 parsed from
 * the SMSB ER files
 * @author psperneac
 */
public class SMSBUnifiedBillingProcessor extends CRMProcessor
{    
	public SMSBUnifiedBillingProcessor(Context ctx, int queueSize, int threads)
	{
		super();
        init(ctx, "SMSBUnifiedBilling", "SMSBUnifiedBillingERErrFile", queueSize, threads, new SMSBUnifiedBillingAgent(ctx , this));            
	}
 }
