package ru.vinpin.statisticservice.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;

@Component("customKeyGenerator")
public class CustomKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return method.getName() + "_" + Arrays.deepHashCode(params);
    }
}