package me.kingtux.tuxjsql.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.statements.SelectStatement;
import me.kingtux.tuxjsql.core.statements.SubWhereStatement;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import org.apache.commons.dbcp.BasicDataSource;

import java.io.File;
import java.util.Properties;

@SuppressWarnings("Duplicates")
public class SQLITEBuilder implements SQLBuilder {
    private BasicDataSource basicDataSource;
    @Override
    public TableBuilder createTable() {
        return new SQLITETableBuilder(this);
    }

    @Override
    public WhereStatement createWhere() {
        return new SQLiteWhereStatement();
    }

    @Override
    public SubWhereStatement createSubWhere() {
        return new SQLITESubWhere();
    }

    @Override
    public ColumnBuilder createColumn() {
        return new SQLiteColumnBuilder();
    }


    @Override
    public void createConnection(Properties properties) {
        basicDataSource = new BasicDataSource();
        basicDataSource.setUrl("jdbc:sqlite:" + new File(properties.getProperty("db.file")).getAbsolutePath());
        basicDataSource.setDriverClassName("org.sqlite.JDBC");
        basicDataSource.setInitialSize(Integer.parseInt(properties.getProperty("db.poolsize", "5")));
    }

    @Override
    public SelectStatement createSelectStatement() {
        return new SQLITESelectStatement();
    }

    @Override
    public BasicDataSource getDataSource() {
        return basicDataSource;
    }

    @Override
    public void setDataSource(BasicDataSource bds) {
        basicDataSource = bds;
    }


}
