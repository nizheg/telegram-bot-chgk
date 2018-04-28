package me.nizheg.telegram.bot.chgk.config;

import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.util.HttpSessionMutexListener;

import javax.annotation.Nonnull;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Nikolay Zhegalin
 */
public class WebApplication extends AbstractAnnotationConfigDispatcherServletInitializer {

    public WebApplication() {}

    @Override
    public void onStartup(@Nonnull ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);
        servletContext.addListener(HttpSessionMutexListener.class);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[]{
            AppConfig.class
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{
            WebConfig.class
        };
    }

    @Override
    @Nonnull
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return new Filter[]{characterEncodingFilter};
    }


}
