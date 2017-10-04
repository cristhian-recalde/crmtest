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
package com.trilogy.app.crm.client.xmlhttp;

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

/**
 *
 * @author gary.anderson@redknee.com
 */
public class ScriptManagementHome
    extends AdapterHome
{
    public ScriptManagementHome(final Context context, final Home delegate)
    {
        super(context, new ScriptManagementAdapter(), delegate);
    }
}


class ScriptManagementAdapter
    implements Adapter
{
    /**
     * {@inheritDoc}
     */
    public Object adapt(Context context, Object obj)
        throws HomeException
    {
        final XMLTranslationConfiguration config = (XMLTranslationConfiguration)obj;

        if (config.getTranslatorScript() == null || config.getTranslatorScript().trim().length() == 0)
        {
            loadScript(context, config);
        }

        return config;
    }

    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context context, Object obj)
        throws HomeException
    {
        final XMLTranslationConfiguration config = (XMLTranslationConfiguration)obj;

        if (config.getTranslatorScript() != null)
        {
            storeScript(context, config);
        }

        return config;
    }

    /**
    *
    *
    * @param context
    * @param config
    */
    private void loadScript(final Context context, final XMLTranslationConfiguration config)
    {
        final File scriptFile = getFile(context, config);
        if (!scriptFile.exists())
        {
            return;
        }

        final BufferedReader reader;

        try
        {
            reader = new BufferedReader(new FileReader(scriptFile));
        }
        catch (FileNotFoundException exception)
        {
            new MajorLogMsg(this, "Failed to load script " + scriptFile.getName(), exception).log(context);
            return;
        }

        final StringBuilder buffer = new StringBuilder();

        try
        {
            String line = reader.readLine();
            while (line != null)
            {
                buffer.append(line);
                buffer.append('\n');
                line = reader.readLine();
            }

            config.setTranslatorScript(buffer.toString());
            config.setTranslatorStatusMessage("Loaded " + scriptFile.getName());
        }
        catch (final IOException exception)
        {
            new MajorLogMsg(this, "Failure while reading script from " + scriptFile.getName(), exception).log(context);
            config.setTranslatorStatus(TranslatorStatusEnum.ERROR);
            config.setTranslatorStatusMessage("Failure loading " + scriptFile.getName() + ": " + exception.getMessage());
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch (IOException exception)
            {
                new MinorLogMsg(this, "Failed to close reader for " + scriptFile.getName(), exception).log(context);
            }
        }
    }

    /**
     *
     *
     * @param context
     * @param config
     */
    private void storeScript(Context context, XMLTranslationConfiguration config)
    {
        final File scriptFile = getFile(context, config);

        final PrintWriter writer;

        try
        {
            writer = new PrintWriter(new FileWriter(scriptFile));
        }
        catch (IOException exception)
        {
            new MajorLogMsg(this, "Failed to create writer for " + scriptFile.getName(), exception).log(context);
            return;
        }

        writer.print(config.getTranslatorScript());
        
        writer.close();
    }

    /**
     * Retrive the script file from $PROJECT_HOME/etc/xmlServices
	 * CoreSupport.getFile() will prepend the $PROJECT_HOME/etc to the filename 
     *
     * @param context
     * @param config
     * @return
     */
    private File getFile(Context context, XMLTranslationConfiguration config)
    {
        final String baseFileName = "xmlServices" + java.io.File.separator 
            + "XMLTranslationScript-"
            + config.getProvisionableServiceType() + "-"
            + config.getProvisionType()+ ".txt";
        
        final String fileName = CoreSupport.getFile(context, baseFileName);

        return new File(fileName);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


}
