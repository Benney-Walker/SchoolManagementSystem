package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Entity.Institution;
import com.codewithben.schoolmanagementsystem.Utility.BrandingImages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Downloads school logos and signatures from storage and keeps them in memory for report
 * generation.
 *
 * - Cached by URL. Every upload gets a new URL, so a cached image is never out of date.
 * - Bounded (least recently used entries are dropped past {@link #MAX_ENTRIES}).
 * - If a download fails, the report is generated without that image instead of failing,
 *   and the URL is not retried for {@link #FAILURE_RETRY_AFTER} so storage isn't hammered.
 */
@Slf4j
@Service
public class BrandingAssetService {

    static final int MAX_ENTRIES = 200;
    static final Duration FAILURE_RETRY_AFTER = Duration.ofSeconds(60);

    private final ObjectStorageService storage;
    private final Clock clock;

    private final Map<String, BufferedImage> images = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > MAX_ENTRIES;
                }
            });

    private final Map<String, Instant> failedAt = new ConcurrentHashMap<>();

    @Autowired
    public BrandingAssetService(ObjectStorageService storage) {
        this(storage, Clock.systemUTC());
    }

    BrandingAssetService(ObjectStorageService storage, Clock clock) {
        this.storage = storage;
        this.clock = clock;
    }

    /** Everything a report needs to show a school's identity. */
    public record ReportBranding(
            String schoolName,
            String motto,
            String address,
            BufferedImage logo,
            BufferedImage signature
    ) {
    }

    public ReportBranding brandingFor(Institution institution) {
        return new ReportBranding(
                institution.getInstitutionName(),
                blankToNull(institution.getMotto()),
                blankToNull(institution.getAddress()),
                getImage(institution.getReportLogoUrl()).orElse(null),
                getImage(institution.getReportSignatureUrl()).orElse(null)
        );
    }

    /** The image behind a stored URL, from the cache or storage; empty if none or unavailable. */
    public Optional<BufferedImage> getImage(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }
        BufferedImage cached = images.get(url);
        if (cached != null) {
            return Optional.of(cached);
        }

        Instant lastFailure = failedAt.get(url);
        if (lastFailure != null && clock.instant().isBefore(lastFailure.plus(FAILURE_RETRY_AFTER))) {
            return Optional.empty();
        }

        try {
            BufferedImage image = BrandingImages.decode(storage.get(url));
            if (image == null) {
                throw new IllegalStateException("Stored file is not an image");
            }
            images.put(url, image);
            failedAt.remove(url);
            return Optional.of(image);
        } catch (RuntimeException e) {
            failedAt.put(url, clock.instant());
            log.warn("Could not load report image {}; reports will be generated without it", url, e);
            return Optional.empty();
        }
    }

    /** Adds a freshly uploaded image, so the first report after an upload needs no download. */
    public void put(String url, BufferedImage image) {
        images.put(url, image);
        failedAt.remove(url);
    }

    public void evict(String url) {
        if (url != null) {
            images.remove(url);
            failedAt.remove(url);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
