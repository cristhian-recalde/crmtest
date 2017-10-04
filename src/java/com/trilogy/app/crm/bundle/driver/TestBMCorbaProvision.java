/*
 *  TestBucketProvHome.java
 *
 * This code is a protected work and subject to domestic and international
 * copyright law(s). A complete listing of authors of this work is readily
 * available. Additionally, source code is, by its very nature, confidential
 * information and inextricably contains trade secrets and other information
 * proprietary, valuable and sensitive to Redknee, no unauthorised use,
 * disclosure, manipulation or otherwise is permitted, and may only be used
 * in accordance with the terms of the licence agreement entered into with
 * Redknee Inc. and/or its subsidiaries.
 *
 * Copyright (c) Redknee Inc. (Migreated for testing purposes) and its subsidiaries. All Rights Reserved.
 */
package com.trilogy.app.crm.bundle.driver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.trilogy.app.crm.bean.core.BundleProfile;
import com.trilogy.app.crm.bundle.ActivationFeeCalculationEnum;
import com.trilogy.app.crm.bundle.ActivationTypeEnum;
import com.trilogy.app.crm.bundle.BundleCategory;
import com.trilogy.app.crm.bundle.BundleCategoryAssociation;
import com.trilogy.app.crm.bundle.BundleCategoryHome;
import com.trilogy.app.crm.bundle.BundleCategoryTransientHome;
import com.trilogy.app.crm.bundle.BundleProfileHome;
import com.trilogy.app.crm.bundle.BundleProfileTransientHome;
import com.trilogy.app.crm.bundle.BundleSegmentEnum;
import com.trilogy.app.crm.bundle.CategoryAssociationTypeEnum;
import com.trilogy.app.crm.bundle.ExpiryTypeEnum;
import com.trilogy.app.crm.bundle.GroupChargingTypeEnum;
import com.trilogy.app.crm.bundle.QuotaTypeEnum;
import com.trilogy.app.crm.bundle.RecurrenceTypeEnum;
import com.trilogy.app.crm.bundle.SubscriberBucket;
import com.trilogy.app.crm.bundle.SubscriberBucketHome;
import com.trilogy.app.crm.bundle.UnitTypeEnum;
import com.trilogy.app.crm.bundle.exception.BundleAlreadyExistsException;
import com.trilogy.app.crm.bundle.exception.BundleDoesNotExistsException;
import com.trilogy.app.crm.bundle.exception.BundleManagerException;
import com.trilogy.app.crm.bundle.exception.CategoryAlreadyExistException;
import com.trilogy.app.crm.bundle.exception.CategoryNotExistException;
import com.trilogy.app.crm.bundle.exception.SubscriberProfileDoesNotExistException;
import com.trilogy.app.crm.bundle.home.BMCORBABundleProfileInfoHome;
import com.trilogy.app.crm.bundle.home.BMCORBACategoryInfoHome;
import com.trilogy.app.crm.bundle.home.BMCORBASubscriberBucketInfoHome;
import com.trilogy.app.crm.bundle.service.CORBABundleCategoryHandler;
import com.trilogy.app.crm.bundle.service.CORBABundleProfileHandler;
import com.trilogy.app.crm.bundle.service.CORBASubscriberBucketHandler;
import com.trilogy.app.crm.bundle.service.CRMBundleCategory;
import com.trilogy.app.crm.bundle.service.CRMBundleProfile;
import com.trilogy.app.crm.bundle.service.CRMSubscriberBucketProfile;
import com.trilogy.app.crm.client.BMSubscriberBucketCorbaClient;
import com.trilogy.app.crm.client.BundleCategoryProvisionClient;
import com.trilogy.app.crm.client.BundleProfileProvisionClient;
import com.trilogy.app.crm.client.TestBMBundleCategoryCorbaClient;
import com.trilogy.app.crm.client.TestBMBundleProfileCorbaClient;
import com.trilogy.app.crm.client.TestBMSubscriberBucketCorbaClient;
import com.trilogy.app.crm.unit_test.ContextAwareTestCase;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.home.HomeException;


