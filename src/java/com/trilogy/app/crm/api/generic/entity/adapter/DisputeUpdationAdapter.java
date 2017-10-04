/*
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily 
 * available. Additionally, source code is, by its very nature, confidential 
 * information and inextricably contains trade secrets and other information 
 * proprietary, valuable and sensitive to Redknee, no unauthorised use, 
 * disclosure, manipulation or otherwise is permitted, and may only be used 
 * in accordance with the terms of the licence agreement entered into with 
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright � Redknee Inc. and its subsidiaries. All Rights Reserved. 
 */
package com.trilogy.app.crm.api.generic.entity.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.trilogy.app.crm.api.generic.entity.adapter.DefaultGenericEntityAdapter;
import com.trilogy.app.crm.api.generic.entity.adapter.EntityParsingException;
import com.trilogy.app.crm.api.generic.entity.adapter.PropertyAdapter;
import com.trilogy.app.crm.support.HomeSupportHelper;
import com.trilogy.app.crm.troubleticket.bean.Dispute;
import com.trilogy.app.crm.troubleticket.bean.DisputeStateEnum;
import com.trilogy.app.crm.troubleticket.bean.DisputeXInfo;
import com.trilogy.framework.xhome.beans.CompoundIllegalStateException;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.elang.True;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.util.crmapi.wsdl.v2_0.types.GenericParameter;
import com.trilogy.util.crmapi.wsdl.v3_0.types.genericentity.Entity;

/**
* @author monami.pakira@redknee.com
* @since 9.11.4
*  
*/

public  class DisputeUpdationAdapter extends DefaultGenericEntityAdapter
{
	@Override
    public PropertyAdapter getPropertyAdapter(Context ctx, PropertyInfo property)
    {
        PropertyAdapter propertyAdapter = null;
        if (Dispute.class.isAssignableFrom(property.getBeanClass()))
        {
            propertyAdapter = new PropertyAdapter()
            {
                public void adaptQuery(And and, PropertyInfo info, Object value){}
                
                public void adaptProperty(Context ctx, Object bean, GenericParameter[] genericParameters,
                        PropertyInfo property)
                {
                	Dispute dispute = null;
                	ArrayList<String> propertyList = new ArrayList<String>();
                	
                         for (GenericParameter param : genericParameters)
                         {                        	 
                        	 if(bean instanceof Dispute)
                        	 {
                        		 dispute = (Dispute)bean;
                        	 }
                        	if (LogSupport.isDebugEnabled(ctx))
                  	        {
                  	            LogSupport.debug(ctx, this, "Dispute properties in updation :: "+param.getName() +" "+ param.getValue());
                  	        } 
                        	
                        	if(param.getName().equals("spid"))
                            {
                        		dispute.setSpid((int) param.getValue());
                            }
                        	else if(param.getName().equals("BAN"))
                            {
                        		dispute.setBAN((String) param.getValue());
                            }
                        	else if(param.getName().equals("id"))
                            {
                        		dispute.setId((long) param.getValue());
                            }
                        	else if(param.getName().equals("resolvedAmount"))
                            {
                        		dispute.setResolvedAmount((long) param.getValue());
                            }
                        	else if(param.getName().equals("resolvedAmountAdjustmentType"))
                            {
                        		dispute.setResolvedAmountAdjustmentType((int) param.getValue());
                            }
                        	else if(param.getName().equals("resolutionDate"))
                            {
                        		dispute.setResolutionDate((Date) param.getValue());
                            }
                        	else if(param.getName().equals("state"))
                            {
                        		if((short)param.getValue() == 1 )
                        		{
                        		 dispute.setState(DisputeStateEnum.RESOLVED);
                        		}
                        		else
                        		{
                        			dispute.setState(DisputeStateEnum.ACTIVE);	
                        		}
                            }
                        	propertyList.add(param.getName());
                         }
                       if(!propertyList.contains("spid"))
                       {
                     	   dispute.setSpid(-1);
                       }
                       else if(!propertyList.contains("resolvedAmount"))
                       {
                       	   dispute.setResolvedAmount(-1);
                       }
                       else if(!propertyList.contains("BAN"))
                       {
                    	   dispute.setBAN(null);
                       }
                       else if(!propertyList.contains("id"))
                       {
                    	   dispute.setId(-1);
                       }
                       else if(!propertyList.contains("resolvedAmountAdjustmentType"))
                       {
                      	   dispute.setResolvedAmountAdjustmentType(-1);
                       }
                       else if(!propertyList.contains("resolutionDate"))
                       {
                       	   dispute.setResolutionDate(new Date());
                       }
                       else if(!propertyList.contains("state"))
                       {
                    	   dispute.setState(null);
                       }
                       if (LogSupport.isDebugEnabled(ctx))
             	       {
                          LogSupport.debug(ctx, this, "Dispute values :: " +dispute);
                       }
                      }
            };
        }
        else
        {
            return super.getPropertyAdapter(ctx, property);
        }
        return propertyAdapter;
    }
	
	@Override
	public Object adapt(Context ctx, Entity entity)
			throws EntityParsingException
	{
		Dispute bean = (Dispute) super.adapt(ctx, entity);
		if (LogSupport.isDebugEnabled(ctx))
	    {
		    LogSupport.debug(ctx, this,
		 		"Dispute Bean  :::" + bean);
		}
		CompoundIllegalStateException e = new CompoundIllegalStateException();
		if(bean != null)
		{
			if(bean.getId()==-1)
			{
				if (LogSupport.isDebugEnabled(ctx))
      	        {
				   LogSupport.debug(ctx, this,
				 		"Please provide Id for dispute");
      	        }
				try
				{
					throw new HomeException("Validation failed: Please provide ID");
				} catch (HomeException exp)
				{
					e.thrown(exp);
				}
				finally
				{
					e.throwAll();
				}
				
			}
			else 
			{
				And filter = new And();
				filter.add(new EQ(DisputeXInfo.ID, bean.getId()));
				List<Dispute> disputeBean;
				try 
				{
					disputeBean = (List<Dispute>) HomeSupportHelper.get(ctx).getBeans(ctx, Dispute.class, filter);
					if(disputeBean!=null && disputeBean.isEmpty())
					{
						if (LogSupport.isDebugEnabled(ctx))
              	        {
							LogSupport.debug(ctx, this,
						 		"Please provide correct Id for dispute");
              	        }
						try
						{
							throw new HomeException("Validation failed: No Dispute found for provided ID");
						} catch (HomeException exp)
						{
							e.thrown(exp);
						}
						finally
						{
							e.throwAll();
						}
				     }
				}
				catch (HomeInternalException e1) {
					e1.printStackTrace();
				} catch (HomeException e1) {
					e1.printStackTrace();
				}
				
			}
		}
		return bean;
	}
}