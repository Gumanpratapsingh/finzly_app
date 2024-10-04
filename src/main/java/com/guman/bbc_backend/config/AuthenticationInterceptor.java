//package com.guman.bbc_backend.config;
//
//import com.guman.bbc_backend.auth.LoginService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Component
//public class AuthenticationInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private LoginService loginService;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("Authorization");
//        System.out.println("authentication handler");
//        if (token != null && loginService.isValidSession(token)) {
//            return true;
//        }
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        return false;
//    }
//}

//in last build we are not using this we now uses the manual method to check the request