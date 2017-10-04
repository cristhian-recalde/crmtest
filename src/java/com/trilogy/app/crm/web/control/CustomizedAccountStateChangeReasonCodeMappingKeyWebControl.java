/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s).  A complete listing of authors of this work is readily
 * available.  Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee.  No unauthorized use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the license agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */

package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.AccountReasonCodeMapping;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingHome;
import com.trilogy.app.crm.bean.AccountReasonCodeMappingXInfo;
import com.trilogy.app.crm.bean.AccountStateChangeReason;
import com.trilogy.app.crm.bean.AccountStateChangeReasonHome;
import com.trilogy.app.crm.bean.AccountStateChangeReasonKeyWebControl;
import com.trilogy.app.crm.bean.AccountStateChangeReasonXInfo;
import com.trilogy.app.crm.bean.AccountStateEnum;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.In;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.visitor.Visitor;
import com.trilogy.framework.xhome.webcontrol.KeyWebControlOptionalValue;

public class CustomizedAccountStateChangeReasonCodeMappingKeyWebControl extends AccountStateChangeReasonKeyWebControl {
	
	//public static final KeyWebControlOptionalValue DEFAULT=new KeyWebControlOptionalValue("--", "-1");
	public static final String DEFAULT_TEXT = "(Default)";
	public static final String DEFAULT_REASON_CODE_OBJECT = "DEFAULT.REASON.CODE.OBJECT";

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(boolean autoPreview) {
		super(autoPreview);
		
	}
	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl()
	{
		super();
	}

	
	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize)
	{
		super(listSize);
	}

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize, boolean autoPreview)
	{
		super(listSize, autoPreview);
	}

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize, boolean autoPreview, boolean isOptional)
	{
		super(listSize, autoPreview, isOptional);
	}

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize, boolean autoPreview, boolean isOptional, boolean allowCustom)
	{
		super(listSize, autoPreview, isOptional, allowCustom);
	}

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize, boolean autoPreview, Object optionalValue)
	{
		super(listSize, autoPreview, optionalValue);
	}
	
	
	

	public CustomizedAccountStateChangeReasonCodeMappingKeyWebControl(int listSize, boolean autoPreview, Object optionalValue, boolean allowCustom)
	{
		super(listSize, autoPreview, optionalValue, allowCustom);
	}
	
	@Override
	public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
	{
		Object beanObj = ctx.get(com.redknee.framework.xhome.webcontrol.AbstractWebControl.BEAN);
		Context subCtx = ctx.createSubContext();
		com.redknee.app.crm.bean.Account acc = (com.redknee.app.crm.bean.Account)beanObj;
		
		if(acc !=null)
		{
			try
			{
			    AccountReasonCodeMapping defaultMapping = null;
				Home mapHome = (Home)ctx.get(AccountReasonCodeMappingHome.class);
				Home reasonHome = (Home)ctx.get(AccountStateChangeReasonHome.class);
				And mapFilter = new And();
				mapFilter.add(new EQ(AccountReasonCodeMappingXInfo.SPID,acc.getSpid()));
				mapFilter.add(new EQ(AccountReasonCodeMappingXInfo.ACCOUNT_STATE,acc.getState()));
				Collection<AccountReasonCodeMapping> idCOl = mapHome.select(ctx, mapFilter);
				Set<Integer> idSet = new HashSet<Integer>();
				for (AccountReasonCodeMapping mapBean : idCOl)
				{
					idSet.add(mapBean.getReasonCode());
					if(mapBean.getIsDefault()==Boolean.TRUE)
					{
					    defaultMapping = mapBean;
					    ctx.put(DEFAULT_REASON_CODE_OBJECT, mapBean);
					}
				}
				In in = new In(AccountStateChangeReasonXInfo.ID, idSet);
				Home reasonMappedHome = reasonHome.where(ctx, in);
				
				CustomOverridingHome reasonsHome = new CustomOverridingHome();
                
                for (AccountStateChangeReason accountStateChangeReason : (reasonMappedHome != null ? (Collection<AccountStateChangeReason>)reasonMappedHome.selectAll(ctx) : new ArrayList<AccountStateChangeReason>()))
                {
                    if(defaultMapping != null && accountStateChangeReason.getID() == defaultMapping.getReasonCode())
                        reasonsHome.setDefault(ctx, accountStateChangeReason);
                    else
                        reasonsHome.create(ctx, accountStateChangeReason);
                }
                if(ctx.getInt("MODE",DISPLAY_MODE)==0)
    			{
                	And filter = new And();
                	filter.add(new EQ(AccountStateChangeReasonXInfo.ID,obj));
                	Home reasonHomeview = reasonHome.where(ctx, filter);
                	subCtx.put(getHomeKey(), reasonHomeview);
    			}else
    			{
    				subCtx.put(AccountStateChangeReasonHome.class, reasonsHome);
    			}
                
				/*if(reasonMappedHome !=null)
				{	
					subCtx.put(AccountStateChangeReasonHome.class, reasonMappedHome);
				}*/
				
			} catch(HomeException e){}
			
		}
		super.toWeb(subCtx, out, name, obj);
		ctx.remove(DEFAULT_REASON_CODE_OBJECT);
	}
	
	
	@Override
	public String getDesc(Context ctx, Object obj)
	{
		AccountStateChangeReason reasonBean = (AccountStateChangeReason)obj;
		AccountReasonCodeMapping defaultMapping = (AccountReasonCodeMapping)ctx.get(DEFAULT_REASON_CODE_OBJECT);
		
		if(defaultMapping != null && reasonBean.getID() == defaultMapping.getReasonCode())
            return String.valueOf(reasonBean.getReasonCode() + "-"+reasonBean.getName()+ DEFAULT_TEXT);
		
		return String.valueOf(reasonBean.getReasonCode() + "-"+reasonBean.getName());
	}	
	
	private class CustomOverridingHome implements Home
    {
        private final ArrayList<AccountStateChangeReason> list = new ArrayList<AccountStateChangeReason>();;
        
            public void setDefault(Context ctx, Object obj)
            {
               if(list.isEmpty())
                {
                	list.add((AccountStateChangeReason)obj);
                }else
                {
                	int defaultIndex = 0;
                	list.add((AccountStateChangeReason)obj);
                	AccountReasonCodeMapping defaultMapping = (AccountReasonCodeMapping)ctx.get(DEFAULT_REASON_CODE_OBJECT);
                	for(int i =0;i<list.size();i++)
                	{
                		AccountStateChangeReason reasonBeanDummy = list.get(i);
                		if(reasonBeanDummy.getID()==defaultMapping.getReasonCode())
                		{
                			defaultIndex= i;
                			break;
                		}
                	}
                	
                	Collections.swap(list, 0, defaultIndex);
                	//list.set(0, (AccountStateChangeReason)obj);
                }
                    
                //ctx.put(DEFAULT_REASON_CODE_OBJECT, obj);
            }
        
            @Override
            public Object find(Context context, Object obj) throws HomeException, HomeInternalException
            {
            	if(obj instanceof Integer)
            	{
            		Home home = (Home)context.get(AccountStateChangeReasonHome.class);
            		AccountStateChangeReason reasonBean = (AccountStateChangeReason)home.find(obj);
            		return reasonBean;
            	}else if(obj instanceof AccountStateChangeReason)
            	{
            		AccountStateChangeReason bean = (AccountStateChangeReason)obj;
                    for (AccountStateChangeReason accountStatement : list)
                    {
                        if(accountStatement.getReasonCode() == bean.getReasonCode())
                            return accountStatement;
                    }
            	}
                
                return null;
            }
            
            @Override
            public Collection selectAll(Context context)
                    throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            	
                return list;
            }
            
            @Override
            public void setContext(Context context)
            {
            }
            
            @Override
            public Context getContext()
            {
                return null;
            }
            
            @Override
            public Home where(Context context, Object obj)
            {
                return null;
            }
            
            @Override
            public Object store(Context context, Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Collection select(Context context, Object obj)
                    throws HomeException, HomeInternalException, UnsupportedOperationException
            {
                return null;
            }
            
            @Override
            public void removeAll(Context context, Object obj)
                    throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public void remove(Context context, Object obj)
                    throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public Visitor forEach(Context context, Visitor visitor, Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public void drop(Context context) throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public Object create(Context context, Object obj) throws HomeException, HomeInternalException
            {
                boolean flag = list.add((AccountStateChangeReason)obj);
                
                return flag == true ? obj : false;
            }
            
            @Override
            public Object cmd(Context context, Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Object store(Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Collection select(Object obj) throws HomeException, HomeInternalException, UnsupportedOperationException
            {
                return null;
            }
            
            @Override
            public void removeAll(Object obj) throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public void remove(Object obj) throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public Visitor forEach(Visitor visitor, Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Object find(Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public void drop() throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public Object create(Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Object cmd(Object obj) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Collection selectAll() throws HomeException, HomeInternalException, UnsupportedOperationException
            {
                return null;
            }
            
            @Override
            public void removeAll(Context context) throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public void removeAll() throws HomeException, HomeInternalException, UnsupportedOperationException
            {
            }
            
            @Override
            public Visitor forEach(Context context, Visitor visitor) throws HomeException, HomeInternalException
            {
                return null;
            }
            
            @Override
            public Visitor forEach(Visitor visitor) throws HomeException, HomeInternalException
            {
                return null;
            }
    }
}
