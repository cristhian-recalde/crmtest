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
package com.trilogy.app.crm.client;

import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.StringHolder;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.PMLogMsg;

import com.trilogy.app.pin.manager.SubscriberLangProv;
import com.trilogy.app.pin.manager.param.OutParamID;
import com.trilogy.app.pin.manager.param.Parameter;
import com.trilogy.app.pin.manager.param.ParameterSetHolder;

/**
 * CORBA client for the SubscriberLangProv interface to URCS.
 * 
 * @author cindy.wong@redknee.com
 * @since 8.6
 */
public class SubscriberLanguageCorbaClient extends
    AbstractCrmClient<SubscriberLangProv> implements
    SubscriberLanguageClient
{

	private static final String CLIENT_NAME = "SubscriberLangProv";

	private static final String SERVICE_DESCRIPTION =
	    "CORBA client to update and query subscriber language settings";

	public SubscriberLanguageCorbaClient(Context ctx)
	{
		super(ctx, CLIENT_NAME, SERVICE_DESCRIPTION,
 SubscriberLangProv.class);
	}

	/**
	 * @param ctx
	 * @param spid
	 * @param msisdn
	 * @return
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#getSubscriberLanguage(com.redknee.framework.xhome.context.Context,
	 *      int, java.lang.String)
	 */
	@Override
	public String getSubscriberLanguage(int spid, String msisdn)
	    throws SubscriberLanguageException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(),
		        "SubscriberLangProv_getSubscriberLanguage",
		        "Retrieves subscriber language setting");
		int result = -1;
		try
		{
			final SubscriberLangProv service = getService();
			if (service == null)
			{
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result,
				        "Cannot retrieve SubscriberLangProvOperations CORBA service.");
				LogSupport.minor(getContext(), this, exception.getMessage(),
				    exception);
				throw exception;
			}
			if (LogSupport.isDebugEnabled(getContext()))
			{
				LogSupport
				    .debug(
				        getContext(),
				        this,
				        "Sending CORBA request to URCS SubcriberLangProvClient.getSubscriberLanguage with SPID = "
				            + spid + " and MSISDN = " + msisdn);
			}

			StringHolder languageHolder = new StringHolder();
			result =
			    service.getSubscriberLanaguage(spid, msisdn, languageHolder);
			if (result != SubscriberLangProv.SUCCESS)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("URCS SubscriberLangProv.getSubscriberLanguage request returned with this error: ");
				sb.append(SubscriberLanguageException.getVerboseResult(
				    getContext(), result));
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result, sb.toString());
				if (LogSupport.isDebugEnabled(getContext()))
				{
					LogSupport.debug(getContext(), this, sb.toString());
				}
				throw exception;
			}

			return languageHolder.value;
		}
		catch (COMM_FAILURE e)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "CORBA communication with URCS SubcriberLangProvClient Service server failed while attempting to call SubscriberLangProv.getSubscriberLanguage SPID = "
			            + spid + " and MSISDN = " + msisdn);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result,
			        "Communication failure occured between CRM and URCS.");
			throw exception;
		}
		catch (Throwable t)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "Unknown error while attempting to communicate with URCS SubcriberLangProvClient Service to call getSubscriberLanguage for SPID = "
			            + spid + " and MSISDN = " + msisdn);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result, "Unknown error.");
			throw exception;
		}
		finally
		{
			pm.log(getContext());
		}
	}

	/**
	 * @param ctx
	 * @param spid
	 * @param msisdn
	 * @return
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#getSubscriberLanguageWithDefault(com.redknee.framework.xhome.context.Context,
	 *      int, java.lang.String)
	 */
	@Override
	public String getSubscriberLanguageWithDefault(int spid, String msisdn)
	    throws SubscriberLanguageException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(),
		        "SubscriberLangProv_getSubscriberLanguageWithDefault",
		        "Retrieves subscriber language setting");
		int result = -1;
		try
		{
			final SubscriberLangProv service = getService();
			if (service == null)
			{
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result,
				        "Cannot retrieve SubscriberLangProvOperations CORBA service.");
				LogSupport.minor(getContext(), this, exception.getMessage(),
				    exception);
				throw exception;
			}
			if (LogSupport.isDebugEnabled(getContext()))
			{
				LogSupport
				    .debug(
				        getContext(),
				        this,
				        "Sending CORBA request to URCS SubcriberLangProvClient.getSubscriberLanguageWithDefault with SPID = "
				            + spid + " and MSISDN = " + msisdn);
			}

			StringHolder languageHolder = new StringHolder();
			ParameterSetHolder paramSetHolder = new ParameterSetHolder();
			result =
			    service.getSubscriberLanguageWithDefault(spid, msisdn,
			        languageHolder, paramSetHolder);
			if (result == SubscriberLangProv.SUCCESS
			    && languageHolder.value != null
			    && !languageHolder.value.isEmpty())
			{
				return languageHolder.value;
			}

			String defaultLanguage = null;
			if (result != SubscriberLangProv.INTERNAL_ERROR
			    && null != paramSetHolder.value)
			{
				for (Parameter param : paramSetHolder.value)
				{
					if (OutParamID.DEFAULT_LANGUAGE == param.parameterID)
					{
						defaultLanguage = param.value.stringValue();
						break;
					}
				}
			}

			StringBuilder sb = new StringBuilder();
			sb.append("URCS SubcriberLangProvClient.getSubscriberLanguageWithDefault request returned with this error: ");
			sb.append(SubscriberLanguageException.getVerboseResult(
			    getContext(), result));
			if (defaultLanguage == null || defaultLanguage.isEmpty())
			{
				sb.append("\nNo language or default language set for the subscriber");
			}
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result, sb.toString());
			if (LogSupport.isDebugEnabled(getContext()))
			{
				LogSupport.debug(getContext(), this, sb.toString());
			}
			throw exception;
		}
		catch (COMM_FAILURE e)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "CORBA communication with URCS SubcriberLangProvClient Service server failed while attempting to call SubcriberLangProvClient.getSubscriberLanguageWithDefault with SPID = "
			            + spid + " and MSISDN = " + msisdn);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result,
			        "Communication failure occured between CRM and URCS.");
			throw exception;
		}
		catch (Throwable t)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "Unknown error while attempting to communicate with URCS SubcriberLangProvClient Service to call SubcriberLangProvClient.getSubscriberLanguageWithDefault SPID = "
			            + spid + " and MSISDN = " + msisdn);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result, "Unknown error.");
			throw exception;
		}
		finally
		{
			pm.log(getContext());
		}
	}

	/**
	 * @param ctx
	 * @param spid
	 * @return
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#isSubscriberLanguageUpdateSupported(com.redknee.framework.xhome.context.Context,
	 *      int)
	 */
	@Override
	public boolean isSubscriberLanguageUpdateSupported(int spid)
	    throws SubscriberLanguageException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(),
		        "SubscriberLangProv_isSubscriberLanguageUpdateSupported",
		        "Checks if per-subscriber multi-language is supported");
		int result = -1;
		try
		{
			final SubscriberLangProv service = getService();
			if (service == null)
			{
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result,
				        "Cannot retrieve SubscriberLangProvOperations CORBA service.");
				LogSupport.minor(getContext(), this, exception.getMessage(),
				    exception);
				throw exception;
			}
			if (LogSupport.isDebugEnabled(getContext()))
			{
				LogSupport
				    .debug(
				        getContext(),
				        this,
				        "Sending CORBA request to URCS SubcriberLangProvClient.isSubscriberLanguageUpdateSupported with SPID = "
				            + spid);
			}

			BooleanHolder updateSupported = new BooleanHolder();
			result =
			    service.isSubscriberLanguageUpdateSupported(spid,
			        updateSupported);
			if (result != SubscriberLangProv.SUCCESS)
			{

				StringBuilder sb = new StringBuilder();
				sb.append("URCS SubcriberLangProvClient.isSubscriberLanguageUpdateSupported request returned with this error: ");
				sb.append(SubscriberLanguageException.getVerboseResult(
				    getContext(), result));
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result, sb.toString());
				if (LogSupport.isDebugEnabled(getContext()))
				{
					LogSupport.debug(getContext(), this, sb.toString());
				}
				throw exception;
			}
			return updateSupported.value;
		}
		catch (COMM_FAILURE e)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "CORBA communication with URCS SubcriberLangProvClient Service server failed while attempting to call SubcriberLangProvClient.isSubscriberLanguageUpdateSupported with SPID = "
			            + spid);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result,
			        "Communication failure occured between CRM and URCS.");
			throw exception;
		}
		catch (Throwable t)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "Unknown error while attempting to communicate with URCS SubcriberLangProvClient Service to call SubcriberLangProvClient.isSubscriberLanguageUpdateSupported SPID = "
			            + spid);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result, "Unknown error.");
			throw exception;
		}
		finally
		{
			pm.log(getContext());
		}
	}

	/**
	 * @param ctx
	 * @param spid
	 * @param msisdn
	 * @param language
	 * @return
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#setSubscriberLanguage(com.redknee.framework.xhome.context.Context,
	 *      int, java.lang.String, java.lang.String)
	 */
	@Override
	public String
	    setSubscriberLanguage(int spid, String msisdn, String language)
	        throws SubscriberLanguageException
	{
		final LogMsg pm =
		    new PMLogMsg(getModule(),
		        "SubscriberLangProv_setSubscriberLanguage",
		        "Sets the subscriber's language");
		int result = -1;
		try
		{
			final SubscriberLangProv service = getService();
			if (service == null)
			{
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result,
				        "Cannot retrieve SubscriberLangProvOperations CORBA service.");
				LogSupport.minor(getContext(), this, exception.getMessage(),
				    exception);
				throw exception;
			}
			if (LogSupport.isDebugEnabled(getContext()))
			{
				LogSupport
				    .debug(
				        getContext(),
				        this,
				        "Sending CORBA request to URCS SubcriberLangProvClient.setSubscriberLanguage with SPID = "
				            + spid
				            + ", MSISDN = "
				            + msisdn
				            + ", LANG = "
				            + language);
			}

			StringHolder prevLanguageHolder = new StringHolder();
			result =
			    service.setSubscriberLanguage(spid, msisdn,
			        language == null ? "" : language, prevLanguageHolder);
			if (result != SubscriberLangProv.SUCCESS)
			{

				StringBuilder sb = new StringBuilder();
				sb.append("URCS SubcriberLangProvClient.setSubscriberLanguage request returned with this error: ");
				sb.append(SubscriberLanguageException.getVerboseResult(
				    getContext(), result));
				SubscriberLanguageException exception =
				    new SubscriberLanguageException(result, sb.toString());
				if (LogSupport.isDebugEnabled(getContext()))
				{
					LogSupport.debug(getContext(), this, sb.toString());
				}
				throw exception;
			}
			return prevLanguageHolder.value;
		}
		catch (COMM_FAILURE e)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "CORBA communication with URCS SubcriberLangProvClient Service server failed while attempting to call SubcriberLangProvClient.setSubscriberLanguage with SPID = "
			            + spid
			            + ", MSISDN = "
			            + msisdn
			            + ", LANG = "
			            + language);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result,
			        "Communication failure occured between CRM and URCS.");
			throw exception;
		}
		catch (Throwable t)
		{
			result = SubscriberLangProv.CONNECTION_ERROR;
			LogSupport
			    .minor(
			        getContext(),
			        this,
			        "Unknown error while attempting to communicate with URCS SubcriberLangProvClient Service to call SubcriberLangProvClient.setSubscriberLanguage SPID = "
			            + spid
			            + ", MSISDN = "
			            + msisdn
			            + ", LANG = "
			            + language);
			SubscriberLanguageException exception =
			    new SubscriberLanguageException(result, "Unknown error.");
			throw exception;
		}
		finally
		{
			pm.log(getContext());
		}
	}

	private String getModule()
	{
		return this.getClass().getName();
	}
}
