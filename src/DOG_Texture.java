import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.Arrays;

public class DOG_Texture implements PlugInFilter {

    final static private int SIZE = 32;

    private ImagePlus imp;

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
        ImageProcessor subip = ip.crop().resize(SIZE);

        double features[] = TextureTools
                .generateLaplacianPyramidFeatures(subip);

        System.out.println(Arrays.toString(features));
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        return DOES_8G | ROI_REQUIRED;
    }

}
