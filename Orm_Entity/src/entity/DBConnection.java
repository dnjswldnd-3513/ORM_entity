package entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

public class DBConnection {

    private static String url = "jdbc:mysql://localhost/medinow?serverTimezone=UTC&allowLoadLocalInfile=true";
    private static String name = "root";
    private static String password = "1234";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, name, password);
    }

    public static void update(String sql, Object... values) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql);
            for (int i = 0; i < values.length; ++i) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer insert(String sql, Object... values) {
        try {
            PreparedStatement statement = getConnection().prepareStatement(sql, 1);
            for (int i = 0; i < values.length; ++i) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <E> void query(Class<E> clazz, Map<Integer, E> cache) {
        try {
            String sql = String.format("select * from %s", clazz.getSimpleName());
            PreparedStatement statement = getConnection().prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            Field[] fs = Arrays.stream(clazz.getFields()).toArray(Field[]::new);
            Constructor<E> constructor = clazz
                    .getConstructor(Arrays.stream(fs).map(Field::getType).toArray(Class[]::new));

            while (rs.next()) {
                Object[] args = new Object[fs.length];
                for (int i = 0; i < fs.length; i++) {
                    Field field = fs[i];
                    Object value = rs.getObject(i + 1);

                    if (db.Entity.isCk(field)) {
                        Class<?> refClass = field.getType();
                        value = refClass.getDeclaredMethod("getById", int.class).invoke(null, (Integer) value);
                    } else if (field.getType() == LocalDate.class) {
                        value = rs.getDate(i + 1).toLocalDate();
                    }
                    args[i] = value;
                }
                E entity = constructor.newInstance(args);
                Integer id = (Integer) fs[0].get(entity);
                cache.put(id, entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
