package webapp;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

public class MySNS {
    private String region;
    private SnsClient snsClient;
    private String topicArn;

    public MySNS() {
        region = "us-east-1";
        topicArn = System.getenv("TOPIC_ARN");
        snsClient = SnsClient.builder().region(Region.of(region)).build();
    }

    public void publish(String message) {
        PublishRequest publishRequest = PublishRequest.builder().topicArn(topicArn).message(message).build();
        snsClient.publish(publishRequest);
    }
}
