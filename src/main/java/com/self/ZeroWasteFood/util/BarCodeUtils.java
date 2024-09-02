package com.self.ZeroWasteFood.util;


import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BarCodeUtils {

    private BarCodeUtils(){}

    public static Result extractBarCodeFromImage(File img) throws NotFoundException, IOException {
        BufferedImage bufferedImage = ImageIO.read(img);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));
        return new MultiFormatReader().decode(binaryBitmap);
    }
}
