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
package com.trilogy.app.crm.bulkloader.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.map.MultiValueMap;

import com.trilogy.framework.xhome.beans.AbstractBean;
import com.trilogy.framework.xhome.beans.IllegalPropertyArgumentException;
import com.trilogy.framework.xhome.beans.XBeans;
import com.trilogy.framework.xhome.beans.xi.PropertyInfo;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.context.ContextAware;
import com.trilogy.framework.xhome.csv.AbstractCSVSupport;
import com.trilogy.framework.xhome.elang.And;
import com.trilogy.framework.xhome.elang.EQ;
import com.trilogy.framework.xhome.home.Home;
import com.trilogy.framework.xhome.language.MessageMgr;
import com.trilogy.framework.xhome.support.StringSeperator;
import com.trilogy.framework.xhome.util.format.ThreadLocalSimpleDateFormat;
import com.trilogy.framework.xlog.log.LogSupport;
import com.trilogy.framework.xlog.log.MinorLogMsg;
import com.trilogy.framework.xlog.log.PMLogMsg;
import com.trilogy.app.crm.support.HomeSupportHelper;

/**
 * Given a GenericBeanBulkloader configuration, create the Module that will parse the 
 * CSV Command.
 * @author angie.li@redknee.com
 *
 * @since 8.2
 */
public class CSVParser extends AbstractCSVSupport implements ContextAware
{
    public CSVParser(Context ctx, GenericBeanBulkloader bulkloader)
    {
        setContext(ctx);
        intiliaze(bulkloader);
    }
    
    private void intiliaze(GenericBeanBulkloader bulkloader) 
    {
        if (bulkloader.getDelimiter()!=null && !bulkloader.getDelimiter().isEmpty())
        {
            delimiter_ = bulkloader.getDelimiter().charAt(0);
        }
        else
        {
            delimiter_ = BulkloadConstants.DEFAULT_DELIMITER;
        }
        if (bulkloader.getClassName() != null && bulkloader.getClassName().length() > 0)
        {
            try
            {
                beanClass_ = Class.forName(bulkloader.getClassName());
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalPropertyArgumentException(GenericBeanBulkloaderXInfo.CLASS_NAME, 
                        "No such Bean exists.  Check the provided Bean Class.");
            }
        }

        final String searchClassName = bulkloader.getSearchClassName();
        if (searchClassName != null && searchClassName.length() > 0)
        {
            try
            {
                searchBeanClass_ = Class.forName(searchClassName);
            }
            catch (ClassNotFoundException e)
            {
                throw new IllegalPropertyArgumentException(GenericBeanBulkloaderXInfo.SEARCH_CLASS_NAME,
                        "No such Search Bean exists.  Check the provided Search Bean Class.");
            }
        }

        //Sort all the fields by type and index order.

        inputCSVFields_ = new MultiValueMap();
        searchFields_ = new MultiValueMap();

       
        /**
         * Use this value in case there is a failure
         */
        this.defaultValueFields_ = new TreeMap<Integer, String>();
       

        inputConstantFields_ = new TreeMap<PropertyInfo, String>();
        searchConstantFields_ = new TreeMap<PropertyInfo, String>();
        inputAddInstr_ = new TreeMap<PropertyInfo, PropertyBulkloadActionEnum>();
        Collection<BulkloadPropertyInfo> allInputFields = bulkloader.getParameters();
        for(BulkloadPropertyInfo field: allInputFields)
        {
            if (field.getType().equals(BulkloadPropertyTypeEnum.INDEX))
            {
                //Do not allow Blank indexes
                addIndexedProperty(inputCSVFields_, field);
                
                if(field.getUseDefaultOnFailure())
                {
                    this.defaultValueFields_.put(
                            Integer.valueOf(field.getValue()), field.getDefaultOnFailure());
                }
            }
            else if (field.getType().equals(BulkloadPropertyTypeEnum.CONSTANT))
            {
                inputConstantFields_.put(field.getPropertyInfo(), field.getValue());
            }
            inputAddInstr_.put(field.getPropertyInfo(), field.getAdditionalInstructions());
        }
        Collection<BulkloadPropertyInfo> allSearchFields = bulkloader.getSearchCriteria();
        for(BulkloadPropertyInfo field: allSearchFields)
        {
            if (field.getType().equals(BulkloadPropertyTypeEnum.INDEX))
            {
                //Do not allow Blank indexes
                addIndexedProperty(searchFields_, field);
            }
            else if (field.getType().equals(BulkloadPropertyTypeEnum.CONSTANT))
            {
                searchConstantFields_.put(field.getPropertyInfo(), field.getValue());
            }
        }
        bulkloaderConfig_ = bulkloader;
    }
    
