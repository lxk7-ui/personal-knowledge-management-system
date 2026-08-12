package com.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.knowledge.entity.NoteTag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {

    @Insert("<script>INSERT INTO note_tag (note_id, tag_id) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.noteId}, #{item.tagId})" +
            "</foreach></script>")
    void batchInsert(@Param("list") List<NoteTag> list);
}
