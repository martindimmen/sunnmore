package martidi.sunnmore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Every friend's chat lines must exist in en_us.json. Finds each {@code *Lines$Pool} enum in the
 * mod by itself (Glenn, Jinith, Bård and any new friend), and checks that key(0) .. key(size - 1)
 * are all in the lang file. A missing key shows up in game as the raw key text.
 */
class LangKeysTest {
	private static final String LANG = "assets/sunnmore/lang/en_us.json";
	private static final Pattern KEY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:", Pattern.MULTILINE);

	@Test
	void everyChatLineExists() throws Exception {
		Set<String> keys = langKeys();
		List<String> pools = poolEnums();
		assertFalse(pools.isEmpty(), "found no *Lines$Pool enums: the scan is broken");
		List<String> missing = new ArrayList<>();
		for (String className : pools) {
			Class<?> pool = Class.forName(className);
			Method key = pool.getDeclaredMethod("key", int.class);
			key.setAccessible(true);
			Field size = pool.getDeclaredField("size");
			size.setAccessible(true);
			for (Object constant : pool.getEnumConstants()) {
				for (int i = 0; i < size.getInt(constant); i++) {
					String wanted = (String) key.invoke(constant, i);
					if (!keys.contains(wanted)) {
						missing.add(wanted);
					}
				}
			}
		}
		assertTrue(missing.isEmpty(), "missing from " + LANG + ": " + missing);
	}

	private static Set<String> langKeys() throws IOException {
		try (InputStream in = LangKeysTest.class.getClassLoader().getResourceAsStream(LANG)) {
			assertTrue(in != null, LANG + " is not on the test classpath");
			String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			Set<String> keys = new HashSet<>();
			Matcher matcher = KEY.matcher(json);
			while (matcher.find()) {
				keys.add(matcher.group(1));
			}
			return keys;
		}
	}

	/** Class names of every martidi.sunnmore.*Lines$Pool on the classpath (compiled class folders). */
	private static List<String> poolEnums() throws IOException, URISyntaxException {
		List<String> names = new ArrayList<>();
		for (URL root : Collections.list(LangKeysTest.class.getClassLoader().getResources("martidi/sunnmore"))) {
			if (!"file".equals(root.getProtocol())) {
				continue;
			}
			Path base = Paths.get(root.toURI()).getParent().getParent();
			try (Stream<Path> files = Files.walk(Paths.get(root.toURI()))) {
				files.map(path -> base.relativize(path).toString().replace('\\', '/'))
					.filter(name -> name.endsWith("Lines$Pool.class"))
					.map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
					.forEach(names::add);
			}
		}
		return names;
	}
}
