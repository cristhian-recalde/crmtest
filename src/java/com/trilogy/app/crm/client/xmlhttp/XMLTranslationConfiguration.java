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

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;

/**
 *
 * @author gary.anderson@redknee.com
 */
public class XMLTranslationConfiguration
    extends AbstractXMLTranslationConfiguration
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Transalator getTranslator(final Context context)
    {
        final CompiledScripts scripts = (CompiledScripts)context.get(CompiledScripts.class);
        return scripts.getTranslator(context, this);
    }

    /**
     * {@inheritDoc}
     */
    public TranslatorStatusEnum getTranslatorStatus()
    {
        final Context context = ContextLocator.locate();
        final CompiledScripts scripts = (CompiledScripts)context.get(CompiledScripts.class);
        return scripts.getStatus(context, this);
    }

    /**
     * {@inheritDoc}
     */
    public String getTranslatorStatusMessage()
    {
        final Context context = ContextLocator.locate();
        final CompiledScripts scripts = (CompiledScripts)context.get(CompiledScripts.class);
        return scripts.getStatusMessage(context, this);
    }



}
