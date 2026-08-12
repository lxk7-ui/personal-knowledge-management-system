package com.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count FROM note " +
            "WHERE create_time >= #{startDate} GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDay(@Param("startDate") LocalDateTime startDate);
}
