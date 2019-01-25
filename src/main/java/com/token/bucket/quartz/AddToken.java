package com.token.bucket.quartz;

import com.token.bucket.bucket.TokenBucket;
import com.token.bucket.util.RedisUtil;
import com.token.bucket.util.RunToken;
import com.token.bucket.util.SpringUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AddToken implements Job {

    private TokenBucket tokenBucket;

    private RedisUtil redisUtils;

    private DiscoveryClient discoveryClient;

    public void run() {
        String instanceNum = mult();

        for (Map.Entry<String, String> m : tokenBucket.getMethod().entrySet()) {

            Object obj = redisUtils.get(m.getKey());

            String[] data = m.getValue().split(",");

            BigDecimal rate = mult(data[0], instanceNum);

            BigDecimal max = mult(data[1], instanceNum);

            if (obj == null) {
                redisUtils.set(m.getKey(), rate);
                return;
            }

            BigDecimal num = new BigDecimal(obj.toString());

            num = rate.add(num).setScale(2, BigDecimal.ROUND_HALF_UP);

            if (num.compareTo(max) == 1) {
                return;
            }

            redisUtils.set(m.getKey(), num);
            System.out.println(String.format("↑ %s have %s --- max %s", m.getKey(), num.doubleValue(), max.doubleValue()));
        }
    }

    private BigDecimal mult(String num1, String num2) {

        BigDecimal bigDecimal1 = new BigDecimal(num1);
        BigDecimal bigDecimal2 = new BigDecimal(num2);

        return bigDecimal1.multiply(bigDecimal2).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        getBean();
        run();
    }

    void getBean() {
        if (tokenBucket == null)
            tokenBucket = (TokenBucket) SpringUtil.getBean(TokenBucket.class);


        if (redisUtils == null)
            redisUtils = (RedisUtil) SpringUtil.getBean(RedisUtil.class);

        if (discoveryClient == null)
            discoveryClient = (DiscoveryClient) SpringUtil.getBean(DiscoveryClient.class);
    }

    String mult() {
        if (!RunToken.enableEureka)
            return "1";

        List<ServiceInstance> lists = discoveryClient.getInstances(RunToken.applicationName);

//        System.out.println(String.format("%s 当前实例数：%s",RunToken.applicationName,lists.size()));

        return String.valueOf(lists.size());
    }
}
