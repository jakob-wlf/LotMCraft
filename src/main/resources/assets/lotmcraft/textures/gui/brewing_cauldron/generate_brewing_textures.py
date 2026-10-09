#!/usr/bin/env python3
"""
Generates all placeholder textures for the redesigned LOTM brewing cauldron GUI.

    pip install pillow
    python generate_brewing_textures.py [output_dir]

Output (default ./textures/gui/brewing_cauldron/):
    brewing_cauldron_gui.png  256x256 canvas, 176x240 panel in the top-left (replaces old file)
    liquid_strip.png          112x192   8 frames of 112x24, stacked vertically
    stream.png                24x31     4 frames of 6x31, side by side
    droplet.png               6x6
    slot_glow.png             24x24
    recipe_ghost.png          16x16
    particles.png             16x8      row 0: 4 bubbles, row 1: 4 steam puffs (4x4 cells)

All geometry (slot positions, liquid window, stream channel) is shared with
BrewingCauldronScreen.java - if you change a number here, change it there too.
"""
import math
import os
import sys

from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join('textures', 'gui', 'brewing_cauldron')
os.makedirs(OUT, exist_ok=True)

W, H = 176, 240  # panel size

# ----------------------------------------------------------------------------
# Geometry (panel-relative pixel coordinates) - keep in sync with the screen class
# ----------------------------------------------------------------------------
SLOT_SUP = [(48, 37), (112, 37), (14, 37), (146, 37)]   # supplementary ingredients: slots 0,1,5,6
SLOT_MAIN = (80, 72)                                     # main ingredient (in the liquid): slot 2
SLOT_OUT = (80, 122)                         # slot 3
SLOT_REC = (152, 122)                        # slot 4

LIQ_X, LIQ_Y, LIQ_W, LIQ_H, LIQ_FRAMES = 32, 64, 112, 24, 8   # liquid sprite placement
LIQ_CX, LIQ_CY = 88, 76                                       # centre of liquid ellipse
WIN_RX, WIN_RY = 50, 10                                       # transparent window in background
SPR_RX, SPR_RY = 52, 12                                       # liquid sprite ellipse (slightly larger)

STREAM_X, STREAM_Y, STREAM_W, STREAM_H = 85, 89, 6, 31

LINES = [  # ley lines: x0, y0, x1, y1, side_pixel_dx
    (65, 54, 79, 70, 1),
    (110, 54, 96, 70, -1),
]
H_LINES = [(31, 46, 44), (65, 110, 44), (129, 144, 44)]  # x0, x1, y  (outer->inner, inner<->inner, inner->outer)


# ----------------------------------------------------------------------------
# Helpers
# ----------------------------------------------------------------------------
def hsh(x, y, s=0):
    h = (x * 374761393 + y * 668265263 + s * 982451653) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return h ^ (h >> 16)


def rnd(x, y, s=0):
    return (hsh(x, y, s) & 0xFFFF) / 65535.0


def cl(v):
    return max(0, min(255, int(round(v))))


def lerp(a, b, t):
    return a + (b - a) * t


def mul(c, f):
    return (cl(c[0] * f), cl(c[1] * f), cl(c[2] * f))


def mixc(a, b, t):
    return tuple(cl(lerp(a[i], b[i], t)) for i in range(3))


img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
P = img.load()


def put(x, y, c, a=255):
    if 0 <= x < W and 0 <= y < H:
        P[x, y] = (cl(c[0]), cl(c[1]), cl(c[2]), a)


def blend(x, y, c, a):
    """Blend colour c over an existing pixel with factor a (keeps pixel alpha)."""
    if not (0 <= x < W and 0 <= y < H):
        return
    p = P[x, y]
    if p[3] == 0:
        return
    P[x, y] = (cl(lerp(p[0], c[0], a)), cl(lerp(p[1], c[1], a)), cl(lerp(p[2], c[2], a)), p[3])


def rect(x0, y0, x1, y1, c, a=255):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(x, y, c, a)


# ----------------------------------------------------------------------------
# 1. Wall (charcoal-violet stone bricks) + inventory tray
# ----------------------------------------------------------------------------
BRICK_H, BRICK_W = 12, 28


