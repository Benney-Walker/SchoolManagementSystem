package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Utility.BrandingImages;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BrandingAssetServiceTest {

    private static final String LOGO_URL = "https://acc.r2.cloudflarestorage.com/bucket/institutions/IN1/branding/logo-1.png";

    /** Storage that serves files from a map and counts downloads. */
    static class FakeStorage extends ObjectStorageService {
        final Map<String, byte[]> files = new HashMap<>();
        final AtomicInteger downloads = new AtomicInteger();

        FakeStorage() {
            super("", "auto", "", "", "");
        }

        @Override
        public byte[] get(String url) {
            downloads.incrementAndGet();
            byte[] file = files.get(url);
            if (file == null) {
                throw new IllegalStateException("not found");
            }
            return file;
        }
    }

    /** A clock the test can move forward. */
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-11-15T10:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static byte[] png(int width, int height) {
        return BrandingImages.toPng(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
    }

    @Test
    void imageIsDownloadedOnceThenServedFromTheCache() {
        FakeStorage storage = new FakeStorage();
        storage.files.put(LOGO_URL, png(40, 20));
        BrandingAssetService service = new BrandingAssetService(storage, new MutableClock());

        assertEquals(40, service.getImage(LOGO_URL).orElseThrow().getWidth());
        assertTrue(service.getImage(LOGO_URL).isPresent());
        assertEquals(1, storage.downloads.get());
    }

    @Test
    void noUrlMeansNoImageAndNoDownload() {
        FakeStorage storage = new FakeStorage();
        BrandingAssetService service = new BrandingAssetService(storage, new MutableClock());

        assertTrue(service.getImage(null).isEmpty());
        assertTrue(service.getImage(" ").isEmpty());
        assertEquals(0, storage.downloads.get());
    }

    @Test
    void failedDownloadGivesNoImageAndIsNotRetriedForAMinute() {
        FakeStorage storage = new FakeStorage();
        MutableClock clock = new MutableClock();
        BrandingAssetService service = new BrandingAssetService(storage, clock);

        assertTrue(service.getImage(LOGO_URL).isEmpty());
        assertTrue(service.getImage(LOGO_URL).isEmpty());
        assertEquals(1, storage.downloads.get());

        storage.files.put(LOGO_URL, png(10, 10));
        clock.now = clock.now.plus(BrandingAssetService.FAILURE_RETRY_AFTER).plus(Duration.ofSeconds(1));

        assertTrue(service.getImage(LOGO_URL).isPresent());
        assertEquals(2, storage.downloads.get());
    }

    @Test
    void storedFileThatIsNotAnImageCountsAsAFailure() {
        FakeStorage storage = new FakeStorage();
        storage.files.put(LOGO_URL, "not an image".getBytes());
        BrandingAssetService service = new BrandingAssetService(storage, new MutableClock());

        assertTrue(service.getImage(LOGO_URL).isEmpty());
    }

    @Test
    void uploadedImageIsServedWithoutDownloadAndEvictForgetsIt() {
        FakeStorage storage = new FakeStorage();
        BrandingAssetService service = new BrandingAssetService(storage, new MutableClock());
        BufferedImage uploaded = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);

        service.put(LOGO_URL, uploaded);
        assertSame(uploaded, service.getImage(LOGO_URL).orElseThrow());
        assertEquals(0, storage.downloads.get());

        service.evict(LOGO_URL);
        assertTrue(service.getImage(LOGO_URL).isEmpty());
        assertEquals(1, storage.downloads.get());
    }

    @Test
    void brandingCarriesTheSchoolsDetailsAndTreatsBlankAsUnset() {
        FakeStorage storage = new FakeStorage();
        storage.files.put(LOGO_URL, png(30, 30));
        BrandingAssetService service = new BrandingAssetService(storage, new MutableClock());

        Institution school = new Institution();
        school.setInstitutionName("Sunrise Academy");
        school.setMotto("  Knowledge is light ");
        school.setAddress(" ");
        school.setReportLogoUrl(LOGO_URL);

        BrandingAssetService.ReportBranding branding = service.brandingFor(school);

        assertEquals("Sunrise Academy", branding.schoolName());
        assertEquals("Knowledge is light", branding.motto());
        assertNull(branding.address());
        assertNotNull(branding.logo());
        assertNull(branding.signature());
    }
}
