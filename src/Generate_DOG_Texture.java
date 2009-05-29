/*
 *  Texture Tools for ImageJ
 *
 *  Copyright (c) 2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  Texture Tools is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, version 2.
 *
 *  Texture Tools is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Texture Tools. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.Rectangle;

public class Generate_DOG_Texture implements PlugInFilter {

    private static final int MIN_SIZE = 16;

    private ImagePlus imp;

    @Override
    public void run(ImageProcessor ip) {
        // adjust image ROI to square
        Rectangle r = ip.getRoi();
        int size = Math.max(MIN_SIZE, Integer.highestOneBit(Math.min(r.width,
                r.height)));
        r.x += (r.width - size) / 2;
        r.y += (r.height - size) / 2;
        r.width = size;
        r.height = size;

        // constrain
        int w = ip.getWidth();
        int h = ip.getHeight();
        if (r.x < 0) {
            r.x = 0;
        }
        if (r.y < 0) {
            r.y = 0;
        }
        int overX = (r.x + r.width) - w;
        if (overX > 0) {
            r.x -= overX;
        }
        int overY = (r.y + r.height) - h;
        if (overY > 0) {
            r.y -= overY;
        }

        // tell user we've changed the roi
        imp.setRoi(r);

        // crop
        ImageProcessor subip = ip.crop();

        IntegralImage imgs[] = TextureTools
                .generateLaplacianIntegralPyramid(subip);

        double features[] = TextureTools.generateFeatures(imgs);

        StringBuilder sb = new StringBuilder(size + ",");
        for (double d : features) {
            sb.append(d);
            sb.append(",");
        }

        IJ.write(sb.substring(0, sb.length() - 1));
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        if (imp.getWidth() < MIN_SIZE || imp.getHeight() < MIN_SIZE) {
            IJ.error("Image must be at least " + MIN_SIZE + "x" + MIN_SIZE);
            return DONE;
        }

        return DOES_RGB | ROI_REQUIRED;
    }

}
