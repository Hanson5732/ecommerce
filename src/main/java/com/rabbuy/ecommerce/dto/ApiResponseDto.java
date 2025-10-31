package com.rabbuy.ecommerce.dto;

/**
 * 这是一个泛型类，用于封装所有的 API 响应。
 *
 */
public class ApiResponseDto<T> {

    private int code; // 1 = 成功, 0 = 失败
    private String msg;
    private T data;

    // 私有构造函数
    private ApiResponseDto(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // --- 静态工厂方法 ---

    /**
     * 对应 Result.success()
     * @return {"code": 1, "msg": null, "data": null}
     */
    public static <T> ApiResponseDto<T> success() {
        return new ApiResponseDto<>(1, null, null);
    }

    /**
     * 对应 Result.success_with_data(data)
     * @return {"code": 1, "msg": null, "data": ...}
     */
    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>(1, null, data);
    }

    /**
     * 对应 Result.error(msg)
     * @return {"code": 0, "msg": "...", "data": null}
     */
    public static <T> ApiResponseDto<T> error(String msg) {
        return new ApiResponseDto<>(0, msg, null);
    }

    // --- Getters (必须有，以便 JAX-RS 序列化) ---
    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public T getData() {
        return data;
    }
}