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
package com.trilogy.app.crm;

/**
 * This interface defines constants that should have been part of Framework code.
 *
 * @author victor.stratan@redknee.com
 */
public interface CommonFramework
{
    /**
     * Used to set and determine the edit mode for the web control.
     * Takes values from com.redknee.framework.xhome.webcontrol.OutputWebControl
     */
    public static String MODE = "MODE";

    /**
     * Used to indicate if the output should be presented in table view. boolean.
     */
    public static String TABLE_MODE = "TABLE_MODE";

    /**
     * This is actualy a CRM constant, but since it is related to the other constants here
     * it ended up in this interface.
     */
    public static Object REAL_MODE = "REALMODE";
}