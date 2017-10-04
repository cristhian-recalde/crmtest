package com.trilogy.app.crm.technology;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.trilogy.app.crm.bean.GSMPackage;
import com.trilogy.app.crm.bean.GSMPackageHome;
import com.trilogy.app.crm.bean.LnpReqirementEnum;
import com.trilogy.app.crm.bean.Msisdn;
import com.trilogy.app.crm.bean.MsisdnHome;
import com.trilogy.app.crm.bean.Subscriber;
import com.trilogy.app.crm.bean.SubscriberHome;
import com.trilogy.app.crm.bean.SubscriberStateEnum;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversion;
import com.trilogy.app.crm.bean.SubscriberTechnologyConversionWebControl;
import com.trilogy.app.crm.bean.TDMAPackage;
import com.trilogy.app.crm.bean.TDMAPackageHome;
import com.trilogy.app.crm.support.PricePlanSupport;
import com.trilogy.framework.xhome.beans.ExceptionListener;
import com.trilogy.framework.xhome.beans.SafetyUtil;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.msp.SetSpidProxyWebControl;
import com.trilogy.framework.xhome.web.agent.WebAgents;
import com.trilogy.framework.xhome.web.renderer.ButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultButtonRenderer;
import com.trilogy.framework.xhome.web.renderer.DefaultFormRenderer;
import com.trilogy.framework.xhome.web.renderer.FormRenderer;
import com.trilogy.framework.xhome.webcontrol.HTMLExceptionListener;
import com.trilogy.framework.xhome.webcontrol.OutputWebControl;
import com.trilogy.framework.xhome.webcontrol.RequestServicer;
import com.trilogy.framework.xhome.webcontrol.WebControl;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.app.crm.bean.TDMAPackageXInfo;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;

public class TechnologyConversion implements RequestServicer {
	
	private WebControl wc = new SetSpidProxyWebControl(new SetTechnologyProxyWebControl(new SubscriberTechnologyConversionWebControl()));
	
	public void service(final Context ctx, final HttpServletRequest req, final HttpServletResponse res)
	throws ServletException, IOException
    {
		
		PrintWriter out = WebAgents.getWriter(ctx);
		Subscriber sub = (Subscriber) ctx.get(Subscriber.class);
		
		Context subContext = ctx.createSubContext();		
		final ButtonRenderer buttonRenderer = (ButtonRenderer) subContext.get(ButtonRenderer.class,
				DefaultButtonRenderer.instance());

		SubscriberTechnologyConversion conObject = new SubscriberTechnologyConversion();
		wc.fromWeb(subContext,conObject,req,"SubscriberTechnologyConversion");
		if (sub != null)
		{
			conObject.setId(sub.getId());
			conObject.setSpid(sub.getSpid());
			conObject.setSubscriberType(sub.getSubscriberType());
		}
		
		
		if (buttonRenderer.isButton(subContext, "Convert"))
		{	
			final MessageMgr manager = new MessageMgr(subContext, this);
			final HTMLExceptionListener exceptions = new HTMLExceptionListener(manager);
			subContext.put(ExceptionListener.class, exceptions);
						
			try
			{
				//validatePackage(ctx,conObject.getTechnology(),conObject.getPackageId());
				validatePackage(ctx,conObject.getTechnology(),conObject.getPackageId(),conObject.getSpid());
			}
			catch(Exception e)
			{
				exceptions.thrown(e);
			}
			
			if(conObject.getPricePlan() == -1)
			{
				exceptions.thrown(new HomeException("Please select a valid price plan"));
			}
			
			if(exceptions.hasErrors())
			{
				exceptions.toWeb(subContext, out, "", conObject);
			}
			else
			{
				// do the conversion
				try
				{
					convert(ctx,sub,conObject);
					out.write("<font color=green><b>Conversion successful</b></font>");
				}
				catch(Exception e)
				{
					out.write("<font color=red><b>Conversion Failed, Please see log for more details</b></font>");
					out.write("<br><b>" + e.getMessage() + "</b>");
					LogSupport.debug(ctx,this,"Technology Conversion failed for sub " + sub.getId(),e);
				}
			}
		}
		else			
		{
			if(sub == null)
			{
				out.write("<font color=red><b>Subscriber reference is null,Please select subscriber again</b></font>");
				return;
			}
			if(!validTechnologyTypeForConversion(sub))
			{
				out.write("<font color=red><b>Only TDMA to GSM is supported</b></font>");
				return;
			}
			if(!validStatesForConversion(sub))
			{
				out.write("<font color=red><b>The subscriber has to be in Active, Suspend or Expired state</b></font>");
				return;
			}		
			
		}		
		renderScreen(ctx,out,conObject,buttonRenderer);
		
	}

