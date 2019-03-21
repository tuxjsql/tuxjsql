package me.kingtux.tuxjsql.mysql;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Query;
import me.kingtux.tuxjsql.core.statements.SelectStatement;
import me.kingtux.tuxjsql.core.Table;

import java.util.stream.Collectors;
@SuppressWarnings("All")
public class MySQLSelectStatement extends SelectStatement {
    @Override
    public Query build(Table table) {
        if (columns == null || columns.isEmpty()) {
            columns = table.getColumns().stream().map(Column::getName).collect(Collectors.toList());
        }
        StringBuilder columnsToSelect = new StringBuilder();
        for (String column : columns) {
            if (!columnsToSelect.toString().isEmpty()) {
                columnsToSelect.append(",");
            }
            columnsToSelect.append(column);
        }
        StringBuilder builder = new StringBuilder(String.format(SQLQuery.SELECT.getQuery(), columnsToSelect, table.getName()));
        builder.append(whereStatement == null ? "" : " WHERE " + whereStatement.build().getQuery());
        return new Query(builder.toString(), whereStatement == null ? null : whereStatement.values());    }
}
