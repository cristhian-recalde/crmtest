package com.trilogy.app.crm.home.calldetail;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.calldetail.CallDetail;
import com.trilogy.app.crm.extension.spid.SAPReportSpidExtension;
import com.trilogy.app.crm.extension.spid.SAPReportSpidExtensionXInfo;
import com.trilogy.app.crm.support.NoExceptionHomeQuerySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class CalldetailSAPFieldsSettingHome
extends HomeProxy
{

    public CalldetailSAPFieldsSettingHome(Home home)
    {
        super(home); 
    }
    
    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        CallDetail cdr = (CallDetail) obj;
        
        final com.redknee.framework.license.LicenseMgr lmgr = (com.redknee.framework.license.LicenseMgr) ctx.get(com.redknee.framework.license.LicenseMgr.class);
        if (lmgr.isLicensed(ctx, com.redknee.app.crm.ModelCrmLicenseConstants.SAP_REPORT)) {

            setSAPFields(ctx, cdr);
        }
        return super.create(ctx, cdr);
    }

    
    private void setSAPFields(Context ctx, CallDetail cdr)
    {
        Account acct = NoExceptionHomeQuerySupport.findBean(ctx, Account.class, new EQ(AccountXInfo.BAN, cdr.getBAN()));
        
        AccountCategory acctType = NoExceptionHomeQuerySupport.findBean(ctx,AccountCategory.class, new EQ(AccountCategoryXInfo.IDENTIFIER, Long.valueOf(acct.getType()))); 
        
        if ( acctType != null)
        {    
            cdr.setGlcode2(acctType.getGLCode());
            cdr.setAccountType(acct.getType()); 
        } else 
        {
            new MinorLogMsg(this, "fail to find Account Type " + acct.getType(), null).log(ctx);
            // it is unlike to happen, return in such case
            return; 
 
        }
        
        GLCodeMapping glcode1 = NoExceptionHomeQuerySupport.findBean(ctx,GLCodeMapping.class, new EQ(GLCodeMappingXInfo.GL_CODE, cdr.getGLCode()));
        GLCodeMapping glcode2 = NoExceptionHomeQuerySupport.findBean(ctx,GLCodeMapping.class, new EQ(GLCodeMappingXInfo.GL_CODE, cdr.getGlcode2()));
    
        SAPReportSpidExtension spidConfig = NoExceptionHomeQuerySupport.findBean(ctx,SAPReportSpidExtension.class, new EQ(SAPReportSpidExtensionXInfo.SPID, Integer.valueOf(cdr.getSpid())));
            
        if (glcode1 != null)
        {    
            cdr.setSAPCenterCode1(glcode1.getCenterCode());
            cdr.setSAPCenterType1(glcode1.getCenterType()); 
        } else 
        {
            new MinorLogMsg(this, "fail to find glcode " + cdr.getGLCode(), null).log(ctx);
        }
        if (glcode2 != null)
        { 
            cdr.setSAPCenterCode2(glcode2.getCenterCode());
            cdr.setSAPCenterType2(glcode2.getCenterType()); 
        } else
        {
            new MinorLogMsg(this, "fail to find glcode " + cdr.getGlcode2(), null).log(ctx);
         
        }
        
        // set postingkey
        if (cdr.getCharge()>=0)
        {
             if (spidConfig != null)
            {
                cdr.setSapDocHeader( spidConfig.getSapDocHeader());
                cdr.setDebitPostingKey(spidConfig.getDebitPostingKeyNegative()); 
                cdr.setCreditPostingKey(spidConfig.getDebitPostingKeyPositive()); 
            }else 
            {
                new MinorLogMsg(this, "fail to find SID SAP report extension for spid " + cdr.getSpid(), null).log(ctx);
 
            }
            
        } else 
        {
            if (spidConfig != null)
            {
                cdr.setSapDocHeader( spidConfig.getSapDocHeader());
                cdr.setDebitPostingKey(spidConfig.getCreditPostingKeyNegative()); 
                cdr.setCreditPostingKey(spidConfig.getCreditPostingKeyPositive()); 
            }else 
            {
                new MinorLogMsg(this, "fail to find SID SAP report extension for spid " + cdr.getSpid(), null).log(ctx);
 
            }
        }
    }
        
    
}
