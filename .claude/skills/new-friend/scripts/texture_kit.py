"""Helpers for painting a friend's textures with Pillow (pip install pillow).

Import it from your own paint script (keep that script out of the repo; commit only the PNGs):

    import sys; sys.path.insert(0, ".claude/skills/new-friend/scripts")
    from texture_kit import faces, fill, px, rows, new_skin, recolor_spawn_egg, client_jar

    skin = new_skin(128, 64)                       # same size as LayerDefinition.create(mesh, 128, 64)
    head = faces(0, 0, 8, 8, 8)                    # same numbers as texOffs(0, 0).addBox(..., 8, 8, 8)
    fill(skin, head["front"], (233, 190, 160, 255))
    px(skin, head["front"], 2, 4, (70, 120, 190, 255))   # an eye, x/y inside that face
    skin.save("src/main/resources/assets/sunnmore/textures/entity/<friend>/<friend>.png")

    egg = recolor_spawn_egg(client_jar(), base=[dark, mid, base, light], spots=(r, g, b, 255))
    egg.save("src/main/resources/assets/sunnmore/textures/item/<friend>_spawn_egg.png")

Box UV layout (Minecraft model cubes): for a cube at texOffs(u, v) with size w x h x d, the faces
sit as returned by faces(). "front" is the side facing the way the mob looks (-z).
"""
import colorsys
import glob
import io
import os
import zipfile

from PIL import Image

CLEAR = (0, 0, 0, 0)


def faces(u, v, w, h, d):
    """Rectangles (x0, y0, width, height) of each face of a cube at texOffs(u, v), size w x h x d."""
    return {
        "top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h),
    }


def new_skin(width=128, height=64):
    return Image.new("RGBA", (width, height), CLEAR)


def fill(img, rect, color):
    x0, y0, w, h = rect
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            img.putpixel((x, y), color)


def px(img, rect, x, y, color):
    """One pixel at (x, y) inside a face rectangle."""
    img.putpixel((rect[0] + x, rect[1] + y), color)


def rows(img, rect, y0, y1, color):
    """Rows y0..y1-1 of a face rectangle, full width (a belt, a sleeve cuff, shoes)."""
    x0, top, w, _ = rect
    for y in range(y0, y1):
        for x in range(w):
            img.putpixel((x0 + x, top + y), color)


def client_jar():
    """The Minecraft 26.3 client jar Loom downloaded, for vanilla textures to start from."""
    pattern = os.path.expanduser("~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar")
    found = glob.glob(pattern)
    if not found:
        raise FileNotFoundError("Run ./gradlew build once so Loom downloads " + pattern)
    return found[0]


def recolor_spawn_egg(jar_path, base, spots, outline=(30, 30, 30, 255)):
    """Vanilla's spawn egg shape, recoloured. base: 4 colours dark to light; spots: one colour."""
    egg = Image.open(io.BytesIO(zipfile.ZipFile(jar_path).read(
        "assets/minecraft/textures/item/enderman_spawn_egg.png"))).convert("RGBA")
    greys = sorted({p[0] + p[1] + p[2] for p in egg.getdata()
                    if p[3] and colorsys.rgb_to_hsv(*[c / 255 for c in p[:3]])[1] < 0.3})
    out = Image.new("RGBA", egg.size, CLEAR)
    for y in range(egg.height):
        for x in range(egg.width):
            r, g, b, a = egg.getpixel((x, y))
            if not a:
                continue
            saturation = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)[1]
            if saturation >= 0.3:
                out.putpixel((x, y), spots)
            elif (r, g, b) == (0, 0, 0):
                out.putpixel((x, y), outline)
            else:
                rank = greys.index(r + g + b) / max(1, len(greys) - 1)
                out.putpixel((x, y), base[min(3, int(rank * 4))])
    return out
