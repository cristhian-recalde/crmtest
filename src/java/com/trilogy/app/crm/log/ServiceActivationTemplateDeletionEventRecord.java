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
package com.trilogy.app.crm.log;

import java.security.Principal;

import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.ERLogMsg;

import com.trilogy.app.crm.bas.tps.ServiceActivationTemplate;


/**
 * Represents the ServiceActivationTemplate Deletion event record (893) described
 * in the E-Care FS.
 *
 * @author Lily Zou
 * @date   Dec 10, 2004
 */
public final
class ServiceActivationTemplateDeletionEventRecord
    implements EventRecord
{
    /**
     * Creates a new ServiceActivationTemplateDeletionEventRecord for the given
     * template.
     *
     * @param template The ServiceActivationTemplate for which the ER will be
     * created.
     *
     * @exception IllegalArgumentException Thrown if the given template is null.
     */
    public ServiceActivationTemplateDeletionEventRecord(final ServiceActivationTemplate template)
    {
        if (template == null)
        {
            throw new IllegalArgumentException("The given template is null.");
        }

        template_ = template;
    }


    /**
     * {@inheritDoc}
     */
    public void generate(final Context context)
    {
        new ERLogMsg(
            IDENTIFIER,
            CLASS,
            TITLE,
            template_.getSpid(),
            new String[]
            {
                getUserIdentifier(context),
                String.valueOf(template_.getIdentifier())
            }).log(context);
    }


    /**
     * Gets the user's identifier.
     *
     * @param context The operating context.
     * @return The user's identifier if one is set in the context, blank otherwise.
     */
    private String getUserIdentifier(final Context context)
    {
        final User principal = (User)context.get(Principal.class);

        final String pricipleIdentifier;
        if (principal != null)
        {
            pricipleIdentifier = principal.getName();
        }
        else
        {
            pricipleIdentifier = "";
        }

        return pricipleIdentifier;
    }


    /**
     * The numeric identifier of the ER.
     */
    private static final int IDENTIFIER = 1100;

    /**
     * The String identifier of the ER.
     */
    private static final String TITLE = "Subscriber Creation Template Deletion Event";

    /**
     * The Service Activation template for which the ER will be created.
     */
    private final ServiceActivationTemplate template_;

} // class
