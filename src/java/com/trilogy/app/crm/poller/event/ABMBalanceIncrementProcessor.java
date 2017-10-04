/*
 * Copyright (c) 1999-2005, REDKNEE. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of REDKNEE.
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with REDKNEE.
 *
 * REDKNEE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. REDKNEE SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */
package com.trilogy.app.crm.poller.event;

import com.trilogy.framework.core.home.PMHome;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.ContextualizingHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.LRUCachingHome;

import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.poller.agent.ABMBalanceIncrementAgent;

/**
 * This class is called to process every one of the ER442 parsed from
 * the ABM ER files.
 * 
 * @author lily.zou
 * @date   Jan.02, 2005
 */
public class ABMBalanceIncrementProcessor extends ABMProcessor
{
    public static final int ABM_VRA_ER_IDENTIFIER = 442;
    
    
    /**
     * Constructor
     */
    public ABMBalanceIncrementProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "ABMBalanceIncrement", "ABMBalanceIncrementERErrFile", queueSize, threads, new ABMBalanceIncrementAgent(this));
    }
    
    
    // ABM Balance Increment ER442 Format:  
    // Date, Time, ERID, ER Class, ER Name or SID, SPID, UserID, MSISDN, SCPID, Reference,Account Information, Final Balance,Balance Currency Type,Requested Currency Type, Requested Amount,Expiry Extension,Result Code,Use Service ID,Service ID,Transaction Type,Expiry Date Updated,Original Expiry Date, Final Expiry Date // 23 field
    // 
    //
    // A sample ER:   
    // 2005/01/07,10:13:16,442,700,Synaxis2200,0,OCG,9919534,,AUTOMATED_TESTCASE_13,-999900|1000,1000900,CAD,CAD,1000000,10,0,false,0,1,1,2005/03/15,2005/03/25

}
