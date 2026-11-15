package com.codewithben.schoolmanagementsystem.Service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/**
 * Files in the private Cloudflare R2 bucket (S3-compatible API).
 *
 * Objects are identified by their URL, {@code <endpoint>/<bucket>/<key>}, which is what the
 * database stores. The bucket is private, so the URL is an identifier, not a public link:
 * files are always read through this service with the bucket credentials.
 *
 * Only URLs inside the configured bucket are accepted ({@link #keyFromUrl}), so a stored URL
 * can never make the server fetch an arbitrary address.
 *
 * When the storage properties are empty the app still starts; {@link #isConfigured()} is
 * false and every operation throws {@link StorageNotConfiguredException}.
 */
@Slf4j
@Service
public class ObjectStorageService {

    private final String endpoint;
    private final String region;
    private final String bucket;
    private final String accessKeyId;
    private final String secretAccessKey;

    private volatile S3Client client;

    public ObjectStorageService(
            @Value("${storage.r2.endpoint:}") String endpoint,
            @Value("${storage.r2.region:auto}") String region,
            @Value("${storage.r2.bucket:}") String bucket,
            @Value("${storage.r2.access-key-id:}") String accessKeyId,
            @Value("${storage.r2.secret-access-key:}") String secretAccessKey) {
        this.endpoint = stripTrailingSlashes(nullToEmpty(endpoint));
        this.region = nullToEmpty(region).isBlank() ? "auto" : region.trim();
        this.bucket = nullToEmpty(bucket);
        this.accessKeyId = nullToEmpty(accessKeyId);
        this.secretAccessKey = nullToEmpty(secretAccessKey);
        if (!isConfigured()) {
            log.warn("File storage (R2) is not configured: logo/signature uploads are disabled "
                    + "and reports are generated without them");
        }
    }

    public boolean isConfigured() {
        return !endpoint.isEmpty() && !bucket.isEmpty() && !accessKeyId.isEmpty() && !secretAccessKey.isEmpty();
    }

    /** Stores the bytes under {@code key} and returns the object's URL. */
    public String put(String key, byte[] content, String contentType) {
        client().putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
                RequestBody.fromBytes(content)
        );
        return urlFor(key);
    }

    /** Downloads the object behind a URL produced by {@link #put}. */
    public byte[] get(String url) {
        String key = keyFromUrl(url)
                .orElseThrow(() -> new IllegalArgumentException("Not a file in this bucket"));
        return client().getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build()
        ).asByteArray();
    }

    /** Deletes the object behind a URL; URLs outside this bucket are ignored. */
    public void delete(String url) {
        Optional<String> key = keyFromUrl(url);
        if (key.isEmpty()) {
            return;
        }
        client().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key.get()).build());
    }

    public String urlFor(String key) {
        return endpoint + "/" + bucket + "/" + key;
    }

    /** The object key for a URL in this bucket, or empty for any other URL. */
    public Optional<String> keyFromUrl(String url) {
        if (url == null || endpoint.isEmpty() || bucket.isEmpty()) {
            return Optional.empty();
        }
        String prefix = endpoint + "/" + bucket + "/";
        if (!url.startsWith(prefix)) {
            return Optional.empty();
        }
        String key = url.substring(prefix.length());
        if (key.isBlank() || key.contains("..") || key.startsWith("/")) {
            return Optional.empty();
        }
        return Optional.of(key);
    }

    private S3Client client() {
        if (!isConfigured()) {
            throw new StorageNotConfiguredException();
        }
        S3Client current = client;
        if (current == null) {
            synchronized (this) {
                current = client;
                if (current == null) {
                    current = S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(region))
                            .credentialsProvider(StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                            .serviceConfiguration(S3Configuration.builder()
                                    .pathStyleAccessEnabled(true)
                                    .build())
                            // R2 does not support the checksums newer SDK versions add to every
                            // request by default; only send them when an operation requires it.
                            .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                            .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                            .overrideConfiguration(ClientOverrideConfiguration.builder()
                                    .apiCallAttemptTimeout(Duration.ofSeconds(5))
                                    .apiCallTimeout(Duration.ofSeconds(15))
                                    .build())
                            .build();
                    client = current;
                }
            }
        }
        return current;
    }

    @PreDestroy
    void close() {
        S3Client current = client;
        if (current != null) {
            current.close();
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String stripTrailingSlashes(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static class StorageNotConfiguredException extends RuntimeException {
        public StorageNotConfiguredException() {
            super("File storage is not configured");
        }
    }
}
