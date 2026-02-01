package com.lms.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
    }

    @Pointcut("within(@org.springframework.stereotype.Service *)")
    public void servicePointcut() {
    }

    @Pointcut("within(@org.springframework.stereotype.Repository *)")
    public void repositoryPointcut() {
    }

    @Around("controllerPointcut()")
    public Object logAroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("REST Request: {}.{}() with arguments: {}", 
                className, methodName, Arrays.toString(joinPoint.getArgs()));
        
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("REST Response: {}.{}() completed in {}ms", 
                    className, methodName, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("REST Error: {}.{}() failed in {}ms with exception: {}", 
                    className, methodName, duration, e.getMessage());
            throw e;
        }
    }

    @Before("servicePointcut()")
    public void logBeforeService(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            log.debug("Entering: {}.{}() with arguments: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(pointcut = "servicePointcut()", returning = "result")
    public void logAfterReturningService(JoinPoint joinPoint, Object result) {
        if (log.isDebugEnabled()) {
            log.debug("Exiting: {}.{}() with result: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    result);
        }
    }

    @AfterThrowing(pointcut = "servicePointcut()", throwing = "e")
    public void logAfterThrowingService(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage());
    }
}
