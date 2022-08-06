package com.padingpading.interview.advance.spi.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * jdbc链接
 */
public class JdbcConnetion {
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //使用调用者的的classLoader加载Driver
        Connection connection = DriverManager
                .getConnection("jdbc:mysql://rm-uf63ey8y71gkx24r8no.mysql.rds.aliyuncs.com", "zeus", "Zeus123123");
        System.out.println(connection);
    }
    
}
