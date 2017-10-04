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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.MajorLogMsg;

import com.trilogy.app.crm.bean.ServiceFee2TableWebControl;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.ServiceFee2;


/** Modified version of ServiceFeeTableWebControl. **/
public class DefaultServiceEditorWebControl
   extends ServiceFee2TableWebControl
{

   public DefaultServiceEditorWebControl()
   {
   }
   
   /**
    * Make visible perperty to as read-only, otherwise leave unchanged.
    * @param ctx
    * @param property 
    */
   void setPropertyReadOnly(Context ctx, String property) 
   {
	   	ViewModeEnum mode = getMode(ctx, property);
	   	if (mode != ViewModeEnum.NONE) 
	   	{
	   		setMode(ctx, property, ViewModeEnum.READ_ONLY);
	   	}
   	
   }   
                   
   /** Convert the Set to a List for super.toWeb. **/
   /** @param obj set of Service keys **/
   @Override
public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
   {
      try
      {
          Subscriber       sub      = (Subscriber)       ctx.get(AbstractWebControl.BEAN);
          PricePlanVersion plan     = sub.getRawPricePlanVersion(ctx);
          Set              services = (Set)              obj;
          List             list     = new ArrayList();
          //Home serviceHome = (Home)ctx.get(ServiceHome.class);

          if (plan == null)
          {
              out.println("<strong><font color=\"red\">No Services Provisioned!</font></strong>");
              return;
          }
          
          // Disable creation of new AdjustmentInfo's
          ctx = ctx.createSubContext();
          ctx.put(NUM_OF_BLANKS, -1);
      
          setPropertyReadOnly(ctx, "ServiceFee2.serviceId");
          setPropertyReadOnly(ctx, "ServiceFee2.fee");
       // Javascript embedded into BundleFee and ServiceFee web-control control the auto-selection and need them to be fields.
          //setPropertyReadOnly(ctx, "ServiceFee2.servicePeriod");
          ctx.put("ServiceFee2.mandatory.mode",     ViewModeEnum.NONE);
          ctx.put("ServiceFee2.carryOver.mode",     ViewModeEnum.NONE);
          setPropertyReadOnly(ctx, "ServiceFee2.cltcDisabled");
          
          Collection serviceFees = plan.getServiceFees(ctx).values();
          //Collection cltcInvalidServices = ServiceCreditLimitCheckSupport.selectInvalidCltcServiceIds(ctx, sub);
          
          
          for ( Iterator i = serviceFees.iterator() ; i.hasNext() ; )
          {
              ServiceFee2 fee_ = (ServiceFee2) i.next();
              ServiceFee2 fee = null;
	            try {
	                fee = (ServiceFee2)fee_.clone();
	            } catch (CloneNotSupportedException e1) {
	                new MajorLogMsg(this, "Fail to clone ServiceFee2=" + fee_, e1).log(ctx);
	            }
	          Object key = Long.valueOf(fee.getServiceId());
         
              fee.setDispCLTC(true);
              fee.setChecked(services.contains(key));
              
              //ServiceFee2 flag is no longer for CLCT, is general for service provisioned 
              //indicator
//              boolean cltcDisable = false;
//              if (!sub.isAboveCreditLimit())
//              {
//                  Service service = (Service)serviceHome.find(ctx, key);
//                  cltcDisable = service.isEnableCLTC();
//              }
//              fee.setCltcDisabled(cltcInvalidServices.contains(key));
              fee.setCltcDisabled(sub.getProvisionedServices(ctx).contains(key));
              fee.setEnabled(!(sub.getRecurDisabledServices().contains(key)));
              list.add(fee);
          }

          // MAALP: 05/03/04 - TT 402262116 
          // create a subcontext which overrides DISPLAY_MODE 
          // in order to make ServiceFee2tableWebControl
          // call outputCheckBox method 
          // and saves the real mode in order to properly display check box
		  Context subcontext = ctx.createSubContext();
		  int mode = ctx.getInt("MODE", DISPLAY_MODE);
		  subcontext.put("REALMODE", mode);
          if (mode == DISPLAY_MODE)
          {
			subcontext.put("MODE", EDIT_MODE);
          }
          
          super.toWeb(subcontext, out, name, list);
      }
      catch (HomeException e)
      {
      }
   }
   
   
   /** Convert the List from super.fromWeb to a Set. **/
   @Override
public Object fromWeb(Context ctx, ServletRequest req, String name)
   {
      Set  set  = new HashSet();
      List list = new ArrayList();

      super.fromWeb(ctx, list, req, name);
      
      for ( Iterator i = list.iterator() ; i.hasNext() ; )
      {
         ServiceFee2 fee = (ServiceFee2) i.next();
         
         set.add(Long.valueOf(fee.getServiceId()));
      }
      
      return set;
   }

   
   @Override
public void outputCheckBox(Context ctx, PrintWriter out, String name, Object bean, boolean isChecked)
   {
      ServiceFee2 fee = (ServiceFee2) bean;
      
      out.print("<input type=\"hidden\" name=\"");
      out.print(name);
      out.print(SEPERATOR);
      out.print("serviceId\" value=\"");
      out.print(fee.getServiceId());
      out.println("\" />");
         
	  // MAALP: 05/03/04 - TT 402262116
	  // Restore the real mode and display check box based on it
	  // When view mode is selected show "x" by the checked items,
	  // otherwise display a regular check box
	  int mode = ctx.getInt("REALMODE", DISPLAY_MODE);
      // skushwaha if ( fee.isMandatory() || (mode == DISPLAY_MODE && fee.isChecked()))
	  if ( fee.getServicePreference().equals(
				ServicePreferenceEnum.MANDATORY) || (mode == DISPLAY_MODE && fee.isChecked()))
      {
         out.print(" <td> &nbsp;<b>X</b><input type=\"hidden\" name=\"");
         out.print(name);
         out.print(SEPERATOR);
         out.print("_enabled\" value=\"X\" />");
         
         out.println("</td>");
      }
      //else if (mode == DISPLAY_MODE || fee.isCltcDisabled())
      else if (mode == DISPLAY_MODE)
      {
		// leave an empty cell in the row if service isn't checked
		out.print("<td>&nbsp;</td>");
      }
      else
      {
         super.outputCheckBox(ctx, out, name, bean, fee.isChecked());
      }
   }
   
}


