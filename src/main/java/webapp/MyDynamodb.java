package webapp;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class MyDynamodb {
    private AmazonDynamoDB client;
    private String region;
    private String tableName;

    public MyDynamodb() {
        region = "us-east-1";
        tableName = System.getenv("DYNAMO_TABLE");
        client = AmazonDynamoDBClient.builder().withRegion(region).build();
    }

    public boolean containKey(String key) {
        Table table = new DynamoDB(client).getTable(tableName);
        return table.getItem("token", key) != null;
    }
}