	private void renderScreen(Context ctx, PrintWriter out, SubscriberTechnologyConversion conObject, ButtonRenderer buttonRenderer) 
	{
		ctx.put("MODE", OutputWebControl.EDIT_MODE);
		FormRenderer form = (FormRenderer)ctx.get(FormRenderer.class,DefaultFormRenderer.instance());
		form.Form(out,ctx);
		wc.toWeb(ctx,out,"SubscriberTechnologyConversion",conObject);
		out.print("<br>");
		buttonRenderer.inputButton(out, ctx, "Preview");
		out.print("&nbsp;");
		buttonRenderer.inputButton(out, ctx, "Convert");
		form.FormEnd(out);
	}
	
	private boolean validTechnologyTypeForConversion(Subscriber sub) {
		return (sub.getTechnology() == TechnologyEnum.TDMA);
	}

	private void convert(Context ctx, Subscriber sub, SubscriberTechnologyConversion conObject) throws Exception
	{
		Home msisdnHome = (Home)ctx.get(MsisdnHome.class);
		Msisdn msisdn = (Msisdn)msisdnHome.find(ctx,sub.getMSISDN());
		int oldMsisdnGroup = msisdn.getGroup();
		
		msisdn.setTechnology(conObject.getTechnology());
		msisdn.setGroup(conObject.getMSISDNGroup());
		msisdn.setLnpRequired(LnpReqirementEnum.REQUIRED);
		
		msisdnHome.store(ctx,msisdn);
		try
		{
			Home subHome = (Home)ctx.get(SubscriberHome.class);
			sub.setTechnology(conObject.getTechnology());

            sub.switchPricePlan(ctx, conObject.getPricePlan());

			sub.setPricePlan(conObject.getPricePlan());
			sub.setPricePlanVersion(PricePlanSupport.getCurrentVersion(ctx,conObject.getPricePlan()).getVersion());
			// Set the secondary priceplan version to -1, we don't want it to have PP of different technology type
			sub.setSecondaryPricePlan(-1);
			sub.setSecondaryPricePlanStartDate(null);
			sub.setIntentToProvisionServices(conObject.getServicesForDisplay());
			sub.setPackageId(conObject.getPackageId());
			
			subHome.store(ctx,sub);
		}
		catch(Exception e)
		{
			LogSupport.debug(ctx,this,"Couldn't save sub " + sub.getId() + " after technology conversion due to the following error",e);
			//reverse
			msisdn.setTechnology(conObject.getConvertFrom());
			msisdn.setGroup(oldMsisdnGroup);
			msisdn.setLnpRequired(LnpReqirementEnum.NOT_REQUIRED);
			msisdnHome.store(ctx,msisdn);
			
			throw new HomeException("Couldn't save sub " + sub.getId() + " after technology conversion due to the following error",e);
		}
	}

	private boolean validStatesForConversion(Subscriber sub) 
	{
		switch(sub.getState().getIndex())
		{
			case SubscriberStateEnum.ACTIVE_INDEX : 
			case SubscriberStateEnum.SUSPENDED_INDEX : 
			case SubscriberStateEnum.EXPIRED_INDEX:
			{
				return true;
			}
			default :
			{
				return false;
			}
		}

	}

	private void validatePackage(Context ctx,TechnologyEnum tech,String packageId , int spid  ) throws Exception
	{
		if(TechnologyEnum.GSM == tech)
        {
            final Home packHome = (Home) ctx.get(GSMPackageHome.class);
            final GSMPackage pk = (GSMPackage) packHome.find(ctx,packageId);
            if (pk == null)
            {
            	throw new HomeException("GSM Package not found");
            }
        }
        else if(TechnologyEnum.TDMA == tech || TechnologyEnum.CDMA == tech)
        {
        	  And and = new And();
      			  and.add(new EQ(TDMAPackageXInfo.PACK_ID, packageId));
      			  and.add(new EQ(TDMAPackageXInfo.SPID, spid));
      	      
            final Home pkgHome = (Home) ctx.get(TDMAPackageHome.class);
            final TDMAPackage pk = (TDMAPackage) pkgHome.find(ctx, and);
            if (pk == null)
            {
            	throw new HomeException(tech + " Package not found");
            }
        }
		
	}

}
