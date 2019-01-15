package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 捕获全局异常。
 * 包含了@Component 组件。可以通过扫描加入bean。
 * @ControllerAdvice 控制器增强
 * 可以配合@ExceptionHandler来增强所有的@requestMapping方法。
 */
@ControllerAdvice
public class ExceptionCatch {
    // log4g日志
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCatch.class);

    // 使用EXCEPTIONS存放异常类型和错误代码的映射，ImmutableMap的特点的一旦创建不可改变，并且线程安全
    // 记录错误类和错误信息。
    private static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;
    //使用builder来构建一个异常类型和错误代码的异常
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();

    // 初始化ImmutableMap
    static{
        //在这里加入一些基础的异常类型判断
        builder.put(HttpMessageNotReadableException.class, CommonCode.INVALID_PARAM);
    }

    /**
     * 捕获 CustomException异常,自定义异常可以预知的异常。
     * 定义异常类型，如果@requestMapping标注的方法抛出对应异常会对该方法进行增强。
     * @param e 捕获到抛出的异常对象。
     * @return
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException e) {
        // 输出日志
        LOGGER.error("catch exception : {}\r\nexception: ",e.getMessage(), e);
        // 从捕获的异常获取要响应在页面的状态信息。
        // 代理抛出异常的方法响应页面。
        return new ResponseResult(e.getResultCode());
    }


    /**
     * 捕获除了自定义异常之外的异常，也就是SpringMVC帮抛出的异常。
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception exception){
        //记录日志
        LOGGER.error("catch exception:{}",exception.getMessage());

        // 如果ImmutableMap是第一次使用
        // 通过ImmutableMap.Builder初始化并建造。
        if(EXCEPTIONS == null) {
            EXCEPTIONS = builder.build();
        }

        // 判断当前异常是否存在于ImmutableMap
        // 如果存在取出异常信息返回。
        if (EXCEPTIONS.containsKey(exception.getClass())) {
            // 存在将当前信息响应到页面。
            return new ResponseResult(EXCEPTIONS.get(exception.getClass()));
        }

        // 不存在,抛出一个通用的信息。系统繁忙。。。
        return new ResponseResult(CommonCode.SERVER_ERROR);
    }
}
