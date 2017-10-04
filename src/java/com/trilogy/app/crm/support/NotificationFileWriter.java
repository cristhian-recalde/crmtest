package com.trilogy.app.crm.support;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.trilogy.app.crm.paymentmethod.filegenerator.PaymentFileRecord;
import com.trilogy.framework.xhome.context.Context;
import com.trilogy.framework.xlog.log.LogSupport;

public class NotificationFileWriter
{
	private Context ctx;
	private PrintWriter fileWriter ;
	private String fileName = null;
	
	public NotificationFileWriter()
	{		
	}

	public void init(Context ctx, String outputDirectory, String outBoundFileName, String fileExtension, Date currentDate, boolean append) throws Exception
	{
		this.ctx = ctx;
		Character formatCharacter = '%';
		fileName = fetchFileNameByDateFormat(ctx, outBoundFileName, formatCharacter, currentDate);
		fileName = fileName + fileExtension;
		this.fileWriter = new PrintWriter(new FileOutputStream(new File(outputDirectory+ File.separator + fileName), true));
	}

	public void populateDataInFile(PaymentFileRecord fileRecordBean)
	{
		LogSupport.info(ctx, this, "Starting writing file name[" + fileName + "]");
		if (fileRecordBean.getHeader() != null && fileRecordBean.getHeader().length() > 0)
		{
			fileWriter.write(fileRecordBean.getHeader().toString());
		}
		writeFileDataSegment(fileRecordBean.getDataRecordList());
		if (fileRecordBean.getTrailer() != null && fileRecordBean.getTrailer().length() > 0)
		{
			fileWriter.write(fileRecordBean.getTrailer().toString());
		}
		if (fileRecordBean.getEndRecord() != null && fileRecordBean.getEndRecord().length() > 0)
		{
			fileWriter.write(fileRecordBean.getEndRecord().toString());
		}
		close();
	}

	private void writeFileDataSegment(List<StringBuffer> fileData)
	{
		for (Iterator<StringBuffer> itr = fileData.iterator(); itr.hasNext();)
		{
			StringBuffer data = (StringBuffer) itr.next();
			LogSupport.info(ctx, this, "Started printing record[ " + data + " ], in file[" + fileName + "]");		
			fileWriter.write(data.toString());
		}
	}

	public void close() 
	{
		if (fileWriter != null)
		{	
			fileWriter.flush(); 
			fileWriter.close();
			fileWriter = null; 
		}			
	}
	
	/**
     * @param outBoundFileName
     * @param formatCharacter 
     */
    private String fetchFileNameByDateFormat(Context ctx, String outBoundFileName, Character formatCharacter, Date currentDate)
    {
        int index = outBoundFileName.indexOf(formatCharacter);
        if(index != -1)
        {
            String subString = outBoundFileName.substring(index);
            String[] arrStr = subString.trim().split(formatCharacter.toString());
            for (int i = 1; i < arrStr.length; i++)
            {
                String dateFormat = arrStr[i];
                if(i%2 != 0)
                {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                    String dateString = sdf.format(currentDate);
                    outBoundFileName = outBoundFileName.replaceAll(dateFormat, dateString);
                }
            }
        }
         outBoundFileName = outBoundFileName.replace(formatCharacter.toString(),"");
         return outBoundFileName;
    }
}
