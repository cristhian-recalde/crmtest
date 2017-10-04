/**
 * 
 */
package com.trilogy.app.crm.util;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.dunning.DunningProcessException;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.LogSupport;


/**
 * @author hmurumkar
 * @since
 */
public class CrmCommonUtil
{
    public static Class klass = CrmCommonUtil.class;
    
    /**
     * Retrieves the SPID for an account.
     *
     * @param context
     * @param account
     * @return
     */
    public static CRMSpid retrieveSpid(final Context context, final Account account) throws DunningProcessException
    {
        CRMSpid spid = (CRMSpid) context.get(CRMSpid.class);

        if (spid==null || spid.getSpid()!=account.getSpid())
        {
            try
            {
                spid = HomeSupportHelper.get(context).findBean(context, CRMSpid.class,
                        new EQ(CRMSpidXInfo.ID, Integer.valueOf(account.getSpid())));
            }
            catch (final HomeException exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve SPID '");
                cause.append(account.getSpid());
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append(" for account '");
                sb.append(account.getBAN());
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, klass, sb.toString(), exception);
                throw new DunningProcessException(cause.toString(), exception);
            }
        }

        if (spid == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("SPID '");
            cause.append(account.getSpid());
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append(" for account '");
            sb.append(account.getBAN());
            sb.append("'");
            LogSupport.minor(context, klass, sb.toString());
            throw new DunningProcessException(cause.toString());
        }
        return spid;
    }
    
    /**
     * Retrieves the SPID for an account.
     *
     * @param context
     * @param spid
     * @return
     */
    public static CRMSpid retrieveSpid(final Context context, int spid) throws HomeException
    {
        CRMSpid crmSpid = (CRMSpid) context.get(CRMSpid.class);

        if (crmSpid==null || crmSpid.getSpid()!=spid)
        {
            try
            {
                crmSpid = HomeSupportHelper.get(context).findBean(context, CRMSpid.class,
                        new EQ(CRMSpidXInfo.ID, Integer.valueOf(spid)));
            }
            catch (final HomeException exception)
            {
                StringBuilder cause = new StringBuilder();
                cause.append("Unable to retrieve SPID '");
                cause.append(spid);
                cause.append("'");
                StringBuilder sb = new StringBuilder();
                sb.append(cause);
                sb.append("': ");
                sb.append(exception.getMessage());
                LogSupport.minor(context, klass, sb.toString(), exception);
                throw new HomeException(cause.toString(), exception);
            }
        }

        if (crmSpid == null)
        {
            StringBuilder cause = new StringBuilder();
            cause.append("SPID '");
            cause.append(spid);
            cause.append("' not found");
            StringBuilder sb = new StringBuilder();
            sb.append(cause);
            sb.append("'");
            LogSupport.minor(context, klass, sb.toString());
            throw new HomeException(cause.toString());
        }
        return crmSpid;
    }

}
