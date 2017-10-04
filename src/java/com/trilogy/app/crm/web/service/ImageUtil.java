package com.trilogy.app.crm.web.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import java.io.BufferedOutputStream;


/*
 * author: simar.singh@redknee Utilities to manipulate images
 */
public class ImageUtil
{

    public static void resizeImage(String sourceFile, String destinationFile, String type, int maxWidth_X,
            int maxHeight_Y) throws IOException
    {
        resizeImage(new File(sourceFile), new File(destinationFile), type, maxWidth_X, maxHeight_Y);
    }
    
    public static void resizeImage(File sourceFile, File destinationFile, String type, int maxWidth_X,
            int maxHeight_Y) throws IOException
    {
        BufferedImage image = resizeImage(sourceFile, maxWidth_X, maxHeight_Y);
        final BufferedOutputStream imageBufferFileStream = new BufferedOutputStream(new FileOutputStream(destinationFile));
        try{
            writeImageToStream(image, type,imageBufferFileStream);
        }finally{
            //do not know what happened; altleast close the stream
            imageBufferFileStream.close();
        }
    }


    public static BufferedImage resizeImage(File f, int maxWidth_X, int maxHeight_Y) throws IOException
    {
        // re-size image - preserve the aspect ratio
        ImageInputStream iis = ImageIO.createImageInputStream(f);
        Iterator readers = ImageIO.getImageReaders(iis);
        ImageReader reader = (ImageReader) readers.next();
        reader.setInput(iis, true);
        ImageReadParam param = reader.getDefaultReadParam();
        BufferedImage originalImage = reader.read(0, param);
        int oHeight_Y = originalImage.getHeight();
        int oWidth_X = originalImage.getWidth();
        // float fHeight_Y = maxHeight_Y;
        // float fWidth_X = maxWidth_X;
        final int shrinkFactor;
        if (oHeight_Y > oWidth_X && oHeight_Y > maxHeight_Y)
        {
            // height is greater so scale height and factor out width using the original
            // aspect ratio
            shrinkFactor = (int) (((float) oHeight_Y / maxHeight_Y) + 1f);
        }
        else if (oWidth_X > oHeight_Y && oWidth_X > maxWidth_X)
        {
            // width is greater so scale width and factor out height using the original
            // aspect ratio.
            shrinkFactor = (int) (((float) oWidth_X / maxWidth_X) + 1f);
        }
        else
        {
            // return the original image
            return originalImage;
        }
        if(shrinkFactor==1)
        {
            //no shrinking required
            return originalImage;
        }
        // we do not need to re-size
        param.setSourceSubsampling(shrinkFactor, shrinkFactor, 0, 0);
        return reader.read(0, param);
    }


    public static void writeImageToStream(BufferedImage image, String type, OutputStream out) throws IOException
    {
        ImageIO.write(image, type, out);
    }


    public static void writeImageToFile(BufferedImage image, String type, File file) throws IOException
    {
        ImageIO.write(image, type, file);
    }
}
