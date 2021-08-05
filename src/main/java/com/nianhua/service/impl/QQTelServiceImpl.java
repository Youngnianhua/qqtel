package com.nianhua.service.impl;

import com.nianhua.dao.QqTelDAO;
import com.nianhua.model.QqTel;
import com.nianhua.service.QQTelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QQTelServiceImpl implements QQTelService {
    @Autowired
    QqTelDAO qqTelDAO;

    @Override
    public QqTel getEndQQ() {
        return qqTelDAO.getEndQQ();
    }

    @Override
    public int add(QqTel qqTel) {
        return qqTelDAO.add(qqTel);
    }

    @Override
    public int addList(List<QqTel> lists) {
        return qqTelDAO.addList(lists);
    }
}
