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
package com.trilogy.app.crm.dunning.support;

import com.trilogy.app.crm.bean.ProvisionCommand;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.hlr.HlrSupport;
import com.trilogy.app.crm.home.sub.HLRCommandFindHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.snippet.log.Logger;

/**
 * Provides support for sending commands to HLR from the DunningProcess.
 *
 * @author gary.anderson@redknee.com
 */
public class DunningHLRSupport
{

    /**
     * Creates a new support object for the given subscriber.
     *
     * @param subscriber The subscriber for which to send commands.
     * @throws IllegalArgumentException Thrown if the subscriber parameter is null.
     */
    public DunningHLRSupport(final Subscriber subscriber)
    {
        if (subscriber == null)
        {
            throw new IllegalArgumentException("Could not create support object.  The subscriber parameter is null.");
        }

        this.subscriber_ = subscriber;
    }

    /**
     * Sends an state update command to HLR to set the state of the subscriber to
     * "active".
     *
     * @param context The operating context.
     * @return True if the update succeeds; false otherwise.
     */
    public boolean setStateActive(final Context context)
    {
        return updateHLR(context, "active");
    }

    /**
     * Sends an state update command to HLR to set the state of the subscriber to
     * "oneWayBar".
     *
     * @param context The operating context.
     * @return True if the update succeeds; false otherwise.
     */
    public boolean setStateOneWayBarred(final Context context)
    {
        return updateHLR(context, "oneWayBar");
    }

    /**
     * Sends an state update command to HLR to set the state of the subscriber to
     * "twoWayBar".
     *
     * @param context The operating context.
     * @return True if the update succeeds; false otherwise.
     */
    public boolean setStateTwoWayBarred(final Context context)
    {
        return updateHLR(context, "twoWayBar");
    }

    /**
     * Gets the HLR command for the given key.
     *
     * @param context The operating context.
     * @param key The ProvisionCommandHome key for the HLR command.
     * @return The HLR command for the given key.
     */
    private ProvisionCommand getCommand(final Context context, final String key)
    {
        final HLRCommandFindHelper hlrCmdFinder = new HLRCommandFindHelper(context);

        ProvisionCommand command = null;

        try
        {
            command = hlrCmdFinder.findCommand(context, key, this.subscriber_);
        }
        catch (final HomeException exception)
        {
            if (LogSupport.isDebugEnabled(context))
            {
                new DebugLogMsg(this, "Can't find ProvisionCommand for Subscriber [" + this.subscriber_ + "]",
                        exception).log(context);
            }
        }

        return command;
    }



    /**
     * Updates the HLR by sending the given command.
     *
     * @param context The operating context.
     * @param commandIdentifier The identifier of the HLR command to send.
     * @return True if the HLR command succeeded; false otherwise.
     */
    private boolean updateHLR(final Context context, final String commandIdentifier)
    {
        final ProvisionCommand hlrCommand = getCommand(context, commandIdentifier);

        return updateHLR(context, hlrCommand);
    }

    /**
     * Updates HLR with the provided command.
     *
     * @param context The operating context.
     * @param hlrCommand The HLR command.
     * @return Returns <code>true</code> if the update was successful,
     *         <code>false</code> otherwise.
     */
    public boolean updateHLR(final Context context, final ProvisionCommand hlrCommand)
    {
        try
        {
            return HlrSupport.updateHlr(context, subscriber_, hlrCommand);
        } catch (Throwable t)
        {
            Logger.minor(context, this, "fail to send to hlrCommand " + hlrCommand.getId() + 
                    " for subscriber " + subscriber_.getId());
        }
        
        return false; 
    }

    /**
     * The subscriber for which to send commands.
     */
    private final Subscriber subscriber_;

}
