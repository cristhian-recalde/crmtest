package com.trilogy.app.crm.taxation;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import billsoft.eztax.ZipAddress;

/**
 * Interface for Tax Adapter Support
 * Added during US Tax Support system
 *  
 * @author bdhavalshankh
 * @since 9.6.0
 *
 */

public interface TaxAdapter
{
    public long calculatePaymentTax(Context ctx, int spid, String msisdn, long amount, ZipAddress zipAddress) throws HomeException;
}
