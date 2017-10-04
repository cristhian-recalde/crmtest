package com.trilogy.app.crm.ban;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidHome;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.extension.spid.BANGenerationSpidExtension;
import com.trilogy.app.crm.sequenceId.AccountRollOverNotifier;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;


public class DoryBANGeneration implements BANGenerator
{

    @Override
    public String generateBAN(Context ctx, Account account) throws HomeException
    {
        String prefix = getPrefix(ctx, account.getSpid());
        StringBuffer buf = new StringBuffer(prefix);
        String centerPiece = getNextSequenceId(ctx, account);
        while ((buf.length() + centerPiece.length()) < 7)
        {
            centerPiece = "0" + centerPiece;
        }
        buf.append(centerPiece);
        int postFix = getPostfix(ctx, buf.toString());
        buf.append(postFix);
        return buf.toString();
    }


    public String getNextSequenceId(Context ctx, Account account) throws HomeException
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
        return Long.toString(nextNum);
    }


    private int getPostfix(final Context ctx, final String prefix)
    {
        StringBuffer buf = new StringBuffer("1");
        buf.append(prefix.substring(0, 7));
        int[] productValue = new int[buf.length()];
        for (int i = 0; i < buf.length(); i++)
        {
            StringBuffer charValue = new StringBuffer();
            charValue.append(buf.charAt(i));
            int result = multipliers[i] * Integer.parseInt(charValue.toString());
            productValue[i] = result;
        }
        int sum = 0;
        for (int i = 0; i < productValue.length; i++)
        {
            sum += productValue[i];
        }
        int reminder = sum % 11;
        int checkDigit = 11 - reminder;
        if (checkDigit == 11)
            checkDigit = 1;
        else if (checkDigit == 10)
            checkDigit = 0;
        return checkDigit;
    }


    private String getPrefix(final Context ctx, final int s) throws HomeException
    {
        CRMSpid spid = SpidSupport.getCRMSpid(ctx, s);
        String prefix = new String("");
        /*
         * will only create a new account number if the ban is not set
         */
        for (Object o : spid.getExtensions())
        {
            try
            {
                if (o instanceof BANGenerationSpidExtension)
                {
                    BANGenerationSpidExtension banGenExt = (BANGenerationSpidExtension) o;
                    prefix = banGenExt.getParameters();
                }
            }
            catch (Exception ex)
            {
                new MinorLogMsg(this, " Unable to load the custom generator.  Hence, using the default generator", ex)
                        .log(ctx);
            }
        }
        return prefix;
    }

    private static int multipliers[] =
        {9, 8, 7, 6, 5, 4, 3, 2};
}
