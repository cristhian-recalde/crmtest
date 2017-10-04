/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.subscriber.provision;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xlog.log.MajorLogMsg;

/**
 * @author jchen
 */
public class SubscriberProvisionResultCode 
{
	/**
	 * Adds the given monetary amount, resulting from a state-change, to the
	 * amount stored in the context.  If no such amount is stored in the
	 * context, it is added.
	 *
	 * @param ctx The operating context.
	 * @param amount The amount by which to increate the amount stored in the
	 * context.
	 *
	 * @return The updated amount.
	 */
	public static long addChargeAmount(final Context ctx, final long amount)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.addAdjustAoumt(amount);
	}

	/**
	 * Gets the monetary amount resulting from a state-change from the ctx.
	 * If no such amount is stored in the ctx, zero is returned.
	 *
	 * @param ctx The monetary amount resulting from a state-change.
	 *
	 * @return The updated amount.
	 */
	public static long getChargeAmount(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getAdjustAmount();
	}
	

	public static void setProvisionIpcErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultIpc( code);
	}

	public static int getProvisionIpcErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultIpc();
	}	
	
	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionEcpErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultEcp( code);
	}

    /**
     * Sets the Blackberry error code that was generated as a result of the state
     * change.
     *
     * @param ctx The operating ctx.
     * @param code The Blackberry error code.
     */
    public static void setProvisionBlackberryErrorCode(final Context ctx, final int code)
    {
        ProvisionResultCode rc = getProvisionResultCode(ctx);
        rc.setResultBlackberry(code);
    }


    /**
     * Sets the Wimax error code that was generated as a result of the state
     * change.
     *
     * @param ctx The operating ctx.
     * @param code The Aptilo error code.
     */
    public static void setProvisionWimaxErrorCode(final Context ctx, final int code)
    {
        ProvisionResultCode rc = getProvisionResultCode(ctx);
        rc.setResultWimax(code);
    }
    

    /**
     * Sets the Alcatel error code that was generated as a result of the state
     * change.
     *
     * @param ctx The operating ctx.
     * @param code The Blackberry error code.
     */
    public static void setProvisionAlcatelErrorCode(final Context ctx, final int code)
    {
        ProvisionResultCode rc = getProvisionResultCode(ctx);
        rc.setResultAlcatel(code);
    }

    /**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionEcpErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultEcp();
	}
	
	
	public static void setProvisionPricePlanChangeErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultPricePlan( code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionPricePlanChangeErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultPricePlan();
	}
	
	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionUpsErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultUps(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionUpsErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultUps();
	}
	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionSMSBErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultSmsb(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionSMSBErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultSmsb();
	}

	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionLastResultCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultLast(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionLastResultCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultLast();
	}
	
	
	
	
	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionHlrResultCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultHlr(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionHlrResultCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultHlr();
	}
	
	
	public static void setProvisionCrmResultCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultCrm(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionCrmResultCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultCrm();
	}
	
	
	
	
	/**
	 * Sets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @param code The SMSB error code.
	 */
	public static void setProvisionCreditLimitResultCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultCreditLimit(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionCreditLimitResultCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultCreditLimit();
	}
	
	public static int getProvisionStateChangeResultCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultStateChange();
	}
	
	public static void setProvisionStateChangeResultCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultStateChange(code);
	}
	
	/**
	 * FIXME, calculated from state charge + provision + unprovision + auxService, + other services fees
	 * lie reactivation fee.
	 * @param ctx
	 * @return
	 */
	public static int getProvisionAdjustMinutes(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getAdjustMinutes();
	}
	
	public static int addProvisionAdjustMinutes(final Context ctx, int mins)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.addAdjustMinutes(mins);
	}
	
	public static void setProvisionChargeErrorCode(final Context ctx, final int code)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		rc.setResultCharge(code);
	}

	/**
	 * Gets the SMSB error code that was generated as a result of the state
	 * change.
	 *
	 * @param ctx The operating ctx.
	 * @return The SMSB error code that was generated as a result of the state
	 * change.
	 */
	public static int getProvisionChargeErrorCode(final Context ctx)
	{
		ProvisionResultCode rc = getProvisionResultCode(ctx);
		return rc.getResultCharge();
	}
	

	public static void addException(Context ctx, String errMsg, Exception e, Subscriber oldSub, Subscriber newSub)
	{
		String subs = "";
//		subs = subs + (oldSub == null ? "null" : oldSub.getId());
//		
//		subs = subs + ", newSub=";
//		subs = subs + (oldSub == null ? "null" : oldSub.getId());
//		
		
		Subscriber sub = (oldSub == null ? newSub : oldSub);
		subs = "Sub=" + sub.getId();
		subs = subs + ", err=" + e;
		
		new MajorLogMsg(LOG_MOD, subs, e).log(ctx);
		
		HTMLExceptionListener el =
            (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
		if (el != null)
			el.thrown(e);        
	}
	
	
	public static HTMLExceptionListener getExceptionListener(Context ctx)
	{
		return (HTMLExceptionListener) ctx.get(HTMLExceptionListener.class);
	}

	
	private final static String LOG_MOD = SubscriberProvisionResultCode.class.getName();

	public static ProvisionResultCode getProvisionResultCode(Context ctx)
	{
		return (ProvisionResultCode)ctx.get(ProvisionResultCode.class);
	}
}


