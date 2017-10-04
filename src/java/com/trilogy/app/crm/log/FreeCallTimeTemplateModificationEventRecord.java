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

import com.trilogy.app.crm.bean.FreeCallTime;


/**
 * Represents the Free Call Time Template Modification event record (892)
 * described in the E-Care FS.
 *
 * @author gary.anderson@redknee.com
 */
public final
class FreeCallTimeTemplateModificationEventRecord
    implements EventRecord
{
    /**
     * Creates a new FreeCallTimeTemplateModificationEventRecord for the given
     * template.
     *
     * @param oldTemplate The old FreeCallTime template for which the ER will be
     * created.
     * @param newTemplate The new FreeCallTime template for which the ER will be
     * created.
     *
     * @exception IllegalArgumentException Thrown if either of the given
     * templates is null, and if the two templates have different identities.
     */
    public FreeCallTimeTemplateModificationEventRecord(
        final FreeCallTime oldTemplate,
        final FreeCallTime newTemplate)
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

                String.valueOf(oldTemplate_.getFreeCallTime()),
                String.valueOf(newTemplate_.getFreeCallTime()),

                String.valueOf(oldTemplate_.getRollOverMinutes().getIndex()),
                String.valueOf(newTemplate_.getRollOverMinutes().getIndex()),

                String.valueOf(oldTemplate_.getMaximumRollOver()),
                String.valueOf(newTemplate_.getMaximumRollOver()),

                String.valueOf(oldTemplate_.getRollOverPercentage()),
                String.valueOf(newTemplate_.getRollOverPercentage()),

                String.valueOf(oldTemplate_.getExpiryPercentage()),
                String.valueOf(newTemplate_.getExpiryPercentage()),

                String.valueOf(oldTemplate_.getUsagePrecedence().getIndex()),
                String.valueOf(newTemplate_.getUsagePrecedence().getIndex()),

                String.valueOf(oldTemplate_.getGroupLimit()),
                String.valueOf(newTemplate_.getGroupLimit())
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
    private static final int IDENTIFIER = 892;

    /**
     * The String identifier of the ER.
     */
    private static final String TITLE = "Free Call Time Template Modification Event";

    /**
     * The old FreeCallTime template for which the ER will be created.
     */
    private final FreeCallTime oldTemplate_;

    /**
     * The new FreeCallTime template for which the ER will be created.
     */
    private final FreeCallTime newTemplate_;

} // class
