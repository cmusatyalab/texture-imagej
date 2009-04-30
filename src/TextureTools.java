import ij.ImagePlus;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class TextureTools {
    private TextureTools() {
    }

    final static private int NUM_LAP_PYR_LEVELS = 4;

    final static private float[] GAUSSIAN_5X5 = { 2, 4, 5, 4, 2, 4, 9, 12, 9,
            4, 5, 12, 15, 12, 5, 4, 9, 12, 9, 4, 2, 4, 5, 4, 2 };

    static IntegralImage[] generateLaplacianIntegralPyramid(ImageProcessor img) {
        ImageProcessor[] gaussianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];
        ImageProcessor[] laplacianIm = new ImageProcessor[NUM_LAP_PYR_LEVELS + 1];
        IntegralImage[] result = new IntegralImage[NUM_LAP_PYR_LEVELS];

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

        // make integral images
        for (int i = 1; i <= NUM_LAP_PYR_LEVELS; i++) {
            result[i - 1] = new IntegralImage((ByteProcessor) laplacianIm[i]);
        }

        // display ?
        if (false) {
            for (int i = 0; i < result.length; i++) {
                new ImagePlus("integral " + i, result[i].toImageProcessor())
                        .show();
            }
        }

        return result;
    }

    static double[] generateFeatures(IntegralImage imgs[]) {
        double result[] = new double[imgs.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = imgs[i].getAverage();
        }

        return result;
    }

    private static ImageProcessor createOnePyramidStepDown(ImageProcessor img) {
        ImageProcessor blurred = img.duplicate();
        blurred.convolve(GAUSSIAN_5X5, 5, 5);
        blurred.setInterpolationMethod(ImageProcessor.NEAREST_NEIGHBOR);
        return blurred.resize(blurred.getWidth() / 2);
    }
}
