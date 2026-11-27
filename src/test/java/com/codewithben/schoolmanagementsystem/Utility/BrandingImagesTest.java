package com.codewithben.schoolmanagementsystem.Utility;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BrandingImagesTest {

    private static byte[] encode(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, out));
        return out.toByteArray();
    }

    private static BufferedImage image(int width, int height, int type) {
        return new BufferedImage(width, height, type);
    }

    @Test
    void smallPngIsKeptAtItsSizeAndStoredAsPng() throws IOException {
        byte[] stored = BrandingImages.prepare(encode(image(200, 100, BufferedImage.TYPE_INT_RGB), "png"),
                BrandingImages.Kind.LOGO);

        assertTrue(BrandingImages.isPng(stored));
        BufferedImage decoded = BrandingImages.decode(stored);
        assertEquals(200, decoded.getWidth());
        assertEquals(100, decoded.getHeight());
    }

    @Test
    void largeImageIsScaledDownKeepingItsShape() throws IOException {
        byte[] stored = BrandingImages.prepare(encode(image(1200, 300, BufferedImage.TYPE_INT_RGB), "png"),
                BrandingImages.Kind.SIGNATURE);

        BufferedImage decoded = BrandingImages.decode(stored);
        assertEquals(BrandingImages.MAX_SIDE, decoded.getWidth());
        assertEquals(150, decoded.getHeight());
    }

    @Test
    void jpegIsAcceptedAndStoredAsPng() throws IOException {
        byte[] stored = BrandingImages.prepare(encode(image(300, 300, BufferedImage.TYPE_INT_RGB), "jpg"),
                BrandingImages.Kind.LOGO);

        assertTrue(BrandingImages.isPng(stored));
    }

    @Test
    void transparencyIsKept() throws IOException {
        BufferedImage source = image(50, 50, BufferedImage.TYPE_INT_ARGB); // fully transparent

        BufferedImage decoded = BrandingImages.decode(
                BrandingImages.prepare(encode(source, "png"), BrandingImages.Kind.SIGNATURE));

        assertTrue(decoded.getColorModel().hasAlpha());
        assertEquals(0, (decoded.getRGB(10, 10) >>> 24) & 0xFF);
    }

    @Test
    void emptyUploadIsRefused() {
        assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(new byte[0], BrandingImages.Kind.LOGO));
        assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(null, BrandingImages.Kind.LOGO));
    }

    @Test
    void uploadOverTheSizeLimitIsRefused() {
        byte[] tooBigForSignature = new byte[BrandingImages.Kind.SIGNATURE.maxBytes() + 1];
        tooBigForSignature[0] = (byte) 0x89;

        BrandingImages.InvalidImageException e = assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(tooBigForSignature, BrandingImages.Kind.SIGNATURE));
        assertEquals("The signature must be 512 KB or smaller", e.getMessage());
    }

    @Test
    void otherFileTypesAreRefusedWhateverTheirName() throws IOException {
        byte[] gif = encode(image(10, 10, BufferedImage.TYPE_INT_RGB), "gif");
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes();

        assertEquals("Only PNG or JPEG images are allowed", assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(gif, BrandingImages.Kind.LOGO)).getMessage());
        assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(svg, BrandingImages.Kind.LOGO));
    }

    @Test
    void fileWithAPngHeaderButNoImageIsRefused() throws IOException {
        byte[] png = encode(image(10, 10, BufferedImage.TYPE_INT_RGB), "png");
        byte[] broken = Arrays.copyOf(png, 20);

        assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(broken, BrandingImages.Kind.LOGO));
    }

    @Test
    void imageWithHugeDimensionsIsRefusedBeforeDecoding() throws IOException {
        byte[] wide = encode(image(BrandingImages.MAX_SOURCE_SIDE + 1, 1, BufferedImage.TYPE_BYTE_GRAY), "png");

        assertThrows(BrandingImages.InvalidImageException.class,
                () -> BrandingImages.prepare(wide, BrandingImages.Kind.LOGO));
    }
}
