package demo;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.spring.SpringResourceFactory;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.jaxrs.validation.ValidationExceptionMapper;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.ws.rs.Path;

@SpringBootApplication
@ImportResource({"classpath:META-INF/cxf/cxf.xml"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private ApplicationContext ctx;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        CXFServlet cxfServlet = new CXFServlet();
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(cxfServlet, "/services/*");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

    /**
     * Cxf Server initialization.
     */
    @Bean
    public Server jaxRsServer() {
        LinkedList<ResourceProvider> resourceProviders = new LinkedList<>();
        for (String beanName : ctx.getBeanDefinitionNames()) {
            if (ctx.findAnnotationOnBean(beanName, Path.class) != null) {
                SpringResourceFactory factory = new SpringResourceFactory(beanName);
                factory.setApplicationContext(ctx);
                resourceProviders.add(factory);
            }
        }

        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setBus(ctx.getBean(SpringBus.class));
        //ValidationExceptionMapper allows server to answer with 400 when validation failed
        factory.setProviders(Arrays.asList(new JacksonJsonProvider(), new ValidationExceptionMapper()));
        factory.setResourceProviders(resourceProviders);

        factory.setOutInterceptors(Arrays.<Interceptor<? extends Message>>asList(new LoggingOutInterceptor()));
        factory.setInInterceptors(Arrays.<Interceptor<? extends Message>>asList(new LoggingInInterceptor(), new JAXRSBeanValidationInInterceptor()));

        Map<String, Object> properties = new HashMap<>();
        //case insensitive for providers query params
        properties.put("org.apache.cxf.http.case_insensitive_queries", true);

        factory.setProperties(properties);

        return factory.create();
    }
}