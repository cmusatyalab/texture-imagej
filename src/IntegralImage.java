import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class IntegralImage {
    final private long[] img;

    final private int w;

    final private int h;

    public IntegralImage(ByteProcessor ip) {
        w = ip.getWidth() + 1;
        h = ip.getHeight() + 1;
        img = new long[w * h];

        // generate integral image
        byte[] pxls = (byte[]) ip.getPixels();

        int pxlsi = 0;
        for (int y = 1; y < h; y++) {
            long rowSum = 0;
            for (int x = 1; x < w; x++) {
                int i = y * w + x;

                rowSum += (pxls[pxlsi++] & 0xFF);
                img[i] = rowSum + img[i - w];
            }
        }
    }

    public long getSum(int x, int y, int width, int height) {
        // this contains the part we want
        long d = img[(y + height) * w + (x + width)];

        // this is extra to the left and above what we want
        long a = img[y * w + x];
        long b = img[y * w + (x + width)];
        long c = img[(y + height) * w + x];

        return d + a - b - c;
    }

    public double getAverage(int x, int y, int width, int height) {
        // System.out.println("getAverage: " + x + "," + y + "," + width + ","
        // + height);
        return (double) getSum(x, y, width, height) / (double) (width * height);
    }

    public long getSum() {
        return getSum(0, 0, w - 1, h - 1);
    }

    public double getAverage() {
        return getAverage(0, 0, w - 1, h - 1);
    }

    public ImageProcessor toImageProcessor() {
        float floatimg[][] = new float[h - 1][w - 1];
        for (int y = 0; y < h - 1; y++) {
            for (int x = 0; x < w - 1; x++) {
                floatimg[y][x] = img[(y + 1) * w + (x + 1)];
            }
        }
        return new FloatProcessor(floatimg);
    }

    public int getWidth() {
        return w - 1;
    }

    public int getHeight() {
        return h - 1;
    }

    @Override
    public String toString() {
        return "integral image: " + getWidth() + "x" + getHeight() + ", sum: "
                + getSum();
    }
}
