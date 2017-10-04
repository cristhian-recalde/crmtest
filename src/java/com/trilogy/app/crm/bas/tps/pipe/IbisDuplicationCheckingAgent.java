/*
 * Created on Dec 15, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.TransactionHome;
import com.trilogy.app.crm.bean.TransactionXInfo;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.app.crm.tps.pipe.TPSPipeConstant;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * TPS record duplication checking, if duplicate found, then 
 * the processing will be terminated. 
 * 
 * @author Larry Xia
 */
public class IbisDuplicationCheckingAgent extends PipelineAgent {
	
	public IbisDuplicationCheckingAgent(ContextAgent delegate)
	{
	   super(delegate);
	}

	/**
	* 	 @param ctx
	*           A context
	* @exception AgentException
	*               thrown if one of the services fails to initialize
	*/

	public void execute(Context ctx) throws AgentException
	{
		TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class); 
		AdjustmentType type = tps.getAdjType();
		Account acct = tps.getAccount();
		Subscriber subs = tps.getSubscriber(); 

  		Home home = (Home) ctx.get(TransactionHome.class); 
   
 		try 
 		{
			  		 		

            And andPredicate = new And();
            andPredicate.add(new EQ(TransactionXInfo.ADJUSTMENT_TYPE, Long.valueOf(type.getCode())));
            andPredicate.add(new EQ(TransactionXInfo.BAN, acct.getBAN()));
            andPredicate.add(new EQ(TransactionXInfo.EXT_TRANSACTION_ID, tps.getTransactionNum()));
            andPredicate.add(new EQ(TransactionXInfo.TRANS_DATE, Long.valueOf(tps.getPaymentDate().getTime())));

			if ( subs != null)
		          andPredicate.add(new EQ(TransactionXInfo.SUBSCRIBER_ID, subs.getId())); 
			
            Object obj = home.find(andPredicate);

			if ( obj == null ){
				pass(ctx, this, "no duplicate transaction found");
			} else {
				ERLogger.genAccountAdjustmentER(ctx,
					tps,
					TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION);

				fail(ctx, this, 
					"duplication transaction found, operation stopped",
					null, TPSPipeConstant.FAIL_DUPLICATE_TRANSACTION
				); 
			}		
		} catch ( Throwable t){
			if (LogSupport.isDebugEnabled(ctx))
			{
				new DebugLogMsg(this, "Transaction home error", t).log(ctx);
			}
			pass(ctx, this, "no duplicate transaction found");			
		}
 	}

}
