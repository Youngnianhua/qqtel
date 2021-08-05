package com.nianhua.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QqTel implements Serializable {
    /**
     * qq
     */
    private Long qq;

    /**
     * tel
     */
    private String tel;

    private static final long serialVersionUID = 1L;
}