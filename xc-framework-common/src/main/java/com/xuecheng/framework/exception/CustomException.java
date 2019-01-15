package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * 自定义异常类
 * 用来作为可以预知的异常类。
 * 创建需要错误信息，因为出错不需要其它信息只需要ResultCode.
 */
public class CustomException extends RuntimeException {
	private static final long serialVersionUID = 1108988181402026068L;

	private ResultCode resultCode;

	public CustomException(ResultCode resultCode) {
		// 异常信息为错误代码+异常信息
		// 传给父类构造，可以通过getMessage()获取对应信息。
		super("错误代码："+resultCode.code()+"错误信息："+resultCode.message());
		this.resultCode = resultCode;
	}

	/**
	 * 获取当前异常的错误信息
	 *   //操作是否成功,true为成功，false操作失败
	 *     boolean success();
	 *     //操作代码
	 *     int code();
	 *     //提示信息
	 *     String message();
	 * @return
	 */
	public ResultCode getResultCode(){
		return this.resultCode;
	}
}
