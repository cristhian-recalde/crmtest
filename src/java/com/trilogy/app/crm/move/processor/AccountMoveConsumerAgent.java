package com.trilogy.app.crm.move.processor;

import com.trilogy.app.crm.api.queryexecutor.account.AccountQueryExecutors.UpdateAccountParentV2QueryExecutor;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;

/**
 * Account move Consumer Agent for update account parent BAN.
 * @author vgote
 * @since 9.5
 *
 */
public class AccountMoveConsumerAgent implements ContextAgent {

	@Override
	public void execute(Context ctx) throws AgentException {

	    UpdateAccountParentV2QueryExecutor executor = (UpdateAccountParentV2QueryExecutor) ctx.get(UpdateAccountParentV2QueryExecutor.class);

		if (executor != null)
		{
			try 
			{
				executor.moveAccount(ctx);
			} 
			catch (AccountMoveException e) 
			{
				throw new AgentException("AccountMoveException when trying to move account.", e);
			} 
		}
	}
}
