package com.trilogy.app.crm.move.processor;

import com.trilogy.app.crm.api.queryexecutor.account.AccountQueryExecutors.UpdateAccountParentV2QueryExecutor;
import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.invoice.process.ProducerAgent;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * Account Move Producer Agent for change parent BAN..
 * @author vgote
 * @since 9.5
 *
 */
public class AcountMoveProducerAgent extends ProducerAgent
{

	public AcountMoveProducerAgent(final Context ctx,
			final ContextAgent agent,
			final String threadName,
			final int threadSize,
			final int queueSize)
	{
		super(ctx, agent, threadName, threadSize, queueSize);
	}

	public void produceAccountMoveThread(Context ctx, Account account, String parentID, Long newDepositAmount, Boolean responsible, Integer expiryExtention) 
	        throws AccountMoveException
	{

		//Put the consumer in context.
	    UpdateAccountParentV2QueryExecutor updateParentAccount = new UpdateAccountParentV2QueryExecutor(account, parentID, newDepositAmount, responsible, expiryExtention);
		ctx.put(UpdateAccountParentV2QueryExecutor.class, updateParentAccount);

		try 
		{
			if(isQueueExhausted())
			{
				throw new AccountMoveException("Cannot move accounts. Please try again Later.", null);
			}
			else
			{
				execute(ctx);
			}
			
		}
		catch (AgentException e)
		{
			new MajorLogMsg(this, e.getMessage(),e).log(ctx);
			throw new AccountMoveException("Agent Exception while trying to move account. ", e);
		}
	}
}