    private void addIndexedProperty(MultiValueMap indexMap, 
            BulkloadPropertyInfo field) 
    {
        String value = field.getValue().trim();
        if(value != null && value.length() > 0)
        {
            Integer index = Integer.valueOf(field.getValue());
            indexMap.put(index, field.getPropertyInfo());
        }
        else
        {
            throw new IllegalPropertyArgumentException(BulkloadPropertyInfoXInfo.VALUE, 
                    "Index value cannot be blank.");
        }    
    }

    /**
     * This operation is not supported.  This is strictly a CSV Parser.
     */
    public StringBuffer append(StringBuffer arg0, char arg1, Object arg2) 
    {
        throw new UnsupportedOperationException();
    }

    /** 
     * Returned bean could be null.
     */
    public Object parse(StringSeperator separator)
    {
        ArrayList<String> allValues = getParameters(separator);
        
        Object bean;
        boolean flag = !bulkloaderConfig_.getAction().equals(BulkloaderActionEnum.CREATE) && !bulkloaderConfig_.getSearchType().equals(SearchTypeEnum.NONE);
        SearchTypeEnum searchType = bulkloaderConfig_.getSearchType();

        if (searchBeanClass_ != null && (flag ||  (searchType.equals(SearchTypeEnum.CUSTOM_HOME) && searchType.getCollection().size() > 0)))
        {
            if (searchType.equals(SearchTypeEnum.CUSTOM_HOME))
              {
                bean = retrieveBean(allValues);
            }
            else
            {
                bean = findRetrieveBean(allValues);
            }
        }
        else
        {
            bean = instantiateBean();
        }

        if (bean != null)
        {
            //Set all the fields from the bulkload command to the bean
            PMLogMsg pm = logPMLogMsg(getContext(), BulkloadConstants.PM_PARSE_MODIFY_BEAN);
            bean = setBulkloadedValues(bean, allValues);
            bean = setConstantValues(bean, allValues);
            pm.log(getContext());
        }
        return bean;
    }

    /**
     * Return an array of the given separated tokens
     * @param separator
     * @return
     */
    private ArrayList<String> getParameters(StringSeperator separator) 
    {
        ArrayList<String> list = new ArrayList<String>();
        while (separator.hasNext())
        {
            list.add(separator.next());
        }
        return list;
    }
    
    public ArrayList<String> getInputParameters(StringSeperator separator) 
    {
        ArrayList<String> list = new ArrayList<String>();
        while (separator.hasNext())
        {
            list.add(separator.next());
        }
        return list;
    }
    private Object instantiateBean()
    {
        Object bean = null;
        try
        {
            bean = XBeans.instantiate(beanClass_, getContext());
        }
        catch (Exception e)
        {
            String msg = "Failed to instantiate bean " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }

        return bean;
    }

