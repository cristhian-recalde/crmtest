package com.trilogy.app.crm.transfer;

import javax.servlet.http.HttpServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;
import com.trilogy.framework.xhome.webcontrol.WebController;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

public class ApplyDisputeFeePredicate implements Predicate
{

	public boolean f(Context ctx, Object obj) throws AbortVisitException
	{
		try
		{
			TransferDispute dispute = (TransferDispute)obj;

			// TODO: the following hack is used to determine if we are in tablemode or not
            // once FW properly supports hiding web actions sperately from tablebiew and detail view
            // the hack can be removed.
            HttpServletRequest req = (HttpServletRequest)ctx.get(HttpServletRequest.class);
            String cmd = req.getParameter("CMD");
            String key = req.getParameter("key");
            boolean beanview =
                cmd != null           ||
                key != null           ||
                WebController.isCmd("Update",  req) ||
                WebController.isCmd("Preview", req) ||
                WebController.isCmd("Copy",    req) ||
                WebController.isCmd("Save",    req) ||
                WebController.isCmd("Delete",  req) ||
                WebController.isCmd("New",     req);

			// Display the ApplyDisputeFee action only if the fee hasn't already been applied
			return !dispute.isDisputFeeApplied() && beanview;
		}
		catch (Exception e)
		{
		    if(LogSupport.isDebugEnabled(ctx))
		    {
			    new DebugLogMsg(this, "Encountered a Exception while trying to check if Apply Dispute Fee action should be visible.  Action will be hidden.", e).log(ctx);
		    }
			return false;	
		}	
	}

}
