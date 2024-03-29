package me.kingtux.tuxjsql.core;

import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.connection.CPProvider;
import me.kingtux.tuxjsql.core.connection.ConnectionProvider;
import me.kingtux.tuxjsql.core.exceptions.NoSQLBuilderException;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is the starting point for your TuxJSQL jour
 */
public class TuxJSQLBuilder {
    private TuxJSQLBuilder() {

    }

    /**
     * Get a SQLBuilder by the class
     *
     * @param clazz the class path
     * @param classLoader classloader to use
     * @return the SQLBuilder
     * @throws Exception unable to create tuxjsql
     */
    public static SQLBuilder getBuildByClazz(String clazz, ClassLoader classLoader) throws Exception {
        Class<?> cla;
        try {
            cla = Class.forName(clazz, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new NoSQLBuilderException("Unable to find SQLBuilder for ", e);
        }
        return getBuildByClazz(cla);
    }

    /**
     * Builds a new SQLBuilder using the reflectasm
     *
     * @param clazz the clazz object
     * @return the SQLBuilder
     * @throws NoSuchMethodException Unable to find constructor
     * @throws IllegalAccessException unable to access constructor
     * @throws InvocationTargetException Failure in the constructor
     * @throws InstantiationException Something broke
     */
    public static SQLBuilder getBuildByClazz(Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        return (SQLBuilder) clazz.getConstructor().newInstance();

    }

    /**
     * Create a TuxJSQL via a properties
     * Follow this guide: Coming Soon
     *
     * @param properties the java properties object with rules
     * @return the TuxJSQL object
     * @throws Exception unable to create TuxJSQL
     */
    public static TuxJSQL create(Properties properties) throws Exception {
        if (properties.containsKey("executors.count")) {
            return create(properties, Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("executors.count"))));
        } else {
            return create(properties, Executors.newSingleThreadExecutor());
        }
    }

    private static ExecutorService getExecutorsService(Properties properties) {
        if (properties.containsKey("executors.count")) {
            return Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("executors.count")));
        } else {
            return Executors.newSingleThreadExecutor();
        }
    }

    public static TuxJSQL create(Properties properties, ClassLoader classLoader) throws Exception {
        return create(properties, getExecutorsService(properties), classLoader);
    }


    public static TuxJSQL create(Properties properties, ExecutorService service) throws Exception {
        return create(properties, service, TuxJSQL.class.getClassLoader());
    }

    public static TuxJSQL create(Properties properties, ExecutorService service, ClassLoader classLoader) throws Exception {
        SQLBuilder builder;
        if (properties.containsKey("db.type")) {
            builder = getBuildByClazz(properties.getProperty("db.type"), classLoader);
        } else {
            throw new IllegalArgumentException("Must provide a DB type");
        }

        return create(properties, builder, service);
    }


    public static TuxJSQL create(Properties properties, SQLBuilder builder, ExecutorService service) throws Exception {
        ConnectionProvider provider = CPProvider.getCP();
        builder.configureConnectionProvider(provider, properties);
        if (provider.isClosed()) {
            return null;
        }
        return new TuxJSQL(provider, builder, service);
    }
}
