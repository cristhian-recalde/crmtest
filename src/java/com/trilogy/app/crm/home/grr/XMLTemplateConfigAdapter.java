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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.AdapterHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.grr.XMLTemplateConfig;

/**
 *
 * @author sajid.memon@redknee.com
 */
public class XMLTemplateConfigAdapter 
		implements Adapter
{
    /**
     * {@inheritDoc}
     */
    public Object adapt(Context context, Object obj)
        throws HomeException
    {
        final XMLTemplateConfig xmlTemplateConfig = (XMLTemplateConfig) obj;

        String content = "";
        try
        {
        	content = GenericRequestResponseSupport.loadFromFile(context, xmlTemplateConfig.getPath(), xmlTemplateConfig.getFileName());
        	if (content == null)
        	{
        		content = "";
        	}
        }
        catch (IOException io)
        {
        	throw new HomeException("Fail to Load XML Content from File : " + xmlTemplateConfig.getFileName() + " : Error : " + io.getLocalizedMessage());
        	//new MinorLogMsg(this, "Fail to Load XML Content from File : " + xmlTemplateConfig.getFileName(), io).log(context);
        }
        finally
        {
        	xmlTemplateConfig.setXMLContent(content);	
        }
        
		return xmlTemplateConfig;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context context, Object obj)
        throws HomeException
    {
        final XMLTemplateConfig xmlTemplateConfig = (XMLTemplateConfig)obj;
        try
        {
        	GenericRequestResponseSupport.storeToFile(context, xmlTemplateConfig.getPath(), xmlTemplateConfig.getFileName(), xmlTemplateConfig.getXMLContent());
        }
        catch (IOException io)
        {
        	throw new HomeException("Fail to save XML Content to File : " + xmlTemplateConfig.getFileName() + " : Error : " + io.getLocalizedMessage());
        }
        return xmlTemplateConfig;
    }
    
}
    



