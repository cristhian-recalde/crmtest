/*
 *  SubscriberLooupAgent.java
 *
 *  Author : Larry Xia
 *  Date   : Oct 24, 2003
 *
 *  Copyright (c) Redknee, 2003
 *  - all rights reserved
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.EntryLogMsg;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.TaxAuthority;
import com.trilogy.app.crm.bean.core.Transaction;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;


/**
 * look up the account's tax rate from tax authority table. 
 * 
 * @author Larry Xia
  *
 */
public class TaxrateLookupAgent
   extends PipelineAgent
{

   public TaxrateLookupAgent(ContextAgent delegate)
   {
      super(delegate);
   }

   /**
	* 
	* @param ctx
	*           A context
	* @exception AgentException
	*               thrown if one of the services fails to initialize
	*/

   @Override
public void execute(Context ctx)
      throws AgentException
   {
		Account acct = (Account) ctx.get(Account.class); 
		Transaction trans = (Transaction) ctx.get(Transaction.class);
		
 		try{
 			TaxAuthority tax_auth = HomeSupportHelper.get(ctx).findBean(ctx, TaxAuthority.class, acct.getTaxAuthority());
			if ( tax_auth != null){
				ctx.put(TaxAuthority.class, tax_auth);
				pass(ctx, this, "Tax Authority found. " + tax_auth.getIdentifier() + " - " + tax_auth.getInvoiceDescription());
			} else {
				throw new Exception("Tax Authority not found."); 						
			}			
	 
		} catch ( Exception e){
			
 			ERLogger.genAccountAdjustmentER(ctx,
 				trans,
			    TPSPipeConstant.RESULT_CODE_UPS_RESULT_NOT_APPLY,
 				TPSPipeConstant.FAIL_TO_FIND_TAX_RATE);

			// send out alarm
			new EntryLogMsg(10534, this, "","", new String[]{"TaxAuthority table searching fails"}, e).log(ctx);
			fail(ctx, this, e.getMessage(), e, TPSPipeConstant.FAIL_TO_FIND_TAX_RATE);
		}
	 
  	} 

}


