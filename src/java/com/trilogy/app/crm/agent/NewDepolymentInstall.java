package com.trilogy.app.crm.agent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.trilogy.app.crm.bean.AccountCategory;
import com.trilogy.app.crm.bean.AccountCategoryHome;
import com.trilogy.app.crm.bean.AdjustmentType;
import com.trilogy.app.crm.bean.AdjustmentTypeHome;
import com.trilogy.app.crm.bean.CustomerTypeEnum;
import com.trilogy.app.crm.bean.DealerCode;
import com.trilogy.app.crm.bean.DealerCodeHome;
import com.trilogy.app.crm.bean.DiscountClass;
import com.trilogy.app.crm.bean.DiscountClassHome;
import com.trilogy.app.crm.bean.GLCodeMapping;
import com.trilogy.app.crm.bean.GLCodeMappingHome;
import com.trilogy.app.crm.bean.MsisdnGroup;
import com.trilogy.app.crm.bean.MsisdnGroupHome;
import com.trilogy.app.crm.bean.PackageGroup;
import com.trilogy.app.crm.bean.PackageGroupHome;
import com.trilogy.app.crm.bean.PricePlanHome;
import com.trilogy.app.crm.bean.PricePlanStateEnum;
import com.trilogy.app.crm.bean.PricePlanVersionHome;
import com.trilogy.app.crm.bean.ServiceHome;
import com.trilogy.app.crm.bean.ServicePeriodEnum;
import com.trilogy.app.crm.bean.ServiceTypeEnum;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.bean.SystemAdjustTypeMapping;
import com.trilogy.app.crm.bean.TaxComponentType;
import com.trilogy.app.crm.bean.TaxComponentTypeHome;
import com.trilogy.app.crm.bean.core.PricePlan;
import com.trilogy.app.crm.bean.core.PricePlanVersion;
import com.trilogy.app.crm.bean.core.Service;
import com.trilogy.app.crm.bean.core.ServiceFee2;
import com.trilogy.app.crm.bean.core.ServicePackageVersion;
import com.trilogy.app.crm.support.ExtensionSupportHelper;
import com.trilogy.app.crm.support.ExternalAppMappingSupportHelper;
import com.trilogy.app.crm.support.ExternalAppSupportHelper;
import com.trilogy.app.crm.technology.TechnologyEnum;
import com.trilogy.app.crm.xhome.home.ConfigShareTotalCachingHome;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.framework.xhome.home.GenericXMLHome;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeProxy;
import com.trilogy.framework.xhome.home.Homes;
import com.trilogy.framework.xhome.home.TransientHome;
import com.trilogy.framework.xhome.support.XMLSupport;
import com.trilogy.framework.xhome.xdb.XDBHome;
import com.trilogy.framework.xlog.log.LogSupport;
/**
 * @author dmishra
 * Create default configuration that would require end to end callflow work outofbox
 *
 */
public class NewDepolymentInstall implements ContextAgent
{

