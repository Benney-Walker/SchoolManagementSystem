package com.codewithben.schoolmanagementsystem.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectStorageServiceTest {

    private static final String ENDPOINT = "https://acc.r2.cloudflarestorage.com";

    private static ObjectStorageService configured() {
        return new ObjectStorageService(ENDPOINT + "/", "auto", "eazy-branding", "key-id", "secret");
    }

    @Test
    void urlIsEndpointBucketAndKeyWithoutDoubleSlashes() {
        assertEquals(ENDPOINT + "/eazy-branding/institutions/IN1/branding/logo-1.png",
                configured().urlFor("institutions/IN1/branding/logo-1.png"));
    }

    @Test
    void onlyUrlsInsideTheBucketMapToAKey() {
        ObjectStorageService storage = configured();

        assertEquals("institutions/IN1/branding/logo-1.png",
                storage.keyFromUrl(ENDPOINT + "/eazy-branding/institutions/IN1/branding/logo-1.png").orElseThrow());
        assertTrue(storage.keyFromUrl("https://evil.example.com/eazy-branding/x.png").isEmpty());
        assertTrue(storage.keyFromUrl(ENDPOINT + "/other-bucket/x.png").isEmpty());
        assertTrue(storage.keyFromUrl(ENDPOINT + "/eazy-branding/../other/x.png").isEmpty());
        assertTrue(storage.keyFromUrl(ENDPOINT + "/eazy-branding/").isEmpty());
        assertTrue(storage.keyFromUrl(null).isEmpty());
    }

    @Test
    void missingSettingsMeanNotConfigured() {
        assertTrue(configured().isConfigured());
        assertFalse(new ObjectStorageService("", "auto", "", "", "").isConfigured());
        assertFalse(new ObjectStorageService(ENDPOINT, "auto", "bucket", "key-id", " ").isConfigured());
        assertFalse(new ObjectStorageService(null, null, null, null, null).isConfigured());
    }

    @Test
    void operationsFailClearlyWhenNotConfigured() {
        ObjectStorageService storage = new ObjectStorageService("", "auto", "", "", "");

        assertThrows(ObjectStorageService.StorageNotConfiguredException.class,
                () -> storage.put("k", new byte[]{1}, "image/png"));
    }
}
