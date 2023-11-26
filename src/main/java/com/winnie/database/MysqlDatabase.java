package com.winnie.database;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.winnie.app.model.entity.House;
import com.winnie.app.model.entity.Payment;
import com.winnie.app.model.entity.Tenant;
import com.winnie.app.model.entity.User;
import com.winnie.database.helper.DbTable;
import com.winnie.database.helper.DbTableColumn;
import com.winnie.database.helper.DbTableId;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

@Singleton
@Startup
public class MysqlDatabase implements Serializable {



  /*  private static final String URL = "jdbc:mysql://localhost:3306/Rentals";

    private static final String USER = "root";

    private static final String PASSWORD = "root";*/
  /*  private static MysqlDatabase database;*/
    private Connection connection;
    /*private MysqlDatabase() throws SQLException , NamingException {
        //connection = DriverManager.getConnection(URL, USER, PASSWORD);

       *//* //switched from driver manager to data source
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUrl(URL);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);

        connection = dataSource.getConnection();*//*



        Context ctx = new InitialContext();
        DataSource dataSource= (DataSource) ctx.lookup("java:jboss/datasources/Rentals");

        connection = dataSource.getConnection();

    }*/

    public Connection getConnection() {
        return connection;
    }

    @PostConstruct
    private void init() throws SQLException, NamingException {
        Context ctx = new InitialContext();
        DataSource dataSource = (DataSource) ctx.lookup("java:jboss/datasources/Rentals");
        connection = dataSource.getConnection();

        System.out.println("Executed. on start up!!");

        this.updateSchema();
    }

   /* public static MysqlDatabase getInstance() throws SQLException {
        if (database == null) {
            try {
                database = new MysqlDatabase();
            } catch (SQLException| NamingException e) {
                throw new RuntimeException(e);
            }
        }

        return database;

    }*/

    public  void updateSchema(){

        System.out.println("----------Updating schema database----------------");
        try {/*
            Connection connection = MysqlDatabase.getInstance().getConnection();*/


            List<Class<?>> entities = new ArrayList<>();
            entities.add(User.class);
            entities.add(House.class);
            entities.add(Tenant.class);
            entities.add(Payment.class);



            for (Class<?> clazz : entities) {
                if (!clazz.isAnnotationPresent(DbTable.class))
                    continue;

                DbTable dbTable = clazz.getAnnotation(DbTable.class);

                StringBuilder sqlBuilder = new StringBuilder();

                sqlBuilder.append("create table if not exists ").append(dbTable.name()).append("(");

                List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

                for (Field field : fields) {
                    if (!field.isAnnotationPresent(DbTableColumn.class))
                        continue;

                    DbTableColumn dbTableColumn = field.getAnnotation(DbTableColumn.class);

                    sqlBuilder.append(dbTableColumn.name()).append(" ")
                            .append(dbTableColumn.type())
                            .append(field.isAnnotationPresent(DbTableId.class)?" NOT NULL AUTO_INCREMENT PRIMARY KEY" : "")
                            .append(",");
                }

                sqlBuilder.append(")");

                String tableCreationSql = sqlBuilder.toString().replace(",)", ")");
                System.out.println("Creating table: " + tableCreationSql);
                connection.prepareStatement(tableCreationSql).executeUpdate();

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


    }
    public void insert(Object entity) {
        try {

            Class<?> clazz = entity.getClass();
            if (!clazz.isAnnotationPresent(DbTable.class))
                return;

            DbTable dbTable = clazz.getAnnotation(DbTable.class);

            List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

            StringBuilder columnBuilder = new StringBuilder();
            StringBuilder paramPlaceHolderBuilder = new StringBuilder();
            List<Object> parameters = new ArrayList<>();

            for (Field field : fields) {
                if (!field.isAnnotationPresent(DbTableColumn.class) || field.isAnnotationPresent(DbTableId.class))
                    continue;

                field.setAccessible(true);
                if (field.get(entity) == null)
                    continue;

                DbTableColumn dbTableColumn = field.getAnnotation(DbTableColumn.class);

                columnBuilder.append(dbTableColumn.name()).append(",");
                paramPlaceHolderBuilder.append("?").append(",");
                parameters.add(field.get(entity));

            }

            String queryBuilder = "insert into " +
                    dbTable.name() +
                    "(" +
                    columnBuilder +
                    ")" +
                    " values(" +
                    paramPlaceHolderBuilder +
                    ")";

            String query = queryBuilder.replace(",)", ")");
            System.out.println("Query: " + query);

          /*  PreparedStatement sqlStmt = MysqlDatabase.getInstance().getConnection()
                    .prepareStatement(query);*/

            PreparedStatement sqlStmt = connection.prepareStatement(query);

            int paramIdx = 1;
            for (Object param : parameters) {
                if (param instanceof Enum)
                    sqlStmt.setString(paramIdx++, ((Enum<?>) param).name());
                else if (param.getClass().isAssignableFrom(BigDecimal.class))
                    sqlStmt.setBigDecimal(paramIdx++, (BigDecimal) param);
                else if (param.getClass().isAssignableFrom(Long.class))
                    sqlStmt.setLong(paramIdx++, (long) param);
                else if (param.getClass().isAssignableFrom(Date.class))
                    sqlStmt.setDate(paramIdx++, new java.sql.Date(((Date) param).getTime()));
                else
                    sqlStmt.setString(paramIdx++, (String) param);
            }

            sqlStmt.executeUpdate();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public <T> List<T> select(Class<T> filter) {
        System.out.println("starting select/................");
        try {
            Class<?> clazz = filter;
            System.out.println();

            if (!clazz.isAnnotationPresent(DbTable.class))
                return new ArrayList<>();

            DbTable dbTable = clazz.getAnnotation(DbTable.class);
            StringBuilder stringBuilder = new StringBuilder();

            System.out.println(stringBuilder.toString());
            stringBuilder.append("SELECT * FROM ")
                    .append(dbTable.name()).append(";");/*
            Connection conn = MysqlDatabase.getInstance().getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(stringBuilder.toString());*/
            PreparedStatement preparedStatement=connection.prepareStatement(stringBuilder.toString());

            System.out.println("executing query begins/................");
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                T object = (T) clazz.getDeclaredConstructor().newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    DbTableColumn dbColumn = field.getAnnotation(DbTableColumn.class);
                    if (dbColumn != null) {
                        String columnName = dbColumn.name();

                        Object value = resultSet.getObject(columnName);


                        if (value instanceof java.sql.Date && field.getType().equals(Date.class)) {
                            value = new Date(((java.sql.Date) value).getTime());
                        }



                        // Convert String to enum if needed
                        if (field.getType().isEnum() && value instanceof String) {
                            value = Enum.valueOf((Class<Enum>) field.getType(), (String) value);
                        }

                        field.setAccessible(true);
                        field.set(object, value);
                    }
                }

                result.add(object);
            }
            return result;

        } catch (SQLException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    @PreDestroy
    public void closeConnection(){
        try {
            if (connection != null)
                connection.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
/*
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }*/
}
