package com.trilogy.app.crm.home.transaction;

import com.trilogy.app.crm.bean.Account;
import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryXInfo;
import com.trilogy.app.crm.bean.AccountXInfo;
import com.trilogy.app.crm.bean.AdjustmentInfo;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeXInfo;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingXInfo;
import com.trilogy.app.crm.bean.Transaction;
import com.trilogy.app.crm.support.NoExceptionHomeQuerySupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xlog.log.MinorLogMsg;

public class TransactionSAPFieldsSettingHome
extends HomeProxy
{
    
    public TransactionSAPFieldsSettingHome(Home home)
    {
        super(home); 
    }

    @Override
    public Object create(Context ctx, Object obj) throws HomeException, HomeInternalException
    {
        Transaction cdr = (Transaction) obj;
        
        final com.redknee.framework.license.LicenseMgr lmgr = (com.redknee.framework.license.LicenseMgr) ctx.get(com.redknee.framework.license.LicenseMgr.class);
        
        if (lmgr.isLicensed(ctx, com.redknee.app.crm.ModelCrmLicenseConstants.SAP_REPORT)) 
        {

            setSAPFields(ctx, cdr);
        }
        
        return super.create(ctx, cdr);
    }

    
    private void setSAPFields(Context ctx, Transaction trans)
    {
        Account acct = NoExceptionHomeQuerySupport.findBean(ctx, Account.class, new EQ(AccountXInfo.BAN, trans.getBAN()));
        
        AccountCategory acctType = NoExceptionHomeQuerySupport.findBean(ctx,AccountCategory.class, new EQ(AccountCategoryXInfo.IDENTIFIER, Long.valueOf(acct.getType()))); 
        
        if ( acctType != null)
        {    
            trans.setGlcode2(acctType.getGLCode());
            trans.setAccountType(acct.getType()); 
        } else 
        {
            new MinorLogMsg(this, "fail to find Account Type " + acct.getType(), null).log(ctx);
            // it is unlike to happen, return in such case
            return; 
 
        }
        
        GLCodeMapping glcode1 = NoExceptionHomeQuerySupport.findBean(ctx,GLCodeMapping.class, new EQ(GLCodeMappingXInfo.GL_CODE, trans.getGLCode()));
        GLCodeMapping glcode2 = NoExceptionHomeQuerySupport.findBean(ctx,GLCodeMapping.class, new EQ(GLCodeMappingXInfo.GL_CODE, trans.getGlcode2()));
    
        final AdjustmentType adjustmentType = NoExceptionHomeQuerySupport.findBean(ctx, AdjustmentType.class, new EQ(AdjustmentTypeXInfo.CODE, Integer.valueOf(trans.getAdjustmentType())));
        AdjustmentInfo info = null;
         
        if (adjustmentType != null)
        {    
            info =    (AdjustmentInfo) adjustmentType.getAdjustmentSpidInfo().get(trans.getSpid());
        }
        
        
        if (glcode1 != null)
        {    
            trans.setSAPCenterCode1(glcode1.getCenterCode());
            trans.setSAPCenterType1(glcode1.getCenterType()); 
        } else 
        {
            new MinorLogMsg(this, "fail to find glcode " + trans.getGLCode(), null).log(ctx);
        }
        if (glcode2 != null)
        { 
            trans.setSAPCenterCode2(glcode2.getCenterCode());
            trans.setSAPCenterType2(glcode2.getCenterType()); 
        } else
        {
            new MinorLogMsg(this, "fail to find glcode " + trans.getGlcode2(), null).log(ctx);
         
        }
        
        if (trans.getAmount()>=0)
        {
             
            if (info != null)
            {
                trans.setSapDocHeader( info.getSapDocHeader());
                trans.setDebitPostingKey(info.getDebitPostingKeyNegative()); 
                trans.setCreditPostingKey(info.getDebitPostingKeyPositive()); 
            }else 
            {
                new MinorLogMsg(this, "fail to find SID SAP report extension for spid " + trans.getSpid() + 
                        " in adjustment type " + trans.getAdjustmentType(), null).log(ctx);
 
            }
            
        } else 
        {
            if (info != null)
            {
                trans.setSapDocHeader( info.getSapDocHeader());
                trans.setDebitPostingKey(info.getCreditPostingKeyNegative()); 
                trans.setCreditPostingKey(info.getCreditPostingKeyPositive()); 
            }else 
            {
                new MinorLogMsg(this, "fail to find SID SAP report extension for spid " + trans.getSpid() + 
                        " in adjustment type " + trans.getAdjustmentType(), null).log(ctx);
 
            }
        }
    }
    
    
    
}
