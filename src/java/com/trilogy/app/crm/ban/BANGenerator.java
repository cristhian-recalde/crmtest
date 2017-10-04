package com.trilogy.app.crm.ban;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;


public interface BANGenerator
{
    public String generateBAN(final Context ctx, final Account account) throws HomeException;
}
