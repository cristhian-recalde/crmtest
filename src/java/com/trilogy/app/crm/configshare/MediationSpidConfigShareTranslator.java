package com.trilogy.app.crm.configshare;

import java.util.ArrayList;
import java.util.Collection;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.support.IdentitySupport;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.LogSupport;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.CRMSpidIdentitySupport;
import com.trilogy.app.crm.bean.CRMSpidXInfo;
import com.trilogy.app.crm.support.HomeSupportHelper;


public class MediationSpidConfigShareTranslator implements ConfigChangeRequestTranslator
{
    private static ConfigChangeRequestTranslator instance_ = null;
    public static ConfigChangeRequestTranslator instance()
    {
        if (instance_ == null)
        {
            instance_ = new MediationSpidConfigShareTranslator();
        }
        return instance_;
    }

    protected MediationSpidConfigShareTranslator()
    {
    }

    public Collection<? extends AbstractBean> translate(Context ctx, ConfigChangeRequest request) throws ConfigChangeRequestTranslationException
    {
        if (LogSupport.isDebugEnabled(ctx))
        {
            new DebugLogMsg(this, "Using CRMSpidConfigShareTranslator for the CRMSpid,  overriding default interface",
                    null).log(ctx);
        }
        Collection<CRMSpid> list = new ArrayList<CRMSpid>();
        try
        {
            IdentitySupport identitySupport = CRMSpidIdentitySupport.instance();
            Object spid = identitySupport.fromStringID(request.getBeanId());
            CRMSpid createNewBean = HomeSupportHelper.get(ctx).findBean(ctx, CRMSpid.class, spid);
            if (createNewBean == null)
            {
                createNewBean = (CRMSpid) identitySupport.toBean(spid);
            }
            Collection<IndividualUpdate> updates = request.getUpdateRequest().values();
            for (IndividualUpdate update : updates)
            {
                if (CRMSpidXInfo.ROAMING_TAX_AUTHORITY.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.ROAMING_TAX_AUTHORITY.fromString(update.getNewValue());
                    CRMSpidXInfo.ROAMING_TAX_AUTHORITY.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.TAX_AUTHORITY.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.TAX_AUTHORITY.fromString(update.getNewValue());
                    CRMSpidXInfo.TAX_AUTHORITY.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.ENABLE_CHARGING_COMPONENTS.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.ENABLE_CHARGING_COMPONENTS.fromString(update.getNewValue());
                    CRMSpidXInfo.ENABLE_CHARGING_COMPONENTS.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.CHARGING_COMPONENTS_CONFIG.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.CHARGING_COMPONENTS_CONFIG.fromString(update.getNewValue());
                    CRMSpidXInfo.CHARGING_COMPONENTS_CONFIG.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.MT_CALL_TYPE.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.MT_CALL_TYPE.fromString(update.getNewValue());
                    CRMSpidXInfo.MT_CALL_TYPE.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.MT_BILL_OPTION_IGNORE.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.MT_BILL_OPTION_IGNORE.fromString(update.getNewValue());
                    CRMSpidXInfo.MT_BILL_OPTION_IGNORE.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.NAME.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.NAME.fromString(update.getNewValue());
                    CRMSpidXInfo.NAME.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.IMSI_PREFIX.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.IMSI_PREFIX.fromString(update.getNewValue());
                    CRMSpidXInfo.IMSI_PREFIX.set(createNewBean, newValue);
                }
                else if (CRMSpidXInfo.IMSI_PREFIX2.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.IMSI_PREFIX2.fromString(update.getNewValue());
                    CRMSpidXInfo.IMSI_PREFIX2.set(createNewBean, newValue);
                }else if (CRMSpidXInfo.AUTOPUSH_NOTES_TO_DCRM.getName().equals(update.getFieldName()))
                {
                    Object newValue = CRMSpidXInfo.AUTOPUSH_NOTES_TO_DCRM.fromString(update.getNewValue());
                    CRMSpidXInfo.AUTOPUSH_NOTES_TO_DCRM.set(createNewBean, newValue);
                }
            }
            list.add(createNewBean);
        }
        catch (HomeException e)
        {
            throw new ConfigChangeRequestTranslationException("Exception occurred retrieving spid " + request.getBeanId(), e);
        }
        return list;
    }
}
