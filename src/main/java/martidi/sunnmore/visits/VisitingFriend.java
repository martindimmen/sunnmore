package martidi.sunnmore.visits;

/** A friend NPC that can come by on a visit (Glenn, Jinith). */
public interface VisitingFriend {
	/** Ticks until he leaves again. 0 means he stays for good. */
	void setDespawnDelay(int ticks);

	/** True for a friend who came by himself and will leave (not egg, /summon or name-tagged ones). */
	boolean isVisiting();

	/** His arrival line, loud enough for players around to know he is here. */
	void sayArrival();
}
