package com.nianhua.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口返回类
 *
 * @By: nianhua
 */
@Data
public class ResultVo<T> implements Serializable {
    private boolean flag;
    private Integer code;
    private String message;
    private T data;

    public ResultVo(boolean flag, Integer code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

    public ResultVo(boolean flag, Integer code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    public ResultVo() {
        this.flag = true;
        this.code = 200;
        this.message = "操作成功!";
    }
}
