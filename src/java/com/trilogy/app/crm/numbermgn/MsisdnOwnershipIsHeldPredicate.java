package com.trilogy.app.crm.numbermgn;

import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnOwnership;
import com.trilogy.app.crm.bean.MsisdnStateEnum;
import com.trilogy.app.crm.support.MsisdnSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.filter.Predicate;
import com.trilogy.framework.xhome.visitor.AbortVisitException;

public class MsisdnOwnershipIsHeldPredicate
    implements Predicate
{
    public boolean f(Context ctx, Object o)
        throws AbortVisitException
    {
        MsisdnOwnership ownership = (MsisdnOwnership)o;
        try
        {
            Msisdn msisdn = MsisdnSupport.getMsisdn(ctx, ownership.getOriginalMsisdn());
            return MsisdnStateEnum.HELD.equals(msisdn.getState());
        }
        catch(Exception e)
        {
            return false;
        }
    }
}