    @SuppressWarnings("unchecked")
    public void execute(Context ctx) throws AgentException
    {
        // create default adjustment types
        try
        {
            restore(ctx, AdjustmentType.class);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Adjustment Type : ", e);
        }
        // create SystemAdjustTypeMapping
        try
        {
            restore(ctx, SystemAdjustTypeMapping.class);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating SystemAdjustTypeMapping : ", e);
        }
        // Create the Default  service Handlers
        try
        {
            ExternalAppMappingSupportHelper.get(ctx).addExternalAppMappingBeans(ctx);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Service Handler : ", e);
        }
        //create default GL code
        try
        {
            final Home glCodeHome = (Home) ctx.get(GLCodeMappingHome.class);
            GLCodeMapping glCodeBean = new GLCodeMapping();
            glCodeBean.setSpid(1);
            glCodeBean.setGlCode("GL Code");
            glCodeHome.create(ctx, glCodeBean);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default GL code: ", e);
        }
        
      //create default Tax Component Type
        try
        {
            final Home taxComponentTypeHome = (Home) ctx.get(TaxComponentTypeHome.class);
            TaxComponentType componentType = XBeans.instantiate(TaxComponentType.class, ctx);
            componentType.setTaxCode("GST");
            componentType.setTaxDescription("GST");
            componentType.setTaxName("GST");
            taxComponentTypeHome.create(ctx, componentType);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Tax Component Type: ", e);
        }
        
        //create default Dealer code
        try
        {
            final Home dealerCodeHome = (Home) ctx.get(DealerCodeHome.class);
            DealerCode dealerCode = XBeans.instantiate(DealerCode.class, ctx);
            dealerCode.setCode("1");
            dealerCode.setSpid(1);
            dealerCode.setDesc("Default Dealer");
            dealerCodeHome.create(ctx, dealerCode);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Dealer code: ", e);
        }
        
        
        //create default Account Type
        try
        {
            final Home categoryHome = (Home) ctx.get(AccountCategoryHome.class);
            AccountCategory category = XBeans.instantiate(AccountCategory.class, ctx);
            category.setAllowBillCycleChange(true);
            category.setGLCode("GL Code");
            category.setCustomerType(CustomerTypeEnum.PERSONAL);
            category.setDescription("Subscriber");
            category.setName("Subscriber");
            category.setSpid(1);
            categoryHome.create(ctx, category);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Account Type: ", e);
        }
        
        //create default Discount Class
        try
        {
            final Home discountClassHome = (Home) ctx.get(DiscountClassHome.class);
            DiscountClass discountClass = XBeans.instantiate(DiscountClass.class, ctx);
            discountClass.setInvoiceDescription("Default Discount");
            discountClass.setGLCode("GL Code");
            discountClass.setName("Default Discount");
            discountClass.setSpid(1);
            discountClass.setTaxAuthority(1);
            discountClassHome.create(ctx, discountClass);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default Discount class: ", e);
        }
        
        //create default package group
        try
        {
            final Home pkgGroupHome = (Home) ctx.get(PackageGroupHome.class);
            PackageGroup pkgGroupBean = new PackageGroup();
            pkgGroupBean.setSpid(1);
            pkgGroupBean.setName("GSM Pkg");
            pkgGroupBean.setTechnology(TechnologyEnum.GSM);
            pkgGroupHome.create(ctx, pkgGroupBean);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default package group: ", e);
        }
        //create Mobile Number group
        try
        {
            final Home msisdnGroupHome = (Home) ctx.get(MsisdnGroupHome.class);
            MsisdnGroup msisdnGroupBean = new MsisdnGroup();
            msisdnGroupBean.setName("GSM Mobile Numbers");
            msisdnGroupBean.setTechnology(TechnologyEnum.GSM);
            msisdnGroupHome.create(ctx, msisdnGroupBean);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating fefault Mobile Number group: ", e);
        }
       
        Service voiceService=null;
        //create default Voice services
        try
        {
            voiceService= createService(ctx, ServiceTypeEnum.VOICE, "GSM-Voice");
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default service GSM-Voice: ", e);
        }
        Service smsService=null;
        
        try
        {
            smsService=createService(ctx, ServiceTypeEnum.SMS, "GSM-SMS");
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default service GSM-SMS: ", e);
        }
        
        Service dataService = null;
        try
        {
            dataService = createService(ctx, ServiceTypeEnum.DATA, "GSM-DATA");
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default service GSM-DATA: ", e);
        }
        
        try
        {
            //create prepaid priceplan
            
            createDefaultPricePlan(ctx, SubscriberTypeEnum.PREPAID, voiceService.getID(), smsService.getID(), dataService.getID());
           
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default prepaid Priceplan: ", e);
        }
        
        try
        {
            //create postpaid priceplan
            
            createDefaultPricePlan(ctx, SubscriberTypeEnum.POSTPAID, voiceService.getID(), smsService.getID(), dataService.getID());
           
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default postpaid Priceplan: ", e);
        }
        
        try
        {
            ExternalAppSupportHelper.get(ctx).addExternalAppResultCodes(ctx);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default external apps result codes: ", e);
        }

        try
        {
            ExternalAppSupportHelper.get(ctx).addExternalAppErrorCodeMessages(ctx);
        }
        catch (Exception e)
        {
            LogSupport.major(ctx, this, "Problem in creating default external apps error code messages: ", e);
        }
    }


    private Home createXMLHome(Context ctx, Class beanType)
    {
        return new GenericXMLHome(ctx, BACKUPDIR + beanType.getName() + ".xml", false, (Home) XBeans.getInstanceOf(ctx,
                beanType, TransientHome.class), (XMLSupport) XBeans.getInstanceOf(ctx, beanType, XMLSupport.class));
    }


    private void restore(Context ctx, Class beanType) throws HomeException
    {
        LogSupport.debug(ctx, this, "Saving " + beanType);
        Home to = null;
        if(!AdjustmentType.class.equals(beanType))
        {    
            to = (Home) ctx.get(XBeans.getClass(ctx, beanType, XDBHome.class));
        }
        else
        {
            HomeProxy h =  (HomeProxy) ctx.get(AdjustmentTypeHome.class);
            to = ((Home) h.findDecorator(ConfigShareTotalCachingHome.class));
        }
        Home from = createXMLHome(ctx, beanType);
        Homes.copy(from, to);
        LogSupport.debug(ctx, this, "Done" + beanType);
    }


