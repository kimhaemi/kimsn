package kr.or.kimsn.radarsms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

//   @Autowired
//   @Qualifier(value = "InterceptorConfig")
//   private HandlerInterceptor interceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // registry.addInterceptor(new InterceptorConfig())
    //     .addPathPatterns("/**")
    //     .excludePathPatterns("/css/**", "/js/**", "/images/**")
    //     ;
  }
}
