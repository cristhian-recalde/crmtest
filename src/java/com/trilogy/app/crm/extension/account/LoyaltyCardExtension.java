/*
 * This code is a protected work and subject to domestic and international copyright
 * law(s). A complete listing of authors of this work is readily available. Additionally,
 * source code is, by its very nature, confidential information and inextricably contains
 * trade secrets and other information proprietary, valuable and sensitive to Redknee. No
 * unauthorized use, disclosure, manipulation or otherwise is permitted, and may only be
 * used in accordance with the terms of the license agreement entered into with Redknee
 * Inc. and/or its subsidiaries.
 * 
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.extension.account;

import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.app.crm.extension.ExtensionInstallationException;


/**
 * TODO
 * 
 * @author asim.mahmood@redknee.com
 */
public class LoyaltyCardExtension extends AbstractLoyaltyCardExtension
{

   
    private static final long serialVersionUID = 1L;
    
    
    public String getSummary(final Context ctx)
    {
        return "LoyaltyCardExtension";
    }


    public void install(final Context ctx) throws ExtensionInstallationException
    {
        throw new ExtensionInstallationException("Cannot add Loyalty Card association from GUI", false, true);
    }


    public void update(final Context ctx) throws ExtensionInstallationException
    {
        throw new ExtensionInstallationException("Cannot modify Loyalty Card association from GUI", false, true);
    }


    public void uninstall(final Context ctx) throws ExtensionInstallationException
    {
        throw new ExtensionInstallationException("Cannot remove Loyalty Card association from GUI", false, true);
    }


    /**
     * {@inheritDoc}
     */
    public void validate(final Context ctx) throws IllegalStateException
    {
        final CompoundIllegalStateException ex = new CompoundIllegalStateException();

        //ex.throwAll();
    }
}
