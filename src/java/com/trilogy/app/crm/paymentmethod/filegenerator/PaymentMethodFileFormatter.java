package com.trilogy.app.crm.paymentmethod.filegenerator;

import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.bean.FieldTypeEnum;
import com.trilogy.app.crm.bean.FileFormatterData;
import com.trilogy.app.crm.bean.FileFormatterFieldConfiguration;
import com.trilogy.app.crm.bean.PaymentMethodConstants;
import com.trilogy.app.crm.calculator.InternalKeyValueCalculator;
import com.trilogy.app.crm.calculator.ValueCalculator;
import com.trilogy.app.crm.paymentmethod.exception.PaymentMethodFileFormatterException;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xhome.support.HexUtil;
import com.trilogy.framework.xlog.log.LogSupport;

/**
 * 
 * @author meenal.rastogi
 * This class is used to generate the header, trailer, data and the end record part of the different
 * files in the required format , with proper values for each of the files.
 *
 */
public class PaymentMethodFileFormatter 
{
	private static final String MODULE_NAME = PaymentMethodFileFormatter.class.getName();

	/**
	 * 
	 * @param ctx
	 * @param formatterData
	 * @param delimiter
	 * @return
	 * 
	 * This method to be called for the header and trailer sections of the files.
	 */
	public static StringBuffer formatFileData(Context ctx, FileFormatterData formatterData, String delimiter) throws PaymentMethodFileFormatterException
	{
		StringBuffer fileLine = new StringBuffer("");
		try
		{
			if (formatterData.isRequired())
			{
				fileLine = formatFileData(ctx, (List<FileFormatterFieldConfiguration>)formatterData.getFieldName(), delimiter); 			

			}
		}
		catch(PaymentMethodFileFormatterException pex)
		{
			throw pex;
		}
		return fileLine;
	}

