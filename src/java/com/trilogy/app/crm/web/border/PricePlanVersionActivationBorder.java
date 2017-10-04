/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 *
 */
package com.trilogy.app.crm.web.border;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.PricePlanVersionID;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestHome;
import com.trilogy.app.crm.bean.PricePlanVersionUpdateRequestXInfo;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.filter.PricePlanVersionIsActivatablePredicate;
import com.trilogy.app.crm.provision.UpdatedPricePlanHandler;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.web.action.PricePlanVersionActivationAction;
import com.trilogy.app.crm.web.action.PricePlanVersionCancelActivationAction;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.MSP;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.border.Border;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * Border to process the price plan version activation request.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.5
 */
public class PricePlanVersionActivationBorder implements Border
{

	public static Border instance()
	{
		return instance;
	}

	protected PricePlanVersionActivationBorder()
	{
		// empty
	}

	/**
	 * Activates a price plan version.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @param version
	 *            Price plan version to be activated.
	 * @throws HomeException
	 *             Thrown if there are problems looking up necessary information
	 *             or processing the request.
	 */
	private void activatePricePlanVersion(final Context ctx,
	    final PricePlanVersion version) throws HomeException
	{
		final PricePlan plan = version.getPricePlan(ctx);
		final PricePlanVersion previousVersion =
		    PricePlanSupport.getCurrentVersion(ctx, plan);
		final UpdatedPricePlanHandler handler =
		    new UpdatedPricePlanHandler(ctx, plan, version);

		if (previousVersion != null)
		{
			handler.createRequests();
		}

		handler.updatePricePlan();
	}

	/**
	 * Asserts that the given PricePlanVersion does not belong to a PricePlan
	 * that is already being propagated to subscribers.
	 * 
	 * @param version
	 *            The PricePlanVersion for which to check for in-progress
	 *            propagation.
	 * @exception HomeException
	 *                Thrown if a current propagation is already in
	 *                progress.
	 */
	private void assertPricePlanVersionNotUpdating(final Context ctx,
	    final PricePlanVersion version) throws HomeException
	{
		if (PricePlanSupport.isPricePlanVersionUpdating(ctx, version.getId()))
		{
			throw new HomeException(
			    "New version creation is not permitted.  Another version change is already in progress.");
		}
	}

	/**
	 * Process the activation.
	 * 
	 * @param ctx
	 * @param req
	 * @param res
	 * @param delegate
	 * @throws ServletException
	 * @throws IOException
	 * @see com.redknee.framework.xhome.web.border.Border#service(com.redknee.framework.xhome.context.Context,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse,
	 *      com.redknee.framework.xhome.webcontrol.RequestServicer)
	 */
	@Override
	public void service(Context ctx, HttpServletRequest req,
	    HttpServletResponse res, RequestServicer delegate)
	    throws ServletException, IOException
	{
		final PrintWriter out = res.getWriter();
		HTMLExceptionListener el =
		    new HTMLExceptionListener(new MessageMgr(ctx, getClass()));

		String action = req.getParameter(".versionsaction");
		try
		{
            Context subCtx = ctx.createSubContext();
            subCtx.put(HTMLExceptionListener.class, el);
            subCtx.put(ExceptionListener.class, el);
            if (PricePlanVersionActivationAction.DEFAULT_KEY.equals(action))
            {
                LogSupport.info(subCtx, this, "Price Plan Version update manually triggered");
                activatePricePlanVersion(subCtx);
            }
            else if (PricePlanVersionCancelActivationAction.DEFAULT_KEY.equals(action))
            {
                LogSupport.info(subCtx, this, "Price Plan Version cancel activation for existing pending activation version");
                cancelPricePlanActivation(subCtx);
            }
            
        }
		catch (AgentException exception)
		{
			el.toWeb(ctx, out, null, null);
		}
		// display the rest of the contents
		delegate.service(ctx, req, res);
	}
	

