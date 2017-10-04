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
package com.trilogy.app.crm.home.validator;

import com.trilogy.app.crm.bean.provision.ExternalProvisionStrategyEnum;
import com.trilogy.app.crm.support.ExternalProvisioningConfigSupport;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;
import com.trilogy.framework.xhome.home.HomeInternalException;
import com.trilogy.framework.xhome.home.HomeProxy;

/**
 * Home that invokes a smaller ValidatorHome pipeline before continuing 
 * to call delegate actions.
 * 
 * Depending on what is installed in the validated pipeline, the primary pipeline's
 * delegate actions will be performed at one of two orders: 
 *  1) chain the Home pipeline provisioning action after the HomeValidator's provisioning action has been performed (HomeChainingHomeValidator class), or 
 *  2) execute the Home pipeline after the HomeValidator's pipeline has been completely executed (HomeSequenceHomeValidator class) 
 *
 * @author angie.li@redknee.com
 */ 
public class ValidatedHome extends HomeProxy 
{

    /**
     * Decorate the given Home pipeline with the given HomeValidators.
     * The given Home pipeline will be validated by the given HomeValidators 
     * @param context
     * @param validatedPipe HomeValidator (validated) pipeline
     * @param primaryPipe Home (not validated) pipeline
     * @param bridgeToPrimaryPipeline TRUE install the primaryPipeline (Home) in the HomeValidator, in a chain; Otherwise, 
     *                                     install the primaryPipeline (Home) to be performed in sequence after the HomeValidtor provisioning action.
     */
    public ValidatedHome(Context context, HomeValidator validatedPipe, Home primaryPipe, boolean bridgeToPrimaryPipeline)
    {
        super(context, primaryPipe);
        // Install the Primary Pipeline delegate according to the selection
        if (bridgeToPrimaryPipeline)
        {
            validatedPipeline_ = installDelegateAsChain(context, validatedPipe, primaryPipe);
        }
        else
        {
            validatedPipeline_ = installDelegateInSequence(context, validatedPipe, primaryPipe);
        }
    }
    
    /**
     * Validate then Resolve conflicts (if configured) in the Home Validator pipeline.
     * Then continue with the delegate Home pipeline.
     */
    public Object create(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        try
        {
            ExternalProvisioningException el = new ExternalProvisioningException();
            validatedPipeline_.validateCreate(context, obj, el);
        }
        catch (ExternalProvisioningException e)
        {
            //Get System config
            if (ExternalProvisioningConfigSupport.getCreateStrategy(context).equals(ExternalProvisionStrategyEnum.ABORT))
            {
                // Throw error and abort the Subscriber external provisioning action.
                throw new HomeException(e.getMessage(), e);  
            }
            validatedPipeline_.resolveCreateConflict(context, obj, e);
        }        

        /* Depending on what is installed in the Validated pipeline, either: 
         *  1) delegate to the rest of the CRM provisioning pipeline, or 
         *  2) execute the rest of the CRM provisioning pipeline after the validatedPipeline
         *     has been completely executed. See class description.*/
        return validatedPipeline_.create(context, obj);
    }
    
    /**
     * Validate then Resolve conflicts (if configured) in the Home Validator pipeline.
     * Then continue with the delegate Home pipeline.
     */
    public void remove(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        try
        {
            ExternalProvisioningException el = new ExternalProvisioningException();
            validatedPipeline_.validateRemove(context, obj, el);
        }
        catch (ExternalProvisioningException e)
        {
            if (ExternalProvisioningConfigSupport.getRemoveStrategy(context).equals(ExternalProvisionStrategyEnum.ABORT))
            {
                // Throw error and abort the Subscriber external provisioning action.
                throw new HomeException(e.getMessage(), e);  
            }
            validatedPipeline_.resolveRemoveConflict(context, obj, e);
        }

        /* Depending on what is installed in the Validated pipeline, either: 
         *  1) delegate to the rest of the CRM provisioning pipeline, or 
         *  2) execute the rest of the CRM provisioning pipeline after the validatedPipeline
         *     has been completely executed. See class description.*/
        validatedPipeline_.remove(context, obj);
    }
    
    /**
     * Validate then Resolve conflicts (if configured) in the Home Validator pipeline.
     * Then continue with the delegate Home pipeline.
     */
    public Object store(Context context, Object obj)
        throws HomeException, HomeInternalException
    {
        try
        {
            ExternalProvisioningException el = new ExternalProvisioningException();
            validatedPipeline_.validateStore(context, obj, el);
        }
        catch (ExternalProvisioningException e)
        {
            //Get System configuration
            if (ExternalProvisioningConfigSupport.getStoreStrategy(context).equals(ExternalProvisionStrategyEnum.ABORT))
            {
                // Throw error and abort the Subscriber external provisioning action.
                throw new HomeException(e.getMessage(), e);  
            }
            validatedPipeline_.resolveStoreConflict(context, obj, e);
        }

        /* Depending on what is installed in the Validated pipeline, either: 
         *  1) delegate to the rest of the CRM provisioning pipeline, or 
         *  2) execute the rest of the CRM provisioning pipeline after the validatedPipeline
         *     has been completely executed. See class description.*/
        return validatedPipeline_.store(context, obj);
    }
    
    
    /**
     * Installs the HomeChainingHomeValidator at the end of the given HomeValidator pipeline
     * @param context
     * @param validatedPipe - the validated pipeline
     * @param primaryPipe - the normal Home pipeline
     * @return the new HomeValidator pipeline
     */
    private HomeValidator installDelegateAsChain(Context context, HomeValidator validatedPipe, Home primaryPipe)
    {
        AbstractValidatorHome delegate = (AbstractValidatorHome) validatedPipe;
        while (!(delegate.getDelegate() instanceof NullValidatorHome))
        {
            delegate = (AbstractValidatorHome) delegate.getDelegate();
        }
        delegate.setDelegate(new HomeChainingHomeValidator(context, primaryPipe));
        return validatedPipe;
    }
    
    /**
     * Installs the HomeSequenceHomeValidator at the start of the given HomeValidator pipeline.
     * @param context
     * @param validatedPipe - the validated pipeline
     * @param primaryPipe - the normal Home pipeline
     * @return the new HomeValidator pipeline
     */
    private HomeValidator installDelegateInSequence(Context context, HomeValidator validatedPipe, Home primaryPipe)
    {
        return new HomeSequenceHomeValidator(context, validatedPipe, primaryPipe);
    }
    
    private HomeValidator validatedPipeline_ = null; 
}
