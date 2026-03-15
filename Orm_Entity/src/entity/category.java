package db;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class category extends Entity<category>{



	public category(Integer cno, String name) {
		super();
		this.cno = cno;
		this.name = name;
	}
	public Integer cno          ;
	public String name         ;



	private static Map<Integer, category> cache = new LinkedHashMap<>();
	static {DB.query(category.class, cache);};

	public static Stream<category> getA(Predicate<category> f){
		return cache.values().stream().filter(f);
	}

	public static category getById(int id) {
		return cache.get(id);
	}
	public void insert() {
		this.cno =  DB.insert(String.format("insert into %s values(%s)", this.getClass().getSimpleName(),
				Arrays.stream(this.getClass().getFields()).map(f -> {
					Object val = isCk(f) ? subEntity(f):getFv(f, this);
					return val != null ? "\""+val.toString()+"\"":"null";
				}).collect(Collectors.joining(","))));
		cache.put(this.cno, this);
	}

	@Override
	public void delete() {
		super.delete();
		cache.remove(this.cno);
	}


}
