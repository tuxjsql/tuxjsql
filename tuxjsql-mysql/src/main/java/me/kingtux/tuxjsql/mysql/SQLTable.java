package me.kingtux.tuxjsql.mysql;import me.kingtux.tuxjsql.core.Column;import me.kingtux.tuxjsql.core.Query;import me.kingtux.tuxjsql.core.Table;import me.kingtux.tuxjsql.core.TuxJSQL;import me.kingtux.tuxjsql.core.result.DBResult;import me.kingtux.tuxjsql.core.result.DBRow;import me.kingtux.tuxjsql.core.statements.SelectStatement;import me.kingtux.tuxjsql.core.statements.WhereStatement;import java.sql.Connection;import java.sql.PreparedStatement;import java.sql.ResultSet;import java.sql.SQLException;import java.util.ArrayList;import java.util.List;import static me.kingtux.tuxjsql.core.TuxJSQL.Utils.resultSetToResultRow;@SuppressWarnings("Duplicates")public class SQLTable extends Table {    private String name;    private List<Column> columns;    private MySQLBuilder builder;    SQLTable(String name, List<Column> columns, MySQLBuilder builder) {        this.builder =builder;        if (getConnection() == null) {            try {                throw new IllegalAccessException("You cannot create a table with setting up a connection!");            } catch (IllegalAccessException e) {                e.printStackTrace();                return;            }        }        this.name = name;        this.columns = new ArrayList<>();        for (Column column : columns) {            SQLColumn sc = (SQLColumn) column;            sc.setTable(this);            this.columns.add(sc);        }    }    public Connection getConnection(){        try {            return builder.getDataSource().getConnection();        } catch (SQLException e) {            e.printStackTrace();        }        return null;    }    @Override    public List<Column> getColumns() {        return columns;    }    @Override    public void update(WhereStatement whereStatement, List<Column> columns, Object... values) {        StringBuilder columsToUpdate = new StringBuilder();        for (Column column : columns) {            if (!columsToUpdate.toString().isEmpty()) {                columsToUpdate.append(",");            }            columsToUpdate.append(column.getName() + "=?");        }        String query = String.format(SQLQuery.UPDATE.getQuery(), name, columsToUpdate, whereStatement.build().getQuery());        getLogger().debug(query);        try {            Connection connection = getConnection();            PreparedStatement preparedStatement = connection.prepareStatement(query);            int fin = 0;            for (int i = 0; i < values.length; i++) {                preparedStatement.setObject(i + 1, values[i]);                fin = i;            }            Object[] valu = whereStatement.values();            for (int i = 0; i < valu.length; i++) {                fin++;                preparedStatement.setObject(fin + 1, valu[i]);            }            preparedStatement.execute();            preparedStatement.close();            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }    }    @Override    public long max(Column c) {        long i = 0;        Connection connection = getConnection();        try (ResultSet resultSet = connection.createStatement().executeQuery(String.format(SQLQuery.MAX.getQuery(), c.getName(), name))) {            resultSet.next();            i = resultSet.getLong(1);            resultSet.close();            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }        return i;    }    @Override    public long min(Column c) {        long i = 0;        Connection connection = getConnection();        try (ResultSet resultSet = connection.createStatement().executeQuery(String.format(SQLQuery.MIN.getQuery(), c.getName(), name))) {            resultSet.next();            i = resultSet.getLong(1);            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }        return i;    }    @Override    public Table createIfNotExists() {        StringBuilder builder = new StringBuilder();        for (Column column : columns) {            if (!builder.toString().isEmpty()) {                builder.append(", ");            }            builder.append(column.build());        }        String query = String.format(SQLQuery.TABLE.getQuery(), name, builder.toString());        executeSimpleStatement(query);        return this;    }    @Override    public void insert(List<Column> columns, Object... values) {        StringBuilder columnsToInsert = new StringBuilder();        StringBuilder question = new StringBuilder();        for (Column column : columns) {            if (!columnsToInsert.toString().isEmpty()) {                columnsToInsert.append(",");                question.append(",");            }            columnsToInsert.append(column.getName());            question.append("?");        }        String query = String.format(SQLQuery.INSERT.getQuery(), name, columnsToInsert.toString(), question.toString());        getLogger().debug(query);        Connection connection = getConnection();        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {            for (int i = 0; i < values.length; i++) {                preparedStatement.setObject(i + 1, values[i]);            }            preparedStatement.execute();            preparedStatement.close();            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }    }    @Override    public DBResult select(SelectStatement statement) {        Query sqlQuery = statement.build(this);        ResultSet resultSet = null;        List<DBRow> rows = null;        try {            getLogger().debug(sqlQuery.getQuery());            getLogger().debug(sqlQuery.getValuesAsString());            Connection connection = getConnection();            PreparedStatement preparedStatement = getConnection().prepareStatement(sqlQuery.getQuery());            if (sqlQuery.getValues() != null && sqlQuery.getValues().length > 0) {                for (int i = 0; i < sqlQuery.getValues().length; i++) {                    preparedStatement.setObject(i + 1, sqlQuery.getValues()[i]);                }            }            resultSet = preparedStatement.executeQuery();            rows = resultSetToResultRow(resultSet, statement.getColumns().size());            preparedStatement.close();            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }        return new DBResult(rows.size(), statement.getColumns().size(), this, rows);    }    @Override    public void delete(WhereStatement whereStatement) {        String query = String.format(SQLQuery.DELETE.getQuery(), name, whereStatement.build().getQuery());        getLogger().debug(query);        getLogger().debug(whereStatement.build().getValuesAsString());        Connection connection = getConnection();        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {            Object[] values = whereStatement.values();            for (int i = 0; i < values.length; i++) {                preparedStatement.setObject(i + 1, values[i]);            }            preparedStatement.execute();            preparedStatement.close();            connection.close();        } catch (SQLException e1) {            e1.printStackTrace();        }    }    @Override    public String getName() {        return name;    }    @Override    public void drop() {        executeSimpleStatement(String.format(SQLQuery.DROP_TABLE.getQuery(), getName()));    }    private void executeSimpleStatement(String statement) {        try {            getLogger().debug(statement);            Connection connection = getConnection();            connection.createStatement().execute(statement);            connection.close();        } catch (SQLException e) {            e.printStackTrace();        }    }    @Override    public void dropColumn(Column column) {        if (!columns.contains(column)) {            throw new IllegalArgumentException("Column Does not exist!");        }        columns.remove(column);        executeSimpleStatement(String.format(SQLQuery.DROP_COLUMN.getQuery(), name, column.getName()));    }    @Override    public void addColumn(Column column) {        executeSimpleStatement(String.format(SQLQuery.ADD_COLUMN.getQuery(), name, column.build()));        SQLColumn column1 = (SQLColumn) column;        column1.setTable(this);        columns.add(column1);    }    @Override    public void modifyColumn(Column column) {        if (!columns.contains(column)) {            throw new IllegalArgumentException("Column Does not exist!");        }        executeSimpleStatement(String.format(SQLQuery.MODIFY_COLUMN.getQuery(), name, column.build()));        //Update the Object :)        columns.remove(column);        SQLColumn column1 = (SQLColumn) column;        column1.setTable(this);        columns.add(column1);    }    @Override    public boolean equals(Object obj) {        if (!(obj instanceof SQLTable)) return false;        return name.equals(((SQLTable) obj).getName());    }}