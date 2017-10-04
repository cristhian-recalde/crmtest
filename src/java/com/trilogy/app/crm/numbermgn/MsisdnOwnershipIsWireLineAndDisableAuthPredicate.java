package com.trilogy.app.crm.numbermgn;

import com.trilogy.app.crm.bean.AcquiredMsisdnPINManagement;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

public class MsisdnOwnershipIsWireLineAndDisableAuthPredicate
    implements Predicate
{
    public boolean f(Context ctx, Object o)
        throws AbortVisitException
    {
        AcquiredMsisdnPINManagement acquireMsisdn = (AcquiredMsisdnPINManagement)o;
        try
        {
            return (acquireMsisdn.isWireLineSubscription() && acquireMsisdn.isAuthenticated());
        }
        catch(Exception e)
        {
            return false;
        }
    }
}