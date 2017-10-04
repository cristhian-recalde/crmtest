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

import java.io.PrintWriter;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.SimpleWebAction;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xlog.log.MinorLogMsg;

/**
 *
 * @author gary.anderson@redknee.com
 */
public class CompileWebAction
    extends SimpleWebAction
{
    public CompileWebAction()
    {
        super("Compile", "Compile");
    }

    public void execute(final Context context)
    throws AgentException
    {
        final XMLTranslationConfigurationID key = parseKey(WebAgents.getParameter(context, "key"));
        if (key != null)
        {
            String message;
            
            final Home home = (Home)context.get(XMLTranslationConfigurationHome.class);
            XMLTranslationConfiguration config;
            try
            {
            	config = (XMLTranslationConfiguration)home.find(context, key);

                if (config != null)
                {
                    // Calling for the translator will trigger a compile if not already compiled.
                    final Transalator translator = config.getTranslator(context);

                    if (translator != null)
                    {
                        message = "<h2>Compilation: Success</h2>";
                    }
                    else
                    {
                        message = "<h2>Compilation: Failure</h2><pre>" + config.getTranslatorStatusMessage() + "</pre>";
                    }
                }
                else
                {
                    message = "<h2>Object not found: " + key.toString() + "</h2>";
                }
            }
            catch (NumberFormatException exception)
            {
                message = "<h2>Object key is malformed: " + key.toString() + "</h2>";
                new MinorLogMsg(this, message, exception).log(context);
                config = null;
            }
            catch (HomeException exception)
            {
                message = "<h2>Failed to locate object with key: " + key.toString() + "</h2>";
                new MinorLogMsg(this, message, exception).log(context);
                config = null;
            }

            final PrintWriter writer = (PrintWriter)context.get(PrintWriter.class);

            writer.println(message);

            returnToSummaryView(context);
        }
    }
    
    protected XMLTranslationConfigurationID parseKey(String str)
    {
       if ( str == null ) return null;

       return (XMLTranslationConfigurationID) XMLTranslationConfigurationIdentitySupport.instance().fromStringID(str);
    }
}