    /**
     * Retrieve the bean from CRM using the Search Criteria as compound key.
     * searchBeanClass_ must not be null.
     *
     * @param allValues - values from the CSV command
     * @return
     */
    private Object findRetrieveBean(ArrayList<String> allValues) 
    {
        Object bean = null;
        try
        {
            PMLogMsg pm = logPMLogMsg(getContext(), BulkloadConstants.PM_PARSE_BUILD_FIND_CONDITION);
            //Iterate through Search Criteria by Index
            Object condition = buildXInfoCondition(searchFields_, allValues, searchConstantFields_);
            pm.log(getContext());

            Home searchHome = HomeSupportHelper.get(getContext()).getHome(getContext(), searchBeanClass_);
            PMLogMsg pm2 = logPMLogMsg(getContext(), BulkloadConstants.PM_PARSE_LOOKUP_BEAN);
            bean =  searchHome.find(getContext(), condition);
            pm2.log(getContext());
        }
        catch (IndexOutOfBoundsException e)
        {
            String msg = "The index configuration for the Bulkloader is out of range for the command being parsed. Index that is wrong: " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        catch (Exception e)
        {
            String msg = "Failed to lookup bean to modify due to " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        return bean;
    }

    /**
     * Retrieve the bean from CRM using the Search Criteria.
     * searchBeanClass_ must not be null.
     *
     * @param allValues - values from the CSV command
     * @return
     */
    private Object retrieveBean(ArrayList<String> allValues) 
    {
        Object bean = null;
        try
        {
            PMLogMsg pm = logPMLogMsg(getContext(), BulkloadConstants.PM_PARSE_BUILD_SEARCH_BEAN);
            Object searchBean = XBeans.instantiate(searchBeanClass_, getContext());
            //Iterate through Search Criteria by Index
            searchBean = populateBean(searchBean, searchFields_, allValues, this.defaultValueFields_);
            //Iterate through the Search Criteria Constants
            searchBean = populateBeanWithConstants(searchBean, searchConstantFields_);
            pm.log(getContext());
            
            Home searchHome = HomeSupportHelper.get(getContext()).getHome(getContext(), searchBeanClass_);
            PMLogMsg pm2 = logPMLogMsg(getContext(), BulkloadConstants.PM_PARSE_LOOKUP_BEAN);
            bean =  searchHome.find(getContext(), searchBean);
            pm2.log(getContext());
        }
        catch (IndexOutOfBoundsException e)
        {
            String msg = "The index configuration for the Bulkloader is out of range for the command being parsed. Index that is wrong: " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        catch (Exception e)
        {
            String msg = "Failed to lookup bean to modify due to " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        return bean;
    }

    /**
     * Set the Values from the CSV Bulkload command into the bean.
     * Return the modified bean.
     * @param bean
     * @param allValues - values from the CSV command
     * @return
     */
    private Object setBulkloadedValues(Object bean, ArrayList<String> allValues) 
    {
        try
        {
            //Iterate through properties to modify by Index
            bean = populateBean(bean, inputCSVFields_, 
                    this.defaultValueFields_, inputAddInstr_, allValues);
        }
        catch (IndexOutOfBoundsException e)
        {
            String msg = "Error occurred while setting values from CSV Bulkload command. The index configuration " +
                    "for the Bulkloader is out of range for the command being parsed. Index that is wrong: " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        catch (Exception e)
        {
            String msg = "Error occurred while setting values from Bulkload command to bean due to " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        return bean;
    }
    
    /**
     * Set the Constant values into the bean.
     * Return the modified bean.
     * @param bean
     * @param allValues - values from the CSV command
     * @return
     */
    private Object setConstantValues(Object bean, ArrayList<String> allValues) 
    {
        try
        {
            //Iterate through properties to modify by Index
            bean = populateBeanWithConstants(bean, inputConstantFields_);
        }
        catch (IndexOutOfBoundsException e)
        {
            String msg = "Error occurred while setting constant values from Bulkload command. The index configuration " +
                    "for the Bulkloader is out of range for the command being parsed. Index that is wrong: " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        catch (Exception e)
        {
            String msg = "Error occurred while setting constant values from Bulkload command to bean due to " + e.getMessage();
            new MinorLogMsg(CSVParser.class, msg, e).log(getContext());
            throwException(msg, e);
        }
        return bean;
    }

    /**
     * Iterate through properties given and place the input values in the given bean.
     * Return the modified bean.
     * @param bean
     * @param propertiesByIndex
     * @param inputValues
     * @param defaultValues TODO
     * @return
     */
    private Object populateBean(Object bean,
            final MultiValueMap propertiesByIndex,
            final ArrayList<String> inputValues, Map<Integer, String> defaultValues) 
    {
        return populateBean(bean, propertiesByIndex, defaultValues, Collections.EMPTY_MAP, inputValues);
    }

    /**
     * Iterate through properties given and place the input values in the given bean.
     * Return the modified bean.
     * @param bean
     * @param propertiesByIndex
     * @param defaultValues TODO
     * @param inputValues
     * @return
     */
    private Object populateBean(Object bean,
            final MultiValueMap propertiesByIndex,
            Map<Integer, String> defaultValues, 
            final Map <PropertyInfo, PropertyBulkloadActionEnum> additionalInstructions, final ArrayList<String> inputValues) 
    {
        Iterator<Integer> i = propertiesByIndex.keySet().iterator();
        while(i.hasNext())
        {
            Integer index = i.next();
            List<PropertyInfo> propertyList = (List<PropertyInfo>) propertiesByIndex.get(index);
            for (int j = 0; j < propertyList.size(); j++)
            {
                PropertyInfo property = (PropertyInfo) propertyList.get(j);
                String value = inputValues.get(index.intValue());
                PropertyBulkloadActionEnum collectionInstructions = PropertyBulkloadActionEnum.NONE;
                if (additionalInstructions != null)
                {
                    PropertyBulkloadActionEnum configInstr = additionalInstructions.get(property);
                    if (configInstr != null)
                    {
                        collectionInstructions = configInstr;
                    }
                }
                // If present take it, else null.
            String defaultValue = defaultValues.get(index);
            bean = setPropertyValue(property, bean, value, defaultValue, collectionInstructions);
       
            }

          

        }
        return bean;
    }

    /**
     * Iterate through the set of constants and bean Properties to set the constants to the given bean.
     * Return the updated bean. 
     * @param bean
     * @param constantsByProperty
     * @return
     */
    private Object populateBeanWithConstants(Object bean,
            Map<PropertyInfo, String> constantsByProperty) 
    {
        Iterator<PropertyInfo> i = constantsByProperty.keySet().iterator();
        while(i.hasNext())
        {
            PropertyInfo property = i.next();
            String value = constantsByProperty.get(property);
            //The Action of a Constant is to Replace
            PropertyBulkloadActionEnum collectionInstruction = PropertyBulkloadActionEnum.REPLACE;
            bean = setPropertyValue(property, bean, value, null, collectionInstruction);
        }
        return bean;
    }
    
    /**
     * Intercept the parsing of Dates by the XInfo class.  Instead use the 
     * Date format set by the Generic Bean Bulkloader.
     * @param property - Property of the bean to modify
     * @param bean - the bean to modify
     * @param value - the new value for the bean.  If the property is a collection, then defer to the 
     * collectionAction to determine if the collection is to be added or removed or replaced.
     * @collectionAction instructions for how to handle the Collection.
     * @return
     */
    private Object determinePropertyValue(final PropertyInfo property, 
            final Object bean,
            String value,
            final PropertyBulkloadActionEnum collectionAction) 
    {
        if (Date.class.isAssignableFrom(property.getType()))
        {
            return parseDate(value);
        }
        else if(Collection.class.isAssignableFrom(property.getType()))
        {
            //We'll deal with Collections specified by XML
            Collection modifiedCollection = handleCollectionInput(property, collectionAction, 
                    value, (Collection) property.get(bean));
            //Set the collection to the bean
            return modifiedCollection;
        }
        else
        {
            return property.fromString(value);
        }
    }

    /**
     * Intercept the parsing of Dates by the XInfo class.  Instead use the 
     * Date format set by the Generic Bean Bulkloader.
     * @param property - Property of the bean to modify
     * @param bean - the bean to modify
     * @param value - the new value for the bean.  If the property is a collection, then defer to the 
     * collectionAction to determine if the collection is to be added or removed or replaced.
     * @param defaultValue TODO
     * @collectionAction instructions for how to handle the Collection.
     * @return
     */
    private Object setPropertyValue(final PropertyInfo property, 
            final Object bean,
            String value,
            String defaultValue, final PropertyBulkloadActionEnum collectionAction) 
    {
        try
        {
            value = value.trim();
            Object propertyValue = determinePropertyValue(property, bean, value, collectionAction);
            propertyValue = propertyValue==null && defaultValue!=null ? 
                    determinePropertyValue(property, bean, defaultValue, collectionAction) :
                        propertyValue;
            
            property.set(bean, propertyValue);
        }
        catch (Exception e)
        {
            /*
             * Try once again, setting the default value is the flag is ON (i.e. value!=null).
             */
            if(defaultValue!=null)
            {
                try
                {
                    Object propertyValue = determinePropertyValue(property, bean, defaultValue, collectionAction);
                    property.set(bean, propertyValue);
                }
                catch(Exception e1)
                {
                    throw new IllegalPropertyArgumentException(property, e1.getMessage());
                }
            }
            else
                throw new IllegalPropertyArgumentException(property, e.getMessage());
        }
        return bean;
    }

    private Object buildXInfoCondition(final Map<Integer, PropertyInfo> propertiesByIndex,
            final ArrayList<String> inputValues,
            Map<PropertyInfo, String> constantsByProperty)
    {
        And condition = new And();

        Iterator<Integer> i = propertiesByIndex.keySet().iterator();
        while(i.hasNext())
        {
            Integer index = i.next();
            PropertyInfo property = propertiesByIndex.get(index);
            String value = inputValues.get(index.intValue());

            Object propertyValue = determinePropertyValue(property, null, value, PropertyBulkloadActionEnum.NONE);

            condition.add(new EQ(property, propertyValue));
        }

        Iterator<PropertyInfo> j = constantsByProperty.keySet().iterator();
        while(j.hasNext())
        {
            PropertyInfo property = j.next();
            String value = constantsByProperty.get(property);
            value = constantsByProperty.get(property);

            Object propertyValue = determinePropertyValue(property, null, value, PropertyBulkloadActionEnum.NONE);

            condition.add(new EQ(property, propertyValue));
        }

        return condition;
    }

    /**
     * Manipulate the given Collection property according to the action given. 
     * @param property - Collection property on the bean to change
     * @param collectionAction - instructions on what to do with the collection
     * @param value - input value
     * @param existingCollection - bean's
     * @return
     */
    private <T extends AbstractBean> Collection<T> handleCollectionInput(final PropertyInfo property, 
            final PropertyBulkloadActionEnum collectionAction, 
            String value,
            Collection<T> existingCollection) 
    {
        if (value != null && !value.startsWith("<beans>"))
        {
            value = value.replace('+',',');
        }
        Collection inputCollection = parseInputCollection(property, value);
        if (collectionAction.equals(PropertyBulkloadActionEnum.ADD))
        {
            //Add the value to the existing collection
            existingCollection.addAll(inputCollection);
        }
        else if (collectionAction.equals(PropertyBulkloadActionEnum.REMOVE))
        {
            //Remove the values from the existing collection.  Remove by ID.
            existingCollection.removeAll(inputCollection);
        }
        else if (collectionAction.equals(PropertyBulkloadActionEnum.REPLACE))
        {
            //Replace the collection with the given collection.
            existingCollection = inputCollection;
        }
        return existingCollection;
    }

    /**
     * Only call this method once it has been determined that the property is a Collection.
     * @param property
     * @param value
     * @return
     */
    private Collection parseInputCollection(PropertyInfo property, String value) 
    {
        Collection col = (Collection) property.fromString(value);
        return col;
    }

    /**
     * Return a Date from the given String parameter, parsed by using the Generic Bean Bulkloader 
     * Date format.
     * @param value
     * @return
     */
    private Object parseDate(String value) 
    {
        try
        {
        return getDateFormat().parse(value);
        }
        catch (Exception e)
        {
            LogSupport.minor(ctx_, this, "Date formatting failed: " + value);
        }
        return null;
    }

    /**
     * Return the Date format for the Generic Bean BulkLoader.
     * @param ctx
     * @return
     */
    private ThreadLocalSimpleDateFormat getDateFormat()
    {
        final MessageMgr mmgr = new MessageMgr(getContext(), GenericBeanBulkloader.class);

        final String format = mmgr.get(BulkloadConstants.MSG_MGR_KEY, BulkloadConstants.DEFAULT_DATE_FORMAT_PATTERN);

        if (format.equals(BulkloadConstants.DEFAULT_DATE_FORMAT_PATTERN))
        {
            return DEFAULT_DATE_FORMAT;
        }

        // Look for a cached entry of a ThreadLocalSimpleDateFormat. Really because the Map is ThreadLocal
        // we didn't need to have a ThreadLocalSimpleDateFormat stored but because this factory and
        // is shared with the ConstantContextFactory it needed to be a ThreadLocalSimpleDateFormat.
        // Sept.13/2005 DMAC
        final Map map = (Map) (threadLocalFormatMap.get());
        ThreadLocalSimpleDateFormat threadLocalFormat = (ThreadLocalSimpleDateFormat) map.get(format);

        if (threadLocalFormat == null)
        {
            threadLocalFormat = new ThreadLocalSimpleDateFormat(format);
            ((Map) threadLocalFormatMap.get()).put(format, threadLocalFormat);
        }

        return threadLocalFormat;
    }

    /**
     * Haven't yet decided what kind of exceptions to throw from this class.
     * @param msg
     * @param e
     * @throws IllegalStateException
     */
    private void throwException(String msg, Exception e) throws IllegalStateException
    {
        throw new IllegalStateException(msg, e);
    }

    public Context getContext() 
    {
        return ctx_;
    }

    public void setContext(Context context) 
    {
        ctx_ = context;
    }
    
    public Class getBeanClass()
    {
        return beanClass_;
    }
    
    public GenericBeanBulkloader getBulkloaderConfig()
    {
        return bulkloaderConfig_;
    }
    
    public MultiValueMap getInputFields()
    {
        return inputCSVFields_;
    }
    
    public MultiValueMap getSearchFields()
    {
        return searchFields_;
    }
    
    public char getDelimiter()
    {
        return delimiter_;
    }
    
    /**
     * @return the defaultValueFields_
     */
    public final TreeMap<Integer, String> getDefaultValueFields()
    {
        return this.defaultValueFields_;
    }

    /**
     * @param defaultValueFields_ the defaultValueFields_ to set
     */
    public final void setDefaultValueFields(
            TreeMap<Integer, String> defaultValueFields)
    {
        this.defaultValueFields_ = defaultValueFields;
    }
    
    private Context ctx_;
    private GenericBeanBulkloader bulkloaderConfig_;
    private Class beanClass_;
    private Class searchBeanClass_;
    private char delimiter_;
    /**
     * Index, PropertyInfo pair
     */
    private MultiValueMap inputCSVFields_;
    private MultiValueMap searchFields_;
    private TreeMap<Integer, String> defaultValueFields_;

    /**
     * PropertyInfo, Value pair
     */
    private TreeMap <PropertyInfo, String> inputConstantFields_;
    private TreeMap <PropertyInfo, String> searchConstantFields_;
    /**
     * Additional Instructions for the given Property
     */
    private TreeMap <PropertyInfo, PropertyBulkloadActionEnum> inputAddInstr_;
    public final static ThreadLocalSimpleDateFormat DEFAULT_DATE_FORMAT = new ThreadLocalSimpleDateFormat(
        BulkloadConstants.DEFAULT_DATE_FORMAT_PATTERN, Locale.CANADA);

    protected static ThreadLocal threadLocalFormatMap = new ThreadLocal()
    {
        @Override
        public Object initialValue()
        {
            return new HashMap();
        }
    };   
    
    protected PMLogMsg logPMLogMsg(Context ctx, String pm)
    {
        return new PMLogMsg(BulkloadConstants.GENERIC_BULKLOADER_MODULE, pm);
    }
}
