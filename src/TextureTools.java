import ij.ImagePlus;
import ij.process.Blitter;
import ij.process.ImageProcessor;

public class TextureTools {
    private TextureTools() {
    }

    final static private int NUM_LAP_PYR_LEVELS = 4;

    final static private float[] GAUSSIAN_5X5 = { 2, 4, 5, 4, 2, 4, 9, 12, 9,
            4, 5, 12, 15, 12, 5, 4, 9, 12, 9, 4, 2, 4, 5, 4, 2 };

    static double[] generateLaplacianPyramidFeatures(ImageProcessor img) {
        ImageProcessor[] gaussianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];
        ImageProcessor[] laplacianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];

        double[] featureValues = new double[NUM_LAP_PYR_LEVELS];

        // make gaussian pyramid
        gaussianIm[NUM_LAP_PYR_LEVELS] = img;

        for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
            int gindex = NUM_LAP_PYR_LEVELS - i;
            gaussianIm[gindex] = createOnePyramidStepDown(gaussianIm[gindex + 1]);
        }

        // make laplacian pyramid
        laplacianIm[0] = gaussianIm[0];
        for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
            laplacianIm[i - 1]
                    .setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
            laplacianIm[i] = laplacianIm[i - 1]
                    .resize(gaussianIm[i].getWidth());
            laplacianIm[i].copyBits(gaussianIm[i], 0, 0, Blitter.DIFFERENCE);
        }

        // calculate response
        for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
            double response = 0.0;
            byte[] pixels = (byte[]) laplacianIm[i].getPixels();
            for (int p = 0; p < pixels.length; p++) {
                response += pixels[p] & 0xFF;
            }

            featureValues[i - 1] = response
                    / (laplacianIm[i].getWidth() * laplacianIm[i].getHeight());
        }

        // display ?
        if (false) {
            for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
                new ImagePlus("laplacian " + i, laplacianIm[i]).show();
                new ImagePlus("gaussian " + i, gaussianIm[i]).show();
            }
        }

        return featureValues;
    }

    private static ImageProcessor createOnePyramidStepDown(ImageProcessor img) {
        ImageProcessor blurred = img.duplicate();
        blurred.convolve(GAUSSIAN_5X5, 5, 5);
        blurred.setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
        return blurred.resize(blurred.getWidth() / 2);
    }
}
