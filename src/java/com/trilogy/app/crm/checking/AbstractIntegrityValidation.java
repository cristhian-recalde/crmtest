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

import com.trilogy.app.crm.bean.CheckTypeEnum;
import com.trilogy.app.crm.util.SubscriberProcessor;


/**
 * Provides a base from which to derive new classes of IntegrityValidation.
 *
 * @author gary.anderson@redknee.com
 */
public abstract
class AbstractIntegrityValidation
    implements IntegrityValidation, SubscriberProcessor
{
    /**
     * Creates a new AbstractIntegrityValidation.  The default MessageHandler is
     * a SimpleMessageHandler, and the default repair option is disabled.
     */
    public AbstractIntegrityValidation()
    {
        this(new SimpleMessageHandler(), false);
    }


    /**
     * Creates a new AbstractIntegrityValidation.
     *
     * @param handler The MessageHandler used to log messages.
     * @param repair True if repairs should be made as a result of identifying
     * integrity problems; false otherwise.
     */
    public AbstractIntegrityValidation(final MessageHandler handler, final boolean repair)
    {
        handler_ = handler;
        repair_ = repair;
    }


    /**
     * Gets the MessageHandler used to log messages.
     *
     * @return The MessageHandler used to log messages.
     */
    public final MessageHandler getMessageHandler()
    {
        return handler_;
    }


    /**
     * Sets the MessageHandler used to log messages.
     *
     * @param handler The MessageHandler used to log messages.
     */
    public final void setMessageHandler(final MessageHandler handler)
    {
        handler_ = handler;
    }


    /**
     * Indicates whether or not rapairs should be made as a result of
     * identifying integrity problems.
     *
     * @return True if repairs should be made as a result of identifying
     * integrity problems; false otherwise.
     */
    public final boolean isRepairEnabled()
    {
        return repair_;
    }


    /**
     * Sets whether or not rapairs should be made as a result of identifying
     * integrity problems.
     *
     * @param enabled True if repairs should be made as a result of identifying
     * integrity problems; false otherwise.
     */
    public final void setRepairEnabled(final boolean enabled)
    {
        repair_ = enabled;
    }


    /**
     * Prints a message to the current message handler, if one is set.
     *
     * @param message The message to pass to the handler.
     */
    protected void print(final String message)
    {
        if (getMessageHandler() != null)
        {
            getMessageHandler().print(message);
        }
    }

    /**
     * Gets the IntegrityValidation required for the given type of task.
     *
     * @param type The type of the task to be performed.
     */
    static public IntegrityValidation getValidator(final CheckTypeEnum type)
    {
        final IntegrityValidation validator;

        switch (type.getIndex())
        {
             case CheckTypeEnum.ECP_INDEX:
            {
                validator = new ECPCheckFixer();
                break;
            }
             case CheckTypeEnum.ABM_INDEX:
            {
                validator = new ABMCheckFixer();
                break;
            }
             case CheckTypeEnum.CRM1_INDEX:
             {
            	 validator = new SubscriberPricePlanCheckFixer();
            	 break;
             }
             case CheckTypeEnum.SMSB_INDEX:
             {
            	 validator = new SMSBCheckFixer();
            	 break;
             }
            default:
            {
                // TODO - 2004-10-18 - Throw a custom exception here.
                validator = null;
            }
        }

        return validator;
    }

    
    /**
     * The MessageHandler used to log messages.
     */
    private MessageHandler handler_;

    /**
     * True if repairs should be made as a result of identifying integrity
     * problems; false otherwise.
     */
    private boolean repair_;

} // interface
