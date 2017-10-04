package com.trilogy.app.crm.account;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountCreationTemplateXInfo;


/**
 * Ensure that there is no ACTs for one spid that have same name ( ACT name is unique for each spid ).
 *
 * @author marcio.marques@gmail.com
 */
public class NoDuplicateACTNameForSameSpidCheckingHome extends HomeProxy
{
    /**
     * Creates a new NoDuplicateACTNameForSameSpidCheckingHome decorator.
     *
    * @param context
     * @param delegate The Home to which we delegate.
     */
    public NoDuplicateACTNameForSameSpidCheckingHome(final Context context, final Home delegate)
    {
        super(context, delegate);
    }

    /**
     * {@inheritDoc}
     */
    public Object create(Context ctx, final Object obj) throws HomeException
    {
        if (isNameUniqueForSameSpid(ctx,(AccountCreationTemplate) obj, CREATE_MODE))
        {
            final Object result = super.create(ctx, obj);

            return result;
        }
        
        throw new HomeException("Same name for this spid is existing. Please use different name.");
    }

    /**
     * {@inheritDoc}
     */
    public Object store(Context ctx, final Object obj) throws HomeException
    {
        if (isNameUniqueForSameSpid(ctx,(AccountCreationTemplate) obj, STORE_MODE))
        {
            Object ret = super.store(ctx, obj);

            return ret;
        }
        final HomeException newException = new HomeException(
            "Same name for this spid is existing. Please use different name.");
        throw newException;
    }

    private boolean isNameUniqueForSameSpid(Context ctx, AccountCreationTemplate newAct, int mode)
    {
        AccountCreationTemplate oldAct = null;

        try
        {
            And predicate = new And();
            predicate.add(new EQ(AccountCreationTemplateXInfo.SPID, Integer.valueOf(newAct.getSpid())));
            predicate.add(new EQ(AccountCreationTemplateXInfo.NAME, newAct.getName()));
            
            oldAct = (AccountCreationTemplate) this.find(ctx, predicate);
            
            if (oldAct == null)
            {
                return true;
            }
            switch (mode)
            {
                case CREATE_MODE:
                {
                    return false;
                }
                case STORE_MODE:
                {
                    if (newAct.getIdentifier() == oldAct.getIdentifier())
                    {
                        return true;
                    }
                    return false;
                }
                default:
            {
                return false;
            }
            }
        }
        catch (final Throwable t)
        {
            new MajorLogMsg(this, "Failed to look-up ACT for SPID " + newAct.getSpid() + " and Name "
                + newAct.getName(), t).log(ctx);

            return false;
        }

    }

    private final int CREATE_MODE = 1;

    private final int STORE_MODE = 2;

} // class
