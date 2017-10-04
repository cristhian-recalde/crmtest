package com.trilogy.app.crm.api.rmi.previewfees;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.api.CRMExceptionFault;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateCriteria;
import com.trilogy.util.crmapi.wsdl.v3_0.types.subscription.SubscriptionUpdateFees;


public interface PreviewUpdateFees
{

    public SubscriptionUpdateFees getUpdateFees(final Context ctx, final SubscriptionUpdateCriteria[] criteria,
            final GenericParameter[] parameters) throws CRMExceptionFault;
}
