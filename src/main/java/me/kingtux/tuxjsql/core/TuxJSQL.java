package me.kingtux.tuxjsql.core;


import me.kingtux.tuxjsql.basic.sql.BasicDataTypes;
import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.connection.ConnectionProvider;
import me.kingtux.tuxjsql.core.logger.BasicLogger;
import me.kingtux.tuxjsql.core.logger.NoLogger;
import me.kingtux.tuxjsql.core.sql.SQLDataType;
import me.kingtux.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxjsql.core.sql.select.JoinStatement;
import me.kingtux.tuxjsql.core.sql.select.SelectStatement;
import me.kingtux.tuxjsql.core.sql.where.SubWhereStatement;
import me.kingtux.tuxjsql.core.sql.where.WhereStatement;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLogger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * TuxJSQL core class.
 *
 * @author KingTux
 */
public final class TuxJSQL {
    private static Logger logger = loadLogger();
    private ConnectionProvider provider;
    private SQLBuilder builder;
    private ExecutorService executor;
    private List<SQLTable> tableCollection = new ArrayList<>();
    private ClassLoader internalClassLoader;

    TuxJSQL(ConnectionProvider provider, SQLBuilder builder, ExecutorService executor) {
        this.internalClassLoader = builder.getClass().getClassLoader();
        if (logger.isInfoEnabled())
            getLogger().info(String.format("TuxJSQL is using %s For its Connections!", provider.name()));
        this.provider = provider;
        this.builder = builder;
        this.executor = executor;
        this.builder.setTuxJSQL(this);
        Runtime.getRuntime().addShutdownHook(new Thread(provider::close));
    }

    /**
     * Changes the executor.
     * Warning this will end all current tasks.
     *
     * @param executor The new executor
     */
    public void setExecutor(ExecutorService executor) {
        Validate.notNull(executor, "The executor Service cant be null.");
        Validate.isTrue(!executor.isShutdown(), "The executor must be usable");
        TuxJSQL.logger.info("Shutting down executor and setting a new one");
        this.executor.shutdownNow();
        this.executor = executor;
    }

    /**
     * <b>REMEMBER to do Connection#close() to return it!</b>
     *
     * @return a connection!
     */
    public Connection getConnection() {
        return provider.getConnection();
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Change the logger
     *
     * @param logger The new logger you want if null it will use NoLogger
     */
    public static void setLogger(Logger logger) {
        if (logger == null) TuxJSQL.logger = new NoLogger();
        TuxJSQL.logger = logger;
    }


    public void addTable(SQLTable table) {
        tableCollection.add(table);
    }

    /**
     * Gets a table from the list
     *
     * @param name the table name
     * @return the table Optional
     */
    public Optional<SQLTable> getTable(String name) {
        return tableCollection.stream().filter(t -> t.getName().equals(name)).findFirst();
    }


    public TableBuilder createTable() {
        return builder.createTable();
    }

    public ColumnBuilder createColumn() {
        return builder.createColumn();
    }

    public WhereStatement createWhere() {
        return builder.createWhere();
    }

    public SubWhereStatement createSubWhereStatement() {
        return builder.createSubWhereStatement();
    }

    public <T> WhereStatement<T> createWhere(T t) {
        return builder.createWhere(t);
    }

    public <T> SubWhereStatement<T> createSubWhereStatement(T t) {
        return builder.createSubWhereStatement(t);
    }

    public SelectStatement createSelectStatement() {
        return builder.createSelectStatement();
    }

    public JoinStatement createJoinStatement(SelectStatement basicSelectStatement) {
        return builder.createJoinStatement(basicSelectStatement);
    }

    public SQLDataType convertDataType(BasicDataTypes dataType) {
        return builder.convertDataType(dataType);
    }

    public ConnectionProvider getProvider() {
        return provider;
    }

    public SQLBuilder getBuilder() {
        return builder;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public ClassLoader getInternalClassLoader() {
        return internalClassLoader;
    }

    private static Logger loadLogger() {
        Logger logger = LoggerFactory.getLogger(TuxJSQL.class);
        if (logger instanceof NOPLogger) {
            logger = new BasicLogger("TuxJSQL");
        }
        return logger;
    }

}
