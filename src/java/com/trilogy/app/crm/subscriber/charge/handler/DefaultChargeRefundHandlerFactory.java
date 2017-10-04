package com.trilogy.app.crm.subscriber.charge.handler;

import java.util.HashMap;

import com.trilogy.app.crm.bean.ChargerHandlerConfigID;
import com.trilogy.app.crm.bean.SubscriberTypeEnum;
import com.trilogy.app.crm.subscriber.charge.ChargableItemResult;
import com.trilogy.app.crm.subscriber.charge.ChargingConstants;
import com.trilogy.framework.xhome.context.Context;

public class DefaultChargeRefundHandlerFactory 
implements ChargeRefundResultHandlerFactory, 
ChargingConstants
{

    
    public DefaultChargeRefundHandlerFactory(Context ctx)
    {
        init(ctx);  
    }
    
    /**
     * will initialize handler on demand in future, when we implements the handler and 
     * make it configurable, 
     * @param source
     * @return
     */
    private ChargeRefundResultHandler getHandler(ChargerHandlerConfigID source)
    {
        return  (ChargeRefundResultHandler) handlers.get(createKey(source));
    }
        
    
    public ChargeRefundResultHandler create(Context ctx, ChargableItemResult source)
    {
        ChargerHandlerConfigID configId = new ChargerHandlerConfigID(
                    source.getSubscriber().getSubscriberType().getIndex(), 
                    source.getAction(), 
                    source.getChargableItemType()); 
        return create(ctx, configId); 
        
    }

    public ChargeRefundResultHandler create(Context ctx, ChargerHandlerConfigID configId)
    {
        ChargeRefundResultHandler handler = getHandler(configId);
        
        if ( handler == null )
        {
            handler = getDefaultHandler(configId);
        }
        
       return handler;     
        
    }

 
    private ChargeRefundResultHandler getDefaultHandler(ChargerHandlerConfigID handlerId)
    {
         
        switch(handlerId.getAction())
        {
            case ACTION_PROVISIONING_REFUND:
                return DEFAULT_REFUND_HANDLER;
            default:
                return  DEFAULT_CHARGE_HANDLER;       
         }

    }
    
    
    private void init(Context ctx)
    {
        
        ChargeRefundResultHandler handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new SuspendedEntityReleaseHandler( 
                ))));
        
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_PACKAGE ), handler);
              
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new SuspendedEntityReleaseHandler( 
                new SuspendingPostpaidServiceHandler()
                ))));
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_SERVICE ), handler);        
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new SuspendedEntityReleaseHandler( 
                new SuspendingPostpaidBundleHandler()
                ))));          
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new SuspendedEntityReleaseHandler( 
                new SuspendingAuxServiceHandler(        
                )))));          
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_AUX_SERVICE ), 
               handler);
           
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new PrepaidHandler(        
                new SuspendedEntityReleaseHandler(        
                new SuspendingServiceHandler(
                ))))));
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_SERVICE ), handler);
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new PrepaidHandler(        
                new SuspendedEntityReleaseHandler(
                new SuspendingBundleHandler(  ))))));
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
        
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new PrepaidHandler(        
                new SuspendedEntityReleaseHandler(        
                new SuspendingPackageHandler())))));

        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_PACKAGE ), handler);
        
        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new PrepaidHandler(        
                new SuspendedEntityReleaseHandler(        
                new SuspendingAuxServiceHandler())))));
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_PROVISIONING_CHARGE, CHARGABLE_ITEM_AUX_SERVICE ), 
                handler);
    
        handler =  DEFAULT_REFUND_HANDLER;          
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_SERVICE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_BUNDLE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_PACKAGE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_AUX_SERVICE ), 
             handler);
     
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_SERVICE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_BUNDLE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_PACKAGE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
             ACTION_PROVISIONING_REFUND, CHARGABLE_ITEM_AUX_SERVICE ), 
             handler);
        
        //not used for now since postpaid sub wont' get their service suspended. 
        handler = DEFAULT_CHARGE_HANDLER;
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_SERVICE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_PACKAGE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_AUX_SERVICE ), handler);

        handler =  new SkippedChargingTypeHandler( 
                new IgnoreUnapplicableTransactionHandler( 
                new LoggingHandler(
                new PrepaidHandler(        
                new SuspendedEntityReleaseHandler(        
                )))));
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_SERVICE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
        installHandler(ctx,new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_PACKAGE ), handler);
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_UNSUSPENDING_CHARGE, CHARGABLE_ITEM_AUX_SERVICE ), handler);

        
        handler = new LoggingHandler();
      
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.POSTPAID_INDEX,  
                ACTION_BUNDLE_OVERUSAGE_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
        installHandler(ctx, new ChargerHandlerConfigID(SubscriberTypeEnum.PREPAID_INDEX,  
                ACTION_BUNDLE_OVERUSAGE_CHARGE, CHARGABLE_ITEM_BUNDLE ), handler);
    }    
    
    
    
    
    
    
    
    /**
     * open a back door for changing the handler pipeline by configuration. 
     * although it is not necessary to be used for now. 
     * @param ctx
     * @param key
     * @param defaultHandler
     */
    private void installHandler(final Context ctx, 
            final ChargerHandlerConfigID key, 
            final ChargeRefundResultHandler defaultHandler)
    {
    	
        handlers.put(createKey(key), defaultHandler); 
    }
     
    public String createKey( final ChargerHandlerConfigID key )
    {
    	return String.valueOf(key.getSubType()) + String.valueOf(key.getAction()) + String.valueOf(key.getChargableItem()); 
    }
    
    
    private HashMap handlers = new HashMap();   

    public static final ChargeRefundResultHandler    DEFAULT_REFUND_HANDLER =  new SkippedChargingTypeHandler( 
            new IgnoreUnapplicableTransactionHandler( new LoggingHandler()));
    public static final ChargeRefundResultHandler  DEFAULT_CHARGE_HANDLER = new SkippedChargingTypeHandler(
                    new IgnoreUnapplicableTransactionHandler(  new LoggingHandler(  new SuspendedEntityReleaseHandler()))); 
    
}
