package webapp;

import com.amazonaws.SDKGlobalConfiguration;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.internal.http.loader.DefaultSdkHttpClientBuilder;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

public class MyS3 {
    private String region;
    private S3Client s3Client;
    private String bucket;
    private static final StatsDClient statsd = new NonBlockingStatsDClient("statsd", "localhost", 8125);

    public MyS3() {
        region = "us-east-1";
        s3Client = S3Client.builder().region(Region.of(region)).build();
        bucket = System.getenv("BUCKET_NAME");
    }

    public void PutObject(String fileName, InputStream binaryStr) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();
        long startTime = System.currentTimeMillis();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(binaryStr.readAllBytes()));
        statsd.recordExecutionTimeToNow("s3.put.timer", startTime);
    }

    public ResponseInputStream<GetObjectResponse> GetObject(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();
        long startTime = System.currentTimeMillis();
        ResponseInputStream<GetObjectResponse> result = s3Client.getObject(getObjectRequest);
        statsd.recordExecutionTimeToNow("s3.get.timer", startTime);
        return result;

    }

    public void DeleteObject(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();
        long startTime = System.currentTimeMillis();
        s3Client.deleteObject(deleteObjectRequest);
        statsd.recordExecutionTimeToNow("s3.delete.timer", startTime);
    }
}