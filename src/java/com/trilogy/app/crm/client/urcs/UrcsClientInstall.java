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
package com.trilogy.app.crm.client.urcs;

import com.trilogy.app.crm.client.AppEcpClient;
import com.trilogy.app.crm.client.AppEcpClientImpl;
import com.trilogy.app.crm.client.AppOcgClient;
import com.trilogy.app.crm.client.AppPinManagerClient;
import com.trilogy.app.crm.client.AppPinManagerCorbaClient;
import com.trilogy.app.crm.client.PricePlanMgmtClient;
import com.trilogy.app.crm.client.PricePlanMgmtClientV2;
import com.trilogy.app.crm.client.PricePlanMgmtCorbaClient;
import com.trilogy.app.crm.client.PricePlanMgmtCorbaClientV2;
import com.trilogy.app.crm.client.SubscriberLanguageClient;
import com.trilogy.app.crm.client.SubscriberLanguageCorbaClient;
import com.trilogy.app.crm.client.bm.BalanceManagementSupport;
import com.trilogy.app.crm.client.bm.SubscriberProfileProvisionClient;
import com.trilogy.app.crm.client.ipcg.CDMAProductS5600IpcgClient;
import com.trilogy.app.crm.client.ipcg.CDMAProductS5600IpcgCorbaClient;
import com.trilogy.app.crm.client.ipcg.CDMASubscriberProv;
import com.trilogy.app.crm.client.ipcg.GSMProductS5600IpcgClient;
import com.trilogy.app.crm.client.ipcg.GSMProductS5600IpcgCorbaClient;
import com.trilogy.app.crm.client.smsb.AppSmsbClient;
import com.trilogy.app.crm.external.vra.PricePlanVoucherTypeMappingServiceRmiClient;
import com.trilogy.app.crm.external.vra.VoucherInfoRetrieveServiceRmiClient;
import com.trilogy.app.crm.external.vra.VoucherRedemptionServiceRmiClient;
import com.trilogy.app.crm.ff.FFECarePMDecorator;
import com.trilogy.app.crm.ff.FFECareRmiServiceClient;
import com.trilogy.app.ff.ecare.rmi.FFECareRmiService;
import com.trilogy.app.vra.interfaces.RMIPricePlanVoucherTypeMappingService;
import com.trilogy.app.vra.interfaces.RMIVoucherInfoRetrieveService;
import com.trilogy.app.vra.interfaces.VoucherRedemptionService;
import com.trilogy.framework.xhome.context.AgentException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAgent;
import com.trilogy.product.s5600.ipcg.provisioning.SubscriberProv;
import com.trilogy.util.snippet.log.Logger;

/**
 * @author rchen
 */
