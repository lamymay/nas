package com.arc.nas.config.aop;

import com.arc.util.StringTool;
import com.arc.nas.model.enums.system.ProjectCodeEnum;
import com.arc.nas.model.exception.BizException;
import com.arc.util.file.FileUtil;
import com.arc.util.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class AspectForController {

    private final static Logger log = LoggerFactory.getLogger(AspectForController.class);

    public static ThreadPoolExecutor executor20Core = new ThreadPoolExecutor(20, 30, 120,
            TimeUnit.SECONDS, new LinkedBlockingQueue(), new CustomizableThreadFactory("线程池MAX30-"),
            new ThreadPoolExecutor.AbortPolicy());

    /**
     * 切点
     */
    @Pointcut("execution(public * com.arc.nas.controller..*Controller.*(..))")
    public void pointcut() {
    }

    //获取参数名和参数值
    public String getParam(ProceedingJoinPoint proceedingJoinPoint) {
        Map<String, Object> map = new HashMap<>();
        Object[] values = proceedingJoinPoint.getArgs();
        String[] names = ((CodeSignature) proceedingJoinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], values[i]);
        }
        return JSON.toJSONString(map);
    }




    /**
     * 环绕通知
     * Around Advice 围绕连接点执行
     * 我们使用@Around来捕获异常信息，并用之前定义好的Result进行异常的返回
     *
     * @param proceedingJoinPoint 切点
     * @return Object
     * @throws Throwable Throwable
     */
    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) {
        long t1 = System.currentTimeMillis();
        new StopWatch();
        // log方法参数
        logMethodAsync(proceedingJoinPoint);
        Object proceed;

        try {
            proceed = proceedingJoinPoint.proceed();
            //log.info("方法耗时={}ms[{},args={}]", (System.currentTimeMillis() - t1), name, JSON.toJSONString(stringStringMap));
            log.info("方法耗时={}", StringTool.displayTimeWithUnit(System.currentTimeMillis(), t1));

        } catch (Throwable throwable) {
            log.error("AOP捕获异常", throwable);
            proceed = convertExceptionDisplay(proceedingJoinPoint, throwable);
            log.info("系统出现异常，总耗时={}", (System.currentTimeMillis() - t1));
            if (proceed instanceof ResponseEntity) {
                return ResponseEntity.status(500).body(Map.of("message", throwable.getMessage()));
            }
        }

        return proceed;

    }

    private void logMethodAsync(ProceedingJoinPoint proceedingJoinPoint) {
        executor20Core.submit(() -> {
            Signature signature = proceedingJoinPoint.getSignature();
            if (signature == null) {
                log.warn("config.aop.AspectForController.logMethodAsync proceedingJoinPoint.getSignature() is null");
            } else {
                String name = signature.getName();
                TreeMap<String, Object> stringStringMap = new TreeMap<>();

                if (signature instanceof MethodSignature) {
                    MethodSignature methodSignature = (MethodSignature) signature;
                    String[] parameterNames = methodSignature.getParameterNames();
                    Object[] args = proceedingJoinPoint.getArgs();
                    if (parameterNames == null) {
                        log.warn("config.aop.AspectForController.logMethodAsync methodSignature.getParameterNames() is null");
                        return;
                    }

                    for (int i = 0; i < parameterNames.length; i++) {
                        Object data = args[i];
                        System.out.println("##########" + data.getClass());

                        if (data instanceof MultipartFile) {
                            MultipartFile file = (MultipartFile) data;
                            log.warn("### 是一个文件,{},size={}", file.getName(), FileUtil.formatFileLengthWithUnit(file.getSize()));
                            continue;
                        } else if (data instanceof HttpServletRequest || data instanceof HttpServletResponse) {
                            log.warn("是特类型 不收集参数 HttpServletRequest / HttpServletResponse");
                            continue;
                        } else {
                            stringStringMap.put(parameterNames[i], args[i]);
                        }
                        //log.info("方法解析参数耗时={}ms[{},参数={}]", (System.currentTimeMillis() - t1), name, JSON.toJSONString(stringStringMap));
                    }
                }
            }

        });

    }


    /**
     * 处理异常的方法
     *
     * @param proceedingJoinPoint ProceedingJoinPoint
     * @param throwable           不同类型的异常处理
     * @return Object
     */
    private Object convertExceptionDisplay(ProceedingJoinPoint proceedingJoinPoint, Throwable throwable) {

        Signature signature = proceedingJoinPoint.getSignature();
        if (signature instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature) signature;
            // 被切的方法
            Method method = methodSignature.getMethod();
            // 返回类型
            Class<?> methodReturnClzType = method.getReturnType();
            log.info("Method={}返回类型={}", ((MethodSignature) signature).getMethod().getName(), methodReturnClzType);
            // 实例化
            if (ResponseEntity.class.isAssignableFrom(methodReturnClzType)) {
                return ResponseEntity.status(500).body(new ErrorData(throwable));
            } else if (Object.class.isAssignableFrom(methodReturnClzType)) {
                return ResponseEntity.status(500).body(new ErrorData(throwable));
            }
        }

        return ResponseEntity.status(500).body(new ErrorData(throwable));
    }

    public static class ErrorData {

        private static final Logger log = LoggerFactory.getLogger(ErrorData.class);

        private String message;
        private long serverResponseTime = System.currentTimeMillis();
        private int code;
        private String details;

        private ErrorData() {
        }

        public ErrorData(Throwable throwable) {
            if (throwable instanceof BizException) {
                BizException bizException = (BizException) throwable;
                log.info("### throwable.getMessage()={}", throwable.getMessage());
                log.info("### bizException.getMessage()={}", bizException.getMessage());
                this.message = throwable.getMessage();
                this.code = bizException.getKey();

            } else if (throwable instanceof RuntimeException) {
                RuntimeException runtimeException = (RuntimeException) throwable;
                log.info("### throwable.getMessage()={}", throwable.getMessage());
                log.info("### runtimeException.getMessage()={}", runtimeException.getMessage());
                this.message = throwable.getMessage();

            } else if (throwable instanceof Throwable) {
                Throwable runtimeException = (Throwable) throwable;
                log.info("### throwable.getMessage()={}", throwable.getMessage());
                log.info("### runtimeException.getMessage()={}", runtimeException.getMessage());
                this.message = throwable.getMessage();

            } else {
                this.code = ProjectCodeEnum.UNKNOWN.getKey();
                this.message = throwable.getMessage();
                this.details = throwable.toString();
            }
        }
    }

}


//    static Map<String, Object> respData = new HashMap<>() {{
//        respData.put("code", ProjectCodeEnum.UNKNOWN.getKey());
//        respData.put("message", ProjectCodeEnum.UNKNOWN.getMsg());
//
//    }};

//        HttpHeaders headers = new HttpHeaders();
//        headers.set("timeConst", msg);//设置个性化的header
//        //headers.set("Content-Encoding", "gzip");
//        //headers.setContentDispositionFormData("attachment","XXX.xls");
//
//        return ResponseEntity
//                .ok()
////        .contentType(MediaType.APPLICATION_OCTET_STREAM)
////        .contentLength(resource.contentLength())
//                .header("cost", msg)
////        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"))
//                .body(result);
//
//
//          try {
//                Object o = methodReturnClzType.getDeclaredConstructor().newInstance();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//