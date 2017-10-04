/*
 * Created on 2004-12-8
 *
 * Copyright (c) 1999-2003 REDKNEE.com. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * REDKNEE.com. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the
 * terms of the license agreement you entered into with REDKNEE.com.
 *
 * REDKNEE.COM MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHCDR EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE IMPLIED WARRANTIES OF MCDRCHANTABILITY, FITNESS FOR
 * A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. REDKNEE.COM SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFCDRED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DCDRIVATIVES.
 */
package com.trilogy.app.crm.home;

import java.util.List;

import com.trilogy.app.crm.bean.CRMSpid;
import com.trilogy.app.crm.bean.IdentifierAware;
import com.trilogy.app.crm.bean.IdentifierEnum;
import com.trilogy.app.crm.bean.IdentifierSequence;
import com.trilogy.app.crm.bean.SpidObjectsTemplate;
import com.trilogy.app.crm.bean.calldetail.CallDetailXInfo;
import com.trilogy.app.crm.support.IdentifierSequenceSupportHelper;
import com.trilogy.framework.core.platform.CoreSupport;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.XDeepCloneable;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.language.XMessageHome;
import com.trilogy.framework.xhome.msp.SpidAware;
import com.trilogy.framework.xlog.log.MajorLogMsg;


/**
 * @author jchen
 *
 * Auto creat spid related properties, like Tax Authorities, because in Buzzard,
 * we will create account objects in background, so we need to precreat this objects
 * All spid related objects will prototyped from the template, and created via their home
 * class
 */
public class AutoCreatePropertiesSpidHome extends HomeProxy implements ContextAware 
{
    
    public AutoCreatePropertiesSpidHome(Context ctx, Home delegate)
    {
        super(delegate);
        setContext(ctx);
    }

 
    /**
     * @return Returns the context.
     */
    @Override
    public Context getContext() {
        return this.context_;
    }
    /**
     * @param context The context to set.
     */
    @Override
    public void setContext(Context context) {
        this.context_ = context;
    }
    
    public Context context_;
    
    
    /* (non-Javadoc)
     * Auto create account object if ban is not set
     * @see com.redknee.framework.xhome.home.Home#create(java.lang.Object)
     */
    @Override
    public Object create(Context ctx, Object obj) throws HomeException 
    {
        CRMSpid spid = (CRMSpid)super.create(ctx,obj);
  
        CRMSpid olddie = (CRMSpid) obj; 
        olddie.setCipherKeyLoaded(true);
        
    	spid.setCipherKeyLength(olddie.getCipherKeyLength());
    	spid.setCipherDescription(olddie.getCipherDescription()); 
    	spid.setCipherKeyType(olddie.getCipherKeyType());   	
    	spid.CreateCipherKey(ctx); 
        
        //try to precreat objects
        if (spid.getId() != -1)
        {
            createTaxAuthority(getContext(), spid.getId());
            createBillingCycle(getContext(), spid.getId());
            createService(getContext(), spid.getId());
            createIdentifierSequences(getContext(), obj);
        }
            	
        return spid;
    }
    
    /**
	 * @param context
	 * @param obj
     * @throws HomeException
	 */
	private void createIdentifierSequences(Context context, Object obj) 
	throws HomeException 
	{
		if (obj instanceof CRMSpid)
		{
			CRMSpid spid = (CRMSpid) obj;
			addOrModifyIdentifierSequence(context, spid, IdentifierEnum.ACCOUNT_ID, spid.getNextNumber(), spid.getNextNumber());
			addOrModifyIdentifierSequence(context, spid, IdentifierEnum.INVOICE_TAX_ID, 0, 1);
            addOrModifyIdentifierSequence(context, spid, IdentifierEnum.PACKAGE_ID_GSM, spid.getNextPackageIdNumber(), spid.getNextPackageIdNumber());
            addOrModifyIdentifierSequence(context, spid, IdentifierEnum.PACKAGE_ID_TDMA, spid.getNextPackageIdNumber(), spid.getNextPackageIdNumber());
        }
	}


	/**
	 * @param context
	 * @param spid
	 * @param account_id
	 * @param l
	 * @param nextNumber
	 * @throws HomeException
	 */
	private void addOrModifyIdentifierSequence(Context context, 
			CRMSpid spid, 
			IdentifierEnum idEnum, 
			long startingNumber, 
			long nextNumber) 
	throws HomeException
	{
		IdentifierSequence id = IdentifierSequenceSupportHelper.get(context).getIdentifierSequence(context,spid.getId(),idEnum);
		
		if (id == null)
		{
			IdentifierSequenceSupportHelper.get(context).createIdentifierSequence(context, 
					spid.getId(), 
					idEnum.getDescription(), 
					startingNumber, Long.MAX_VALUE, nextNumber);
		}
		else
		{
			IdentifierSequenceSupportHelper.get(context).updateIdentifierSequence(context, id, startingNumber, nextNumber);
		}
	}