/**
 * 
 *  
 * @author
 */
public class TestBMCorbaProvision extends ContextAwareTestCase
{

    private int bundleId = 1;
    private int newBundleId = 2;
    private int spid = 1;
    private int adjustmentId = 999;
    private GroupChargingTypeEnum groupScheme = GroupChargingTypeEnum.GROUP_BUNDLE;        
    private BundleSegmentEnum segment = BundleSegmentEnum.POSTPAID;
    private QuotaTypeEnum quotaScheme = QuotaTypeEnum.FIXED_QUOTA;
    private RecurrenceTypeEnum recurrenceType = RecurrenceTypeEnum.ONE_OFF_FIXED_INTERVAL;
    private Set includedBundles = new HashSet();

    private String msisdn = "4160000111";
    private String newMsisdn = "416000222";
    private int subscriptionType = 1;
    private int categoryId = 99;
    private int type = 0;
    
    public TestBMCorbaProvision(String name)
    {
        super(name);
    }

    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by standard JUnit tools (i.e., those that do not provide a
     * context).
     *
     * @return A new suite of Tests for execution.
     */
    public static Test suite()
    {
        return suite(com.redknee.app.crm.TestPackage.createDefaultContext());
    }


    /**
     * Creates a new suite of Tests for execution.  This method is intended to
     * be invoked by the Redknee Xtest code, which provides the application's
     * operating context.
     *
     * @param context The operating context.
     * @return A new suite of Tests for execution.
     */
    public static Test suite(final Context context)
    {
        setParentContext(context);

        final TestSuite suite = new TestSuite(TestBMCorbaProvision.class);

        return suite;
    }

    // INHERIT
    @Override
    public void setUp()
    {
        super.setUp();

        Context ctx = getContext();

        initHomesAndServices(ctx);
    }
    
    /**
     * Instantiates the BM services for CRM
     * @param ctx 
     */
    private void initHomesAndServices(Context ctx)
    {
        Home bundleProfileHome = new BundleProfileTransientHome(ctx);
        bundleProfileHome = new BMCORBABundleProfileInfoHome(ctx, bundleProfileHome);
        ctx.put(BundleProfileHome.class, bundleProfileHome);    
    
        Home bundleCategoryHome = new BundleCategoryTransientHome(ctx);
        bundleCategoryHome = new BMCORBACategoryInfoHome(ctx, bundleCategoryHome);
        ctx.put(BundleCategoryHome.class, bundleCategoryHome);
        
        Home subscriberBucketHome = new BMCORBASubscriberBucketInfoHome(ctx);
        ctx.put(SubscriberBucketHome.class, subscriberBucketHome);
       
        categoryService_ = new CORBABundleCategoryHandler();
        bundleService_ = new CORBABundleProfileHandler();
        bucketService_ = new CORBASubscriberBucketHandler();
        
        ctx.put(BMSubscriberBucketCorbaClient.class, new TestBMSubscriberBucketCorbaClient(ctx));
        ctx.put(BundleCategoryProvisionClient.class, new TestBMBundleCategoryCorbaClient(ctx));
        ctx.put(BundleProfileProvisionClient.class, new TestBMBundleProfileCorbaClient(ctx));
    }

    // INHERIT
    @Override
    public void tearDown()
    {
        super.tearDown();

    }

    public void testCreateCategory()
    {
        Context ctx = getContext();   
        BundleCategory testCategory = createTestCategory(categoryId);
        boolean categoryAlreadyExists = false;
        try
        {
            categoryService_.createCategory(ctx, testCategory);
        }
        catch (CategoryAlreadyExistException e)
        {
            categoryAlreadyExists = true;
        }
        catch (BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }
        assertFalse("Category already exists", categoryAlreadyExists);
        
        try
        {
            categoryService_.createCategory(ctx, testCategory);
        }
        catch (CategoryAlreadyExistException e)
        {
            categoryAlreadyExists = true;
        }
        catch (BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }
        assertTrue("Category does not exist after createCategory()", categoryAlreadyExists);
        
    }
    
