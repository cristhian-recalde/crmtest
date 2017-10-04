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
package com.trilogy.app.crm.clean;

import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.visitor.HomeVisitor;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.transfer.TransferException;
import com.trilogy.app.crm.transfer.TransferExceptionHome;
import com.trilogy.app.crm.transfer.TransferFailureStateEnum;

/**
 * Removes corrected or deleted transfer exception records from database.
 *
 */
public class TransferExceptionCleanUpAgent implements ContextAgent
{
 
    /**
     * Removes a transfer exception from home if it is marked for deletion (ie. in CORRECTED or DELETED state)
     *
     * @author ling.tang@redknee.com
     */
    static final class CleanUpTransferExceptionVisitor extends HomeVisitor
    {
        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = 0L;

        /**
         * Create a new instance of <code>CleanUpTransferExceptionVisitor</code>.
         *
         * @param transferExceptionHome
         *            The home to visit.
         */
        public CleanUpTransferExceptionVisitor(final Home transferExceptionHome)
        {
            super(transferExceptionHome);

            // add safeguard
            if (transferExceptionHome == null)
            {
                throw new IllegalArgumentException("Home must not be null");
            }
        }

        /**
         * If the transfer exception record is in Corrected or Deleted state, remove the record from the home.
         *
         * @param context
         *            The operating context.
         * @param object
         *            The transfer exception to be removed.
         * @see com.redknee.framework.xhome.visitor.VisitorProxy#visit
         */
        @Override
        public void visit(final Context context, final Object object)
        {
            final TransferException record = (TransferException) object;

            // Delete transfer exception if it is in either CORRECTED or DELETED state
            if (record.getState().equals(TransferFailureStateEnum.CORRECTED)
                    || record.getState().equals(TransferFailureStateEnum.DELETED))
            {
                new DebugLogMsg(this, "Transfer Exception " + record.getId() + " has been marked for deletion", null).log(context);
                try
                {
                    getHome().remove(context, record);
                }
                catch (HomeException exception)
                {
                    new MinorLogMsg(this, "Couldn't remove transfer exception record " + record.getId(), exception).log(context);
                }
            }
        }
    }

    /**
     * Delete all Transfer Exception records marked for deletion or have been corrected
     *
     * @param context
     *            The operating context.
     * @throws AgentException
     *             Thrown if the agent cannot carry out its task.
     * @see com.redknee.framework.xhome.context.ContextAgent#execute
     */
    public void execute(final Context context) throws AgentException
    {
        final Home transferExceptionHome = (Home) context.get(TransferExceptionHome.class);
        if (transferExceptionHome == null)
        {
            throw new AgentException("System error: TransferExceptionHome not found in context");
        }

        try
        {
            transferExceptionHome.forEach(context, new CleanUpTransferExceptionVisitor(transferExceptionHome));

        }
        catch (HomeException exception)
        {
            new MinorLogMsg(this, "Error encountered cleaning up Transfer Exception table", exception).log(context);
            throw new AgentException("Error encountered cleaning up Transfer Exception table", exception);
        }
    }
}
