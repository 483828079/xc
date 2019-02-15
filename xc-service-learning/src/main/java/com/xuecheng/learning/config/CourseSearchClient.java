package com.xuecheng.learning.config;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "xc-service-search")
public interface CourseSearchClient {
	@GetMapping(value="/getmedia/{teachPlanId}")
	public TeachplanMediaPub getMedia(@PathVariable("teachPlanId") String teachPlanId);
}
