package com.zxwl.frame.bean;

public class WebSocketResult
{
	/*离线消息*/
	public static final String OFFLINE_MESSAGE = "offlineMessage";
	/*连接失败*/
	public static final String CONNECTION_FAIL = "connectionFail";
	/*处理消息失败*/
	public static final String PROCESS_MESSAGES_FAIL = "ProcessMessagesFail";
	/*常规消息*/
	public static final String MESSAGE = "message";

	/**
	 * 状态码
	 * -1：失败
	 * 0：成功
	 * */
	private int code;

	/** 说明 */
	private String message;

	/** 事件
	 * offlineMessage:离线消息
	 * */
	private String event;

	/*对应数据*/
	private Object data;



	public static WebSocketResult error(String event, Object data)
	{
		return new WebSocketResult(-1, "失败", event, data);
	}

	public static WebSocketResult success(String event, Object data)
	{
		return new WebSocketResult(0, "成功", event, data);
	}

	public WebSocketResult(int code, String message, String event, Object data)
	{
		this.code = code;
		this.message = message;
		this.event = event;
		this.data = data;
	}

	// =======================
	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getEvent()
	{
		return event;
	}

	public void setEvent(String event)
	{
		this.event = event;
	}

	public Object getData()
	{
		return data;
	}

	public void setData(Object data)
	{
		this.data = data;
	}
}