	/**
	 * 
	 * @param ctx
	 * @param fieldConfigs
	 * @param delimiter
	 * @return
	 * This method to be called for the data and end record section of the files
	 */
	public static StringBuffer formatFileData(Context ctx, List<FileFormatterFieldConfiguration> fieldConfigs, String delimiter) throws PaymentMethodFileFormatterException
	{
		StringBuffer fileLine = new StringBuffer("");
		StringBuffer fieldValue = new StringBuffer("");
		try
		{
			for (Iterator<FileFormatterFieldConfiguration> itr = fieldConfigs.iterator(); itr.hasNext();)
			{			
				FileFormatterFieldConfiguration fieldConfig = (FileFormatterFieldConfiguration)itr.next();
				fieldValue = getValueForField(ctx, fieldConfig);
				fieldValue = getLengthModifiedValueForField(ctx, fieldConfig, fieldValue, false);
				
				if (fileLine.length() == 0)
				{
				    appendFixChar(fieldValue, fileLine, fieldConfig.getFieldPrefix(), fieldConfig, false);
					fileLine.append(fieldValue);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldSuffix(), fieldConfig, true);
				}
				else
				{
					fileLine.append(delimiter);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldPrefix(), fieldConfig, false);
					fileLine.append(fieldValue);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldSuffix(), fieldConfig, true);
				}
			}
		}
		catch(PaymentMethodFileFormatterException pex)
		{
			throw pex;
		}
		catch(Exception ex)
		{
			LogSupport.minor(ctx, MODULE_NAME, "Failed to format the record" + ex);
			throw new PaymentMethodFileFormatterException("Failed to format the record", ex, PaymentMethodConstants.FAILURE);
		}
		LogSupport.debug(ctx, MODULE_NAME, "The formatted record is[ "+ fileLine.toString() + "]");
		return new StringBuffer(fileLine);
	}



    private static void appendFixChar(StringBuffer fieldValue, StringBuffer fileLine, String fixChar,
            FileFormatterFieldConfiguration fieldConfig, boolean isSuffix)
    {
        if(fixChar != null && !fixChar.isEmpty())
        {
            if((fieldValue!=null && !fieldValue.toString().trim().isEmpty())
                    || fieldConfig.isIncludeIfEmpty())
            {
                fileLine.append(fixChar);
            }
            
            if(fieldValue != null && isSuffix)
            {
                fileLine.append(fieldConfig.getFieldSeparator());
            }
        }
        
    }
    /**
	 * 
	 * @param ctx
	 * @param fieldConfig
	 * @return
	 */
	private static StringBuffer getValueForField(Context ctx, FileFormatterFieldConfiguration fieldConfig) throws PaymentMethodFileFormatterException
	{
		String fieldValue = "";
		try
		{
			ValueCalculator valueCalculator = fieldConfig.getValueCalculator();
			if(valueCalculator != null)
			{
			    Object valueObj = valueCalculator.getValueAdvanced(ctx);
			    if(valueObj != null)
			    {
			        fieldValue = valueObj.toString();
			    }
            }
		}
		catch (Exception ex)
		{
			LogSupport.major(ctx, MODULE_NAME, " Exception  occured failed while modifing the field value" +
					"  according to needed length, for field : " + fieldConfig.getFieldName(), ex);
			throw new PaymentMethodFileFormatterException("Failed while getting field value", ex, PaymentMethodConstants.FAILURE);
		}
		return new StringBuffer(fieldValue);
	}

	/**
	 * 
	 * @param ctx
	 * @param fieldConfig
	 * @param value
	 * @return
	 */
	private static StringBuffer getLengthModifiedValueForField(Context ctx, FileFormatterFieldConfiguration fieldConfig, 
			StringBuffer fieldValue, boolean isFieldsRightJustified) throws PaymentMethodFileFormatterException
	{
		StringBuffer value = new StringBuffer("");
		String prefixString = "";
		try
		{
			if (fieldConfig.getFieldLength() > 0)
			{
			    if(fieldConfig.getFieldType() != FieldTypeEnum.HexString)
			    {
    				if (fieldValue.length() < fieldConfig.getFieldLength())
    				{
    					int diffInLength = fieldConfig.getFieldLength() - fieldValue.length();
    					String prefix = (fieldConfig.getFieldType() == FieldTypeEnum.Numeric) ? "0" : " ";
    
    					for (int i = 0 ; i < diffInLength; i++)
    					{
    						prefixString += prefix;
    					}				
    				}
    				else if(fieldValue.length() > fieldConfig.getFieldLength())
    				{
    				    String subString = fieldValue.toString().substring(0, fieldConfig.getFieldLength());
    				    fieldValue.setLength(0);
    				    fieldValue.append(subString);
    				}
			    }
			}
			if (fieldConfig.getFieldType() == FieldTypeEnum.Numeric)
			{
				value.append(prefixString).append(fieldValue);
			}
			else if (fieldConfig.getFieldType() == FieldTypeEnum.String && !isFieldsRightJustified)
			{
				value.append(fieldValue).append(prefixString);
			}
			else if (fieldConfig.getFieldType() == FieldTypeEnum.String && isFieldsRightJustified)
			{
				value.append(prefixString).append(fieldValue);
			}
			else if(fieldConfig.getFieldType() == FieldTypeEnum.HexString)
			{
			    LogSupport.info(ctx, PaymentMethodFileFormatter.class.getName(), "fieldValue--"+fieldValue.toString());
			    byte[] hexByte = HexUtil.hex2data(fieldValue.toString());
			    String byteStr = new String(hexByte);
			    fieldValue.setLength(0);
                fieldValue.append(byteStr);
                value.append(fieldValue);
			}
		}
		catch (Exception ex)
		{
			LogSupport.major(ctx, MODULE_NAME, "Failed while modifing the field value" +
					"  according to needed length, exception  "+ ex.getMessage());
			throw new PaymentMethodFileFormatterException("Failed while modifing the field value according to length", ex, PaymentMethodConstants.FAILURE);
		}
		return value;
	}
	
	public static StringBuffer formatFileData(Context ctx, List<FileFormatterFieldConfiguration> fieldConfigs, 
			String delimiter, boolean isFieldsRightJustified) throws PaymentMethodFileFormatterException
	{
		StringBuffer fileLine = new StringBuffer("");
		StringBuffer fieldValue = new StringBuffer("");
		try
		{
			for (Iterator<FileFormatterFieldConfiguration> itr = fieldConfigs.iterator(); itr.hasNext();)
			{			
				FileFormatterFieldConfiguration fieldConfig = (FileFormatterFieldConfiguration)itr.next();
				fieldValue = getValueForField(ctx, fieldConfig);
				fieldValue = getLengthModifiedValueForField(ctx, fieldConfig, fieldValue, isFieldsRightJustified);
				
				if (fileLine.length() == 0)
				{
				    appendFixChar(fieldValue, fileLine, fieldConfig.getFieldPrefix(), fieldConfig, false);
					fileLine.append(fieldValue);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldSuffix(), fieldConfig, true);
				}
				else
				{
					fileLine.append(delimiter);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldPrefix(), fieldConfig, false);
					fileLine.append(fieldValue);
					appendFixChar(fieldValue, fileLine, fieldConfig.getFieldSuffix(), fieldConfig, true);
				}
			}
		}
		catch(PaymentMethodFileFormatterException pex)
		{
			throw pex;
		}
		catch(Exception ex)
		{
			LogSupport.minor(ctx, MODULE_NAME, "Failed to format the record" + ex);
			throw new PaymentMethodFileFormatterException("Failed to format the record", ex, PaymentMethodConstants.FAILURE);
		}
		LogSupport.debug(ctx, MODULE_NAME, "The formatted record is[ "+ fileLine.toString() + "]");
		return new StringBuffer(fileLine);
	}
}
