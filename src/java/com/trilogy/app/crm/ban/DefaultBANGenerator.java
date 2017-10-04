package com.trilogy.app.crm.ban;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.sequenceId.AccountRollOverNotifier;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


public class DefaultBANGenerator implements BANGenerator
{

    @Override
    public String generateBAN(Context ctx, final Account account) throws HomeException
    {
        // look for the next BAN
        final Home spidHome = (Home) ctx.get(CRMSpidHome.class);
        if (spidHome == null)
        {
            throw new HomeException("System Error: CRMSpidHome does not exist in context");
        }
        final CRMSpid spid = (CRMSpid) spidHome.find(ctx, Integer.valueOf(account.getSpid()));
        if (spid == null)
        {
            throw new HomeException(
                    "Configuration Error: Service Provider is mandatory, make sure it exists before continuing");
        }
        final long nextNum = IdentifierSequenceSupportHelper.get(ctx).getNextIdentifier(ctx, IdentifierEnum.ACCOUNT_ID,
                spid.getId(), new AccountRollOverNotifier(ctx, spid));
        return Integer.toString(spid.getId()) + Long.toString(nextNum);
    }
}
