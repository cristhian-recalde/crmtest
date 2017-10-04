package com.trilogy.app.crm.web.control;

import java.io.PrintWriter;

import javax.servlet.ServletRequest;

import com.trilogy.app.crm.bean.AdditionalMsisdnBean;
import com.trilogy.app.crm.bean.MsisdnEntryTypeEnum;
import com.trilogy.app.crm.bean.MsisdnKeyWebControl;
import com.trilogy.app.crm.bean.MsisdnXInfo;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SysFeatureCfg;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.webcontrol.AbstractWebControl;
import com.trilogy.framework.xhome.webcontrol.PrimitiveWebControl;
import com.trilogy.framework.xhome.webcontrol.TextFieldWebControl;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.DebugLogMsg;


/**
 * WebControl that is used for selection of a MSISDN.  This webcontrol is customized
 * so that we can select from the following three options:
 * <br>     1. Acquired MSISDNs - from the list of MSISDNs currently allocated for the BAN associated with the entity
 * <br>     2. Custom MSISDN - allows for manual entry through a text field
 * <br>     3. External MSISDN - also allows for manual entry though a text field, 
 *                          but the MSISDN is treated as a MSISDN from outside our
 *                          MSISDN management scope and thus has independent configuration
 *                          for it's lifecycle.
 * <br>     4. MSISDN Group - allows for selection based on a the MSISDN group that is selected.
 * 
 * Note that the bean that is using this webcontrol must implement the following interfaces:
 * <br>     1. SpidAware
 * <br>     2. TechnologyAware
 * 
 * The bean also needs to have a transient field for the MsisdnEntryTypeEnum value to dictate the entry type being used.
 * 
 * This is really just a fancy proxy webcontrol with a little business logic to accomodate the special entry types.
 * 
 * @author rpatel
 *
 */
public class MsisdnSelectionWebControl extends PrimitiveWebControl
{
    private PropertyInfo banProperty_;
    private PropertyInfo entryTypeProperty_;
    private PropertyInfo subTypeProperty_;
    private PropertyInfo subscriptionTypeProperty_;
    
    private WebControl aquireWebControl_;
    private WebControl customWebControl_;
    private WebControl externalWebControl_;
    private WebControl msisdnGroupWebControl_;
    private WebControl defaultWebControl_;
    
    private boolean isOptional_;
    

    /**
     * Field width for free-form MSISDN web control.
     */
    private static final int FREEFORM_MSISDN_FIELD_WIDTH = 20;


    public MsisdnSelectionWebControl(
            final PropertyInfo msisdnEntryTypeEnumProperty,
            final PropertyInfo msisdnGroupProperty,
            final PropertyInfo banProperty,
            final PropertyInfo spidProperty,
            final PropertyInfo subTypeProperty,
            final PropertyInfo techTypeProperty,
            final PropertyInfo subscriptionTypeProperty,
            boolean isOptional)
    {
        this(
                msisdnEntryTypeEnumProperty,
                msisdnGroupProperty,
                banProperty,
                spidProperty,
                subTypeProperty,
                techTypeProperty,
                subscriptionTypeProperty,
                false,
                isOptional,
                MsisdnEntryTypeEnum.CUSTOM_ENTRY);
    }

    public MsisdnSelectionWebControl(
            final PropertyInfo msisdnEntryTypeEnumProperty,
            final PropertyInfo msisdnGroupProperty,
            final PropertyInfo banProperty,
            final PropertyInfo spidProperty,
            final PropertyInfo subTypeProperty,
            final PropertyInfo techTypeProperty,
            final PropertyInfo subscriptionTypeProperty,
            boolean additionalMsisdn,
            boolean isOptional)
    {
        this(
        		msisdnEntryTypeEnumProperty,
        		msisdnGroupProperty,
        		banProperty,
        		spidProperty,
        		subTypeProperty,
        		techTypeProperty,
        		subscriptionTypeProperty,
        		additionalMsisdn,
        		isOptional,
        		MsisdnEntryTypeEnum.CUSTOM_ENTRY);
    }
    