def draw_wall():
    for y in range(H):
        for x in range(W):
            if y >= 151:  # tray under the alchemy area
                n = (rnd(x, y, 1) - 0.5) * 5
                put(x, y, (26 + n, 22 + n, 32 + n * 1.2))
                continue
            r = y // BRICK_H
            xx = x + (r % 2) * (BRICK_W // 2)
            col = xx // BRICK_W
            bv = (rnd(col, r, 2) - 0.5) * 10
            n = (rnd(x, y, 3) - 0.5) * 7
            c = [40 + bv + n, 35 + bv + n, 47 + (bv + n) * 1.1]
            ly, lx = y % BRICK_H, xx % BRICK_W
            if ly == 0 or lx == 0:
                c = [21, 18, 27]
            elif ly == 1 or lx == 1:
                c = [v + 7 for v in c]
            elif ly == BRICK_H - 1 or lx == BRICK_W - 1:
                c = [v - 7 for v in c]
            put(x, y, c)

    # soft violet glow behind the cauldron + vignette toward the edges
    for y in range(151):
        for x in range(W):
            d = math.hypot(x - 88, y - 90) / 95.0
            glow = max(0.0, 1.0 - d)
            ed = min(x, W - 1 - x, y)
            vig = 0.72 + 0.28 * min(1.0, ed / 18.0)
            p = P[x, y]
            P[x, y] = (cl((p[0] + 10 * glow) * vig), cl((p[1] + 4 * glow) * vig),
                       cl((p[2] + 18 * glow) * vig), 255)


# ----------------------------------------------------------------------------
# 2. Ritual circle on the wall
# ----------------------------------------------------------------------------
GLYPHS = [
    ["111", "101", "111", "010", "010"],
    ["010", "111", "101", "111", "010"],
    ["101", "101", "111", "010", "111"],
    ["111", "010", "111", "010", "111"],
    ["110", "011", "110", "011", "110"],
    ["010", "101", "111", "101", "010"],
]


def draw_ritual_circle():
    cx, cy = 88, 84
    col = (96, 70, 130)
    for y in range(151):
        for x in range(W):
            d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
            if abs(d - 64) < 0.62:
                blend(x, y, col, 0.50)
            elif abs(d - 60) < 0.55:
                blend(x, y, col, 0.32)
            elif abs(d - 49) < 0.5:
                blend(x, y, col, 0.22)
    # tick marks between the two outer rings
    for k in range(48):
        a = k * math.tau / 48
        long = (k % 4 == 0)
        for rr in ([61, 62, 63] if long else [62]):
            blend(int(cx + math.cos(a) * rr), int(cy + math.sin(a) * rr), col, 0.55)
    # 12 sigils
    for k in range(12):
        a = k * math.tau / 12 + math.pi / 12
        gx = int(cx + math.cos(a) * 54.5) - 1
        gy = int(cy + math.sin(a) * 54.5) - 2
        g = GLYPHS[k % len(GLYPHS)]
        for j, row in enumerate(g):
            for i, ch in enumerate(row):
                if ch == '1':
                    blend(gx + i, gy + j, (126, 94, 168), 0.55)


# ----------------------------------------------------------------------------
# 3. Cauldron
# ----------------------------------------------------------------------------
LX, LY, LZ = -0.45, -0.65, 0.62
_n = math.sqrt(LX * LX + LY * LY + LZ * LZ)
LX, LY, LZ = LX / _n, LY / _n, LZ / _n


def shade_sphere(nx, ny, base, amb=0.30, k=0.95, spec=34.0):
    nz = math.sqrt(max(0.0, 1.0 - nx * nx - ny * ny))
    dot = max(0.0, nx * LX + ny * LY + nz * LZ)
    s = (dot ** 14) * spec
    return [base[0] * (amb + k * dot) + s, base[1] * (amb + k * dot) + s * 0.95,
            base[2] * (amb + k * dot) + s * 1.1]


def draw_ellipse_blob(cx, cy, rx, ry, base, y_min=None, seed=0):
    for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
        if y_min is not None and y < y_min:
            continue
        for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
            nx, ny = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            d2 = nx * nx + ny * ny
            if d2 > 1.0:
                continue
            c = shade_sphere(nx, ny, base)
            n = (rnd(x, y, 10 + seed) - 0.5) * 8
            c = [v + n for v in c]
            if d2 > 0.93:
                c = [v * 0.45 for v in c]
            put(x, y, c)


def draw_cauldron():
    iron = (62, 56, 70)

    # legs (behind the body): tapered feet with a flat bottom
    for cx in (46, 130):
        for y in range(118, 143):
            t = (y - 118) / 24.0
            half = 8.5 - 3.0 * t + (2.5 if y >= 139 else 0.0)   # taper, then a small foot flare
            for x in range(int(cx - half) - 1, int(cx + half) + 2):
                nx = (x + 0.5 - cx) / max(half, 1.0)
                if abs(nx) > 1.0:
                    continue
                c = shade_sphere(nx * 0.9, -0.3 + 0.3 * t, (44, 40, 52), amb=0.26, k=0.8, spec=8.0)
                c = [v + (rnd(x, y, 15) - 0.5) * 8 for v in c]
                if abs(nx) > 0.82 or y >= 142:
                    c = [v * 0.42 for v in c]
                put(x, y, c)

    # body
    bcx, bcy, brx, bry = 88, 98, 57, 38
    for y in range(bcy - bry - 1, bcy + bry + 2):
        for x in range(bcx - brx - 1, bcx + brx + 2):
            nx, ny = (x + 0.5 - bcx) / brx, (y + 0.5 - bcy) / bry
            d2 = nx * nx + ny * ny
            if d2 > 1.0:
                continue
            c = shade_sphere(nx, ny, iron)
            n = (rnd(x, y, 11) - 0.5) * 9
            c = [v + n for v in c]
            # a few rusty/violet speckles
            if rnd(x, y, 12) > 0.985:
                c = [c[0] + 10, c[1] + 2, c[2] + 14]
            # shadow just below the rim lip
            eo = ((x + 0.5 - 88) / 59.0) ** 2 + ((y + 0.5 - 74) / 17.0) ** 2
            if 1.0 < eo < 1.35 and y > 74:
                c = [v * (0.55 + 0.45 * (eo - 1.0) / 0.35) for v in c]
            if d2 > 0.95:
                c = [v * 0.4 for v in c]
            put(x, y, c)

    # rivets along a curved latitude line
    for k in range(-5, 6):
        t = k / 5.5
        rx_ = int(88 + 50 * t)
        ry_ = int(106 + 8 * math.sqrt(max(0.0, 1 - t * t)))
        put(rx_, ry_, (118, 108, 134))
        put(rx_ + 1, ry_, (86, 78, 100))
        put(rx_, ry_ + 1, (30, 26, 36))
        put(rx_ + 1, ry_ + 1, (24, 20, 30))

    # rim (lip)
    for y in range(52, 96):
        for x in range(26, 151):
            xf, yf = x + 0.5 - 88, y + 0.5 - 74
            eo = (xf / 59.0) ** 2 + (yf / 17.0) ** 2
            ei = (xf / 53.0) ** 2 + (yf / 13.0) ** 2
            if eo > 1.0 or ei <= 1.0:
                continue
            a, b = math.sqrt(ei), math.sqrt(eo)
            s = (a - 1.0) / max(1e-6, (a - 1.0) + (1.0 - b))
            bump = max(0.0, 1.0 - abs(s - 0.42) / 0.58)
            nx, ny = xf / 59.0, yf / 17.0
            directional = 0.78 + 0.32 * (-nx * 0.45 - ny * 0.5)
            f = (0.50 + 0.85 * bump) * directional
            n = (rnd(x, y, 13) - 0.5) * 8
            base = (86, 78, 98)
            c = [base[0] * f + n, base[1] * f + n, base[2] * f + n * 1.1]
            if s < 0.07 or s > 0.93:
                c = [v * 0.45 for v in c]
            put(x, y, c)

    # opening: back wall / inner shadow + transparent liquid window
    for y in range(52, 96):
        for x in range(26, 151):
            xf, yf = x + 0.5 - 88, y + 0.5 - 74
            ei = (xf / 53.0) ** 2 + (yf / 13.0) ** 2
            if ei > 1.0:
                continue
            wy = y + 0.5 - LIQ_CY
            ew = (xf / WIN_RX) ** 2 + (wy / WIN_RY) ** 2
            if ew <= 1.0:
                P[x, y] = (0, 0, 0, 0)      # window: liquid sprite shows through
                continue
            if y + 0.5 < LIQ_CY:            # back wall (lit a little)
                k = 1.0 - (LIQ_CY - (y + 0.5)) / 18.0
                c = (22 + 16 * k, 17 + 12 * k, 32 + 22 * k)
            else:                           # front inner wall in shadow
                c = (11, 9, 15)
            n = (rnd(x, y, 14) - 0.5) * 4
            put(x, y, (c[0] + n, c[1] + n, c[2] + n))
            if ew < 1.18:                   # faint meniscus ring around the liquid
                blend(x, y, (70, 46, 100), 0.55)


def draw_trench():
    """Dark channel the pouring stream runs through (x 84..91, y 89..119)."""
    for y in range(STREAM_Y, STREAM_Y + STREAM_H):
        for x in range(STREAM_X - 1, STREAM_X + STREAM_W + 1):
            edge = x in (STREAM_X - 1, STREAM_X + STREAM_W)
            if edge:
                put(x, y, (9, 7, 13))
            else:
                t = (y - STREAM_Y) / STREAM_H
                put(x, y, (15 + 5 * t, 11 + 3 * t, 22 + 6 * t))
        blend(STREAM_X + STREAM_W + 1, y, (120, 110, 140), 0.35)   # lit right lip
        blend(STREAM_X - 2, y, (4, 3, 6), 0.55)                    # shadow on the left
    for x in range(STREAM_X - 1, STREAM_X + STREAM_W + 1):
        blend(x, STREAM_Y, (110, 100, 130), 0.45)                   # top lip


# ----------------------------------------------------------------------------
# 4. Haze, grooves
# ----------------------------------------------------------------------------
def draw_haze():
    for y in range(108, 151):
        t = ((y - 108) / 43.0) ** 1.4
        for x in range(W):
            n = 0.5 + 0.25 * math.sin(x * 0.13 + 1.0) + 0.25 * math.sin(y * 0.21 + x * 0.05)
            blend(x, y, (150, 145, 172), 0.30 * t * n)


def groove_diag(x0, y0, x1, y1, side, c):
    dy = y1 - y0
    for y in range(y0, y1 + 1):
        x = x0 + round((y - y0) * (x1 - x0) / dy)
        put(x, y, c)
        blend(x + side, y, c, 0.35)


def draw_grooves():
    c = (58, 38, 82)
    for (x0, y0, x1, y1, side) in LINES:
        groove_diag(x0, y0, x1, y1, side, c)
    for (hx0, hx1, hy) in H_LINES:
        for x in range(hx0, hx1 + 1):
            put(x, hy, c)
            blend(x, hy + 1, c, 0.35)
    # recipe thread (dark amber) from the recipe slot to the cauldron wall
    for x in range(121, 150):
        put(x, 130, (66, 46, 26))
        blend(x, 131, (66, 46, 26), 0.4)
    rect(119, 128, 122, 131, (40, 28, 18))
    rect(120, 129, 121, 130, (110, 82, 46))


# ----------------------------------------------------------------------------
# 5. Slot frames
# ----------------------------------------------------------------------------
def frame18(x0, y0, interior=(28, 24, 34), alpha=255, runes=False):
    """Recessed 18x18 frame, item area is the inner 16x16 at (x0+1, y0+1)."""
    for j in range(18):
        for i in range(18):
            x, y = x0 + i, y0 + j
            if j == 0 or i == 0:
                put(x, y, (8, 6, 12))
            elif j == 17 or i == 17:
                put(x, y, (84, 72, 104))
            else:
                n = (rnd(x, y, 20) - 0.5) * 4
                c = (interior[0] + n, interior[1] + n, interior[2] + n)
                if j == 1 or i == 1:
                    c = mul(c, 0.62)       # inner shadow top/left
                put(x, y, c, alpha)
    if runes:
        v, vd = (150, 108, 214), (92, 64, 134)
        for (cx_, cy_, dx, dy) in ((0, 0, 1, 1), (17, 0, -1, 1), (0, 17, 1, -1), (17, 17, -1, -1)):
            put(x0 + cx_, y0 + cy_, v)
            put(x0 + cx_ + dx, y0 + cy_, vd)
            put(x0 + cx_, y0 + cy_ + dy, vd)


def frame_output(sx, sy):
    """Ornate 20x20 brass/violet frame; item area at (sx, sy)."""
    x0, y0 = sx - 2, sy - 2
    for j in range(20):
        for i in range(20):
            x, y = x0 + i, y0 + j
            ring = min(i, j, 19 - i, 19 - j)
            if ring == 0:
                put(x, y, (176, 142, 84) if (j == 0 or i == 0) else (74, 54, 30))
            elif ring == 1:
                put(x, y, (10, 8, 14))
            else:
                n = (rnd(x, y, 21) - 0.5) * 4
                d = math.hypot(i - 9.5, j - 9.5) / 12.0
                put(x, y, (24 + n + 6 * (1 - d), 19 + n, 31 + n + 8 * (1 - d)))
    stud = (228, 198, 124)
    for cx_, cy_ in ((0, 0), (18, 0), (0, 18), (18, 18)):
        rect(x0 + cx_, y0 + cy_, x0 + cx_ + 1, y0 + cy_ + 1, stud)
    # violet gem where the stream arrives
    rect(x0 + 9, y0, x0 + 10, y0 + 1, (160, 100, 235))
    put(x0 + 9, y0, (225, 185, 255))
    # tiny side marks
    for k in (6, 13):
        put(x0, y0 + k, (120, 92, 52))
        put(x0 + 19, y0 + k, (120, 92, 52))
        put(x0 + k, y0, (120, 92, 52))
        put(x0 + k, y0 + 19, (120, 92, 52))


def frame_recipe(sx, sy):
    """Leather + parchment 20x20 frame; item area at (sx, sy)."""
    x0, y0 = sx - 2, sy - 2
    for j in range(20):
        for i in range(20):
            x, y = x0 + i, y0 + j
            ring = min(i, j, 19 - i, 19 - j)
            if ring == 0:
                if (i in (0, 19)) and (j in (0, 19)):
                    continue                      # rounded corner
                put(x, y, (40, 26, 16))
            elif ring == 1:
                stitch = ((i + j) % 2 == 0) and not (i in (1, 18) and j in (1, 18))
                put(x, y, (156, 118, 72) if stitch else (104, 72, 42))
            else:
                n = (rnd(x, y, 22) - 0.5) * 5
                c = (74 + n, 61 + n, 44 + n)
                if ring == 2:
                    c = mul(c, 0.72)
                put(x, y, c)
    for cx_, cy_ in ((1, 1), (18, 1), (1, 18), (18, 18)):
        put(x0 + cx_, y0 + cy_, (190, 146, 80))


def draw_title_plaque():
    x0, x1, y0, y1 = 34, 141, 4, 17
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            edge = x in (x0, x1) or y in (y0, y1)
            if edge:
                blend(x, y, (110, 88, 140), 0.55)
            else:
                blend(x, y, (6, 4, 10), 0.62)
    for cx_ in (x0 + 3, x1 - 3):
        put(cx_, (y0 + y1) // 2, (150, 108, 214))


def draw_slots():
    for (sx, sy) in SLOT_SUP:
        frame18(sx - 1, sy - 1, runes=True)
    # main ingredient sits in the liquid -> translucent interior
    frame18(SLOT_MAIN[0] - 1, SLOT_MAIN[1] - 1, interior=(14, 10, 22), alpha=96, runes=True)
    frame_output(*SLOT_OUT)
    frame_recipe(*SLOT_REC)
    for row in range(3):
        for col in range(9):
            frame18(8 + col * 18 - 1, 158 + row * 18 - 1, interior=(30, 26, 37))
    for col in range(9):
        frame18(8 + col * 18 - 1, 216 - 1, interior=(30, 26, 37))


# ----------------------------------------------------------------------------
# 6. Panel border, corners, tray separator
# ----------------------------------------------------------------------------
def draw_border():
    # tray separator
    for x in range(3, W - 3):
        put(x, 150, (8, 6, 12))
        put(x, 151, (84, 72, 104) if x % 2 else (66, 56, 84))
        put(x, 152, (14, 11, 20))
    for y in range(H):
        for x in range(W):
            ring = min(x, y, W - 1 - x, H - 1 - y)
            if ring == 0:
                put(x, y, (5, 4, 7))
            elif ring == 1:
                top_left = (x == 1 or y == 1) and (x < W - 2 and y < H - 2)
                put(x, y, (96, 84, 118) if top_left else (46, 38, 60))
            elif ring == 2:
                put(x, y, (17, 14, 23))
    # corner studs
    for (cx_, cy_) in ((3, 3), (W - 7, 3), (3, H - 7), (W - 7, H - 7)):
        rect(cx_, cy_, cx_ + 3, cy_ + 3, (12, 9, 17))
        rect(cx_ + 1, cy_ + 1, cx_ + 2, cy_ + 2, (126, 114, 146))
        put(cx_ + 1, cy_ + 1, (190, 180, 210))
    # rounded outer corners
    for (x, y) in ((0, 0), (1, 0), (0, 1), (W - 1, 0), (W - 2, 0), (W - 1, 1),
                   (0, H - 1), (1, H - 1), (0, H - 2), (W - 1, H - 1), (W - 2, H - 1), (W - 1, H - 2)):
        P[x, y] = (0, 0, 0, 0)


def build_background():
    draw_wall()
    draw_ritual_circle()
    draw_cauldron()
    draw_trench()
    draw_haze()
    draw_grooves()
    draw_title_plaque()
    draw_slots()
    draw_border()
    img.save(os.path.join(OUT, 'brewing_cauldron_gui.png'))


# ----------------------------------------------------------------------------
# 7. Liquid strip (8 looping frames)
# ----------------------------------------------------------------------------
BAYER = [[0, 2], [3, 1]]
LIQ_PAL = [(52, 42, 78), (80, 64, 116), (114, 92, 156), (166, 142, 206)]


def build_liquid():
    strip = Image.new('RGBA', (LIQ_W, LIQ_H * LIQ_FRAMES), (0, 0, 0, 0))
    sp = strip.load()
    cx, cy = LIQ_W / 2.0, LIQ_H / 2.0
    for f in range(LIQ_FRAMES):
        ph = f * math.tau / LIQ_FRAMES
        for y in range(LIQ_H):
            for x in range(LIQ_W):
                u = (x + 0.5 - cx) / SPR_RX
                v = (y + 0.5 - cy) / SPR_RY
                r = math.hypot(u, v)
                if r > 1.0:
                    continue
                th = math.atan2(v * 2.2, u)
                s1 = math.sin(2 * th - 7.0 * r + ph)
                s2 = math.sin(9.0 * u + 4.0 * v + 2 * ph)
                s3 = math.sin(5.0 * u - 11.0 * v - ph)
                w = 0.55 * s1 + 0.27 * s2 + 0.18 * s3          # -1..1
                val = (w + 1.0) * 0.5 * 3.0 + (BAYER[y % 2][x % 2] - 1.5) * 0.28
                idx = max(0, min(3, int(val + 0.5)))
                c = list(LIQ_PAL[idx])
                if r > 0.90:
                    k = 1.0 - (r - 0.90) / 0.10 * 0.45
                    c = [q * k for q in c]
                if v > 0.55:
                    c = [q * 0.85 for q in c]
                # twinkles - each pixel peaks once per loop
                hh = hsh(x, y, 77)
                if hh % 53 == 0 and r < 0.92:
                    pk = (hh >> 8) % LIQ_FRAMES
                    if f == pk:
                        c = [235, 215, 255]
                sp[x, f * LIQ_H + y] = (cl(c[0]), cl(c[1]), cl(c[2]), 255)
    strip.save(os.path.join(OUT, 'liquid_strip.png'))


# ----------------------------------------------------------------------------
# 8. Stream (4 frames x 6x31), droplet, glow, ghost icon, particles
# ----------------------------------------------------------------------------
def build_stream():
    sheet = Image.new('RGBA', (STREAM_W * 4, STREAM_H), (0, 0, 0, 0))
    sp = sheet.load()
    colp = [0.45, 0.80, 1.0, 1.0, 0.80, 0.45]
    pat = [1.0, 0.92, 0.62, 0.42, 0.38, 0.45, 0.62, 0.84]    # period 8
    dark, light = (78, 34, 140), (214, 156, 255)
    for f in range(4):
        for y in range(STREAM_H):
            for x in range(STREAM_W):
                v = colp[x] * pat[(y - 2 * f) % 8]
                c = mixc(dark, light, v)
                if x == 2 and pat[(y - 2 * f) % 8] > 0.9:
                    c = (246, 232, 255)
                sp[f * STREAM_W + x, y] = (c[0], c[1], c[2], 255)
    sheet.save(os.path.join(OUT, 'stream.png'))


def build_droplet():
    d = Image.new('RGBA', (6, 6), (0, 0, 0, 0))
    dp = d.load()
    for y in range(6):
        for x in range(6):
            r = math.hypot(x - 2.5, y - 2.5)
            if r < 1.3:
                dp[x, y] = (255, 246, 255, 255)
            elif r < 2.3:
                dp[x, y] = (196, 126, 255, 210)
            elif r < 3.2:
                dp[x, y] = (140, 70, 230, 90)
    d.save(os.path.join(OUT, 'droplet.png'))


def build_glow():
    g = Image.new('RGBA', (24, 24), (0, 0, 0, 0))
    gp = g.load()
    half, rad = 9.0, 3.0
    for y in range(24):
        for x in range(24):
            qx = abs(x + 0.5 - 12) - (half - rad)
            qy = abs(y + 0.5 - 12) - (half - rad)
            d = math.hypot(max(qx, 0), max(qy, 0)) + min(max(qx, qy), 0) - rad
            if d > 0:
                a = max(0.0, 1.0 - d / 3.2) ** 1.7 * 0.95
            else:
                a = 0.16 + 0.12 * min(1.0, -d / 6.0)
            gp[x, y] = (206, 176, 255, cl(a * 255))
    g.save(os.path.join(OUT, 'slot_glow.png'))


def build_ghost():
    s = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    sp = s.load()
    outline, par, shade, ink = (88, 66, 40), (216, 194, 150), (168, 144, 102), (110, 84, 52)
    for y in range(3, 13):                      # sheet
        for x in range(4, 12):
            sp[x, y] = par + (255,)
    for y in range(3, 13):
        sp[11, y] = shade + (255,)
    for x in range(3, 13):                      # top / bottom rolls
        for y in (1, 2):
            sp[x, y] = par + (255,)
        for y in (13, 14):
            sp[x, y] = par + (255,)
        sp[x, 3] = shade + (255,)
        sp[x, 12] = shade + (255,)
    for (x0, y0, x1, y1) in ((3, 1, 12, 2), (3, 13, 12, 14)):   # outlines
        for x in range(x0 - 1, x1 + 2):
            for y in (y0 - 1, y1 + 1):
                sp[x, y] = outline + (255,)
        for y in range(y0, y1 + 1):
            sp[x0 - 1, y] = outline + (255,)
            sp[x1 + 1, y] = outline + (255,)
    for y in range(3, 13):
        sp[3, y] = outline + (255,)
        sp[12, y] = outline + (255,)
    for y in (5, 7, 9):                          # text lines
        for x in range(5, 10 if y != 9 else 8):
            sp[x, y] = ink + (255,)
    s.save(os.path.join(OUT, 'recipe_ghost.png'))


def build_particles():
    p = Image.new('RGBA', (16, 8), (0, 0, 0, 0))
    pp = p.load()
    core, edge, hi = (214, 190, 255, 230), (190, 150, 255, 200), (255, 250, 255, 255)
    pp[1, 1] = core                                           # size 1
    for (x, y) in ((5, 1), (6, 1), (5, 2), (6, 2)):           # size 2
        pp[x, y] = core
    pp[5, 1] = hi
    for (x, y) in ((8, 0), (9, 0), (10, 0), (8, 1), (10, 1), (8, 2), (9, 2), (10, 2)):   # size 3 ring
        pp[x, y] = edge
    pp[9, 1] = (170, 120, 240, 70)
    pp[8, 0] = hi
    for (x, y) in ((13, 0), (14, 0), (12, 1), (15, 1), (12, 2), (15, 2), (13, 3), (14, 3)):   # size 4 ring
        pp[x, y] = edge
    for (x, y) in ((13, 1), (14, 1), (13, 2), (14, 2)):
        pp[x, y] = (170, 120, 240, 60)
    pp[13, 1] = hi
    puffs = [  # 4x4 alpha maps
        [[0, 60, 60, 0], [60, 120, 120, 60], [60, 120, 100, 50], [0, 50, 50, 0]],
        [[0, 50, 70, 0], [50, 110, 120, 60], [70, 120, 110, 50], [0, 60, 50, 0]],
        [[0, 0, 60, 40], [50, 100, 120, 60], [60, 120, 90, 0], [0, 60, 40, 0]],
        [[40, 60, 0, 0], [60, 120, 100, 40], [0, 90, 120, 60], [0, 40, 60, 0]],
    ]
    for k, m in enumerate(puffs):
        for y in range(4):
            for x in range(4):
                if m[y][x]:
                    pp[k * 4 + x, 4 + y] = (226, 220, 240, m[y][x])
    p.save(os.path.join(OUT, 'particles.png'))


if __name__ == '__main__':
    build_background()
    build_liquid()
    build_stream()
    build_droplet()
    build_glow()
    build_ghost()
    build_particles()
    print('Wrote textures to', os.path.abspath(OUT))
