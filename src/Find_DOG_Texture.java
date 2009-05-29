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

    private static final int MIN_SIZE = 16;

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
        TextureTools.findPairwiseMatches(imgs, features, output, 0.07);

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

            // size is first number
            int size = Integer.parseInt(nums[0]);
            if ((size < MIN_SIZE) || (!TextureTools.isPowerOfTwo(size))) {
                IJ.error("Bad feature vector");
                return DONE;
            }

            // lg(n) - 1, isPowerOfTwo(n)
            int expectedLength = (Integer.numberOfTrailingZeros(size) - 1) * 3;
            System.out.println(expectedLength);

            if ((nums.length - 1) != expectedLength) {
                IJ.error("Bad size of feature vector");
                return DONE;
            }

            double feature[] = new double[expectedLength];
            for (int i = 0; i < expectedLength; i++) {
                feature[i] = Double.parseDouble(nums[i + 1]);
            }

            // System.out.println(Arrays.toString(feature));
            features.add(feature);
        }

        if (imp.getWidth() < MIN_SIZE || imp.getHeight() < MIN_SIZE) {
            IJ.error("Image must be at least " + MIN_SIZE + "x" + MIN_SIZE);
            return DONE;
        }

        this.features = features;

        return DOES_RGB;
    }
}
