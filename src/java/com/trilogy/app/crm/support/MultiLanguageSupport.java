package com.trilogy.app.crm.support;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

import com.trilogy.app.crm.Common;
import com.trilogy.app.crm.client.ProvisioningHomeException;
import com.trilogy.app.crm.client.SubscriberLanguageClient;
import com.trilogy.app.crm.client.SubscriberLanguageException;

/**
 * Support class for subscriber language operations.
 * 
 * @author ling.tang@redknee.com
 */
public class MultiLanguageSupport
{

	/**
	 * Retrieves the language of the msisdn
	 * 
	 * @param ctx
	 *            The operating context
	 * @param spid
	 *            The service provider identifier
	 * @param msisdn
	 *            The mobile number
	 * @return The language of the msisdn
	 * @throws HomeException
	 * @throws ProvisioningHomeException
	 */
	public static String getSubscriberLanguage(Context ctx, int spid,
	    String msisdn) throws HomeException, ProvisioningHomeException
	{
		SubscriberLanguageClient service = getService(ctx);
		try
		{
			return service.getSubscriberLanguage(spid, msisdn);
		}
		catch (SubscriberLanguageException e)
		{
			throw new ProvisioningHomeException(
			    "Failed to get subscriber language [msisdn=" + msisdn + "]",
			    e.getResultCode(), Common.OM_PM_ERROR, e);
		}
	}

	/**
	 * Set or change the language of given msisdn
	 * 
	 * @param ctx
	 *            The operating context
	 * @param spid
	 *            The service provider identifier
	 * @param msisdn
	 *            The mobile number
	 * @param language
	 *            The language of the given msisdn
	 * @return The previous language of the given msisdn
	 * @throws HomeException
	 * @throws ProvisioningHomeException
	 */
	public static String setSubscriberLanguage(Context ctx, int spid,
	    String msisdn, String language) throws HomeException,
	    ProvisioningHomeException
	{
		SubscriberLanguageClient service = getService(ctx);
		try
		{
			return service.setSubscriberLanguage(spid, msisdn, language);
		}
		catch (SubscriberLanguageException e)
		{
			throw new ProvisioningHomeException(
			    "Failed to set subscriber language [msisdn=" + msisdn
			        + "][language=" + language + "]", e.getResultCode(),
			    Common.OM_PM_ERROR, e);
		}
	}

	/**
	 * Retrieves the language of the msisdn
	 * 
	 * @param ctx
	 *            The operating context
	 * @param spid
	 *            The service provider identifier
	 * @param msisdn
	 *            The mobile number
	 * @return The language of the msisdn
	 * @throws HomeException
	 * @throws ProvisioningHomeException
	 */
	public static String getSubscriberLanguageWithDefault(Context ctx,
	    int spid, String msisdn) throws HomeException,
	    ProvisioningHomeException
	{
		SubscriberLanguageClient service = getService(ctx);
		try
		{
			return service.getSubscriberLanguageWithDefault(spid, msisdn);
		}
		catch (SubscriberLanguageException exception)
		{
			throw new ProvisioningHomeException(
			    "Failed to get subscriber language [msisdn=" + msisdn + "]",
			    exception.getResultCode(), Common.OM_PM_ERROR, exception);
		}
	}

	/**
	 * Determines if calls to the 'setSubscriberLanguage' method may succeed
	 * 
	 * @param ctx
	 *            The operating context
	 * @param spid
	 *            The service provider identifier
	 * @return true if 'setSubscriberLanguage' method is supported; false
	 *         otherwise
	 * @throws HomeException
	 */
	public static boolean isSubscriberLanguageUpdateSupported(Context ctx,
	    int spid) throws HomeException
	{
		SubscriberLanguageClient service = getService(ctx);
		try
		{
			return service.isSubscriberLanguageUpdateSupported(spid);
		}
		catch (SubscriberLanguageException e)
		{
			throw new ProvisioningHomeException(
			    "Failed to determine if calls to \"setSubscriberLanguage\" method may succeed [spid="
			        + spid + "]", e.getResultCode(), Common.OM_PM_ERROR, e);
		}
	}

	private static SubscriberLanguageClient getService(Context ctx)
	    throws HomeException
	{
		SubscriberLanguageClient service =
		    (SubscriberLanguageClient) ctx.get(SubscriberLanguageClient.class);
		if (service == null)
		{
			throw new HomeException(
			    "SubscriberLanguageClient service not found in context");
		}
		return service;
	}

}
