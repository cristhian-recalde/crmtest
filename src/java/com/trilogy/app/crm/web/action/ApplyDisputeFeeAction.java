package com.trilogy.app.crm.web.action;

import java.io.PrintWriter;

import com.trilogy.app.crm.transfer.TransferDispute;
import com.trilogy.app.crm.transfer.TransferDisputeHome;
import com.trilogy.app.crm.transfer.TransferDisputeWebControl;
import com.trilogy.app.crm.transfer.TransferDisputeTransactionSupport;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgents;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.agent.ServletBridge;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import javax.servlet.http.HttpServletRequest;

public class ApplyDisputeFeeAction extends SimpleWebAction
{
	public ApplyDisputeFeeAction()
	{
		super("applyDisputeFee", "Apply Dispute Fee");
	}
	
	public void execute(Context ctx)
    	throws AgentException
	{
	    PrintWriter out = WebAgents.getWriter(ctx);
	    String stringKey = WebAgents.getParameter(ctx, ".disputeHistorykey");
		if (stringKey == null || stringKey.equals(""))
	    {
		    stringKey = WebAgents.getParameter(ctx, "key");
	    }
		Home home = (Home)ctx.get(TransferDisputeHome.class);
		
		long key = Long.parseLong(stringKey);
		
		if (LogSupport.isDebugEnabled(ctx))
		{
			new DebugLogMsg(this, "Received request to apply dispute fee for TransferDispute with [key=" + key + "]", null).log(ctx);
		}
		
		try
		{
			TransferDispute dispute = (TransferDispute) home.find(ctx, key);
			if (dispute == null)
			{
				printError(out, "Unable to find specified dispute with [key=" + key + "]");
			}
			else
			{
				
				try
				{
					TransferDisputeTransactionSupport.applyDisputeFee(ctx, dispute);
					printMessage(out, "Succcessfully applied dispute fee to " + dispute.getApplyDisputeFeeTo() + "." );
					if (LogSupport.isDebugEnabled(ctx))
					{
						new DebugLogMsg(this, "Successfully applied dispute fee for TransferDispute with [key=" + key + "] where [contributorSubId=" + dispute.getContSubId() + ",recipientSubId=" + dispute.getRecpSubId() + ",isDisputeFee=" + dispute.isDisputFeeApplied() + ",applyDisputeFeeTo=" + dispute.getApplyDisputeFeeTo() + "]", null).log(ctx);
					}

					dispute.setDisputFeeApplied(true);
					home.store(ctx, dispute);
				}
				catch (HomeException e)
				{
					new InfoLogMsg(this, "Encountered a HomeException while trying to apply Dispute fee for TransferDispute with [key=" + key + "].  Attempt to Apply Dispute Fee will be failed.", e).log(ctx);
					printError(out, "Unable to apply dispute fee for TransferDispute with [key=" + key + "] due to HomeException [message=" + e.getMessage() + "]");
				}
			}
		}
		catch (HomeException e)
		{
			new InfoLogMsg(this, "Encountered a HomeException while trying to retrieve TransferDispute with [key=" + key + "].  Attempt to Apply Dispute Fee will be failed.", e).log(ctx);
			printError(out, "Unable to retrieve TransferDispute with [key=" + key + "] due to HomeException [message=" + e.getMessage() + "].  Check Application logs for more details.");
		}
		
		
		
		
		ContextAgents.doReturn(ctx);
	}

	private void printMessage(PrintWriter out, String msg)
	{
		out.println("<font color=\"green\">" + msg + "</font><br/><br/>");
		
	}

	private void printError(PrintWriter out, String error)
	{
		out.println("<font color=\"red\">" + error + "</font><br/><br/>");		
	}
}
