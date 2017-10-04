package com.trilogy.app.crm.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.trilogy.app.crm.support.CallingGroupSupport;
import com.trilogy.app.crm.support.ClosedUserGroupSupport;
import com.trilogy.app.crm.support.SpidSupport;
import com.trilogy.app.crm.support.SubscriberSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextLocator;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.app.crm.bean.core.AuxiliaryService;


public class ClosedUserGroup extends AbstractClosedUserGroup
{
	

    private static final long serialVersionUID = 1L;
    
    public ClosedUserGroup()
    {
    	super();
    	addPropertyChangeListener(ClosedUserGroup.SPID_PROPERTY, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				int oldSpid = (Integer)evt.getOldValue();
				int newSpid = (Integer)evt.getNewValue();
				
				if(oldSpid != newSpid)
				{
					setCugTemplateID(ClosedUserGroupTemplate.DEFAULT_ID);
				}
			}
		});
    }


    public Subscriber getOwner(Context ctx)
	throws HomeException
	{
	  //	if (this.getCugType().equals(CUGTypeEnum.PrivateCUG))
	  	{	
			if (owner == null)
			{
				return SubscriberSupport.lookupSubscriberForMSISDN(ctx, this.getOwnerMSISDN());
			}
		}
		
		return owner;	
	}

	public void setOwner(Subscriber owner) 
	{
		//if (this.getCugType().equals(CUGTypeEnum.PrivateCUG))
		{			
			this.owner = owner;
		}	
	} 
	
	public CUGTypeEnum getCugType(Context ctx)
	throws HomeException
	{
		ClosedUserGroupTemplate template = getTemplate(ctx);
		if (template == null)
		{
			throw new HomeException("fail to find closed user template " + this.getCugTemplateID()); 
		}
		
		return template.getCugType(); 
	}
	

    public ClosedUserGroupTemplate getTemplate(Context ctx) throws HomeException
    {
        if (null == cugTemplate_ && this.getCugTemplateID() != 0)
        {
            cugTemplate_ = ClosedUserGroupSupport.getCugTemplate(ctx, this.getCugTemplateID(), this.getSpid());
        }
        return cugTemplate_;
    }
	

    public String getDialingPattern(Context ctx)
    {
        if (null == dialingPattern_)
        {
            dialingPattern_ = getDialingPatternImpl(ctx);
        }
        return dialingPattern_;
    }


    protected String getDialingPatternImpl(Context ctx)
    {
        try
        {
          final CRMSpid crmSpid = SpidSupport.getCRMSpid(ctx, getSpid());
            if (null != crmSpid)
            {
                final String dialingPattern = crmSpid.getDialingPattern();
                if (null != dialingPattern && !dialingPattern.isEmpty())
                {
                    return dialingPattern;
                }
            }
            else
            {
                new MinorLogMsg(this, "Could not get CUG-Template and hence dialing pattern for this CUG Instance ["
                        + getID() + "]", null).log(ctx);
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error locating CUG-Template and hence dialing pattern for this CUG Instance ["
                    + getID() + "]", null).log(ctx);
        }
        return "";
    }
    
    
    public String getShortCodePattern(Context ctx)
    {
        if (null == shortCodePattern_)
        {
            shortCodePattern_ = getShortCodePatternImpl(ctx);
        }
        return shortCodePattern_;
    }


    protected String getShortCodePatternImpl(Context ctx)
    {
        try
        {
            final ClosedUserGroupTemplate cugTemplate = getTemplate(ctx);
            if (null != cugTemplate)
            {
                final String shortCodePattern = cugTemplate.getShortCodePattern();
                if (null != shortCodePattern && !shortCodePattern.isEmpty())
                {
                    return shortCodePattern;
                }
            }
            else
            {
                new MinorLogMsg(this, "Could not get CUG-Template and hence short code pattern for this CUG Instance ["
                        + getID() + "]", null).log(ctx);
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error locating CUG-Template and hence short code pattern for this CUG Instance ["
                    + getID() + "]", null).log(ctx);
        }
        return "";
    }
    
    public boolean isShortCodeEnable(Context ctx)
    {
        try
        {
            final ClosedUserGroupTemplate cugTemplate = getTemplate(ctx);
            if (null != cugTemplate)
            {
                return cugTemplate.isShortCodeEnable();
            }
            else
            {
                new MinorLogMsg(this, "Could not get CUG-Template and hence short code pattern for this CUG Instance ["
                        + getID() + "]", null).log(ctx);
            }
        }
        catch (Throwable t)
        {
            new MinorLogMsg(this, "Error locating CUG-Template and hence short code pattern for this CUG Instance ["
                    + getID() + "]", null).log(ctx);
        }
        return true;
    }
    
    /*@Override
    public int getSpid()
    {
        int spid = super.getSpid();
        if (DEFAULT_SPID == spid)
        {
            final Context ctx = ContextLocator.locate();
            if (null != ctx)
            {
                try
                {
                    ClosedUserGroupTemplate template = getTemplate(ctx);
                    if (null != template)
                    {
                        return template.getSpid();
                    }
                }
                catch (Throwable t)
                {
                    new MinorLogMsg(this, t.getMessage(), t).log(ctx);
                    return spid;
                }
            }
        }
        return spid;
    }*/
    
    @Override
    public void setCugTemplateID(long cugTemplateID)
    {
        super.setCugTemplateID(cugTemplateID);
        cugTemplate_ = null;
    }
    
    
    
	
	Subscriber owner;
	
	
	
	
	// for Charger
	
	List <String> newMsisdns; 
	List <String> removeddMsisdns; 
    Map <String, SubscriberAuxiliaryService> subAuxServices; 

	AuxiliaryService auxService_; 

	
	public AuxiliaryService getAuxiliaryService(Context ctx)
	throws HomeException
	{
		if ( auxService_ == null)
		{
			auxService_ = 
                CallingGroupSupport.getAuxiliaryServiceForCUGTemplate(
                        ctx, 
                        this.getCugTemplateID());
		}
		
		return auxService_; 
	}

	public List<String> getNewMsisdns() {
		if (newMsisdns == null)
		{
			newMsisdns = new ArrayList(); 
		}
		return newMsisdns;
	}

	public void setNewMsisdns(List<String> newMsisdns) {
		this.newMsisdns = newMsisdns;
	}

	public List<String> getRemoveddMsisdns() {
		if (removeddMsisdns == null )
		{
			removeddMsisdns = new ArrayList(); 
		}
		return removeddMsisdns;
	}

	public void setRemoveddMsisdns(List<String> removeddMsisdns) {
		this.removeddMsisdns = removeddMsisdns;
	}

	public Map<String, SubscriberAuxiliaryService> getSubAuxServices() {
		if ( subAuxServices == null )
		{
			subAuxServices = new HashMap(); 
		}
		return subAuxServices;
	}

	public void setSubAuxServices(Map<String, SubscriberAuxiliaryService> subAuxServices) {
		this.subAuxServices = subAuxServices;
	}

    // lazy loaded field
    private volatile ClosedUserGroupTemplate cugTemplate_ = null;
    private volatile String shortCodePattern_ = null;
    private volatile String dialingPattern_ = null;
}