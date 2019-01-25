package com.token.bucket.config;

import com.token.bucket.annotation.EnableTokenBucket;
import com.token.bucket.quartz.SchedulerAllJob;
import com.token.bucket.bucket.TokenBucket;
import com.token.bucket.util.RedisUtil;
import com.token.bucket.util.RunToken;
import com.token.bucket.util.SpringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.util.ClassUtils;

@Configuration
public class StartEndLinstener implements ApplicationListener<ApplicationReadyEvent> {

    private  ConfigurableApplicationContext configurableApplicationContext;
    private  DefaultListableBeanFactory defaultListableBeanFactory;

    private static boolean START_STATE = true;

    @Value("${spring.application.name}")
    private String name;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        try {

            if(!isLoad())
                return;

            init(applicationReadyEvent);

            Class c = getMainClass();

            if(c.isAnnotationPresent(EnableTokenBucket.class)) {

                if(c.isAnnotationPresent(EnableEurekaClient.class)) {
                    RunToken.enableEureka = true;

                    RunToken.applicationName = name;
                }

                RunToken.enableTokenBucket = true;
                loadBean(RedisUtil.class);
                loadBean(TokenBucket.class);

                TokenBucket tokenBucket = SpringUtil.getBean(TokenBucket.class);

                if(tokenBucket.getMethod() != null && !tokenBucket.getMethod().isEmpty()) {
                    RunToken.runJob = true;

                    loadBean(SchedulerFactoryBean.class);
                    loadBean(SchedulerAllJob.class);

                    SpringUtil.getBean(SchedulerAllJob.class).scheduleJobs();
                }
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Class getMainClass() throws ClassNotFoundException {
        StackTraceElement[] stackTraceElements = new RuntimeException().getStackTrace();

        for(StackTraceElement ste : stackTraceElements) {
            if ("main".equals(ste.getMethodName())) {
                return Class.forName(ste.getClassName());
            }
        }
        return null;
    }

    private boolean isLoad() {
//        if(ClassUtils.isPresent("org.springframework.cloud.config.client.ConfigClientAutoConfiguration",null)) {
//            return START_STATE = !START_STATE;
//        } else
//            return true;

        return START_STATE = !START_STATE;
    }

    private void loadBean(Class c) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(c);

        char[] chars = c.getSimpleName().toCharArray();
        chars[0] += 32;

//        defaultListableBeanFactory.registerSingleton(String.valueOf(chars), beanDefinitionBuilder.getBeanDefinition());
        defaultListableBeanFactory.registerBeanDefinition(String.valueOf(chars), beanDefinitionBuilder.getBeanDefinition());
    }

    private void init(ApplicationReadyEvent applicationReadyEvent) {
        configurableApplicationContext = applicationReadyEvent.getApplicationContext();
        defaultListableBeanFactory = (DefaultListableBeanFactory)configurableApplicationContext.getAutowireCapableBeanFactory();
        new SpringUtil().setApplicationContext(configurableApplicationContext);
    }
}