    public void testGetCategory()
    {
        Context ctx = getContext(); 
        BundleCategory returnedCategory = null;
        try
        {
            returnedCategory = categoryService_.getCategory(ctx, categoryId);
        }
        catch (CategoryNotExistException e)
        {
            fail("Category does not exist");
        }
        catch (BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }
        assertEquals(returnedCategory.getCategoryId(), categoryId);
    }
    
    public void testGetCategoriesByUnitType()
    {
        Context ctx = getContext(); 
        Home home = null;
        try
        {
            home = categoryService_.getCategoriesByUnitType(ctx, type);
        }
        catch (BundleManagerException be)
        {
            fail ("Bundle Manager exception");
        }
        assertNotNull("No categories found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail ("Home Exception");
        }
        assertEquals(1, numReturnedBundles);
    }
    
    public void testGetCategoriesByUnitTypeRange()
    {
        Context ctx = getContext(); 
        Home home = null;
        Set types = new HashSet();
        types.add(Integer.valueOf(type));
        try
        {
            home = categoryService_.getCategoriesByUnitTypeRange(ctx, types);
        }
        catch (BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }
        assertNotNull("No categories found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home exception");
        }
        assertEquals(1, numReturnedBundles);
    }
    

    public void testRemoveCategory()
    {
        final Context ctx = getContext();   
        try
        {
            categoryService_.removeCategory(ctx, categoryId);
        }
        catch (final CategoryNotExistException e)
        {
            fail("Category does not exist");
        }
        catch (final BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }      

        BundleCategory returnedCategory = null;
        try
        {
            returnedCategory = categoryService_.getCategory(ctx, categoryId);
        }
        catch (final CategoryNotExistException e)
        {
        }
        catch (final BundleManagerException be)
        {
            fail("Bundle Manager exception");
        }      
        assertNull("Category still exists after removeCategory()", returnedCategory);
    }
    
    public void testCreateBundle()
    {
        Context ctx = getContext();       
        includedBundles.add(Long.valueOf(bundleId));
        
        BundleProfile testBundle = createTestBundle(msisdn, 
                                                    bundleId, 
                                                    categoryId, 
                                                    adjustmentId, 
                                                    groupScheme, 
                                                    segment, 
                                                    quotaScheme, 
                                                    recurrenceType);
        
        // createBundle
        boolean bundleAlreadyExists = false;
        try
        {
            bundleService_.createBundle(ctx, testBundle);
        }
        catch (BundleAlreadyExistsException e)
        {            
            bundleAlreadyExists = true;
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }      
        assertFalse("Bundle already exists", bundleAlreadyExists);
        
        // createBundle
        try
        {
            bundleService_.createBundle(ctx, testBundle);
        }
        catch (BundleAlreadyExistsException e)
        {
            bundleAlreadyExists = true;
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertTrue("Bundle does not exist after createBundle()", bundleAlreadyExists);
    }
    
    public void testGetBundlesByCategory()
    {
        Context ctx = getContext();    
        
        // getBundlesByCategory
        BundleProfile returnedBundle = null;
        try
        {
            returnedBundle = bundleService_.getBundleByCategory(ctx, categoryId).iterator().next();        
        }
        catch (BundleManagerException be)
        {
            fail("Bundle Manager exception");            
        }
        assertNotNull("No bundles found", returnedBundle);
        assertEquals(bundleId, returnedBundle.getBundleId());
    }
    
    public void testGetBundleProfile()
    {
        Context ctx = getContext();    
        BundleProfile returnedBundle = null;
        
        // getBundleProfile
        try
        {
            returnedBundle = bundleService_.getBundleProfile(ctx, spid, bundleId);
        }
        catch (BundleDoesNotExistsException e)
        {
            fail ("Bundle does not exist");
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertEquals(bundleId, returnedBundle.getBundleId());
    }
    

    public void testGetBundleByAdjustmentType()
    {
        Context ctx = getContext();    
        BundleProfile bundle = null;
        // getBundlesByAdjustmentType
        try
        {
            bundle = bundleService_.getBundleByAdjustmentType(ctx, adjustmentId);
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertNotNull("No bundles found", bundle);
        
        int numReturnedBundles = 0;
    }
    
    public void testGetBundlesByGroupScheme()
    {
        Context ctx = getContext();  
        Home home = null;
        // getBundlesByGroupScheme
        try
        {
            home = bundleService_.getBundlesByGroupScheme(ctx, groupScheme);
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertNotNull("No bundles found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home exception");
        }
        assertEquals(1, numReturnedBundles);

        // getBundlesByGroupScheme
        try
        {
            home = null;
            home = bundleService_.getBundlesByGroupScheme(ctx, groupScheme, includedBundles, true);
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        
        numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home exception");
        }
        assertEquals(1, numReturnedBundles);
        
    }
    
    public void testGetBundlesBySegment()
    {
        Context ctx = getContext();  
        Home home = null;
        // getBundlesBySegment
        try
        {            
            home = bundleService_.getBundlesBySegment(ctx, segment, includedBundles, false);
        }
        catch (BundleDoesNotExistsException e)
        {
            fail("Bundle does not exist");
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertNotNull("No bundles found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home exception");
        }
        assertEquals(1, numReturnedBundles);
    }
    
    public void testGetBundlesPointBundlesByQuotaScheme()
    {
        Context ctx = getContext();  
        Home home = null;
        // getBundlesPointBundlesByQuotaScheme
        try
        {            
            home = bundleService_.getBundlesPointBundlesByQuotaScheme(ctx, quotaScheme);
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertNotNull("No bundles found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home Exception");
        }
        assertEquals(1, numReturnedBundles);
    }
    
    public void testGetOneTimeBundles()
    {
        Context ctx = getContext();  
        Home home = null;
        // getOneTimeBundles
        try
        {            
            home = bundleService_.getOneTimeBundles(ctx, includedBundles);
        }
        catch (BundleDoesNotExistsException e)
        {            
            fail("Bundle does not exist");
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertNotNull("No bundles found", home);
        
        int numReturnedBundles = 0;
        try
        {
            numReturnedBundles = home.selectAll().size();
        }
        catch (HomeException e)
        {
            fail("Home exception");
        }
        assertEquals(1, numReturnedBundles);
    }
        
    public void testSwitchBundles()
    {
        Context ctx = getContext();    
        // switchBundles
    
        try
        {            
            Collection newBundles = new ArrayList();
            includedBundles.add(Long.valueOf(newBundleId));
            bundleService_.switchBundles(ctx, msisdn, subscriptionType, spid, includedBundles, newBundles, null);
        }
        catch (SubscriberProfileDoesNotExistException e)
        {                
            fail("Bundle does not exist");
        }
        catch (BundleManagerException be)
        {   
            fail("Bundle Manager exception");
        }
        // getBundleProfile
        BundleProfile returnedBundle = null;
        boolean bundleExists = true;
        try
        {
            returnedBundle = bundleService_.getBundleProfile(ctx, spid, bundleId);
        }
        catch (BundleDoesNotExistsException e)
        {
            bundleExists = false;
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertFalse("Old bundle id found after switchBundles()", bundleExists);

        // getBundleProfile
        try
        {
            bundleExists = true;
            returnedBundle = null;
            returnedBundle = bundleService_.getBundleProfile(ctx, spid, newBundleId);
        }
        catch (BundleDoesNotExistsException e)
        {
            bundleExists = false;
        }
        catch (BundleManagerException be)
        {           
            fail("Bundle Manager exception");
        }
        assertTrue("New bundle id not found after switchBundles()", bundleExists);        
    }
        
    public void testRemoveBundleProfile()
    {
        Context ctx = getContext();   
        // removeBundleProfile    
        try
        {
            bundleService_.removeBundleProfile(ctx, spid, newBundleId);
        }
        catch (BundleDoesNotExistsException e)
        {
            fail("Bundle does not exist");
        }
        catch (BundleManagerException be)
        {            
            fail("Bundle Manager exception");
        }
        // getBundleProfile
        boolean bundleExists = true;
        try
        {
            bundleService_.getBundleProfile(ctx, spid, newBundleId);
        }
        catch (BundleDoesNotExistsException e)
        {
            bundleExists = false;
        }
        catch (BundleManagerException be)
        {           
        }
        assertFalse("Bundle still exists after removeBundle()", bundleExists);            
    }    
        
    private BundleCategory createTestCategory(int categoryId)
    {
        BundleCategory category = new BundleCategory();
        category.setCategoryId(categoryId);
        category.setSpid(1);
        category.setName("Test Category 1");
        category.setUnitType(UnitTypeEnum.VOLUME_SECONDS);
        category.setEnabled(true);
        
        return category;
    }
    
    private BundleProfile createTestBundle(String msisdn,
            int bundleId, 
            int categoryId, int adjustmentId, 
            GroupChargingTypeEnum groupScheme, 
            BundleSegmentEnum segment,
            QuotaTypeEnum quoteScheme,
            RecurrenceTypeEnum recurrenceType)
    {
        BundleProfile bundle = new BundleProfile();
        bundle.setBundleId(bundleId);
        bundle.setGroupBundleId(1);
        bundle.setName("Test Bundle 1");
        bundle.setSpid(1);
        bundle.setSegment(segment);
        bundle.setAuxiliary(false);
        bundle.setAuxiliaryServiceCharge(0);
        bundle.setSmartSuspensionEnabled(true);
        BundleCategoryAssociation association = new BundleCategoryAssociation();
        association.setCategoryId(categoryId);
        association.setType(UnitTypeEnum.VOLUME_SECONDS_INDEX);
        Map map = new HashMap();
        map.put(Integer.valueOf(1), association);
        bundle.setBundleCategoryIds(map);
        bundle.setType(0);
        bundle.setQuotaScheme(quoteScheme);
        bundle.setEnablePromotionProvision(true);
        bundle.setReprovisionOnActive(false);
        bundle.setActivationFeeCalculation(ActivationFeeCalculationEnum.FULL);
        bundle.setInitialBalanceLimit(10);
        bundle.setChargingPriority(1);
        bundle.setGroupChargingScheme(groupScheme);
        bundle.setRolloverPercent(100);
        bundle.setRolloverMax(100);
        bundle.setExpiryPercent(0);
        bundle.setActivationScheme(ActivationTypeEnum.ACTIVATE_ON_PROVISION);
        bundle.setExpiryScheme(ExpiryTypeEnum.NEVER_EXPIRE);
        bundle.setAssociationType(CategoryAssociationTypeEnum.SINGLE_UNIT);
        bundle.setRecurrenceScheme(recurrenceType);
        bundle.setValidity(1);
        bundle.setInterval(1);
        bundle.setRecurringStartHour(1);
        bundle.setRecurringStartMinutes(1);
        bundle.setStartDate(Calendar.getInstance().getTime());
        bundle.setEndDate(Calendar.getInstance().getTime());
        bundle.setRolloverMaxPercentage(100);
        bundle.setServiceInitialLimits(new HashMap());
        bundle.setAdjustmentType(adjustmentId);
        bundle.setAuxiliaryAdjustmentType(9999);
        bundle.setAdjustmentTypeDescription("test");
        bundle.setGLCode("9999");
        bundle.setInvoiceDesc("test");
        bundle.setTaxAuthority(1);
        bundle.setEnabled(true);
        
        return bundle;
    }

    private SubscriberBucket createTestBucket()
    {
        SubscriberBucket bucket = new SubscriberBucket();
        
        return bucket;
    }
    
    protected CRMBundleCategory categoryService_;
    protected CRMBundleProfile bundleService_;
    protected CRMSubscriberBucketProfile bucketService_;
    

}
