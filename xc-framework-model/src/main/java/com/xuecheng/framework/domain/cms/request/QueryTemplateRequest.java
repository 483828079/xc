package com.xuecheng.framework.domain.cms.request;

import com.xuecheng.framework.model.request.RequestData;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryTemplateRequest extends RequestData {
	//站点id
	@ApiModelProperty("站点id")
	private String siteId;
	//模板名称
	@ApiModelProperty("模板名称")
	private String templateName;
	//模板参数
	@ApiModelProperty("模板参数")
	private String templateParameter;
}
