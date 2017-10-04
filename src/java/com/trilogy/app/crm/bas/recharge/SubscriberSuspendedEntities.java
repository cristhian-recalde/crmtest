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
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.bas.recharge;

import java.util.HashMap;

/**
 * @author rchen
 *
 */
@SuppressWarnings("unchecked")
public final class SubscriberSuspendedEntities
{
    
    final boolean hasSuspensionInThisCharge()
    {
        return (suspendedServices_!=null && suspendedServices_.size()>0) ||
            (suspendedBundles_!=null && suspendedBundles_.size()>0) ||
            (suspendedAuxServices_!=null && suspendedAuxServices_.size()>0) ||
            (suspendedPackages_!=null && suspendedPackages_.size()>0);
    }

    /**
     * @return the suspendedServices_
     */
    public final HashMap getSuspendedServices()
    {
        if (suspendedServices_==null)
        {
            suspendedServices_ = new HashMap();
        }
        return suspendedServices_;
    }
    
    /**
     * @return the suspendedBundles_
     */
    public final HashMap getSuspendedBundles()
    {
        if (suspendedBundles_==null)
        {
            suspendedBundles_ = new HashMap();
        }
        return suspendedBundles_;
    }
    
    /**
     * @return the suspendedAuxServices_
     */
    public final HashMap getSuspendedAuxServices()
    {
        if (suspendedAuxServices_==null)
        {
            suspendedAuxServices_ = new HashMap();
        }
        return suspendedAuxServices_;
    }
    
    /**
     * @return the suspendedPackages_
     */
    public final HashMap getSuspendedPackages()
    {
        if (suspendedPackages_==null)
        {
            suspendedPackages_ = new HashMap();
        }
        return suspendedPackages_;
    }
    
    /**
     * The services that are suspended for this charge.
     **/
    private HashMap          suspendedServices_ = null;
     
    /**
     * The bundles that are suspended for this charge.
     **/
     private HashMap          suspendedBundles_ = null;
     
    /**
     * The auxiliary services that are suspended for this charge.
     **/
     private HashMap          suspendedAuxServices_ = null;
     
    /**
     * The packages that are suspended for this charge.
     **/
     private HashMap          suspendedPackages_ = null;
     
}
