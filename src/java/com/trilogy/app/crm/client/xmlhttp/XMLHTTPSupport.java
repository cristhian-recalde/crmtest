/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used in
 * accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.client.xmlhttp;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.CritLogMsg;
import com.trilogy.framework.xlog.log.InfoLogMsg;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.service.ServiceProvisionActionEnum;
import com.trilogy.util.snippet.home.HomeDecorator;


/**
 * @author gary.anderson@redknee.com
 */
public final class XMLHTTPSupport
{
    /** Prevent instantiation.  */
    private XMLHTTPSupport()
    {
        // EMPTY
    }

    public static void install(final Context context)
    {
        final String module = XMLHTTPSupport.class.getName();

        new InfoLogMsg(module, "Installing XML-HTTP Support.", null)
                .log(context);

        try
        {
            installCompiledScriptsSupport(context);
            installXMLTranslationConfigPipeline(context);

            new InfoLogMsg(module, "XML-HTTP Support installed successfully.",
                    null).log(context);
        }
        catch (final Exception exception)
        {
            new CritLogMsg(module, "XML-HTTP Support installation FAILED!",
                    exception).log(context);
        }
    }

    public static XMLTranslationConfiguration getConfig(final Context context,
            final Service service, final ServiceProvisionActionEnum action)
        throws HomeException
    {
        final Home home = (Home) context
                .get(XMLTranslationConfigurationHome.class);

        final And criteria =  new And();
        criteria.add(new EQ(
                XMLTranslationConfigurationXInfo.PROVISIONABLE_SERVICE_TYPE,
                Long.valueOf(service.getXmlProvSvcType())));

        criteria.add(new EQ(XMLTranslationConfigurationXInfo.PROVISION_TYPE,
                action));

        final XMLTranslationConfiguration config = (XMLTranslationConfiguration) home
                .find(context, criteria);

        return config;
    }

    /**
     *
     *
     * @param context
     */
    private static void installCompiledScriptsSupport(final Context context)
    {
        context.put(CompiledScripts.class, new CompiledScripts());
    }
    
    public static void installXMLTranslationConfigPipeline(final Context context)
    	throws HomeException
    {
        Home home = new XMLTranslationConfigurationBaseXDBHome(context,
                "XMLTRANSCONFIG");

    	// Since some prefer calling the Context ctx, I include this alias.
    	final Context ctx = context;

        home = ((HomeDecorator) (new XMLTranslationConfigurationPipelineDecorator()))
                .decorate(context, home);

    	context.put(XMLTranslationConfigurationBaseHome.class, home);

    	context.put(XMLTranslationConfigurationHome.class, home);
    }
}
