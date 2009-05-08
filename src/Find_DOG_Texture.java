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
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Find_DOG_Texture implements PlugInFilter {

    private ImagePlus imp;

    private List<double[]> features;

    static private String lastFeatureString = "";

    @Override
    public void run(ImageProcessor ip) {
        // System.out.println(ip);

        // generate integral pyramid for the entire image
        IntegralImage imgs[] = TextureTools
                .generateLaplacianIntegralPyramid(ip);

        // for (IntegralImage ii : imgs) {
        // System.out.println(ii);
        // }

        // create output image
        ImageProcessor output = new ByteProcessor(ip.getWidth(), ip.getHeight());
        output.invertLut();
        output.fill();
        output.setColor(Color.BLACK);

        // do it
        TextureTools.findPairwiseMatches(imgs, features, output,
                TextureTools.BOX_SIZE, 0.07);

        // display
        new ImagePlus("Result of DOG search", output).show();
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        // read parameters
        // split on space
        List<double[]> features = new ArrayList<double[]>();
        if (arg.isEmpty()) {
            arg = IJ.getString("features", lastFeatureString);
            if (arg.isEmpty()) {
                return DONE;
            }
            lastFeatureString = arg;
        }
        String args[] = arg.split(";");
        for (String a : args) {
            String nums[] = a.split(",");
            if (nums.length != 4 * 3) {
                throw new IllegalArgumentException("Bad size of feature vector");
            }

            double feature[] = new double[12];
            int i = 0;
            for (String n : nums) {
                feature[i++] = Double.parseDouble(n);
            }

            features.add(feature);
        }

        this.features = features;

        return DOES_RGB;
    }
}
