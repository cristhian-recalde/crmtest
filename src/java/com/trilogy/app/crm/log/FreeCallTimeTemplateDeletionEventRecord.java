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
 * Represents the Free Call Time Template Deletion event record (893) described
 * in the E-Care FS.
 *
 * @author gary.anderson@redknee.com
 */
public final
class FreeCallTimeTemplateDeletionEventRecord
    implements EventRecord
{
    /**
     * Creates a new FreeCallTimeTemplateDeletionEventRecord for the given
     * template.
     *
     * @param template The FreeCallTime template for which the ER will be
     * created.
     *
     * @exception IllegalArgumentException Thrown if the given template is null.
     */
    public FreeCallTimeTemplateDeletionEventRecord(final FreeCallTime template)
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
    private static final int IDENTIFIER = 893;

    /**
     * The String identifier of the ER.
     */
    private static final String TITLE = "Free Call Time Template Deletion Event";

    /**
     * The FreeCallTime template for which the ER will be created.
     */
    private final FreeCallTime template_;

} // class
