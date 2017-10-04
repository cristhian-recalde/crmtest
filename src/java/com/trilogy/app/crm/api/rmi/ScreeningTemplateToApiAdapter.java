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
package com.trilogy.app.crm.api.rmi;

import com.trilogy.app.crm.bean.ScreeningTemplate;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ScreeningTemplateReference;
import com.trilogy.util.crmapi.wsdl.v2_2.types.generalprovisioning.ScreeningTemplateStateTypeEnum;

/**
 * Adapts screening template object to API objects.
 *
 * @author Marcio Marques
 * @since 8.5
 * 
 */
public class ScreeningTemplateToApiAdapter implements Adapter
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Object adapt(final Context ctx, final Object obj) throws HomeException
    {
        return adaptScreeningTemplateToReference((ScreeningTemplate) obj);
    }

    public Object unAdapt(final Context ctx, final Object obj) throws HomeException
    {
        throw new UnsupportedOperationException();
    }

    public static ScreeningTemplateReference adaptScreeningTemplateToReference(final ScreeningTemplate screeningTemplate)
    {
        final ScreeningTemplateReference reference = new ScreeningTemplateReference();
        adaptScreeningTemplateToReference(screeningTemplate, reference);

        return reference;
    }

    public static ScreeningTemplateReference adaptScreeningTemplateToReference(final ScreeningTemplate screeningTemplate, final ScreeningTemplateReference reference)
    {
        reference.setIdentifier(screeningTemplate.getIdentifier());
        reference.setSpid(screeningTemplate.getSpid());
        reference.setName(screeningTemplate.getName());
        reference.setDescription(screeningTemplate.getDescription());
        if (screeningTemplate.isEnabled())
        {
            reference.setState(ScreeningTemplateStateTypeEnum.ACTIVE.getValue());
        }
        else
        {
            reference.setState(ScreeningTemplateStateTypeEnum.DEPRECATED.getValue());
        }

        return reference;
    }}
