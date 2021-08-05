package com.nianhua.service;

import com.nianhua.model.QqTel;

import java.util.List;

public interface QQTelService {
    QqTel getEndQQ();
    int add(QqTel qqTel);
    int addList(List<QqTel> lists);
}
