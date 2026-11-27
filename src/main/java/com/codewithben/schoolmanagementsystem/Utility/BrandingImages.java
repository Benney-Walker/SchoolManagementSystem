package com.codewithben.schoolmanagementsystem.Utility;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Rules for school logo and signature images, kept free of Spring so they can be unit-tested.
 *
 * An upload is accepted only if it is a PNG or JPEG (checked by the file's signature, not
 * its name), within the size limit, and decodes as an image of sane dimensions. It is then
 * re-encoded as PNG, scaled down to at most {@link #MAX_SIDE} px on its longest side. The
 * re-encode drops any metadata (camera, location) and keeps transparency.
 */
public final class BrandingImages {

    /** Longest side of a stored image; larger uploads are scaled down. */
    public static final int MAX_SIDE = 600;

    /** Uploads with a side larger than this are refused before decoding (decompression bombs). */
    public static final int MAX_SOURCE_SIDE = 6000;

    public enum Kind {
        LOGO("logo", 1024 * 1024),
        SIGNATURE("signature", 512 * 1024);

        private final String label;
        private final int maxBytes;

        Kind(String label, int maxBytes) {
            this.label = label;
            this.maxBytes = maxBytes;
        }

        public String label() {
            return label;
        }

        public int maxBytes() {
            return maxBytes;
        }
    }

    public static final class InvalidImageException extends IllegalArgumentException {
        public InvalidImageException(String message) {
            super(message);
        }
    }

    private BrandingImages() {
    }

    /** Validates an upload and returns the PNG bytes to store. */
    public static byte[] prepare(byte[] upload, Kind kind) {
        if (upload == null || upload.length == 0) {
            throw new InvalidImageException("Choose an image file for the " + kind.label());
        }
        if (upload.length > kind.maxBytes()) {
            throw new InvalidImageException(
                    "The " + kind.label() + " must be " + (kind.maxBytes() / 1024) + " KB or smaller");
        }
        if (!isPng(upload) && !isJpeg(upload)) {
            throw new InvalidImageException("Only PNG or JPEG images are allowed");
        }

        checkDimensions(upload);

        BufferedImage image = decode(upload);
        if (image == null) {
            throw new InvalidImageException("The file is not a readable image");
        }
        return toPng(scaleDown(image));
    }

    /** Decodes stored image bytes, or null if they aren't an image. */
    public static BufferedImage decode(byte[] bytes) {
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            return null;
        }
    }

    public static byte[] toPng(BufferedImage image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", out)) {
                throw new IllegalStateException("No PNG writer available");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Could not encode image", e);
        }
    }

    static boolean isPng(byte[] b) {
        return b.length >= 8
                && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A;
    }

    static boolean isJpeg(byte[] b) {
        return b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    /** Reads the width and height from the header without decoding the pixels. */
    private static void checkDimensions(byte[] upload) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(upload))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidImageException("The file is not a readable image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0) {
                    throw new InvalidImageException("The file is not a readable image");
                }
                if (width > MAX_SOURCE_SIDE || height > MAX_SOURCE_SIDE) {
                    throw new InvalidImageException(
                            "The image is too large; use one under " + MAX_SOURCE_SIDE + " px on each side");
                }
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new InvalidImageException("The file is not a readable image");
        }
    }

    static BufferedImage scaleDown(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int longest = Math.max(width, height);
        boolean hasAlpha = image.getColorModel().hasAlpha();
        int type = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        int targetWidth = width;
        int targetHeight = height;
        if (longest > MAX_SIDE) {
            double scale = (double) MAX_SIDE / longest;
            targetWidth = Math.max(1, (int) Math.round(width * scale));
            targetHeight = Math.max(1, (int) Math.round(height * scale));
        }

        // Always redraw into a standard RGB/ARGB image: PNG-encodes reliably whatever the
        // source colour model was (e.g. CMYK or palette images), and strips metadata.
        BufferedImage result = new BufferedImage(targetWidth, targetHeight, type);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g.dispose();
        }
        return result;
    }
}
