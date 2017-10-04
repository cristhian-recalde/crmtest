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
package com.trilogy.app.crm.home.grr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.app.crm.grr.ClientToXMLTemplateConfig;
import com.trilogy.app.crm.grr.XMLTemplateConfig;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 *
 * @author sajid.memon@redknee.com
 * 
 */
public class GenericRequestResponseSupport
{
	/**
	 * Load content of a file from a specified path. 
	 *
	 * @param context
	 * @param path
	 * @param fileName
	 * @return String
	 */
	public static String loadFromFile(final Context context, String path, String fileName) throws IOException
	{
		final File file = getFile(context, path, fileName);
		if (!file.exists())
		{
			return null;
		}
		final BufferedReader reader;
		reader = new BufferedReader(new FileReader(file));
		final StringBuilder buffer = new StringBuilder();
		String line = reader.readLine();
		while (line != null)
		{
			buffer.append(line);
			buffer.append('\n');
			line = reader.readLine();
		}
		reader.close();
		return buffer.toString();
	}

	/**
	 * Stores the content to a file at a specified path. 
	 *
	 * @param context
	 * @param path
	 * @param fileName
	 * @param content
	 * 
	 */
	public static void storeToFile(Context context, String path, String fileName, String content) throws IOException
	{

		final File file = getFile(context, path, fileName);
		final PrintWriter writer;
		writer = new PrintWriter(new FileWriter(file));
		writer.print(content);
		writer.close();

	}

	/**
	 * Removes the file at a specified path. 
	 *
	 * @param context
	 * @param path
	 * @param fileName
	 * 
	 */
	public static void removeFile(Context context, String path, String fileName) throws IOException
	{
		final File file = getFile(context, path, fileName);

		LogSupport.info(context, GenericRequestResponseSupport.class.getName(), "Removing File : Path : " + path + ", fileName : " + fileName);

		if (file.exists() && file.isFile())
		{
			file.delete();
			LogSupport.info(context, GenericRequestResponseSupport.class.getName(), "File Removed : Path : " + path + ", fileName : " + fileName);
		}
		else
		{
			LogSupport.info(context, GenericRequestResponseSupport.class.getName(), "File does not exists at Path : " + path + ", fileName : " + fileName);
		}
	}


	/**
	 * Retrieve the file from $PROJECT_HOME + "/" + path + "/" + fileName. 
	 * CoreSupport.getProjectHome() returns the path of the Project i.e. $PROJECT_HOME
	 * 
	 * Ex: 
	 * path : etc/GRR/Template/Custom
	 * fileName : Sprint_SPG_activateSubscriber_1_0_0.xml
	 * File : $PROJECT_HOME/etc/GRR/Template/Custom/Sprint_SPG_activateSubscriber_1_0_0.xml
	 *
	 * @param context
	 * @param path
	 * @param fileName
	 * @return File
	 */
	public static File getFile(Context context, String path, String fileName)
	{
		String projectHome = CoreSupport.getProjectHome(context);
		String file = projectHome + java.io.File.separator + path + java.io.File.separator + fileName;
		return new File(file);
	}

	
	/**
	 * 
	 * @param ctx
	 * @param username
	 * @param templateName
	 * @param spid
	 * @return
	 * @throws HomeException
	 */
	public static ClientToXMLTemplateConfig getClientToXMLTemplateConfig(Context ctx, String username, String templateName, int spid) throws HomeException
	{
		ClientToXMLTemplateConfig mappingBean = new ClientToXMLTemplateConfig();
		mappingBean.setUser(username);
		mappingBean.setSpid(spid);
		mappingBean.setTemplateName(templateName);

		mappingBean =  HomeSupportHelper.get(ctx).findBean(ctx, ClientToXMLTemplateConfig.class, mappingBean);

		return mappingBean;
	}

	/**
	 * 
	 * @param ctx
	 * @param templateId
	 * @return
	 * @throws HomeException
	 */
	public static XMLTemplateConfig getXMLTemplateConfig(Context ctx, String templateId) throws HomeException
	{

		XMLTemplateConfig templateConfig = new XMLTemplateConfig();
		templateConfig.setTemplateID(templateId);

		templateConfig =  HomeSupportHelper.get(ctx).findBean(ctx, XMLTemplateConfig.class, templateConfig);

		return templateConfig;
	}
	
	/**
	 * 
	 * @param ctx
	 * @param msisdn
	 * @return
	 */
	public static Long lookupRetailPPIDForMsisdn(Context ctx, String msisdn)
	{
		Long ppId = null;
		try
		{
			Subscriber sub = SubscriberSupport.lookupSubscriberForMSISDN(ctx, msisdn);
			ppId = sub.getPricePlan() ;
			
		}
		catch(Throwable th)
		{
			LogSupport.minor(ctx, GenericRequestResponseSupport.class, "Exception encountered while trying to Lookup priceplan ID for Subscriber with MSISDN :" + msisdn, th);
		}
		
		return ppId;
	}
}
