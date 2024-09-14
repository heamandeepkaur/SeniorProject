package com.example.SeniorProject;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer
{
    public void addViewControllers(ViewControllerRegistry registry)
    {
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/login").setViewName("Login");
        registry.addViewController("/forgetPassword").setViewName("forgetPasswordForm");
        registry.addViewController("/about").setViewName("about");
        registry.addViewController("/signUp").setViewName("signup");
        registry.addViewController("/gallery").setViewName("gallery");
        registry.addViewController("/rental").setViewName("rentals");
        registry.addViewController("/order").setViewName("UserOrder_page");
        registry.addViewController("/faq").setViewName("faq");
        registry.addViewController("/products").setViewName("products");
        registry.addViewController("/UserDetails_page").setViewName("UserDetails_page");
        registry.addViewController("/UserCart_page").setViewName("UserCart_page");
        registry.addViewController("/displayInfo").setViewName("displayInfo");
        registry.addViewController("/resetPassword").setViewName("resetPasswordForm");
        registry.addViewController("/Orders").setViewName("Orders");
        registry.addViewController("/user_order").setViewName("UserOrder_page");
        registry.addViewController("uploadPicture").setViewName("uploadPictureForm");

        registry.addViewController("/card").setViewName("card");
        registry.addViewController("/paymentGood").setViewName("paymentGood");
        registry.addViewController("/paymentBad").setViewName("paymentBad");
        registry.addViewController("/newProduct").setViewName("newProduct");
        registry.addViewController("/theProduct").setViewName("theProduct");
    }
}