public final class UrcsClientInstall implements ContextAgent
{
    public static final Class<AppPinManagerClient>                         APP_PIN_MANAGER_CLIENT_KEY                      = AppPinManagerClient.class;
    public static final Class<AppOcgClient>                                APP_OCG_CLIENT_KEY                              = com.redknee.app.crm.core.agent.ServiceInstall.APP_OCG_CLIENT_KEY;
    public static final Class<AppEcpClient>                                APP_ECP_CLIENT_KEY                              = AppEcpClient.class;
    public static final Class<PromotionProvisionClient>                    PROMOTION_PROVISION_CLIENT_KEY                  = PromotionProvisionClient.class;
    public static final Class<PromotionManagementClient>                   PROMOTION_MANAGEMENT_CLIENT_KEY                 = PromotionManagementClient.class;
    public static final Class<PromotionManagementClientV2>                 PROMOTION_MANAGEMENT_CLIENT_V2_KEY              = PromotionManagementClientV2.class;
    public static final Class<AppSmsbClient>                               APP_SMSB_CLIENT_KEY                             = AppSmsbClient.class;
    public static final Class<BGroupPricePlanMgmtCorbaClient>              BUSINESS_GROUP_PRICE_PLAN_CLIENT_KEY            = BGroupPricePlanMgmtCorbaClient.class;
    public static final Class<ScreeningTemplatesServiceClient>             SCREENING_TEMPLATES_SERVICE_CLIENT_KEY          = ScreeningTemplatesServiceClient.class;
    public static final Class<FFECareRmiServiceClient>                     APP_FF_CLIENT_KEY                               = FFECareRmiServiceClient.class;
    public static final Class<VoucherRedemptionServiceRmiClient>           VOUCHER_REDEPTION_SERVICE_RMI_KEY               = VoucherRedemptionServiceRmiClient.class;
    public static final Class<PricePlanVoucherTypeMappingServiceRmiClient> PRICE_PLAN_VOUCHER_TYPE_MAPPING_SERVICE_RMI_KEY = PricePlanVoucherTypeMappingServiceRmiClient.class;
    public static final Class<VoucherInfoRetrieveServiceRmiClient>         VOUCHER_INFO_RETRIEVE_SERVICE_RMI_KEY           = VoucherInfoRetrieveServiceRmiClient.class;
    public static final Class<PricePlanMgmtClient>                         PRICE_PLAN_MGMT_CLIENT_KEY                      = PricePlanMgmtClient.class;
    public static final Class<PricePlanMgmtClientV2>                       PRICE_PLAN_MGMT_CLIENT_V2_KEY                   = PricePlanMgmtClientV2.class;
    public static final Class<SubscriberProfileProvisionClient>            SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY         = SubscriberProfileProvisionClient.class;
    public static final Class<SubscriberLanguageClient>                    SUBSCRIBER_PROV_LANG_CLIENT_KEY                 = SubscriberLanguageClient.class;
    public static final Class<LoyaltyProvisionClient>                      LOYALTY_PROVISION_CLIENT_KEY                    = LoyaltyProvisionClient.class;
    public static final Class<LoyaltyOperationClient>                      LOYALTY_OPERATION_CLIENT_KEY                    = LoyaltyOperationClient.class;
    public static final Class<GSMProductS5600IpcgClient>                   PRODUCT_S5600_IPCG_CLIENT_KEY                   = GSMProductS5600IpcgClient.class;
    public static final Class<CDMAProductS5600IpcgClient>                  PRODUCT_S5600_IPCG_CDMA_CLIENT_KEY              = CDMAProductS5600IpcgClient.class;
    private static final Class<AccountOperationsClientV4>                  ACCOUNT_OPERATIONS_CLIENT_V4_KEY                   = AccountOperationsClientV4.class;
    public static final Class<ScreeningTemplateProvisionClient>            SCREENING_TEMPLATE_PROVISION_KEY                    = ScreeningTemplateProvisionClient.class;
    
    private UrcsClientInstall()
    {}

    public static UrcsClientInstall INSTANCE = new UrcsClientInstall();


    @SuppressWarnings("unchecked")
    public static <T> T getClient(final Context ctx, Class<T> key)
    {
        return (T) ctx.get(key);
    }


