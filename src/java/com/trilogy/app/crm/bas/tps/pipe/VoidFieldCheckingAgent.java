/*
 * Created on Nov 28, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.trilogy.app.crm.bas.tps.pipe;
import com.trilogy.app.crm.bas.tps.TPSRecord;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 *	if the void field of TPS is set to "Y", then convert the amount to 
 *  negative, for this is a reversed payment.
 * 
 * * @author lxia
 */
public class VoidFieldCheckingAgent 
extends PipelineAgent 
{

	public VoidFieldCheckingAgent(ContextAgent delegate)
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
		 if ( tps.getVoidFlag() ){
		 	tps.setAmount( tps.getAmount() * -1); 
			if (LogSupport.isDebugEnabled(ctx))
			{
			 	new DebugLogMsg(this, 
					"the amount has been reversed", 
					null).log(ctx);
			} 
		 }
		 pass(ctx, this, "void field checked");
   	
	}

}
