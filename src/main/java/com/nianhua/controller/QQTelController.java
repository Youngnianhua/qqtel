package com.nianhua.controller;

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

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.nianhua.util.StringUtil.subString;

@Controller
public class QQTelController {
    private boolean flag = true;

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
     **/
    @ResponseBody
    @RequestMapping("/run")
    public ResultVo runGet(@RequestParam(value = "size", defaultValue = "1000000") Long size,
                           @RequestParam(value = "startNum", required = false) Long startNum,
                           @RequestParam(value = "endNum", defaultValue = "9999999999") Long endNum) {
        //创建一个定长的线程池
        ExecutorService newExecutorService = Executors.newFixedThreadPool(300);
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
                                runGet(finalSize, finalStartNum + finalSize, endNum);
                            }
                        }
                    }
                }

                private void runapp() {
                    String data;
                    try {
                        Connection connect = Jsoup.connect("http://sgk.xyz/qbtxt-api.php?qq=" + (finalStartNum + finalI));
//                        Map head = new HashMap();
//                        head.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//                        head.put("Accept-Encoding","gzip, deflate");
//                        head.put("Accept-Language","zh-CN,zh;q=0.9");
//                        head.put("Cache-Control","max-age=0");
//                        head.put("Connection","keep-alive");
//                        head.put("Cookie","__51vcke__JFGdTuLLGeB1FLjg=3faae609-8749-5426-b5e2-84779ee9d134; __51vuft__JFGdTuLLGeB1FLjg=1625620818835; _aihecong_chat_address=%7B%22city%22%3A%22%E6%B7%B1%E5%9C%B3%22%2C%22region%22%3A%22%E5%B9%BF%E4%B8%9C%22%2C%22country%22%3A%22%E4%B8%AD%E5%9B%BD%22%7D; __51uvsct__JFGdTuLLGeB1FLjg=4; _aihecong_chat_visitorCookie=%7B%22visitormark%22%3A%7B%22addtime%22%3A%222021-07-08T07%3A06%3A34.754Z%22%2C%22address%22%3A%7B%22city%22%3A%22%E6%B7%B1%E5%9C%B3%22%2C%22region%22%3A%22%E5%B9%BF%E4%B8%9C%22%2C%22country%22%3A%22%E4%B8%AD%E5%9B%BD%22%7D%2C%22device%22%3A%7B%22ip%22%3A%22113.90.30.70%22%2C%22height%22%3A%221080%22%2C%22width%22%3A%221920%22%2C%22system%22%3A%22Win10%22%2C%22browser%22%3A%22Chrome%2090.0.4430.212%22%2C%22type%22%3A%22Desktop%22%7D%2C%22utm%22%3A%7B%7D%2C%22mark%22%3A%7B%22sourceType%22%3A%22externallinks%22%2C%22entranceTitle%22%3A%22Q%E7%BB%91%E5%9C%A8%E7%BA%BF%E6%9F%A5%E8%AF%A2%22%2C%22entranceUrl%22%3A%22http%3A%2F%2Fsgk.xyz%2F%22%2C%22source%22%3A%22http%3A%2F%2F51.255.92.24%2F%22%7D%2C%22stays%22%3A%7B%7D%2C%22curFrequency%22%3A4%2C%22pageDepth%22%3A0%2C%22stayDuration%22%3A0%2C%22_id%22%3A%2260e6a3fa5e196b5b32e5ee1d%22%2C%22lasttime%22%3A%222021-07-08T06%3A50%3A39.218Z%22%2C%22channelId%22%3A%223Jho1R%22%2C%22numberId%22%3A18382%2C%22visitorId%22%3A%2260e557855e196b5b32cc1c25%22%2C%22__v%22%3A0%7D%2C%22last%22%3A%7B%22time%22%3A1625727995871%2C%22source%22%3A%22DirectEntry%22%2C%22entranceUrl%22%3A%22http%3A%2F%2Fsgk.xyz%2F%22%2C%22entranceTitle%22%3A%22Q%E7%BB%91%E5%9C%A8%E7%BA%BF%E6%9F%A5%E8%AF%A2%22%2C%22keyWord%22%3Anull%7D%2C%22visitormarkId%22%3A%2260e6a3fa5e196b5b32e5ee1d%22%2C%22visitorId%22%3A%2260e557855e196b5b32cc1c25%22%2C%22lastTime%22%3A1625728431957%7D");
//                        head.put("Host","sgk.xyz");
//                        head.put("Upgrade-Insecure-Requests","1");
//                        head.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36");
//                        connect.headers(head);
                        Document document = connect.get();
                        data = document.getElementsByTag("body").html();
                    } catch (IOException e) {
                        e.printStackTrace();
                        runapp();
                        return;
                    }
                    if (data == null) {
                        runapp();
                        return;
                    } else {
                        synchronized (lists) {
                            if ("QQ".equals(data.substring(0, 2))) {
                                QqTel qqTel = new QqTel(Long.parseLong(subString(data, "QQ:", " mobile:")), subString(data, "mobile:", " province"));
                                lists.add(qqTel);
                            } else if ("库中并没有这个记录".equals(data)) {
                            } else {
                                runapp();
                                return;
                            }
                            if ((lists.size() % 500 == 0 && lists.size() != 0) || (finalI.longValue() == finalSize.longValue())) {
                                System.out.println("当前QQ:" + (finalStartNum + finalI) + " | 当前段目标QQ:" + (finalStartNum + finalSize) + " | 最终目标QQ:" + endNum);
                                try {
                                    qqTelService.addList(lists);
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "成功批量添加" + lists.size() + "条数据");
                                    lists.clear();
                                } catch (DuplicateKeyException e) {
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "有重复数据，调用单条添加");
                                    int i = 0;
                                    for (QqTel list : lists) {
                                        try {
                                            i += qqTelService.add(list);
                                        } catch (DuplicateKeyException dke) {
                                        } catch (PersistenceException pe) {
                                            i += qqTelService.add(list);
                                        }
                                    }
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "单条添加成功" + i + "条记录");
                                    lists.clear();
                                } catch (PersistenceException e) {
                                    qqTelService.addList(lists);
                                    System.out.println("[" + Thread.currentThread().getName() + "]" + "成功批量添加" + lists.size() + "条数据");
                                    lists.clear();
                                }
                            }
                        }
                    }
                }
            });
        }
        return new ResultVo(true, 200, "成功提交到后台");
    }
}
