package me.kingtux.tuxjsql.basic.utils;

import me.kingtux.tuxjsql.basic.response.BasicDBColumnItem;
import me.kingtux.tuxjsql.basic.response.BasicDBRow;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxjsql.core.response.DBColumnItem;
import me.kingtux.tuxjsql.core.response.DBRow;
import org.apache.commons.lang3.Validate;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BasicUtils {
    /**
     * This method converts a ResultSet to a DBSelect response
     *
     * @param set The resultset you want to convert
     * @param tuxJSQL TuxJSQL value
     * @return your List of rows
     */
    public static List<DBRow>  resultSetToDBSelect(ResultSet set, TuxJSQL tuxJSQL) {
        Validate.notNull(set, "ResultSet cant be null.");
        try {
            Validate.isTrue(!set.isClosed(), "ResultSet must be open");
        } catch (SQLException e) {
            TuxJSQL.getLogger().error("Unable to get close status", e);
            return null;
        }
        List<DBRow> rows = new ArrayList<>();
        try {
            ResultSetMetaData metaData = set.getMetaData();
            while (set.next()) {
                int i = metaData.getColumnCount();
                List<DBColumnItem> items = new ArrayList<>();
                for (int j = 1; j <= i; j++) {

                    items.add(new BasicDBColumnItem(set.getObject(j), String.format("%s.%s", metaData.getTableName(j), metaData.getColumnName(j)), tuxJSQL));
                }
                rows.add(new BasicDBRow(items));

            }
        } catch (SQLException e) {
            TuxJSQL.getLogger().error("Unable to read ResultSet", e);
            return null;
        }
        try {
            set.close();
        } catch (SQLException e) {
            TuxJSQL.getLogger().error("Unable to close ResultSet. Probably Pim's fault. ", e);
            return null;
        }

        return rows;
    }

    public static <T> T getAsEnum(String string, ClassLoader classLoader) {
        String[] split = string.split("#");
        if (split.length == 0) return null;
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(split[0]);
        } catch (ClassNotFoundException e) {
            TuxJSQL.getLogger().error("Unable to locate class for enum", e);
            return null;
        }

        return (T) Enum.valueOf((Class<? extends Enum>) clazz, split[1]);
    }

    public static String enumToString(Enum o) {
        return o.getClass().getCanonicalName() + "#" + o.name();
    }
}
