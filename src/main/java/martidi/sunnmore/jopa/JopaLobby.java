package martidi.sunnmore.jopa;

import java.util.ArrayList;
import java.util.List;

/**
 * The JOPA lobby's layout, relative to the middle of its floor: a closed office room 9 by 9 inside,
 * with windows, roof lights and a lit return portal against the north wall. Closed on every side so
 * nobody falls into the void. Free of Minecraft classes so it can be unit tested; JopaLobbyBuilder
 * turns each part into a block.
 */
public final class JopaLobby {
	public enum Part {
		FLOOR, WALL, WINDOW, ROOF, LIGHT, FRAME, PORTAL, AIR
	}

	public record Cell(int x, int y, int z, Part part) {
	}

	/** The lobby floor's height in the JOPA dimension; the lobby is centred on x = 0, z = 0. */
	public static final int ORIGIN_Y = 64;
	/** The walls stand at x and z = -HALF and +HALF. */
	public static final int HALF = 5;
	/** The floor is y = 0, the roof y = HEIGHT. */
	public static final int HEIGHT = 6;
	/** The return portal stands in the plane z = PORTAL_Z (axis X), just inside the north wall. */
	public static final int PORTAL_Z = -HALF + 1;
	/** Where you arrive (feet), relative: in the middle of the room, a few steps from the portal. */
	public static final int[] SPAWN = {0, 1, 1};
	/** Facing north, towards the return portal. */
	public static final float SPAWN_YAW = 180.0F;

	private JopaLobby() {
	}

	public static boolean contains(int x, int y, int z) {
		return Math.abs(x) <= HALF && Math.abs(z) <= HALF && y >= 0 && y <= HEIGHT;
	}

	public static List<Cell> layout() {
		List<Cell> cells = new ArrayList<>();
		for (int x = -HALF; x <= HALF; x++) {
			for (int y = 0; y <= HEIGHT; y++) {
				for (int z = -HALF; z <= HALF; z++) {
					cells.add(new Cell(x, y, z, part(x, y, z)));
				}
			}
		}
		return cells;
	}

	private static Part part(int x, int y, int z) {
		if (z == PORTAL_Z && x >= -2 && x <= 1 && y <= 4) {
			boolean inside = x >= -1 && x <= 0 && y >= 1 && y <= 3;
			return inside ? Part.PORTAL : Part.FRAME;
		}
		if (y == 0) {
			return Part.FLOOR;
		}
		if (y == HEIGHT) {
			boolean light = (Math.abs(x) == 2 && Math.abs(z) == 2) || (x == 0 && z == 0);
			return light ? Part.LIGHT : Part.ROOF;
		}
		boolean xWall = Math.abs(x) == HALF;
		boolean zWall = Math.abs(z) == HALF;
		if (xWall || zWall) {
			// Windows in the middle of the east, west and south walls; the north wall (behind the portal) stays solid.
			int along = xWall ? z : x;
			boolean window = !(xWall && zWall) && z != -HALF && y >= 2 && y <= 4 && Math.abs(along) <= 2;
			return window ? Part.WINDOW : Part.WALL;
		}
		return Part.AIR;
	}
}
