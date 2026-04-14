package com.oran.oranpicturebackend;

import com.oran.oranpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.Bucket;
import com.qcloud.cos.region.Region;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class OranPictureBackendApplicationTests {

    @Test
    void contextLoads() {
    }


    @Resource
    private CosClientConfig cosClientConfig;

    @Test
    void testCosConnection() {
        System.out.println("========== COS配置信息 ==========");
        System.out.println("SecretId: " + cosClientConfig.getSecretId());
        System.out.println("SecretKey: " + cosClientConfig.getSecretKey().substring(0, 5) + "****");
        System.out.println("Region: " + cosClientConfig.getRegion());
        System.out.println("Bucket: " + cosClientConfig.getBucket());
        System.out.println("Host: " + cosClientConfig.getHost());
        System.out.println("系统时间: " + System.currentTimeMillis());
        System.out.println("================================");

        try {
            COSCredentials cred = new BasicCOSCredentials(
                    cosClientConfig.getSecretId(),
                    cosClientConfig.getSecretKey()
            );

            ClientConfig clientConfig = new ClientConfig(new Region(cosClientConfig.getRegion()));

            COSClient cosClient = new COSClient(cred, clientConfig);

            System.out.println("\n========== 测试COS连接 ==========");
            List<Bucket> buckets = cosClient.listBuckets();
            System.out.println("连接成功！");
            System.out.println("Bucket列表:");
            buckets.forEach(bucket -> System.out.println("  - " + bucket.getName()));
            System.out.println("================================");

            cosClient.shutdown();
        } catch (Exception e) {
            System.err.println("\n❌ COS连接失败:");
            e.printStackTrace();
        }
    }
}
