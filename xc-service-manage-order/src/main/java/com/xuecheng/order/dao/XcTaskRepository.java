package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask, String> {
    /**
     * 分页查询，条件是在updateTime之前
     * @param pageable
     * @param updateTime
     * @return
     */
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);


    /**
     * 更新任务处理时间
     * @Modifying 因为SpringDataJpa中已经自带了事物，而自己写的JPQL的方法需要自己加上事物。
     * jpql中可以使用?作为占位符，默认按照参数顺序和占位符顺序进行匹配。
     * 可以在?后面写上数字指定该占位符对应的参数索引(从1开始)。
     * 这里使用的是占位符名称前面加上:表明该位置是个占位符。
     * @Param 注解指定占位符名称对应的参数。
     * @param id
     * @param updateTime
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime  where t.id = :id ")
    public int updateTaskTime(@Param(value = "id") String id, @Param(value = "updateTime")Date updateTime);


    /**
     * 将消息id对应的消息行的version+1
     * @param id
     * @param version
     * @return
     */
    @Modifying
    @Query("update XcTask t set t.version = :version+1  where t.id = :id and t.version = :version")
    public int updateTaskVersion(@Param(value = "id") String id,@Param(value = "version") int version);
}