	/**
     * Cancel pending priceplan activation 
     * 
     * @param ctx
     *            The operating context.
     * @throws AgentException
     *             Thrown if the price plan version activation has resulted in
     *             error.
     */
    private void cancelPricePlanActivation(Context ctx) throws AgentException
    {
        final Context subCtx = ctx.createSubContext();
        ExceptionListener el = (ExceptionListener) subCtx.get(HTMLExceptionListener.class);
        if (el == null)
        {
        el = (ExceptionListener) subCtx.get(ExceptionListener.class);
        }

        final String sKey = WebAgents.getParameter(subCtx, ".versionskey");
        final String[] keys = sKey.split("`");
        if (keys.length != 2)
        {
            final AgentException exception =
                new AgentException("Key not found or not understood.");
            if (el != null)
            {
                el.thrown(exception);
            }
            throw exception;
        }

        final long id = Long.parseLong(keys[0]);
        final int version = Integer.parseInt(keys[1]);

        final Home home = (Home) subCtx.get(PricePlanVersionHome.class);

        try
        {
            final PricePlanVersionID key = new PricePlanVersionID(id, version);
            final PricePlanVersion ppv = (PricePlanVersion) home.find(subCtx, key);
            PricePlan pp = ppv.getPricePlan(subCtx);
            
            MSP.setBeanSpid(subCtx, pp.getSpid());
            
            final Home ppvuHome= (Home) ctx.get(PricePlanVersionUpdateRequestHome.class);
            
            And and = new And();
            and.add(new EQ(PricePlanVersionUpdateRequestXInfo.PRICE_PLAN_IDENTIFIER, ppv.getId()));
            ppvuHome.removeAll(subCtx, and);
            LogSupport.info(subCtx, PricePlanVersionActivationBorder.class, "Successfully cancelled ppv [" + ppv.getId() + ", ver=>"+ ppv.getVersion() + "] activation");
        }
        catch (final HomeException exception)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Manual Cancel of price plan version activation has failed");
            if (exception.getLocalizedMessage() != null
                && !exception.getLocalizedMessage().isEmpty())
            {
                sb.append(": ");
                sb.append(exception.getLocalizedMessage());
            }
            else if (exception.getMessage() != null
                && !exception.getMessage().isEmpty())
            {
                sb.append(": ");
                sb.append(exception.getMessage());
            }
            else
            {
                sb.append(". Please check the application log for more detail.");
            }
            final AgentException e =
                new AgentException(sb.toString(), exception);

            if (el != null)
            {
                el.thrown(e);
            }

            LogSupport.minor(subCtx, this,
                "HomeException caught while attempting to cancel price plan version  for id=> "
                    + id + " version =>" + version, exception);
            throw e;
        }
    }

	
	/**
	 * Activates the current price plan version.
	 * 
	 * @param ctx
	 *            The operating context.
	 * @throws AgentException
	 *             Thrown if the price plan version activation has resulted in
	 *             error.
	 */
    private void activatePricePlanVersion(Context ctx) throws AgentException
    {
        final Context subCtx = ctx.createSubContext();
        ExceptionListener el = (ExceptionListener) subCtx.get(HTMLExceptionListener.class);
        if (el == null)
        {
		el = (ExceptionListener) subCtx.get(ExceptionListener.class);
		}

		final String sKey = WebAgents.getParameter(subCtx, ".versionskey");
		final String[] keys = sKey.split("`");
		if (keys.length != 2)
		{
			final AgentException exception =
			    new AgentException("Key not found or not understood.");
			if (el != null)
			{
				el.thrown(exception);
			}
			throw exception;
		}

		final long id = Long.parseLong(keys[0]);
		final int version = Integer.parseInt(keys[1]);

		final Home home = (Home) subCtx.get(PricePlanVersionHome.class);

		try
		{
            final PricePlanVersionID key = new PricePlanVersionID(id, version);
            final PricePlanVersion ppv = (PricePlanVersion) home.find(subCtx, key);
            PricePlan pp = ppv.getPricePlan(subCtx);
            
            MSP.setBeanSpid(subCtx, pp.getSpid());
            
            // verify it is activatable
            final Predicate predicate = new PricePlanVersionIsActivatablePredicate();
            if (ppv != null && predicate.f(subCtx, ppv))
            {
                // assert it's not updating
                assertPricePlanVersionNotUpdating(subCtx, ppv);
                activatePricePlanVersion(subCtx, ppv);
            }
        }
		catch (final HomeException exception)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Manual activation of Price Plan Version has failed");
			if (exception.getLocalizedMessage() != null
			    && !exception.getLocalizedMessage().isEmpty())
			{
				sb.append(": ");
				sb.append(exception.getLocalizedMessage());
			}
			else if (exception.getMessage() != null
			    && !exception.getMessage().isEmpty())
			{
				sb.append(": ");
				sb.append(exception.getMessage());
			}
			else
			{
				sb.append(". Please check the application log for more detail.");
			}
			final AgentException e =
			    new AgentException(sb.toString(), exception);

			if (el != null)
			{
				el.thrown(e);
			}

			LogSupport.minor(subCtx, this,
			    "HomeException caught while attempting to manually activate price plan "
			        + id + " version " + version, exception);
			throw e;
		}
	}

	private static Border instance = new PricePlanVersionActivationBorder();
}
