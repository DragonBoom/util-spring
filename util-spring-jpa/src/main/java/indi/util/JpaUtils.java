package indi.util;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Table;

import indi.annotation.ToSql;
import indi.data.Three;

public class JpaUtils {

    /**
     * 对于有给定的实体，对所有有  {@link indi.annotation.ToSql} 注解的属性，生成插入字段的SQL语句。
     * @throws IntrospectionException 
     */
    public static final String generateSQL(Class<?> beanClass) {
        List<Three> threes = new LinkedList<>();
        
        Table table = beanClass.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalArgumentException("实体类必须有Table注解! " + beanClass);
        }
        String tableName = table.name();
        
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            ToSql toSql = field.getAnnotation(ToSql.class);
            if (toSql != null) {
                SpringUtils.formatAnnotation(toSql);
                String comment = toSql.comment();
                String name = field.getName();
                Class<?> type = field.getType();
                Three three = Three.of(name, type, comment);
                threes.add(three);
            }
            continue;
        }
        return buildSql(threes, tableName);
    }
    
    /**
     * TODO: add comment
     * 
     * @param threes
     * @param tableName
     * @return
     */
    private static String buildSql(List<Three> threes, String tableName) {
        if (threes == null || threes.size() == 0) {
            return "";
        }
        StringBuilder insertBuilder = new StringBuilder();
        StringBuilder commentsBuilder = new StringBuilder();
        
        threes.forEach(three -> {
            // append insert sql
            String name = (String) three.getFirst();
            Class<?> type = (Class<?>) three.getSecond();
            insertBuilder.append("alter table ").append(tableName).append(" add ").append(name).append(" ")
                .append(getSqlType(type)).append(" ;").append("\n");
            // append add comment sql
            String comment = (String) three.getThird();
            commentsBuilder.append("comment on column ").append(tableName).append(".").append(name).append(" is ")
                .append("'").append(comment).append("'").append(" ;").append("\n");
        });
        
        return insertBuilder.append(commentsBuilder).toString();
    }
    
    /**
     * TODO: comment
     * 
     * @param typeClass
     * @return
     */
    private static String getSqlType(Class<?> typeClass) {
        if (typeClass.equals(String.class)) {
            return "varchar2(80a)";
        } else if (typeClass.equals(Integer.class)) {
            return "number(11a)";
        } else if (typeClass.equals(Long.class)) {
            return "number(11a)";
        } else if (typeClass.equals(Date.class)) {
            return "date(a)";
        }
        return null;
    }
}
