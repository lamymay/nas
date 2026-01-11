package com.arc.nas.config.mybatis.type.handler;

import com.arc.nas.model.enums.system.GenderEnum;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis结果集类型转换为java类型,用于性别转换
 *
 * @since 2020/9/8 9:43 上午
 */
@MappedTypes(GenderEnum.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class GenderEnumTypeHandler implements TypeHandler<GenderEnum> {

    @Override
    public void setParameter(PreparedStatement ps, int i, GenderEnum parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getKey());
    }

    @Override
    public GenderEnum getResult(ResultSet rs, String columnName) throws SQLException {
        int id = rs.getInt(columnName);
        return GenderEnum.getDisplayByKey(id);
    }

    @Override
    public GenderEnum getResult(ResultSet rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        return GenderEnum.getDisplayByKey(id);
    }

    @Override
    public GenderEnum getResult(CallableStatement rs, int columnIndex) throws SQLException {
        int id = rs.getInt(columnIndex);
        return GenderEnum.getDisplayByKey(id);
    }
}
