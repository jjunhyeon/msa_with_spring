package com.example.zuulservice.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// 일반적인 bean형태로만 등록
// 추상클래스의 추상메서드를 제공하지 않았기 때문에 초기오류 발생

@Slf4j
@Component
public class ZuulLoggingFilter extends ZuulFilter {
    //Logger logger = LoggerFactory.getLogger(ZuulLoggingFilter.class);
    // 사용자의 요청이 들어올대마다 run() 메서드 우선 실행
    @Override
    public Object run() throws ZuulException {
        log.info("***********************print log:");

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info("**********************printing logs:"+ request.getRequestURL());
        return null;
    }

    @Override
    public String filterType() {
        return "pre";
    }
    @Override
    public int filterOrder() {
        return 1;
    }

    // 필터로 쓰겟다.
    @Override
    public boolean shouldFilter() {
        return true;
    }

}
