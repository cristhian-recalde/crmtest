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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.language.MessageMgr;

import com.trilogy.app.pin.manager.SubscriberLangProv;

import com.trilogy.app.crm.support.PinManagerSupport;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-11-30
 */
public class SubscriberLanguageException extends Exception
{
	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	public SubscriberLanguageException(final int resultCode,
	    final String message)
	{
		super(message);
		this.resultCode_ = resultCode;
	}

	public static String getVerboseResult(Context ctx, final int resultCode)
	{
		MessageMgr mmgr = new MessageMgr(ctx, getModule());
		String value;
		switch (resultCode)
		{
			case SubscriberLangProv.SUCCESS:
				value = mmgr.get(SUCCESS_KEY, SUCCESS_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.CONNECTION_ERROR:
				value =
				    mmgr.get(CONNECTION_ERROR_KEY,
				        CONNECTION_ERROR_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.INVALID_LANGUAGE:
				value =
				    mmgr.get(INVALID_LANGUAGE_KEY,
				        INVALID_LANGUAGE_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.INVALID_SPID:
				value =
				    mmgr.get(INVALID_SPID_KEY, INVALID_SPID_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.INVALID_SUBSCRIBER:
				value =
				    mmgr.get(INVALID_SUBSCRIBER_KEY,
				        INVALID_SUBSCRIBER_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.SERVER_BUSY:
				value = mmgr.get(SERVER_BUSY_KEY, SERVER_BUSY_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.IGNORE_SPID:
				value = mmgr.get(IGNORE_SPID_KEY, IGNORE_SPID_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.UNKNOWN_SUBSCRIBER:
				value =
				    mmgr.get(UNKNOWN_SUBSCRIBER_KEY,
				        UNKNOWN_SUBSCRIBER_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.UNSUPPORTED_OPERATION:
				value =
				    mmgr.get(UNSUPPORTED_OPERATION_KEY,
				        UNSUPPORTED_OPERATION_DEFAULT_MESSAGE);
				break;
			case SubscriberLangProv.INTERNAL_ERROR:
				value =
				    mmgr.get(INTERNAL_ERROR_KEY, INTERNAL_ERROR_DEFAULT_MESSAGE);
				break;
			default:
				value = mmgr.get(UNKNOWN_ERROR_KEY, UNKNOWN_DEFAULT_MESSAGE);
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("Result code: ");
		sb.append(this.resultCode_);
		sb.append(", ");
		sb.append(super.toString());
		return sb.toString();
	}

	public int getResultCode()
	{
		return resultCode_;
	}

	private static Class getModule()
	{
		return PinManagerSupport.class;
	}

	/**
	 * SubscriberLangProv result code
	 */
	private final int resultCode_;

	public static final String SUCCESS_DEFAULT_MESSAGE = "Succesful operation.";
	public static final String UNKNOWN_SUBSCRIBER_DEFAULT_MESSAGE =
	    "The subscriber referenced in the call is unknown to the service.";
	public static final String INVALID_SPID_DEFAULT_MESSAGE =
	    "The service provider referenced in the call is not appropriate for use in the call.";
	public static final String INVALID_SUBSCRIBER_DEFAULT_MESSAGE =
	    "The identification given for a subscriber is not a valid identifier.";
	public static final String INVALID_LANGUAGE_DEFAULT_MESSAGE =
	    "The given language is not of ISO 639-* format, or is an unsupported format.";
	public static final String UNSUPPORTED_OPERATION_DEFAULT_MESSAGE =
	    "The service providing this interface does not allow language updates.";
	public static final String SERVER_BUSY_DEFAULT_MESSAGE =
	    "The server is unable to execute the method due to current workload.";
	public static final String INTERNAL_ERROR_DEFAULT_MESSAGE =
	    "Internal error occured in Language Manager Service, please check logs.";
	public static final String CONNECTION_ERROR_DEFAULT_MESSAGE =
	    "Connectivity error occurred, please check logs.";
	public static final String UNKNOWN_DEFAULT_MESSAGE =
	    "Unknown Error , please check logs. ";

	private static final String SUCCESS_KEY =
	    "PinManagerSupport.SubscriberLangProv.Success";
	private static final String CONNECTION_ERROR_KEY =
	    "PinManagerSupport.SubscriberLangProv.ConnectionError";
	private static final String INVALID_LANGUAGE_KEY =
	    "PinManagerSupport.SubscriberLangProv.InvalidLanguage";
	private static final String INVALID_SPID_KEY =
	    "PinManagerSupport.SubscriberLangProv.InvalidSpid";
	private static final String INVALID_SUBSCRIBER_KEY =
	    "PinManagerSupport.SubscriberLangProv.InvalidSubscriber";
	private static final String SERVER_BUSY_KEY =
	    "PinManagerSupport.SubscriberLangProv.ServerBusy";
	private static final String IGNORE_SPID_KEY = "IGNORE_SPID";
	private static final String IGNORE_SPID_DEFAULT_MESSAGE =
	    "The SPID passed in has been ignored.";
	private static final String UNKNOWN_SUBSCRIBER_KEY =
	    "PinManagerSupport.SubscriberLangProv.UnknownSubscriber";
	private static final String UNSUPPORTED_OPERATION_KEY =
	    "PinManagerSupport.SubscriberLangProv.UnsupportedOperation";
	private static final String INTERNAL_ERROR_KEY =
	    "PinManagerSupport.SubscriberLangProv.InternalError";
	private static final String UNKNOWN_ERROR_KEY =
	    "PinManagerSupport.SubscriberLangProv.UnknownError";
}