    private Service createService(Context ctx, ServiceTypeEnum serviceType, String serviceName)
            throws HomeException
    {
        final Home serviceHome = (Home) ctx.get(ServiceHome.class);
        Service serviceBean = new Service();
        serviceBean.setName(serviceName);
        serviceBean.setSpid(1);
        serviceBean.setSubscriptionType(1);
        serviceBean.setTechnology(TechnologyEnum.GSM);
        serviceBean.setType(serviceType);
        serviceBean.setTaxAuthority(1);
        serviceBean.setAdjustmentGLCode("GL Code");
        serviceBean.setAdjustmentInvoiceDesc("");
        serviceBean.setServiceExtensions(ExtensionSupportHelper.get(ctx).wrapExtensions(ctx, new java.util.ArrayList<com.redknee.app.crm.extension.service.ServiceExtension>()));
        return (Service)serviceHome.create(ctx, serviceBean);
    }
    
    @SuppressWarnings("unchecked")
    private void createDefaultPricePlan(Context ctx, SubscriberTypeEnum billingType,long voiceServiceId,long smsServiceId, long dataServiceId) throws HomeException
    {
        final Home priceplanHome = (Home) ctx.get(PricePlanHome.class);
        PricePlan priceplanBean = new PricePlan();
        
        if (SubscriberTypeEnum.POSTPAID_INDEX == billingType.getIndex())
        {
            priceplanBean.setName("Postpaid PricePlan");
        }
        else
        {
            priceplanBean.setName("Prepaid PricePlan");
        }
        //priceplanBean.setPricePlanGroup(1);
        priceplanBean.setSpid(1);
        priceplanBean.setSubscriptionType(1);
        priceplanBean.setTechnology(TechnologyEnum.GSM);
        priceplanBean.setPricePlanType(billingType);
        priceplanBean.setSubscriptionLevel(1);
        priceplanBean.setVoiceRatePlan("1");
        priceplanBean.setSMSRatePlan("2");
        priceplanBean.setDataRatePlan("3");
        priceplanBean.setState(PricePlanStateEnum.ACTIVE);//Previously, the state variable was int, defaulted to 0 which meant active.
		//Now, with state type changed to ENUM, and active index being 4, priceplan needs to be explicitly set active
        priceplanBean=(PricePlan)priceplanHome.create(ctx, priceplanBean);
        // pp version
        PricePlanVersion ppversion = new PricePlanVersion();
        // Service Package Version
        ServicePackageVersion servicePkgVersion = new ServicePackageVersion();
        ServiceFee2 service1 = new ServiceFee2();
        service1.setEnabled(true);
        service1.setServiceId(voiceServiceId);
        service1.setServicePeriod(ServicePeriodEnum.MONTHLY);
        service1.setFee(1000);
        ServiceFee2 service2 = new ServiceFee2();
        service2.setEnabled(true);
        service2.setServiceId(smsServiceId);
        service2.setServicePeriod(ServicePeriodEnum.MONTHLY);
        service2.setFee(1000);
        
        ServiceFee2 service3 = new ServiceFee2();
        service3.setEnabled(true);
        service3.setServiceId(dataServiceId);
        service3.setServicePeriod(ServicePeriodEnum.MONTHLY);
        service3.setFee(1000);
        
        Map serviceFee = new HashMap();
        serviceFee.put(voiceServiceId, service1);
        serviceFee.put(smsServiceId, service2);
        serviceFee.put(dataServiceId, service3);
        servicePkgVersion.setServiceFees(serviceFee);
        ppversion.setDescription("Default Priceplan");
        ppversion.setServicePackageVersion(servicePkgVersion);
        ppversion.setVersion(0);
        ppversion.setActivateDate(new Date());
        ppversion.setId(priceplanBean.getId());
        if(SubscriberTypeEnum.POSTPAID_INDEX == billingType.getIndex())
        { ppversion.setDeposit(10000);
          ppversion.setCreditLimit(100000);
        } 
        // priceplanversion
        Home ppVerHome = (Home) ctx.get(PricePlanVersionHome.class);
        ppVerHome.create(ctx, ppversion);
//        priceplanBean.setVersions(ppversion);
        priceplanHome.store(ctx, priceplanBean);
    }
    
    public static String BACKUPDIR = "/opt/redknee/app/crm/current/cfg/new_deployment/xml/";
}
