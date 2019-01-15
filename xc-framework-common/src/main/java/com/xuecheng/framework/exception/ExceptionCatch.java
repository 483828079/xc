package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    /**
     * 捕获 CustomException异常
     * 定义异常类型，如果@requestMapping标注的方法抛出对应异常会对该方法进行增强。
     * @param e 捕获异常从参数传入
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
}
