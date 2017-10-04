package com.trilogy.app.crm.home.account.extension;

import java.util.ArrayList;
import java.util.List;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Adapter;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;

import com.trilogy.app.crm.extension.account.AbstractFriendsAndFamilyExtension;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.bean.MsisdnStringHolder;
import com.trilogy.app.crm.extension.account.FriendsAndFamilyExtension;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;


public class FriendsAndFamilyExtensionAdapter implements Adapter
{
    static final long serialVersionUID = 1L;
    
    public Object unAdapt(Context ctx, Object obj) throws HomeException
    {
        FriendsAndFamilyExtension ext = (FriendsAndFamilyExtension) obj;
        
        // CUG Template ID needs to be stored in the ACT so it can't be transient.  We don't want to store it in the account's extension though.
        // ext.setCugTemplateID(AbstractFriendsAndFamilyExtension.DEFAULT_CUGTEMPLATEID);
        
        return ext;
    }

    public Object adapt(Context ctx, Object obj) throws HomeException
    {
        FriendsAndFamilyExtension ext = (FriendsAndFamilyExtension) obj;
        
        if( ext.getCugID() != AbstractFriendsAndFamilyExtension.DEFAULT_CUGID )
        {
            try
            {
                final  ClosedUserGroup cug = ClosedUserGroupSupport.getCug(ctx, ext.getCugID());
                if( cug != null )
                {
                    // TODO 2009-02-18 enable when new CUG structure is ported
                    ext.setCugTemplateID(cug.getCugTemplateID());
                    ext.setSmsNotificationMSISDN(cug.getSmsNotifyUser());
                    ext.setCugOwnerMsisdn(cug.getOwnerMSISDN());
                    ext.setSpid(cug.getSpid());
                }
            }
            catch (Exception e)
            {
                new MinorLogMsg(this, "Failed to get Cug instance " + ext.getCugID(), e).log(ctx);
            }
        }
        
        return ext;
    }
}
