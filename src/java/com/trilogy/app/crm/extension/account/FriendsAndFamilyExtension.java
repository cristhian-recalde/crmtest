package com.trilogy.app.crm.extension.account;

import java.util.Collection;
import java.util.Iterator;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCreationTemplate;
import com.trilogy.app.crm.bean.AccountHome;
import com.trilogy.app.crm.bean.ClosedUserGroup;
import com.trilogy.app.crm.extension.ExtensionInstallationException;
import com.trilogy.app.crm.support.AutoCugProvisioningException;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.FriendsAndFamilyExtensionSupport;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class FriendsAndFamilyExtension extends AbstractFriendsAndFamilyExtension
{
    /**
     * @{inheritDoc}
     */
    @Override
    public String getSummary(Context ctx)
    {
        return FriendsAndFamilyExtensionXInfo.CUG_ID.getLabel() + "=" + this.getCugID();
    }


    public void install(Context ctx) throws ExtensionInstallationException
    {
        ClosedUserGroup cug = null;
        try
        {
            Account account = getAccount(ctx);
            if( account == null )
            {
                throw new ExtensionInstallationException("Unable to install " + this.getName(ctx) + " extension.  No account found with BAN=" + this.getBAN(), false);
            }
            
            if (this.getCugTemplateID() >= 0)
            {
                /*
                 * TT #8112700050: Adding the account object being saved to the
                 * context, so that the correct subscriber object will be
                 * updated.
                 */
                cug = ClosedUserGroupSupport.createCugForAccount(ctx, account, this.getCugTemplateID(), this.getCugOwnerMsisdn()); 
                this.setCugID(cug.getID());   
                if (cug.getSmsNotifyUser() != null && cug.getSmsNotifyUser().length() > 0)
                {
                    this.setSmsNotificationMSISDN(cug.getSmsNotifyUser());                    
                }
                if (cug.getOwnerMSISDN() != null && cug.getOwnerMSISDN().length() > 0)
                {
                    this.setCugOwnerMsisdn(cug.getOwnerMSISDN());
                }
            }
            else
            {
                throw new ExtensionInstallationException("Unable to install " + this.getName(ctx) + " extension.  No CUG Template selected.", false);
            }
        }
        catch (AutoCugProvisioningException e)
        {
            new DebugLogMsg(this, e.getClass().getSimpleName() + " occurred in " + FriendsAndFamilyExtension.class.getSimpleName() + ".install(): " + e.getMessage(), e).log(ctx);
            handleProvisioningException(ctx, e);
        }
    }

    public void update(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            Account account = getAccount(ctx);
            ClosedUserGroup cug = null;
            if (account == null)
            {
                throw new ExtensionInstallationException("Unable to update " + this.getName(ctx)
                        + " extension.  No account found with BAN=" + this.getBAN(), false);
            }
            
            FriendsAndFamilyExtension oldFnf = FriendsAndFamilyExtensionSupport.getFnfExtension(ctx, account.getBAN());
            if (oldFnf == null)
            {
                throw new ExtensionInstallationException("Unable to update " + this.getName(ctx)
                        + " extension.  No FriendsAndFamily extension found for BAN=" + this.getBAN(), false);
            }
            
            if (oldFnf.getBAN() == this.getBAN()
                    && oldFnf.getCugTemplateID() == this.getCugTemplateID() 
                    && oldFnf.getSmsNotificationMSISDN() == this.getSmsNotificationMSISDN())
            {
                // Nothing to update
                return;
            }
            // Setting CUG ID to the old CUG ID - cug id is a read only field in
            // extension and it is reset to -1 during any update from GUI.
            this.setCugID(oldFnf.getCugID());
            if (this.getCugTemplateID() >= 0)
            {
                /*
                 * TT #8112700050: Adding the account object being saved to the context,
                 * so that the correct subscriber object will be updated.
                 */
                Context appCtx = (Context) ctx.get("app");
                if (appCtx.getBoolean("newPricePlanChange"))
                {
                    cug = ClosedUserGroupSupport.updateCugForAccount(ctx, account, oldFnf.getCugID(),
                        oldFnf.getCugTemplateID(), oldFnf.getSmsNotificationMSISDN());
                }
                else
                {
                    cug = ClosedUserGroupSupport.updateCugForAccount(ctx, account, this.getCugID(),
                            this.getCugTemplateID(), this.getSmsNotificationMSISDN());
                }
                this.setCugID(cug.getID());
                if (cug.getSmsNotifyUser() != null && cug.getSmsNotifyUser().length() > 0)
                {
                    this.setSmsNotificationMSISDN(cug.getSmsNotifyUser());
                }
                if (cug.getOwnerMSISDN() != null && cug.getOwnerMSISDN().length() > 0)
                {
                    this.setCugOwnerMsisdn(cug.getOwnerMSISDN());
                }
                this.setCugTemplateID(oldFnf.getCugTemplateID());
            }
            else
            {
                this.setCugTemplateID(oldFnf.getCugTemplateID());
            }
        }
        catch (AutoCugProvisioningException e)
        {
            new DebugLogMsg(this, AutoCugProvisioningException.class.getSimpleName() + " occurred in "
                    + FriendsAndFamilyExtension.class.getSimpleName() + ".update(): " + e.getMessage(), e).log(ctx);
            handleProvisioningException(ctx, e);
        }
        catch (Exception e)
        {
            new DebugLogMsg(this, Exception.class.getSimpleName() + " occurred in "
                    + FriendsAndFamilyExtension.class.getSimpleName() + ".update(): " + e.getMessage(), e).log(ctx);
            throw new ExtensionInstallationException("Exception occurred while extension update.", false);
        }
    }

    public void uninstall(Context ctx) throws ExtensionInstallationException
    {
        try
        {
            Account account = getAccount(ctx);
            if( account == null )
            {
                throw new ExtensionInstallationException("Unable to uninstall " + this.getName(ctx) + " extension.  No account found with BAN=" + this.getBAN(), false);
            }

            if( this.getCugID() != DEFAULT_CUGID )
            {
                /*
                 * TT #8112700050: Adding the account object being saved to the
                 * context, so that the correct subscriber object will be
                 * updated.
                 */
                Context subCtx = ctx.createSubContext();
                subCtx.put(Account.class, account);

                ClosedUserGroupSupport.removeCugForAccount(subCtx, account, this.getCugID());
                this.setCugID(DEFAULT_CUGID);   
            }   
        }
        catch (AutoCugProvisioningException e)
        {
            new DebugLogMsg(this, AutoCugProvisioningException.class.getSimpleName() + " occurred in " + FriendsAndFamilyExtension.class.getSimpleName() + ".uninstall(): " + e.getMessage(), e).log(ctx);
            handleProvisioningException(ctx, true, e);
        }
    }


    public void validate(Context context) throws IllegalStateException
    {        
        CompoundIllegalStateException exception = new CompoundIllegalStateException();
        
        Object parentBean = getParentBean(context);
        if( parentBean instanceof AccountCreationTemplate )
        {
            if (getCugTemplateID()<0)
            {
                exception.thrown(new IllegalPropertyArgumentException(FriendsAndFamilyExtensionXInfo.CUG_TEMPLATE_ID,
                        "CUG Template required."));
            }
        }
        
        exception.throwAll();
    }
    

    private void handleProvisioningException(Context ctx, AutoCugProvisioningException e) throws ExtensionInstallationException
    {
        handleProvisioningException(ctx, false, e);
    }
    
    
    private void handleProvisioningException(Context ctx, boolean isDeprovisioning, AutoCugProvisioningException e) throws ExtensionInstallationException
    {
        ClosedUserGroup cug;
        final String message;
        final boolean isUpdated;
        
        cug = e.getReturnCug();
        if( cug != null )
        {
            this.setCugID(cug.getID());
            
            isUpdated = true;
            
            // Partial failure.  Some MSISDNs could not be added to the CUG
            Collection msisdnFailureList = e.getMsisdnFailureList();
            if( msisdnFailureList != null )
            {
                message = "Error occurred " + (isDeprovisioning ? "removing" : " adding") + " the following subscribers to CUG with ID=" + cug.getID() + ": " + msisdnFailureList;
                new MinorLogMsg(this, message, null).log(ctx);
            }
            else
            {
                message = "Error occurred " + (isDeprovisioning ? "de" : "") + "provisioning CUG with ID=" + cug.getID() + ": " + e.getMessage();
                new MajorLogMsg(this, message, null).log(ctx);
            }
        }
        else
        {
            // CUG creation failed completely
            isUpdated = false;
            message = "Error occurred " + (isDeprovisioning ? "de" : "") + "provisioning CUG: " + e.getMessage();
            new MajorLogMsg(this, message, null).log(ctx);
        }
        
        throw new ExtensionInstallationException(message, e, isUpdated);
    }
}
