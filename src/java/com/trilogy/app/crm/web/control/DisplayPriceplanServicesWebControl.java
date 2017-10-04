/*
 * Created on Apr 5, 2005
 */
package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.Service;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServiceProvisionStatusEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberServiceDisplay;
import com.trilogy.app.crm.bean.SubscriberServiceDisplayTableWebControl;
import com.trilogy.app.crm.bean.SubscriberServices;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversion;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.service.ServiceStateEnum;
import com.trilogy.app.crm.bean.VoicemailFieldsBean;
import com.trilogy.app.crm.support.CalendarSupportHelper;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.app.crm.support.SubscriberServicesSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.web.action.ActionMgr;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.ViewModeEnum;
import com.trilogy.framework.xlog.log.DebugLogMsg;
import com.trilogy.framework.xlog.log.MajorLogMsg;
import com.trilogy.app.crm.bean.ServicePreferenceEnum;
import com.trilogy.app.crm.CommonTime;

/**
 * @author rattapattu
 */
public class DisplayPriceplanServicesWebControl extends
        SubscriberServiceDisplayTableWebControl
{
    // TODO 2009-01-28 copy pasted form DisplaySubscriberServicesWebControl. Refactor out

    public DisplayPriceplanServicesWebControl()
    {
        super();
    }

    public void toWeb(Context ctx, final PrintWriter out, final String name, final Object obj)
    {
        ctx = ctx.createSubContext();

        SubscriberTechnologyConversion conversionObj = (SubscriberTechnologyConversion) ctx.get(AbstractWebControl.BEAN);

        List beanList = new ArrayList();
        Collection serviceFees = PricePlanSupport.getServiceFees(ctx,conversionObj.getPricePlan());
        
        for (Iterator iter = serviceFees.iterator(); iter.hasNext();)
        {
            ServiceFee2 fee = (ServiceFee2)iter.next() ;
            try
            {
	            SubscriberServiceDisplay bean = adapt(ctx,fee);
	            beanList.add(bean);
            }
            catch(Exception e)
            {
            	new DebugLogMsg(this, e.getMessage(), e).log(ctx);
            }
        }


        ctx.put(NUM_OF_BLANKS, -1);
        disableActions(ctx);
        setPropertyReadOnly(ctx, "SubscriberServiceDisplay.serviceId");
        setPropertyReadOnly(ctx, "SubscriberServiceDisplay.fee");
        setPropertyReadOnly(ctx, "SubscriberServiceDisplay.servicePeriod");
        setPropertyReadOnly(ctx, "SubscriberServiceDisplay.cltcDisabled");
        
        ctx = ctx.createSubContext();
        int mode = ctx.getInt("MODE", DISPLAY_MODE);
        ctx.put("REALMODE", mode);
        if (mode == DISPLAY_MODE)
        {
            ctx.put("MODE", EDIT_MODE);
        }

        super.toWeb(ctx, out, name, beanList);
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name)
    {
    	Set set = new HashSet();
    	Set setFromWeb = new HashSet();
    	Set serviceIdList = new HashSet();
    	
    	// This is invoked within the context of the conversion, where a valid sub is present in the context.
        Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
        Map serviceFeesMap = SubscriberServicesSupport.getServiceFees(ctx,sub);
        
        super.fromWeb(ctx, setFromWeb, req, name);
        
        final Date today = CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(new Date());

        for (Iterator i = setFromWeb.iterator(); i.hasNext();)
        {
            SubscriberServiceDisplay fee = (SubscriberServiceDisplay) i.next();
            SubscriberServices bean = new SubscriberServices();
            bean.setSubscriberId(sub.getId());
            bean.setServiceId(fee.getServiceId());
            bean.setStartDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getStartDate()));
            bean.setEndDate(CalendarSupportHelper.get(ctx).getDateWithNoTimeOfDay(fee.getEndDate()));

            /*hidden fields are not persistent across toWeb & fromWeb methods
             *  therefore I have to querry again to figure out whether it's a mandatory field or not
             */
            //bean.setMandatory(fee.getMandatory());

            Long key = Long.valueOf(fee.getServiceId());
            // the key should be there for sure, but just in case

            ServiceFee2 fees = (ServiceFee2) serviceFeesMap.get(key);
            if (fees != null)
            {
                bean.setMandatory(fees.getServicePreference().equals(ServicePreferenceEnum.MANDATORY));
            }

            if(bean.getMandatory())
            {
                bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
            }
            else
            {
                if(today.equals(bean.getStartDate()))
                {
                    // This will make sure the optional service starting today is provisoned immediately
                    bean.setProvisionedState(ServiceStateEnum.PROVISIONED);
                }
            }
            set.add(bean);
            serviceIdList.add(Long.valueOf(fee.getServiceId()));
        }
        // this function is very important.        
        sub.setServices(serviceIdList);

        return set;
    }

    private void disableActions(Context ctx)
    {
        ActionMgr.disableActions(ctx);
    }

    /**
     * @param ctx
     * @param fee
     * @return
     */
    private SubscriberServiceDisplay adapt(Context ctx, ServiceFee2 fee) throws HomeException
    {
        Long serviceID = Long.valueOf(fee.getServiceId());
        SubscriberServiceDisplay bean = new SubscriberServiceDisplay();
        bean.setServiceId(fee.getServiceId());
        bean.setFee(fee.getFee());
        bean.setServicePeriod(fee.getServicePeriod());
        
        if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
			bean.setMandatory(true);
		else
			bean.setMandatory(false);

        bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE,bean.getStartDate()));
        
        if (fee.getServicePreference().equals(ServicePreferenceEnum.MANDATORY))
        {
           // bean.setStartDate(sub.getStartDate());
            bean.setEndDate(null);
        }
        else if(bean.getStartDate().equals(bean.getEndDate()))
        {
            // During a priceplan change if mandatory service is changed to optional
            // it will display the same day for both start and end date.
            // this is done to avoid that.
            bean.setEndDate(CalendarSupportHelper.get(ctx).findDateYearsAfter(CommonTime.YEARS_IN_FUTURE,bean.getStartDate()));
        }

        return bean;
    }

    void setPropertyReadOnly(Context ctx, String property)
    {
        ViewModeEnum mode = getMode(ctx, property);
        if (mode != ViewModeEnum.NONE)
        {
            setMode(ctx, property, ViewModeEnum.READ_ONLY);
        }

    }
    // copied from DefaultserivceEditorWebControl
    public void outputCheckBox(Context ctx, PrintWriter out, String name,
            Object bean, boolean isChecked)
    {
        SubscriberServiceDisplay fee = (SubscriberServiceDisplay) bean;

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
        if (fee.isMandatory() || (mode == DISPLAY_MODE && fee.isChecked()))
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
