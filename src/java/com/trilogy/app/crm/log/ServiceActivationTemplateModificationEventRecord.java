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
 * Represents the ServiceActivationTemplate Modification event record (892)
 * described in the E-Care FS.
 *
 * @author Lily Zou
 * @date   Dec 10, 2004
 */
public final
class ServiceActivationTemplateModificationEventRecord
    implements EventRecord
{
    /**
     * Creates a new ServiceActivationTemplateModificationEventRecord for the given
     * template.
     *
     * @param oldTemplate The old ServiceActivationTemplate for which the ER will be
     * created.
     * @param newTemplate The new ServiceActivationTemplate for which the ER will be
     * created.
     *
     * @exception IllegalArgumentException Thrown if either of the given
     * templates is null, and if the two templates have different identities.
     */
    public ServiceActivationTemplateModificationEventRecord(
        final ServiceActivationTemplate oldTemplate,
        final ServiceActivationTemplate newTemplate)
    {
        if (oldTemplate == null)
        {
            throw new IllegalArgumentException("The given old template is null.");
        }

        if (newTemplate == null)
        {
            throw new IllegalArgumentException("The given new template is null.");
        }

        if (oldTemplate.getIdentifier() != newTemplate.getIdentifier())
        {
            throw new IllegalArgumentException(
                "The old template has an identity of " + oldTemplate.getIdentifier()
                + " but the new template has an identifier of " + newTemplate.getIdentifier());
        }

        oldTemplate_ = oldTemplate;
        newTemplate_ = newTemplate;
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
            oldTemplate_.getSpid(),
            new String[]
            {
                getUserIdentifier(context),

                String.valueOf(oldTemplate_.getIdentifier()),

                String.valueOf(oldTemplate_.getName()),
                String.valueOf(newTemplate_.getName()),

                String.valueOf(oldTemplate_.getInitialBalance()),
                String.valueOf(newTemplate_.getInitialBalance()),

                "0",
                "0",

                String.valueOf(oldTemplate_.getMaxBalance()),
                String.valueOf(newTemplate_.getMaxBalance()),

                String.valueOf(oldTemplate_.getMaxRecharge()),
                String.valueOf(newTemplate_.getMaxRecharge()),

                String.valueOf(oldTemplate_.getPricePlan()),
                String.valueOf(newTemplate_.getPricePlan()),

                String.valueOf(oldTemplate_.getReactivationFee()),
                String.valueOf(newTemplate_.getReactivationFee())
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
    private static final String TITLE = "Subscriber Creation Template Modification Event";

    /**
     * The old FreeCallTime template for which the ER will be created.
     */
    private final ServiceActivationTemplate oldTemplate_;

    /**
     * The new FreeCallTime template for which the ER will be created.
     */
    private final ServiceActivationTemplate newTemplate_;

} // class
