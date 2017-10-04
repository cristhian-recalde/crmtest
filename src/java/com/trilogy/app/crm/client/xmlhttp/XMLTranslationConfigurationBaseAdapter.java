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
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;

/**
 * 
 * @author gary.anderson@redknee.com
 */
public class XMLTranslationConfigurationBaseAdapter
    implements Adapter
{

    /**
     * {@inheritDoc}
     */
    public Object adapt(Context ctx, Object obj)
        throws HomeException
    {
        final XMLTranslationConfigurationBase base = (XMLTranslationConfigurationBase)obj;
        final XMLTranslationConfiguration config = new XMLTranslationConfiguration();
        
        config.setIdentifier(base.getIdentifier());
        config.setProvisionableServiceType(base.getProvisionableServiceType());
        config.setProvisionType(base.getProvisionType());
        config.setSpid(base.getSpid());
        config.setScriptType(base.getScriptType());
        config.setTextContent(base.getTextContent());
        return config;
    }


    /**
     * {@inheritDoc}
     */
    public Object unAdapt(Context ctx, Object obj)
        throws HomeException
    {
        final XMLTranslationConfigurationBase base = new XMLTranslationConfigurationBase();
        final XMLTranslationConfiguration config = (XMLTranslationConfiguration)obj;
        
        base.setIdentifier(config.getIdentifier());
        base.setProvisionableServiceType(config.getProvisionableServiceType());
        base.setProvisionType(config.getProvisionType());
        base.setSpid(config.getSpid());
        base.setScriptType(config.getScriptType());
        base.setTextContent(config.getTextContent());
        return base;
    }

}
