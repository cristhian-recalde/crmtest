// INSPECTED: 23/09/2003 MLAM

package com.trilogy.app.crm.provision;

import com.trilogy.app.crm.bean.externalapp.ExternalAppEnum;
import com.trilogy.app.crm.bean.externalapp.ExternalAppErrorCodeMsg;
import com.trilogy.app.crm.client.alcatel.AlcatelProvisioningException;
import com.trilogy.app.crm.extension.ExtensionAssociationException;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.framework.xhome.auth.bean.User;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.language.Lang;
import com.trilogy.framework.xhome.language.LangXInfo;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.service.blackberry.ServiceBlackberryException;

public class ProvisionAgentException extends AgentException
{

    // REVIEW(maintainability): order of declaration - private data members should be put at the end of the class. MLAM
    private int             resultCode_      = 0;
    private ExternalAppEnum externalApp_;
    private int             srcResultCode_   = 0;
    private String          originalMessage_ = null;
    public static final String WIMAX = "WiMAX";


    // REVIEW(maintainability): order of declaration - public constructor should be placed before public methods. MLAM
    // FIXED(PaulSperneac)
    /**
     * @param msg
     * @param srcResultCode
     * @param resultCode
     * @param key
     */
    public ProvisionAgentException(Context ctx, String msg, int srcResultCode, ExternalAppEnum externalApp)
    {
        this(ctx, msg, srcResultCode, externalApp, null);
    }


    public ProvisionAgentException(Context ctx, String msg, int srcResultCode, ExternalAppEnum externalApp, Throwable t)
    {
        this(msg, srcResultCode,
                ExternalAppSupportHelper.get(ctx).getResultCode(ctx, externalApp), externalApp, t);
    }


    private static String includeException(String msg, Throwable t)
    {
        if (t instanceof AlcatelProvisioningException)
        {
            AlcatelProvisioningException e = (AlcatelProvisioningException) t;
            if (e.getAlcatelResultMessage() != null || e.getAlcatelResultCode() != null)
            {
                msg += " -";
            }
            if (e.getAlcatelResultMessage() != null)
            {
                msg += " " + e.getAlcatelResultMessage();
            }
            if (e.getAlcatelResultCode() != null)
            {
                msg += " (" + e.getAlcatelResultCode() + ")";
            }
        }
        else if (t instanceof ServiceBlackberryException)
        {
            ServiceBlackberryException sbe = (ServiceBlackberryException) t;
            msg += " - " + (sbe.getMessage() != null ? sbe.getMessage() : sbe.getDescription());
        }
        else if (t instanceof ExtensionAssociationException)
        {
            ExtensionAssociationException eae = (ExtensionAssociationException) t;
            if (eae.getDescription()!=null && !eae.getDescription().trim().isEmpty())
            msg += " - " +  eae.getDescription();
        }
        return msg;
    }


    private ProvisionAgentException(String msg, int srcResultCode, int resultCode, ExternalAppEnum externalApp,
            Throwable t)
    {
        this(new MessageMgr(getLanguageContext(), ProvisionAgentException.class), msg, srcResultCode, resultCode, externalApp, t);
    }
        
    private ProvisionAgentException(MessageMgr mmgr, String msg, int srcResultCode, int resultCode, ExternalAppEnum externalApp,
            Throwable t)
    {
        super(mmgr.get(RESULT_DESCRIPTION_KEY, DEFAULT_RESULT_DESCRIPTION_VALUE) + " = " + resultCode + " (" + externalApp.getDescription() + ") -> " + includeException(msg, t), t);
        setResultCode(resultCode);
        setExternalApp(externalApp);
        setSourceResultCode(srcResultCode);
        setOriginalMessage(msg);

    }
    
    private static Context getLanguageContext()
    {
        Context subContext = ContextLocator.locate().createSubContext();
        String language = ExternalAppErrorCodeMsg.DEFAULT_LANGUAGE;

        final User principal = (User) subContext.get(java.security.Principal.class, new User());
        if (principal!=null && principal.getLanguage()!=null && !principal.getLanguage().trim().isEmpty())
        {
            language = principal.getLanguage();
            subContext.put(Lang.class, getLanguage(subContext, language));
        }
        return subContext;
    }

    private static Lang getLanguage(final Context ctx, final String language)
    {
        Lang lang = (Lang) ctx.get(Lang.class, Lang.DEFAULT);
        try
        {
            lang = HomeSupportHelper.get(ctx).findBean(ctx, Lang.class, new EQ(LangXInfo.CODE, language));
        }
        catch (Throwable t)
        {
            LogSupport.minor(ctx, ProvisionAgentException.class.getName(), "Unable to retrieve language " + language + ": " + t.getMessage(), t);
        }
        
        return lang;
    }

    private static String RESULT_DESCRIPTION_KEY = "Result Description";
    private static String DEFAULT_RESULT_DESCRIPTION_VALUE = "Provisioning Result";

    /**
     * Getter for the result code
     * 
     * @return
     */
    public int getResultCode()
    {
        return resultCode_;
    }


    /**
     * Setter for the result code.
     * 
     * @param resultCode
     */
    public void setResultCode(int resultCode)
    {
        resultCode_ = resultCode;
    }


    /**
     * Getter for the key
     * 
     * @return
     */
    public ExternalAppEnum getExternalApp()
    {
        return externalApp_;
    }


    /**
     * Setter for the key
     * 
     * @param key
     */
    public void setExternalApp(ExternalAppEnum externalApp)
    {
        externalApp_ = externalApp;
    }


    /**
     * Getter for the source result code
     * 
     * @return
     */
    public int getSourceResultCode()
    {
        return srcResultCode_;
    }


    /**
     * Setter for the source result code
     * 
     * @param srcResultCode
     */
    public void setSourceResultCode(int srcResultCode)
    {
        srcResultCode_ = srcResultCode;
    }


    public String getOriginalMessage()
    {
        return originalMessage_;
    }


    public void setOriginalMessage(String originalMessage)
    {
        this.originalMessage_ = originalMessage;
    }
}
