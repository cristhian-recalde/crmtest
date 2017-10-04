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
package com.trilogy.app.crm.notification.template;

import com.trilogy.app.crm.notification.template.NotificationTemplate;

/**
 * This template interface is used for notification templates that indicate some
 * occurrence of a state change.
 * 
 * @author aaron.gourley@redknee.com
 * @since 8.8/9.0
 */
public interface StateChangeNotificationTemplate extends NotificationTemplate
{
    public int getPreviousState();

    public void setPreviousState(int previousState) throws IllegalArgumentException;

    public int getNewState();

    public void setNewState(int newState) throws IllegalArgumentException;
}
