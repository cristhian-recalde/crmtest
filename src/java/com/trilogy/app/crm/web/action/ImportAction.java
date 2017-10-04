/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright &copy; Redknee Inc. and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.web.action;

import com.trilogy.framework.xhome.web.action.*;
import java.security.Permission;


/**
 * Provides a link to the "CUG Import" screen.
 *
 * @author jimmy.ng@redknee.com
 */
public class ImportAction
    extends SimpleWebAction
{
    /**
     * Create a new ImportAction.
     */
    public ImportAction()
    {
        super("cugImport", "Import");
    }


    /**
     * Create a new ImportAction with the given permission.
     *
     * @param permission The permission required to use the action.
     */
    public ImportAction(final Permission permission)
    {
        this();
        setPermission(permission);
    }
} //class