	void createService(Context ctx , int spid) throws HomeException
    {
        SpidObjectsTemplate sot = (SpidObjectsTemplate)ctx.get(SpidObjectsTemplate.class);
        createObjectList(ctx, spid, sot.getServiceList());
    }
    
    
    /**
     * Create objects from the tamplate for the specified spid.
     * Those objects need to implements: XDeepCloneable, IdentifierAware, SpidAware
     * If id is not 0, auto generated identifier will be manually assigned
     * If id is zero, underlying home class will auto generates id 
     * @param ctx
     * @param spid
     * @param objList
     * @throws HomeException
     */
     public void createObjectList(Context ctx, int spid, List objList) throws HomeException {
        int size = objList.size();
        
        long identity = generateSequenceId(spid);
        for (int i = 0; i < size; i++)
        {
            XDeepCloneable spidAwareObject = (XDeepCloneable) objList.get(i);
            Class homeClass = XBeans.getClass(getContext(), spidAwareObject.getClass(), Home.class);
            
            Home home = (Home) getContext().get(homeClass);
            
            try 
            {
                Object s = spidAwareObject.clone();
                if (((IdentifierAware)s).getIdentifier() != 0)
                {
                    ((IdentifierAware)s).setIdentifier(identity++);
                }
                ((SpidAware)s).setSpid(spid);
                
                home.create(ctx,s);
            }
            catch(Exception e)
            {
                String msg = "Failed to create XBean Class";
	            new MajorLogMsg(
	                    this,
	                    msg,
	                    e).log(getContext());
	            
	            //todo, clean up account record if failed
	            throw new HomeException(msg, e);
            }
        }
    }


    /**
     * Auto create a Tax Authority for this spid
     * @param ctx
     * @param spid
     */
    void createTaxAuthority(Context ctx , int spid) throws HomeException
    {
        SpidObjectsTemplate sot = (SpidObjectsTemplate)ctx.get(SpidObjectsTemplate.class);
        createObjectList(ctx, spid, sot.getTaxAuthorityList());
    }
    
    
    void createBillingCycle(Context ctx , int spid) throws HomeException
    {
        SpidObjectsTemplate sot = (SpidObjectsTemplate)ctx.get(SpidObjectsTemplate.class);
        createObjectList(ctx, spid, sot.getBillingCycleList());
    }
    
    /**
     * 
     * Auto create sequence id for the first object per spid, 
     * @param spid
     * @return
     */
    private int generateSequenceId(int spid) {
        return 10000 * (spid + 1) + 1;
    } 
    
    @Override
    public void remove(Context ctx, final Object obj)
    throws HomeException
	{
		if (obj instanceof CRMSpid)
		{
			CRMSpid spid = (CRMSpid) obj;
			
			deleteIdentifierSequence(ctx, spid, IdentifierEnum.ACCOUNT_ID);
			deleteIdentifierSequence(ctx, spid, IdentifierEnum.INVOICE_TAX_ID);
            deleteIdentifierSequence(ctx, spid, IdentifierEnum.PACKAGE_ID_GSM);
            deleteIdentifierSequence(ctx, spid, IdentifierEnum.PACKAGE_ID_TDMA);
		}
		super.remove(ctx, obj);
    }


	/**
	 * @param ctx
	 * @param spid
	 * @param invoice_tax_id
	 * @throws HomeException
	 */
	private void deleteIdentifierSequence(Context ctx, CRMSpid spid, IdentifierEnum idEnum)
	throws HomeException
	{
		IdentifierSequence id = IdentifierSequenceSupportHelper.get(ctx).getIdentifierSequence(ctx,spid.getId(),idEnum);
		
		if (id != null)
		{
			IdentifierSequenceSupportHelper.get(ctx).deleteIdentifierSequence(ctx, id);
		}
		
	}
	
	private void createSpidLabelMessages(Context ctx, int spid)
	{
	    final String language = CoreSupport.getApplication(ctx).getLocaleIsoLanguage();
	    final String firstCompChargePropLabel = CallDetailXInfo.COMPONENT_CHARGE1.toString()  +  ".Label" + ".spid";
	    final String secondCompChargePropLabel = CallDetailXInfo.COMPONENT_CHARGE2.toString() +  ".Label" + ".spid";
	    final String thirdCompChargePropLabel = CallDetailXInfo.COMPONENT_CHARGE3.toString()  +  ".Label" + ".spid";
	    
	    final String firstCompGlCodePropLabel = CallDetailXInfo.COMPONENT_GLCODE1.toString()  +  ".Label" + ".spid";
        final String secondCompGlCodePropLabel = CallDetailXInfo.COMPONENT_GLCODE2.toString() +  ".Label" + ".spid";
        final String thirdCompGlCodePropLabel = CallDetailXInfo.COMPONENT_GLCODE3.toString()  +  ".Label" + ".spid";
        
        Home home = (Home)ctx.get(XMessageHome.class);
	}
 }
