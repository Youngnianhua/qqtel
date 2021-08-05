package com.nianhua.dao;

import com.nianhua.model.QqTel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface QqTelDAO {
    QqTel getEndQQ();
    int add(QqTel qqTel);
    int addList(@Param("lists")List<QqTel> lists);
}