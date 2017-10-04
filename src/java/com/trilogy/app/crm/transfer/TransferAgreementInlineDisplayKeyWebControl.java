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
package com.trilogy.app.crm.transfer;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.NullHome;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.ReadOnlyWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * Provides a custom key web control for Transfer Agreement that displays the
 * details of the selected agreement inline (read-only) in bean view.
 *
 * @author gary.anderson@redknee.com
 */
public class TransferAgreementInlineDisplayKeyWebControl
    extends AbstractWebControl
{
    /**
     * Creates a new web control.
     */
    public TransferAgreementInlineDisplayKeyWebControl()
    {
        mainWebControl_ = new TransferAgreementKeyWebControl(1, true, true);
        inlineDisplay_ = new ReadOnlyWebControl(new TransferAgreementWebControl());
    }


    /**
     * {@inheritDoc}
     */
    public Object fromWeb(final Context ctx, final ServletRequest req, final String name)
    {
        return mainWebControl_.fromWeb(ctx, req, name);
    }


    /**
     * {@inheritDoc}
     */
    public void fromWeb(final Context ctx, final Object obj, final ServletRequest req, final String name)
    {
        mainWebControl_.fromWeb(ctx, obj, req, name);
    }


    /**
     * {@inheritDoc}
     */
    public void toWeb(final Context parentContext, final PrintWriter out, final String name, final Object obj)
    {
        final Context context = wrapContext(parentContext);

        if (context.getBoolean("TABLE_MODE", false))
        {
            mainWebControl_.toWeb(context, out, name, obj);
        }
        else
        {
            if (context.getInt("MODE", EDIT_MODE) != DISPLAY_MODE)
            {
                mainWebControl_.toWeb(context, out, name, obj);
            }

            final Home home = (Home)context.get(TransferAgreementHome.class);

            TransferAgreement agreement = null;


            try
            {
                agreement = (TransferAgreement)home.find(context, obj);
            }
            catch (final HomeException exception)
            {
                new MajorLogMsg(this, "Failed to find TransferAgreement " + obj, exception).log(context);
            }

            if (agreement != null)
            {
                setMode(context, TransferAgreementXInfo.SPID, ViewModeEnum.NONE);
                setMode(context, TransferAgreementXInfo.OWNER_ID, ViewModeEnum.NONE);

                if (context.getInt("MODE", EDIT_MODE) != DISPLAY_MODE)
                {
                    setMode(context, TransferAgreementXInfo.DESCRIPTION, ViewModeEnum.NONE);
                }

                inlineDisplay_.toWeb(context, out, name, agreement);
            }
        }
    }


    /**
     * Provides data manipulation within the context for this web control (e.g.,
     * filtering of Transfer Agreement).
     *
     * @param parentContext The parent context.
     * @return The context with manipulated data.
     */
    private Context wrapContext(final Context parentContext)
    {
        final TransferContract transfer = (TransferContract)parentContext.get(AbstractWebControl.BEAN);

        Home home = null;

        if (transfer.getPrivacy() == GroupPrivacyEnum.PRIVATE
            && (transfer.getOwnerID() == null || transfer.getOwnerID().trim().length() == 0))
        {
            // Private contracts must specify an owner. If no owner is
            // specified, then we can have no agreements. Without this special
            // check, we'd search for agreements with a blank owner, which would
            // find Public agreements.
            home = NullHome.instance();
        }
        else if (transfer.getPrivacy() == GroupPrivacyEnum.PRIVATE)
        {
            home = (Home)parentContext.get(TransferAgreementHome.class);
            home = home.where(parentContext, new EQ(TransferAgreementXInfo.OWNER_ID, transfer.getOwnerID()));
        }
        else
        {
            // If the contract is Public, then we ignore any owner that might be
            // set (e.g., from before a screen refresh), and simply search for
            // Public agreements.
            home = (Home)parentContext.get(TransferAgreementHome.class);
            home = home.where(parentContext, new EQ(TransferAgreementXInfo.OWNER_ID, ""));
        }

        final Context context = parentContext.createSubContext();
        context.put(TransferAgreementHome.class, home);

        return context;
    }


    /**
     * The main web control used as a delegate for display in tables and for
     * selection as a key web control.
     */
    private final TransferAgreementKeyWebControl mainWebControl_;

    /**
     * The web control used to display (read-only) the details of the selected
     * agreement.
     */
    private final OutputWebControl inlineDisplay_;
}