    public MsisdnSelectionWebControl(
            final PropertyInfo msisdnEntryTypeEnumProperty,
            final PropertyInfo msisdnGroupProperty,
            final PropertyInfo banProperty,
            final PropertyInfo spidProperty,
            final PropertyInfo subTypeProperty,
            final PropertyInfo techTypeProperty,
            final PropertyInfo subscriptionTypeProperty,
            boolean additionalMsisdn,
            boolean isOptional,
            MsisdnEntryTypeEnum defaultSelectionType)
    {
    	banProperty_ = banProperty;
        entryTypeProperty_ = msisdnEntryTypeEnumProperty;
        isOptional_ = isOptional;
        subTypeProperty_ = subTypeProperty;
        subscriptionTypeProperty_ = subscriptionTypeProperty;
        
        aquireWebControl_ = new FilterWebControl(new MsisdnKeyWebControl(), banProperty_, MsisdnXInfo.BAN);
        customWebControl_ = new TextFieldWebControl(FREEFORM_MSISDN_FIELD_WIDTH);
        externalWebControl_ = new TextFieldWebControl(FREEFORM_MSISDN_FIELD_WIDTH);
        if (additionalMsisdn)
        {
            msisdnGroupWebControl_ = new SetSpidProxyWebControl(new AdditionalMsisdnGroupAvailMsisdnWebControl(msisdnGroupProperty, banProperty, spidProperty, subTypeProperty, techTypeProperty, subscriptionTypeProperty, isOptional_ ));
        }
        else
        {
            msisdnGroupWebControl_ = new SetSpidProxyWebControl(new MsisdnGroupAvailMsisdnWebControl(msisdnGroupProperty, banProperty, spidProperty, subTypeProperty, techTypeProperty, subscriptionTypeProperty, isOptional_ ));
        }
        defaultWebControl_ = getWebControl(defaultSelectionType.getIndex());
    }

    public Object fromWeb(Context ctx, ServletRequest req, String name) throws NullPointerException
    {
        Object obj = ctx.get(AbstractWebControl.BEAN);
        if(obj == null)
        {
            // assume that the bean is new and so we stick with the default values
            new DebugLogMsg(this, "Unable to retrieve bean from Context.  Will assume that the MSISDN is currently unset.", null).log(ctx);
            return "";
        }
        else
        {
            WebControl webControl = getWebControl(ctx, obj);
            if(webControl != null)
            {
                return webControl.fromWeb(ctx, req, name);
            }
            else
            {
                return "";
            }
        }
    }

    private WebControl getWebControl(Context ctx, Object bean)
    {
        int entryType = ((Number) entryTypeProperty_.get(bean)).intValue();
        if (entryType == ((Number) entryTypeProperty_.getDefault()).intValue())
        {
            return defaultWebControl_;
        }
        SubscriberTypeEnum subscriberType = SubscriberTypeEnum.HYBRID;
        
        if (bean instanceof AdditionalMsisdnBean)
        {
            AdditionalMsisdnBean addMsisdn = (AdditionalMsisdnBean) bean;
            if (((AdditionalMsisdnBean) bean).getSubscriber()!=null)
            {
                subscriberType = (SubscriberTypeEnum) subTypeProperty_.get(addMsisdn.getSubscriber());
            }
        }
        else
        {
            subscriberType = (SubscriberTypeEnum) subTypeProperty_.get(bean);
        }
        
        return getWebControl(ctx, entryType, subscriberType);
    }

	private WebControl getWebControl(Context ctx, int entryTypeIndex, SubscriberTypeEnum subscriberType) throws IllegalArgumentException
	{
        final SysFeatureCfg systemConfiguration = (SysFeatureCfg) ctx.get(SysFeatureCfg.class);
        if (!systemConfiguration.isPrepaidMsisdnSelectionEnabled() && SubscriberTypeEnum.PREPAID.equals(subscriberType))
        {
            return customWebControl_;
        }
        else
        {
            return getWebControl(entryTypeIndex);
        }
	}


    private WebControl getWebControl(int entryTypeIndex) throws IllegalArgumentException
    {
        switch(entryTypeIndex)
        {
            case MsisdnEntryTypeEnum.AQUIRED_MSISDNS_INDEX:
                return aquireWebControl_;
            case MsisdnEntryTypeEnum.CUSTOM_ENTRY_INDEX:
                return customWebControl_;
            case MsisdnEntryTypeEnum.EXTERNAL_INDEX:
                return externalWebControl_;
            case MsisdnEntryTypeEnum.MSISDN_GROUP_INDEX:
                return msisdnGroupWebControl_;
            default:
                throw new IllegalArgumentException("Encounted an uxexpected MsisdnEntryTypeEnum index [index=" + entryTypeIndex + "].  Unablee to determin which webcontrol to use.");
        }
    }

    public void toWeb(Context ctx, PrintWriter out, String name, Object obj)
    {
        Object bean = ctx.get(AbstractWebControl.BEAN);
        // Nothing special, simply delegate   
        WebControl wc = getWebControl(ctx, bean);
        if( wc != null)
        {
            wc.toWeb(ctx, out, name, obj);
        }
        else
        {
            throw new NullPointerException("Unable to find a delegate WebControl in MsisdnSelectionWebControl");
        }    
    }
}
