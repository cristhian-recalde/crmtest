/*
 * Created on Nov 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;

import com.trilogy.app.crm.bas.tps.PaymentTypeEnum;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.log.ERLogger;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

/**
 *  dispatch subscriber based payment and group based payment to 
 *  different processing pipe line. Because in this version, we don't
 *  support group payment, so group based payment will be terminated
 *  in this agent, log and ER will be created. 
 *  @author lxia
 *
  */
// INSPECTED: 03/12/2003 ltse

public class IBISPaymentTypeCheckingAgent extends PipelineAgent {
	public IBISPaymentTypeCheckingAgent(ContextAgent delegate)
	{
	   super(delegate);
	}

	/**
	* @param ctx
	*           A context
	* @exception AgentException
	*               thrown if one of the services fails to initialize
	*/

 	public void execute(Context ctx) throws AgentException
	{
		TPSRecord tps = (TPSRecord) ctx.get(TPSRecord.class); 

		if (tps.getPaymentType().equalsIgnoreCase( PaymentTypeEnum.BILL.getDescription())){

			if ( ctx.has( Subscriber.class)){
  				pass(ctx, this, "subscriber bill payment to be processed");
 				
			} else {
                pass(ctx, this, "account bill payment to be processed");
			}
			
		}
		else {
 
			if ( ctx.has(Subscriber.class)){
				pass(ctx, this, "subscriber deposit to be processed");
 			} else {
                pass(ctx, this, "account deposit to be processed");
			}
		}
	 		
 	}
}
