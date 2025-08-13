package com.cedu.api;

import jakarta.annotation.Resource;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.cedu.controller")
public class WrapResponseAdvice implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        //Возможно создать эндпоиты для сырых данных. !Пока не нужно
        //return !returnType.hasMethodAnnotation(NoWrap.class) ;
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {

        if (body instanceof ProblemDetail
                || body instanceof Resource
                || body instanceof byte[]
                ||body instanceof CharSequence) {
            return body;
        }

        if (body instanceof Page<?> page) {
            return PageResponse.of(page);
        }

        if (body instanceof ApiResponse<?> || body instanceof PageResponse<?>) {
            return body;
        }

        return ApiResponse.of(body);
    }
}
