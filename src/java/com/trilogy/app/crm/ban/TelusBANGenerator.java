package com.trilogy.app.crm.ban;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;

public class TelusBANGenerator implements BANGenerator
{

    @SuppressWarnings("deprecation")
    @Override
    public String generateBAN(Context ctx, Account account) throws HomeException
    {

        final long nextNum = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx, IdentifierEnum.ACCOUNT_ID, null);
        synchronized(banFormatString)
        {
            return String.format(banFormatString, nextNum);
        }    
    }

    
    final  String banFormatString = "%014d"; 
}
