package com.arc.nas.config.mybatis.type.handler;

/**
 * @since 2021/1/12 4:52 下午
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Jackson 实现 JSON 字段类型处理器
 */

@MappedJdbcTypes(JdbcType.VARCHAR)
public class JacksonTypeHandler<T extends Object> extends BaseTypeHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(JacksonTypeHandler.class);

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final Class<T> type;

    public JacksonTypeHandler(Class<T> type) {
        if (log.isTraceEnabled()) {
            log.trace("JacksonTypeHandler(" + type + ")");
        }
        if (null == type) {
            throw new PersistenceException("Type argument cannot be null");
        }
        this.type = type;
    }

    private T parse(String json) {
        try {
            if (json == null || json.length() == 0) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJsonString(T obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, T parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(columnIndex, toJsonString(parameter));
    }
}


