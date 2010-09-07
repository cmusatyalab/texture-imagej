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

import ij.ImagePlus;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.List;

public class TextureTools {
    private TextureTools() {
    }

    final static private float[] GAUSSIAN_5X5 = { 2, 4, 5, 4, 2, 4, 9, 12, 9,
            4, 5, 12, 15, 12, 5, 4, 9, 12, 9, 4, 2, 4, 5, 4, 2 };

    static IntegralImage[] generateLaplacianIntegralPyramid(ImageProcessor img) {
        int minDimension = Math.min(img.getWidth(), img.getHeight());
        // floor(lg(w)) - 1
        int pyramidLevels = 30 - Integer.numberOfLeadingZeros(minDimension);

        // System.out.println("pyramid levels: " + pyramidLevels);

        ImageProcessor[] gaussianIm = new ImageProcessor[pyramidLevels + 1];
        ImageProcessor[] laplacianIm = new ImageProcessor[pyramidLevels + 1];
        IntegralImage[] result = new IntegralImage[pyramidLevels * 3];

        // make gaussian pyramid
        gaussianIm[pyramidLevels] = img;

        for (int i = pyramidLevels - 1; i >= 0; i--) {
            gaussianIm[i] = createOnePyramidStepDown(gaussianIm[i + 1]);
        }

        // for (ImageProcessor imageProcessor : gaussianIm) {
        // System.out.println("gauss: " + imageProcessor);
        // }

        // make laplacian pyramid
        laplacianIm[0] = gaussianIm[0];
        // System.out
        // .println("laplacianIm[0] width: " + laplacianIm[0].getWidth());
        for (int i = 1; i <= pyramidLevels; i++) {
            laplacianIm[i - 1]
                    .setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
            laplacianIm[i] = gaussianIm[i - 1].resize(gaussianIm[i].getWidth(),
                    gaussianIm[i].getHeight());
            laplacianIm[i].copyBits(gaussianIm[i], 0, 0, Blitter.DIFFERENCE);
        }

        // for (ImageProcessor imageProcessor : laplacianIm) {
        // System.out.println("laplace: " + imageProcessor);
        // }

        // make integral images
        for (int i = 1; i <= pyramidLevels; i++) {
            int ii = (i - 1) * 3;

            ByteProcessor rr = (ByteProcessor) laplacianIm[i].toFloat(0, null)
                    .convertToByte(true);
            ByteProcessor gg = (ByteProcessor) laplacianIm[i].toFloat(1, null)
                    .convertToByte(true);
            ByteProcessor bb = (ByteProcessor) laplacianIm[i].toFloat(2, null)
                    .convertToByte(true);

            result[ii] = new IntegralImage(rr);
            result[ii + 1] = new IntegralImage(gg);
            result[ii + 2] = new IntegralImage(bb);
            // System.out.println(rr);
            // System.out.println(result[ii]);
        }

        // display ?
        if (false) {
            for (int i = 0; i < gaussianIm.length; i++) {
                new ImagePlus("gaussian " + i, gaussianIm[i]).show();
            }
            for (int i = 0; i < laplacianIm.length; i++) {
                new ImagePlus("laplacian " + i, laplacianIm[i]).show();
            }
        }
        if (false) {
            for (int i = 0; i < result.length; i++) {
                new ImagePlus("integral " + i, result[i].toImageProcessor())
                        .show();
            }
        }

        return result;
    }

    static boolean isPowerOfTwo(int n) {
        if (n <= 0) {
            return false;
        } else {
            return (n & (n - 1)) == 0;
        }
    }

    static double[] generateFeatures(IntegralImage imgs[]) {
        IntegralImage ii = imgs[imgs.length - 1];
        return generateFeatures(imgs, 0, 0, ii.getWidth(), ii.getHeight());
    }

    static double[] generateFeatures(IntegralImage imgs[], int x, int y, int w,
            int h) {
        double result[] = new double[imgs.length];

        for (int i = 0; i < result.length; i++) {
            int scale = ((result.length - 1) - i) / 3;
            int xx = x >> scale;
            int yy = y >> scale;
            int ww = w >> scale;
            int hh = h >> scale;
            try {
                if (ww == 0 || hh == 0) {
                    result[i] = 0;
                } else {
                    result[i] = imgs[i].getAverage(xx, yy, ww, hh);
                }
            } catch (RuntimeException e) {
                System.out.println("(" + x + "," + y + "), (" + w + "x" + h
                        + ")");
                System.out.println(" scale: " + scale + ", (" + xx + "," + yy
                        + "), (" + ww + "x" + hh + ")");
                System.out.println(imgs[i]);

                throw e;
            }
        }

        return result;
    }

    private static ImageProcessor createOnePyramidStepDown(ImageProcessor img) {
        ImageProcessor blurred = img.duplicate();
        blurred.convolve(GAUSSIAN_5X5, 5, 5);
        blurred.setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
        return blurred.resize(blurred.getWidth() / 2, blurred.getHeight() / 2);
    }

    static void findPairwiseMatches(IntegralImage[] imgs,
            List<double[]> features, ImageProcessor output,
            double distanceThreshold) {
        int w = output.getWidth();
        int h = output.getHeight();

        for (double[] feature : features) {
            int boxSize = 1 << ((feature.length / 3) + 1);
            // System.out.println("feature length: " + feature.length
            // + ", boxSize: " + boxSize);
            for (int y = 0; y < h - boxSize; y++) {
                for (int x = 0; x < w - boxSize; x++) {
                    double ff[] = generateFeatures(imgs, x, y, boxSize, boxSize);
                    double distance = 0.0;
                    int vectorOffset = ff.length - feature.length;
                    for (int i = 0; i < feature.length; i++) {
                        double us = ff[i + vectorOffset];
                        double them = feature[i];
                        if (us > them) {
                            distance += Math.abs(us - them) / us;
                        } else {
                            distance += Math.abs(us - them) / them;
                        }
                    }
                    distance /= feature.length;
                    // System.out.println(Arrays.toString(ff));
                    // System.out.println(" " + Arrays.toString(feature));
                    // System.out.println(" " + distance);
                    if (distance <= distanceThreshold) {
                        // System.out.println("filling " + x + "," + y);
                        output.setRoi(x, y, boxSize, boxSize);
                        output.fill();
                    }
                    if (false) {
                        output.setRoi(x, y, boxSize, boxSize);
                        float v = (float) distance;
                        output.setColor(new Color(v, v, v));
                        output.fill();
                    }
                }
            }
        }
    }
}