    @Override
    public void execute(Context ctx) throws AgentException
    {
        Logger.info(ctx, this, "Installing URCS clients...", null);

        final AppSmsbClient clientSmsb = new AppSmsbClient(ctx);
        ctx.put(APP_SMSB_CLIENT_KEY, clientSmsb);

        final FFECareRmiService ffEcareRmiService = new FFECarePMDecorator(ctx, new FFECareRmiServiceClient(ctx));
        ctx.put(APP_FF_CLIENT_KEY, ffEcareRmiService);

        final VoucherRedemptionService voucherRedemptionRmiService = new VoucherRedemptionServiceRmiClient(ctx);
        ctx.put(VOUCHER_REDEPTION_SERVICE_RMI_KEY, voucherRedemptionRmiService);

        final RMIPricePlanVoucherTypeMappingService pricePlanVoucherTypeMappingServiceRmiClient = new PricePlanVoucherTypeMappingServiceRmiClient(ctx);
        ctx.put(PRICE_PLAN_VOUCHER_TYPE_MAPPING_SERVICE_RMI_KEY, pricePlanVoucherTypeMappingServiceRmiClient);
        
        final RMIVoucherInfoRetrieveService voucherInfoRetrieveServiceRmiClient = new VoucherInfoRetrieveServiceRmiClient(ctx);
        ctx.put(VOUCHER_INFO_RETRIEVE_SERVICE_RMI_KEY, voucherInfoRetrieveServiceRmiClient);
        
        final AppEcpClientImpl clientEcp = new AppEcpClientImpl(ctx);
        ctx.put(APP_ECP_CLIENT_KEY, clientEcp);

        // final EcpBearerServiceCorbaClient clientEcpBearer = new EcpBearerServiceCorbaClient(ctx);
        // ctx.put(ECP_BEARER_CLIENT_KEY, clientEcpBearer);
        //        
        // final AppEcpVpnClient clientEcpVpn = new AppEcpVpnClient(ctx);
        // ctx.put(APP_ECP_VPN_CLIENT_KEY, clientEcpVpn);

        // NOTE: AppOcgClient is installed by AppCrmCore's ServiceInstall
        
        final AppPinManagerCorbaClient clientPinManager = new AppPinManagerCorbaClient(ctx);
        ctx.put(APP_PIN_MANAGER_CLIENT_KEY, clientPinManager);

        final GSMProductS5600IpcgCorbaClient productS5600Ipcg = new GSMProductS5600IpcgCorbaClient(ctx);
        ctx.put(PRODUCT_S5600_IPCG_CLIENT_KEY, productS5600Ipcg);
        
        final CDMAProductS5600IpcgCorbaClient productS5600IpcgCDMA = new CDMAProductS5600IpcgCorbaClient(ctx);
        ctx.put(PRODUCT_S5600_IPCG_CDMA_CLIENT_KEY, productS5600IpcgCDMA);

        final SubscriberLanguageCorbaClient clientSubLang = new SubscriberLanguageCorbaClient(ctx);
        ctx.put(SUBSCRIBER_PROV_LANG_CLIENT_KEY, clientSubLang);
        
        final PricePlanMgmtCorbaClient clientPlanMgmt = new PricePlanMgmtCorbaClient(ctx);
        ctx.put(PRICE_PLAN_MGMT_CLIENT_KEY, clientPlanMgmt);
        
        final PricePlanMgmtCorbaClientV2 clientPlanMgmtV2 = new PricePlanMgmtCorbaClientV2(ctx);
        ctx.put(PRICE_PLAN_MGMT_CLIENT_V2_KEY, clientPlanMgmtV2);

        final BGroupPricePlanMgmtCorbaClient clientBGPP = new BGroupPricePlanMgmtCorbaClient(ctx);
        ctx.put(BUSINESS_GROUP_PRICE_PLAN_CLIENT_KEY, clientBGPP);

        final SubscriberProfileProvisionClient subProfileProvClient = BalanceManagementSupport.installSubscriberProfileProvisionClient(ctx);
        ctx.put(SUBSCRIBER_PROFILE_PROVISION_CLIENT_KEY, subProfileProvClient);
        
        final PromotionProvisionClient clientPrvsn = new PromotionProvisionClientImpl(ctx);
        ctx.put(PROMOTION_PROVISION_CLIENT_KEY, clientPrvsn);

        final PromotionManagementClient clientMngmnt = new PromotionManagementClientImpl(ctx);
        ctx.put(PROMOTION_MANAGEMENT_CLIENT_KEY, clientMngmnt);

        final PromotionManagementClientV2 clientMngmnt2 = new PromotionManagementClientV2Impl(ctx);
        ctx.put(PROMOTION_MANAGEMENT_CLIENT_V2_KEY, clientMngmnt2);
        
        final ScreeningTemplatesServiceClient screeningTemplateClient = new ScreeningTemplatesServiceCorbaClient(ctx);
        ctx.put(SCREENING_TEMPLATES_SERVICE_CLIENT_KEY, screeningTemplateClient);
        
        final LoyaltyProvisionClient loyaltyProvisionClient = new LoyaltyProvisionClientImpl(ctx);
        ctx.put(LOYALTY_PROVISION_CLIENT_KEY, loyaltyProvisionClient);
        
        final LoyaltyOperationClient loyaltyOperationClient = new LoyaltyOperationClientImpl(ctx);
        ctx.put(LOYALTY_OPERATION_CLIENT_KEY, loyaltyOperationClient);  
        
        ctx.put(ACCOUNT_OPERATIONS_CLIENT_V4_KEY, new AccountOperationsClientV4Impl(ctx));
        
        final ScreeningTemplateProvisionClient screeningTemplateProvisionClient = new ScreeningTemplateProvisionClientImpl(ctx);
        ctx.put(SCREENING_TEMPLATE_PROVISION_KEY, screeningTemplateProvisionClient);
        
        installCustomizedClients(ctx);
    }
    
    private void installCustomizedClients(Context ctx) throws AgentException
    {
        com.redknee.app.urcs.client.agent.Install.installClient(ctx, SubscriberProv.class, CDMASubscriberProv.class, new com.redknee.app.urcs.client.legacy.s5600.SubProvisionPlugin());
    }
}
