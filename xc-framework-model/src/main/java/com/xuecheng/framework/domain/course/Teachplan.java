package com.xuecheng.framework.domain.course;

import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 课程计划为树型结构，由树根（课程）和树枝（章节）组成，
 * 为了保证系统的可扩展性，在系统设计时将课程计划设置为树型结构。
 * 一共分为三级 grand分类。 1 为根节点
 * parentId记录上级信息，parentId为0也就是没有上级节点等同于grand为1.
 */
@Data
@ToString
@Entity
@Table(name="teachplan")
/*uuid [ 32位16进制数的字符串 ]*/
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
public class Teachplan implements Serializable {
    private static final long serialVersionUID = -916357110051689485L;
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(length = 32)
    private String id;
    private String pname;
    private String parentid;
    private String grade; // 层级 1层 课程名称 2层 大章节 3层 章节下的目录。
    private String ptype;
    private String description;
    private String courseid;
    private String status;
    private Integer orderby;
    private Double timelength;
    private String trylearn;

}
