package martidi.sunnmore.stian;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import martidi.sunnmore.stian.ChurchBlueprint.Placement;

/**
 * What Stian does next on a church: place the first block that isn't there yet, or wait for the
 * material it needs. "Done" is read from the world, so a church picks up where it left off.
 */
public final class ChurchProgress {
	public sealed interface Step permits Place, Missing, Finished {
	}

	public record Place(Placement placement) implements Step {
	}

	/** The next block needs an item the plot doesn't have; stillNeeded is how many the rest of the church needs. */
	public record Missing(String item, int stillNeeded) implements Step {
	}

	public record Finished() implements Step {
	}

	private ChurchProgress() {
	}

	public static Step next(List<Placement> order, Predicate<Placement> done, ToIntFunction<String> available) {
		for (Placement p : order) {
			// A door's upper half has no item: it is placed together with the lower half.
			if (p.item() == null || done.test(p)) {
				continue;
			}
			if (available.applyAsInt(p.item()) <= 0) {
				return new Missing(p.item(), stillNeeded(order, done).getOrDefault(p.item(), 0));
			}
			return new Place(p);
		}
		return new Finished();
	}

	/** Item id to how many more the church still needs. */
	public static Map<String, Integer> stillNeeded(List<Placement> order, Predicate<Placement> done) {
		Map<String, Integer> needed = new TreeMap<>();
		for (Placement p : order) {
			if (p.item() != null && !done.test(p)) {
				needed.merge(p.item(), 1, Integer::sum);
			}
		}
		return needed;
	}
}
