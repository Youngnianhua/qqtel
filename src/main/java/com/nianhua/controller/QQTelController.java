package com.nianhua.controller;

import com.alibaba.fastjson.JSONObject;
import com.nianhua.entity.ResultVo;
import com.nianhua.model.QqTel;
import com.nianhua.service.QQTelService;
import org.apache.ibatis.exceptions.PersistenceException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Controller
public class QQTelController {
    private boolean flag = true;
    private LocalTime oldTime = LocalTime.now();
    private LocalTime newTime;
    private Long num = 0L;

    @Autowired
    QQTelService qqTelService;

    @ResponseBody
    @RequestMapping("/get")
    public ResultVo get() {
        return new ResultVo(true, 200, "查询成功", qqTelService.getEndQQ());
    }

    /**
     * size: 每一分段的大小，可不填，不同则默认为一百万
     * startNum: 开始QQ，可不填，不填则为数据库中最大的QQ
     * endNum: 爬取的最后一个QQ，可不填，不填则默认为10位数最大QQ(若自己填写则要大于startNum)
     * thread: 线程池数量，默认50线程
     **/
    @ResponseBody
    @RequestMapping("/run")
    public ResultVo runGet(@RequestParam(value = "size", defaultValue = "1000000") Long size,
                           @RequestParam(value = "startNum", required = false) Long startNum,
                           @RequestParam(value = "endNum", defaultValue = "9999999999") Long endNum,
                           @RequestParam(value = "thread", defaultValue = "50") int thread) {
        //创建一个定长的线程池
        ExecutorService newExecutorService = Executors.newFixedThreadPool(thread);
        //判断是否输入了起始值QQ
        if (startNum == null) {
            //从数据库中查找最大的QQ
            QqTel endQQ = qqTelService.getEndQQ();
            if (endQQ != null) {
                startNum = endQQ.getQq();
                //第一次往前推1000个号码，以免遗漏
                if (flag) {
                    startNum -= 1000;
                    flag = false;
                }
            } else {
                startNum = 10000L;
            }
        }
        //判断是否超过了目标段
        if (startNum + size > endNum) {
            size = endNum - startNum;
        }
        List<QqTel> lists = new ArrayList<>();
        for (Long i = 1L; i <= size; i++) {
            final Long finalI = i;
            final Long finalSize = size;
            final Long finalStartNum = startNum;
            //往线程池里丢线程
            newExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    runapp();
                    //判断此目标段是否结束
                    synchronized (finalI) {
                        if (finalI.longValue() == finalSize.longValue()) {
                            System.out.println("我准备销毁线程池了");
                            newExecutorService.shutdown();
                            try {
                                newExecutorService.awaitTermination(10, TimeUnit.SECONDS);
                            } catch (Exception e) {
                                System.out.println("线程结束10s超时");
                            }
                            if ((finalI + finalStartNum) < endNum) {
                                System.out.println("我准备进入下个目标段了");
                                runGet(finalSize, finalStartNum + finalSize, endNum, thread);
                            }
                        }
                    }
                }

                private void runapp() {
                    JSONObject data;
                    try {
                        Connection connect = Jsoup.connect("https://api.blogs.ink/api/qqmob/?qq=" + (finalStartNum + finalI));
//                        Map head = new HashMap();
//                        head.put(":authority", "chaqbang.com");
//                        head.put(":path", "/");
//                        head.put(":scheme", "https");
//                        head.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//                        head.put("accept-encoding", "gzip, deflate, br");
//                        head.put("accept-language", "zh-CN,zh;q=0.9");
//                        head.put("cache-control", "max-age=0");
//                        head.put("content-length", "12");
//                        head.put("content-type", "application/x-www-form-urlencoded");
//                        head.put("origin", "https://chaqbang.com");
//                        head.put("referer", "https://chaqbang.com/");
//                        head.put("sec-ch-ua", "\"Google Chrome\";v=\"95\", \"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"");
//                        head.put("sec-ch-ua-mobile", "?0");
//                        head.put("sec-ch-ua-platform", "\"Windows\"");
//                        head.put("sec-fetch-dest", "document");
//                        head.put("sec-fetch-mode", "navigate");
//                        head.put("sec-fetch-site", "same-origin");
//                        head.put("sec-fetch-user", "?1");
//                        head.put("upgrade-insecure-requests", "1");
//                        head.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36");
//                        connect.headers(head);
//                        connect.data("number","10001");
                        Document document = connect.ignoreContentType(true).get();
                        data = JSONObject.parseObject(document.getElementsByTag("body").html());
                    } catch (IOException e) {
                        runapp();
                        return;
                    }
                    if (data == null) {
                        runapp();
                        return;
                    } else {
                        synchronized (lists) {
                            if (data.getInteger("code") == 200) {
                                QqTel qqTel = new QqTel(Long.parseLong(data.getJSONObject("data").getString("qq")), data.getJSONObject("data").getString("mobile"));
                                lists.add(qqTel);
                            } else if (data.getInteger("code") == 202) {
                            } else {
                                runapp();
                                return;
                            }
                            if ((lists.size() % 100 == 0 && lists.size() != 0) || (finalI.longValue() == finalSize.longValue())) {
                                newTime = LocalTime.now();
                                Duration duration = Duration.between(oldTime, newTime);
                                System.out.println("当前QQ:" + (finalStartNum + finalI) + " | 当前段目标QQ:" + (finalStartNum + finalSize) + " | 最终目标QQ:" + endNum);
                                System.out.println("当前时间:" + new Timestamp(System.currentTimeMillis()) + " | 耗时:" + (duration.toMillis() / 1000.000) + "秒 | 查询了" + (finalI - num) + "个QQ");
                                oldTime = newTime;
                                num = finalI;
                                try {
                                    qqTelService.addList(lists);
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "无重复数据，调用批量添加,成功批量添加" + lists.size() + "条数据");
                                    lists.clear();
                                } catch (DuplicateKeyException e) {
                                    int i = 0;
                                    for (QqTel list : lists) {
                                        try {
                                            i += qqTelService.add(list);
                                        } catch (DuplicateKeyException dke) {
                                        } catch (PersistenceException pe) {
                                            i += qqTelService.add(list);
                                        }
                                    }
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "有重复数据，调用单条添加,单条添加成功" + i + "条记录");
                                    lists.clear();
                                } catch (PersistenceException e) {
                                    qqTelService.addList(lists);
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "无重复数据，调用批量添加,成功批量添加" + lists.size() + "条数据");
                                    lists.clear();
                                }
                                System.out.println("------------------------------------------------------------");
                            }
                        }
                    }
                }
            });
        }
        return new ResultVo(true, 200, "成功提交到后台");
    }
}
