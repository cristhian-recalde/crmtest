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

import com.trilogy.app.crm.bean.PricePlan;
import com.trilogy.app.crm.bean.PricePlanVersion;


/**
 * Represents the Price Plan Version deletion event record (889) described in
 * the E-Care FS.
 *
 * @author gary.anderson@redknee.com
 */
public final
class PricePlanVersionDeletionEventRecord
    implements EventRecord
{
    /**
     * Creates a new Event Record for the given price plan information.
     *
     * @param plan The owning price plan.
     * @param version The version of the price plan.
     * @param result The result code of the deletion process.
     */
    public PricePlanVersionDeletionEventRecord(
        final PricePlan plan,
        final PricePlanVersion version,
        final int result)
    {
        // TODO - 2004-10-13 - Check the parameters.
        this.plan_ = plan;
        this.version_ = version;
        this.result_ = result;
    }


    /**
     * {@inheritDoc}
     */
    public void generate(final Context context)
    {
        String activationDate;

        activationDate = ERLogger.formatERDateWithTime(this.version_.getActivateDate());

        new ERLogMsg(
            IDENTIFIER,
            CLASS,
            TITLE,
            this.plan_.getSpid(),
            new String[]
            {
                getPricipleIdentifier(context),
                String.valueOf(this.version_.getId()),
                String.valueOf(this.version_.getVersion()),
                this.plan_.getName(),
                activationDate,
                Integer.toString(this.result_),
            }).log(context);
    }


    /**
     * Gets the name of the user controlling this generation from the context.
     *
     * @param context The operating context.
     * @return The name of the user if there is one; otherwise a blank string.
     */
    private String getPricipleIdentifier(final Context context)
    {
        final User principal = (User)context.get(Principal.class);

        final String pricipleIdentifier;
        if (principal != null && principal.getName() != null)
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
    private static final int IDENTIFIER = 889;

    /**
     * The String identifier of the ER.
     */
    private static final String TITLE = "Price Plan Version Deletion Event";

    /**
     * The owning price plan.
     */
    private final PricePlan plan_;

    /**
     * The version of the price plan.
     */
    private final PricePlanVersion version_;

    /**
     * The result code of the deletion.
     */
    private final int result_;

} // class
