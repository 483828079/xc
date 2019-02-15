package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.Teachplan;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 使用该拓展类查询，记录根节点所有子节点。
 */
@Data
@ToString
public class TeachplanNode extends Teachplan {
    List<TeachplanNode> children;

    // 媒资信息
    private String mediaId;
    private String mediaFileOriginalName;
}
