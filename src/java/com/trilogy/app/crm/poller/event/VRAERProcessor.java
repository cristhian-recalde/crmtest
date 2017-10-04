/*
's the * Copyright (c) 1999-2004, REDKNEE. All Rights Reserved.
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

import com.trilogy.app.crm.poller.agent.VRAERAgent;
import com.trilogy.framework.xhome.context.Context;


/**
 * This class is called to process every one of the ER655 parsed from
 * the VRA ER files
 *
 * @author Lily Zou
 * @date   Dec.02, 2004
 */
public class VRAERProcessor extends CRMProcessor
{
    public static final String TRANSACTION_FROM_VAR_ER_POLLER    = "Transaction from VRA ER poller";
    
    public VRAERProcessor(Context ctx, int queueSize, int threads)
    {
        super();
        init(ctx, "VRAER", "VRAERErrFile", queueSize, threads, new VRAERAgent(this));
    }
   
    //Voucher Recharge ER
    //A Sample ER: 
    //
    //2005/01/07,15:15:29,1050,700,Synaxis-2100VRA,111,rkadm,9050000001,123456,24852,Canada,1111111111111436,1,5000,IDR,5000,IDR,80000,0,20090130,111,1,0,0,0
    // 
    //
    //                             ( index No.1 )
    // Date, Time, ERID, ER Class, ER Name or SID, SPID, UserID, MSISDN, refID,Transaction ID,    
    // subLocation, voucherNum, voucherLocation, voucherValue, voucherCurrent, creditValue, creditCurrency, newBalance,
    // originalBalance, newExpiry, VMS ID, VMS Route Scenario,  VMS Result Code, Charging Result Code, Result Code//25
}
