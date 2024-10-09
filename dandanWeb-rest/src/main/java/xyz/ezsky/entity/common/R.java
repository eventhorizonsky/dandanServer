package xyz.ezsky.entity.common;


import lombok.Data;


import java.util.HashMap;

import java.util.Map;


@Data
public class R {


    private Boolean success;


    private Integer code;


    private String message;


    /**
     * 接口请求时间戳
     */

    private Long timestamp;


    private Object data ;


    private R setSuccess(Boolean success) {

        this.success = success;

        return this;

    }


    private R setMessage(String message) {

        this.message = message;

        return this;

    }


    private R setData(Map<String, Object> data) {

        this.data = data;

        return this;

    }


    private R setCode(Integer code) {

        this.code = code;

        return this;

    }


    private R() {

    }


    private R(Long timestamp) {

        this.timestamp = timestamp;

    }


    /**
     * 通用返回成功
     *
     * @return
     */

    public static R success() {

        return new R(System.currentTimeMillis())

                .setSuccess(ResultCodeEnum.SUCCESS.getSuccess())

                .setCode(ResultCodeEnum.SUCCESS.getCode())

                .setMessage(ResultCodeEnum.SUCCESS.getMessage());


    }


    /**
     * 通用返回失败
     *
     * @return
     */

    public static R failure() {

        return new R(System.currentTimeMillis())

                .setSuccess(ResultCodeEnum.FAIL.getSuccess())

                .setCode(ResultCodeEnum.FAIL.getCode())

                .setMessage(ResultCodeEnum.FAIL.getMessage());


    }


    /**
     * 设置结果，形参为结果枚举
     *
     * @param result
     * @return
     */

    public static R setResult(ResultCodeEnum result) {

        return new R(System.currentTimeMillis())

                .setSuccess(result.getSuccess())

                .setCode(result.getCode())

                .setMessage(result.getMessage());


    }


    // 自定义返回数据

    public R data(Map<String, Object> map) {

        return this.setData(map);


    }


    // 通用设置data

    public R data(Object value) {

        this.data=value;

        return this;

    }


    // 自定义状态信息

    public R message(String message) {

        return this.setMessage(message);


    }


    // 自定义状态码

    public R code(Integer code) {

        return this.setCode(code);


    }


    // 自定义返回结果

    public R success(Boolean success) {

        return this.setSuccess(success);


    }


}


