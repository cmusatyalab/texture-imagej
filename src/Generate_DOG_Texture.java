import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;

public class Generate_DOG_Texture implements PlugInFilter {

    private ImagePlus imp;

    static final private int SIZE = 32;

    @Override
    public void run(ImageProcessor ip) {
        // adjust image ROI to square
        Rectangle r = ip.getRoi();
        // int size = Math.min(r.width, r.height);
        int size = SIZE;
        r.x += (r.width - size) / 2;
        r.y += (r.height - size) / 2;
        r.width = size;
        r.height = size;

        // tell user we've changed the roi
        imp.setRoi(r);

        // resize
        ImageProcessor subip = ip.crop().resize(TextureTools.BOX_SIZE);

        if (false) {
            IntegralImage ii = new IntegralImage((ByteProcessor) subip);
            System.out.println(ii.getSum());
            System.out.println(ii.getSum(0, 0, 1, 1));
            System.out.println(ii.getSum(0, 1, 1, 1));
            System.out.println(ii.getSum(1, 0, 1, 1));
            System.out.println(ii.getSum(1, 1, 1, 1));
            new ImagePlus("integral", ii.toImageProcessor()).show();
        }

        IntegralImage imgs[] = TextureTools
                .generateLaplacianIntegralPyramid(subip);

        double features[] = TextureTools.generateFeatures(imgs);

        StringBuilder sb = new StringBuilder();
        for (double d : features) {
            sb.append(d);
            sb.append(",");
        }

        IJ.write(sb.substring(0, sb.length() - 1));
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        return DOES_8G | ROI_REQUIRED;
    }

}
