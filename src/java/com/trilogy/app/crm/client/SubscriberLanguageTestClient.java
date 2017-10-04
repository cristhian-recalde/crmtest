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

import java.util.HashMap;
import java.util.Map;

/**
 * @author cindy.wong@redknee.com
 * @since 2010-12-19
 */
public class SubscriberLanguageTestClient implements SubscriberLanguageClient
{

	public static final String DEFAULT_LANGUAGE = "default";

	/**
	 * @param spid
	 * @param msisdn
	 * @return
	 * @throws SubscriberLanguageException
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#getSubscriberLanguage(int, java.lang.String)
	 */
	@Override
	public String getSubscriberLanguage(int spid, String msisdn)
	    throws SubscriberLanguageException
	{
		Map<String, String> spidMap = maps_.get(spid);
		if (spidMap == null)
		{
			return null;
		}
		return spidMap.get(msisdn);
	}

	/**
	 * @param spid
	 * @param msisdn
	 * @return
	 * @throws SubscriberLanguageException
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#getSubscriberLanguageWithDefault(int, java.lang.String)
	 */
	@Override
	public String getSubscriberLanguageWithDefault(int spid, String msisdn)
	    throws SubscriberLanguageException
	{
		String lang = getSubscriberLanguage(spid, msisdn);
		return (lang == null) ? DEFAULT_LANGUAGE : lang;
	}

	/**
	 * @param spid
	 * @return
	 * @throws SubscriberLanguageException
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#isSubscriberLanguageUpdateSupported(int)
	 */
	@Override
	public boolean isSubscriberLanguageUpdateSupported(int spid)
	    throws SubscriberLanguageException
	{
		return true;
	}

	/**
	 * @param spid
	 * @param msisdn
	 * @param language
	 * @return
	 * @throws SubscriberLanguageException
	 * @see com.redknee.app.crm.client.SubscriberLanguageClient#setSubscriberLanguage(int, java.lang.String, java.lang.String)
	 */
	@Override
	public String
	    setSubscriberLanguage(int spid, String msisdn, String language)
	        throws SubscriberLanguageException
	{
		Map<String, String> spidMap = maps_.get(spid);
		if (spidMap == null)
		{
			spidMap = new HashMap<String, String>();
			maps_.put(spid, spidMap);
		}
		spidMap.put(msisdn, language);
		return language;
	}

	Map<Integer, Map<String, String>> maps_ =
	    new HashMap<Integer, Map<String, String>>();

}
