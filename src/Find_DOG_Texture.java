import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.List;

public class Find_DOG_Texture implements PlugInFilter {

    private ImagePlus imp;

    private List<double[]> features;

    static final private int BOX_SIZE = 32;

    static final private int STEP = 8;

    @Override
    public void run(ImageProcessor ip) {
        // generate integral pyramid for the entire image
        IntegralImage imgs[] = TextureTools
                .generateLaplacianIntegralPyramid(ip);

        // create output image
        ImageProcessor output = new ByteProcessor(ip.getWidth(), ip.getHeight());

        // do it
        TextureTools
                .findPairwiseMatches(imgs, features, output, BOX_SIZE, STEP);

        // display
        new ImagePlus("Result of DOG search", output).show();
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        // read parameters
        // split on space
        List<double[]> features = new ArrayList<double[]>();

        String args[] = arg.split("\\s+");
        for (String a : args) {
            String nums[] = a.split(",");
            if (nums.length != 4) {
                throw new IllegalArgumentException("Bad size of feature vector");
            }

            double feature[] = new double[4];
            int i = 0;
            for (String n : nums) {
                feature[i] = Double.parseDouble(n);
            }

            features.add(feature);
        }

        this.features = features;

        return DOES_8G;
    }

}
