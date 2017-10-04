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
package com.trilogy.app.crm.util;

import java.io.Serializable;

import com.trilogy.framework.xhome.language.MessageMgr;


/**
 * Provides a convenient method of getting configurable text from a message
 * manager.
 *
 * @author gary.anderson@redknee.com
 */
public final
class MessageManagerText implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates the new MessageManagerText text with the given key and default text.
     *
     * @param key The key used to look up the text in the message manager.
     * @param defaultText The default text to use should the entry not be found
     * in the message manager.
     */
    public MessageManagerText(final String key, final String defaultText)
    {
        key_ = key;
        defaultText_ = defaultText;
    }


    /**
     * Gets the configured text from the given message manager.
     *
     * @param manager The manager from which to get the text.
     *
     * @return The configured text.
     */
    public String get(final MessageMgr manager)
    {
        if (null == manager)
        {
            return defaultText_;
        }
        return manager.get(key_, defaultText_);
    }

    
    /**
     * Gets the configured text from the given message manager using a format.
     *
     * @param manager The manager from which to get the text.
     * @param values The values to plug into the format.
     *
     * @return The configured text.
     */
    public String get(final MessageMgr manager, final Object[] values)
    {
        if (null == manager)
        {
            return defaultText_;
        }
        return manager.get(key_, defaultText_, values);
    }


    /**
     * The key used to look up the text in the message manager.
     */
    private final String key_;

    /**
     * The default text to use should the entry not be found in the message
     * manager.
     */
    private final String defaultText_;

} // class
