package db;

import entity.DBConnection;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Collectors;

public abstract class Entity<T> {

    public static boolean isCk(Field f) {
        return f.getName().startsWith("fk_");
    }
    public void update() {
        try {
            Class<?> cls = this.getClass();
            Deque<Field> fileds = new LinkedList<>(Arrays.asList(cls.getFields()));
            fileds.add(fileds.poll());

            String sql = String.format("update %s set %s where %s = ?",
                    cls.getSimpleName(),
                    fileds.stream().limit(fileds.size() - 1).map(f -> String.format("%s = ?", f.getName().replaceFirst("fk_", ""))).collect(Collectors.joining(",")),
                    fileds.getLast().getName());
            DBConnection.update(sql, fileds.stream().map(f -> isCk(f) ? subEntity(f) : getFv(f, this)).toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        try {
            Class<?> cls = this.getClass();
            Field id = cls.getDeclaredFields()[0];
            DBConnection.update(String.format("delete from %s where %s = ?", cls.getSimpleName(),id.getName()),id.get(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getFv(Field f,Object ob) {
        try {
            return f.get(ob);
        } catch (Exception e) {
            e.printStackTrace();
        }return null;
    }

    public Object subEntity(Field f) {
        Entity sub = (Entity) getFv(f, this);
        return getFv(sub.getClass().getFields()[0], sub);
    }
}
