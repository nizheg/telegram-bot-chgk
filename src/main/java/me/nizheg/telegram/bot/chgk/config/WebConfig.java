package me.nizheg.telegram.bot.chgk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Nonnull;

/**
 * @author Nikolay Zhegalin
 */
@Configuration
@ComponentScan({"me.nizheg.telegram.bot.chgk.web", "me.nizheg.payments.yandex.web"})
public class WebConfig extends DelegatingWebMvcConfiguration {

    @Override
    public void addResourceHandlers(@Nonnull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("/");
    }

    @Bean
    @Override
    public @Nonnull
    RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = super.requestMappingHandlerAdapter();
        requestMappingHandlerAdapter.setSynchronizeOnSession(true);
        return requestMappingHandlerAdapter;
    }
}
