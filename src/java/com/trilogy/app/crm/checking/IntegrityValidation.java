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
package com.trilogy.app.crm.checking;

import com.trilogy.app.crm.util.SubscriberProcessor;


/**
 * Extends the concept of a subscriber processor to an integrity validator by
 * adding options for making repairs and logging progress.
 *
 * @author gary.anderson@redknee.com
 */
public
interface IntegrityValidation
    extends SubscriberProcessor
{
    /**
     * Gets the MessageHandler used to log messages.
     *
     * @return The MessageHandler used to log messages.
     */
    MessageHandler getMessageHandler();


    /**
     * Sets the MessageHandler used to log messages.
     *
     * @param handler The MessageHandler used to log messages.
     */
    void setMessageHandler(MessageHandler handler);


    /**
     * Indicates whether or not rapairs should be made as a result of
     * identifying integrity problems.
     *
     * @return True if repairs should be made as a result of identifying
     * integrity problems; false otherwise.
     */
    boolean isRepairEnabled();


    /**
     * Sets whether or not rapairs should be made as a result of identifying
     * integrity problems.
     *
     * @param enabled True if repairs should be made as a result of identifying
     * integrity problems; false otherwise.
     */
    void setRepairEnabled(boolean enabled);


    /**
     * Print the results of processing subscribers to the current
     * MessageHandler.
     */
    void printResults();

} // interface
