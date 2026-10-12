#!/usr/bin/env python3
"""Brass Reliquary HUD texture generator (pixel art, native GUI scale).
Run:  python gen_textures.py [output_dir]
Needs: Pillow
"""
import math, os, random, sys
from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else "out"
os.makedirs(OUT, exist_ok=True)


def C(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


# ---------------------------------------------------------------- palette
BR_HI, BR_LT, BR_MD, BR_DK, BR_DD = C('F4D58A'), C('D9A94F'), C('B8893B'), C('7A5624'), C('3F2A12')
IR_HI, IR_LT, IR_MD, IR_DK, IR_DD = C('4B4B57'), C('353540'), C('26252D'), C('1B1A1F'), C('0F0E13')
OUTLINE = C('09080B')


def clamp(v):
    return max(0, min(255, int(v)))


def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))


def stops_lerp(stops, u):
    u = max(0.0, min(0.9999, u))
    n = len(stops) - 1
    i = int(u * n)
    return lerp(stops[i], stops[i + 1], u * n - i)


def brass_ramp(s):
    if s > 0.55: return BR_HI
    if s > 0.15: return BR_LT
    if s > -0.25: return BR_MD
    if s > -0.65: return BR_DK
    return BR_DD


def new(w, h):
    im = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    return im, im.load()


def blend(p, x, y, c):
    """alpha-composite colour c over pixel (x,y)"""
    if not (0 <= x < p_size[0] and 0 <= y < p_size[1]):
        return
    d = p[x, y]
    a = c[3] / 255.0
    da = d[3] / 255.0
    oa = a + da * (1 - a)
    if oa <= 0:
        return
    out = tuple(int((c[i] * a + d[i] * da * (1 - a)) / oa) for i in range(3)) + (int(oa * 255),)
    p[x, y] = out


p_size = (0, 0)


def set_size(im):
    global p_size
    p_size = im.size


def save(im, name):
    im.save(os.path.join(OUT, name))


# ---------------------------------------------------------------- generic bevel panel (9-slice friendly)
def bevel_panel(w, h, profile, interior, cut=2, noise=0, seed=1):
    rnd = random.Random(seed)
    im, p = new(w, h)
    n = len(profile)
    for y in range(h):
        for x in range(w):
            dl, dt, dr, db = x, y, w - 1 - x, h - 1 - y
            if dl + dt < cut or dr + dt < cut or dl + db < cut or dr + db < cut:
                continue
            d = min(dl, dt, dr, db)
            if d >= n:
                c = interior
                if noise:
                    k = rnd.randint(-noise, noise)
                    c = (clamp(c[0] + k), clamp(c[1] + k), clamp(c[2] + k), c[3])
            else:
                lit = min(dl, dt) <= min(dr, db)
                c = profile[d][0 if lit else 1]
            p[x, y] = c
    # outline pass for chamfered corners
    snap = im.copy().load()
    for y in range(h):
        for x in range(w):
            if snap[x, y][3] == 0:
                continue
            for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
                if 0 <= nx < w and 0 <= ny < h and snap[nx, ny][3] == 0:
                    p[x, y] = OUTLINE
                    break
    return im, p


def rivet(p, cx, cy):
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            r = math.hypot(dx, dy)
            if r > 2.3:
                continue
            if r > 1.6:
                p[cx + dx, cy + dy] = BR_DD
            elif dx == -1 and dy == -1:
                p[cx + dx, cy + dy] = BR_HI
            elif dx + dy >= 1:
                p[cx + dx, cy + dy] = BR_DK
            else:
                p[cx + dx, cy + dy] = BR_LT


# ================================================================ frame_9slice.png  48x48, border 12
def make_frame():
    prof = [
        (OUTLINE, OUTLINE),          # 0
        (BR_HI, BR_MD),              # 1
        (BR_LT, BR_DK),              # 2
        (BR_MD, BR_DK),              # 3
        (BR_DK, BR_DD),              # 4
        (OUTLINE, IR_DD),            # 5 groove
        (IR_HI, IR_DK),              # 6 iron band
        (IR_LT, IR_DK),              # 7
        (IR_MD, IR_DK),              # 8
        (IR_DD, IR_LT),              # 9 inner bevel (recessed)
        (BR_DK, BR_LT),              # 10 thin brass inlay
        (C('0A090D'), C('0A090D')),  # 11 inner shadow
    ]
    im, p = bevel_panel(48, 48, prof, C('1B1A1F', 226), cut=2, noise=3, seed=7)
    for cx, cy in ((6, 6), (41, 6), (6, 41), (41, 41)):
        rivet(p, cx, cy)
    save(im, 'frame_9slice.png')


# ================================================================ plaque.png  32x32, border 8
def make_plaque():
    prof = [
        (OUTLINE, OUTLINE),
        (BR_HI, BR_MD),
        (BR_LT, BR_DK),
        (BR_MD, BR_DK),
        (BR_DK, BR_DD),
        (BR_DD, BR_DD),
        (C('0B0A07'), BR_DK),
        (C('15120D'), C('15120D')),
    ]
    im, p = bevel_panel(32, 32, prof, C('17140F', 236), cut=2, noise=2, seed=3)
    for cx, cy in ((4, 4), (27, 4), (4, 27), (27, 27)):
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                p[cx + dx, cy + dy] = BR_LT
        p[cx - 1, cy - 1] = BR_HI
        p[cx + 1, cy + 1] = BR_DK
        for k in (-1, 0, 1):
            p[cx + k, cy - k] = BR_DD
    save(im, 'plaque.png')


# ================================================================ counter_plate.png  24x14, border 3
def make_counter_plate():
    prof = [
        (OUTLINE, OUTLINE),
        (BR_LT, BR_DK),
        (BR_DK, BR_DD),
    ]
    im, p = bevel_panel(24, 14, prof, C('14110C', 240), cut=1, noise=2, seed=5)
    save(im, 'counter_plate.png')


# ================================================================ tube_frame.png / tube_glass.png  128x16
TUBE_W, TUBE_H = 128, 16
CH_X, CH_Y, CH_W, CH_H = 12, 4, 104, 8      # liquid area


def make_tube():
    im, p = new(TUBE_W, TUBE_H)
    set_size(im)
    cap_rows = [OUTLINE, BR_MD, BR_HI, BR_HI, BR_LT, BR_LT, BR_MD, BR_MD,
                BR_MD, BR_DK, BR_DK, BR_DK, BR_DD, BR_DD, BR_DD, OUTLINE]
    # caps
    for (x0, x1) in ((0, 11), (116, 127)):
        for x in range(x0, x1 + 1):
            for y in range(TUBE_H):
                p[x, y] = cap_rows[y]
        # grooves / bands
        left = (x0 == 0)
        for gx in ((3, 8) if left else (118, 123)):
            for y in range(1, TUBE_H - 1):
                p[gx, y] = lerp(cap_rows[y], OUTLINE, 0.55)
                nx = gx + 1
                p[nx, y] = lerp(cap_rows[y], BR_HI, 0.25)
        # outline edges
        for y in range(TUBE_H):
            p[x0 if left else x1, y] = OUTLINE
            p[x1 if left else x0, y] = OUTLINE
    # rounded outer corners
    for (x, y) in ((0, 0), (0, 15), (127, 0), (127, 15), (1, 0), (0, 1), (126, 0), (127, 1),
                   (1, 15), (0, 14), (126, 15), (127, 14)):
        p[x, y] = (0, 0, 0, 0)
    for (x, y) in ((1, 1), (126, 1), (1, 14), (126, 14), (2, 0), (125, 0), (2, 15), (125, 15),
                   (0, 2), (127, 2), (0, 13), (127, 13)):
        p[x, y] = OUTLINE
    # rivets in caps
    for (cx, cy) in ((5, 8), (122, 8)):
        pass  # grooves already give enough detail; keep caps clean
    # mid rails
    top = [OUTLINE, BR_LT, BR_MD, BR_DK]
    bot = [BR_MD, BR_DK, BR_DD, OUTLINE]
    for x in range(12, 116):
        for i, c in enumerate(top):
            p[x, i] = c
        for i, c in enumerate(bot):
            p[x, 12 + i] = c
    # channel (dark glass interior, cylindrical shading)
    rows = [IR_DD, C('14121A'), C('1A1822'), C('1F1C29'), C('1F1C29'), C('1A1822'), C('14121A'), IR_DD]
    for x in range(CH_X, CH_X + CH_W):
        for i, c in enumerate(rows):
            p[x, CH_Y + i] = c
    # shadow near caps
    for i, a in enumerate((90, 55, 28)):
        for y in range(CH_Y, CH_Y + CH_H):
            blend(p, CH_X + i, y, (0, 0, 0, a))
            blend(p, CH_X + CH_W - 1 - i, y, (0, 0, 0, a))
    # tick notches in the top rail
    for frac in (0.25, 0.5, 0.75):
        x = CH_X + int(CH_W * frac)
        p[x, 2] = BR_DD
        p[x, 14] = BR_DD if False else p[x, 14]
    save(im, 'tube_frame.png')

    # glass overlay
    g, gp = new(TUBE_W, TUBE_H)
    set_size(g)
    for x in range(CH_X + 2, CH_X + CH_W - 2):
        gp[x, CH_Y] = (255, 255, 255, 80)
        gp[x, CH_Y + 1] = (255, 255, 255, 38)
        gp[x, CH_Y + 6] = (255, 255, 255, 14)
        gp[x, CH_Y + 7] = (0, 0, 0, 70)
    for i, a in enumerate((70, 38, 16)):
        for y in range(CH_Y, CH_Y + CH_H):
            blend(gp, CH_X + i, y, (0, 0, 0, a))
            blend(gp, CH_X + CH_W - 1 - i, y, (0, 0, 0, a))
    for x0 in (30, 78):  # diagonal glare streaks
        for k in range(CH_H - 2):
            blend(gp, x0 + (CH_H - 2 - k), CH_Y + 1 + k, (255, 255, 255, 46))
            blend(gp, x0 + (CH_H - 2 - k) + 1, CH_Y + 1 + k, (255, 255, 255, 22))
    for frac in (0.25, 0.5, 0.75):
        x = CH_X + int(CH_W * frac)
        for y in (CH_Y, CH_Y + 1, CH_Y + 6, CH_Y + 7):
            gp[x, y] = (255, 255, 255, 120)
        gp[x, CH_Y + 2] = (255, 255, 255, 50)
        gp[x, CH_Y + 5] = (255, 255, 255, 50)
    save(g, 'tube_glass.png')


# ================================================================ medallion (cog + face) 32x32
def make_medallion():
    N = 32
    im, p = new(N, N)
    mask = [[False] * N for _ in range(N)]
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            ang = math.atan2(dy, dx) % (2 * math.pi)
            ph = (ang / (2 * math.pi) * 12) % 1
            lo = 0.14 + 0.08 * ((r - 12.6) / 3.0)
            mask[y][x] = r <= 12.6 or (r <= 15.6 and lo < ph < 1 - lo)

    def m(x, y):
        return 0 <= x < N and 0 <= y < N and mask[y][x]

    bound = [[mask[y][x] and not (m(x + 1, y) and m(x - 1, y) and m(x, y + 1) and m(x, y - 1))
              for x in range(N)] for y in range(N)]
    stops = [BR_HI, BR_LT, BR_MD, BR_DK, BR_DD]
    for y in range(N):
        for x in range(N):
            if not mask[y][x]:
                continue
            dx, dy = x + .5 - 16, y + .5 - 16
            if bound[y][x]:
                p[x, y] = OUTLINE
                continue
            u = 0.5 + 0.5 * (dx + dy) / 22.0
            c = stops_lerp(stops, u)
            if bound[y - 1][x] or bound[y][x - 1]:
                c = BR_HI if u < 0.7 else BR_MD
            elif bound[y + 1][x] or bound[y][x + 1]:
                c = BR_DK if u > 0.3 else BR_MD
            p[x, y] = c
    save(im, 'medallion_cog.png')

    f, fp = new(N, N)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            s = (-dx - dy) / (1.4142 * r) if r > 0 else 0
            if r > 11.9:
                continue
            if r > 11.0:
                fp[x, y] = OUTLINE
            elif r > 9.0:
                fp[x, y] = brass_ramp(s)
            elif r > 8.0:
                fp[x, y] = brass_ramp(-s) if False else lerp(IR_DD, BR_DD, 0.5 + 0.5 * -s * 0.4)
            else:
                t = 0.5 + 0.5 * (dx + dy) / 12.0
                fp[x, y] = lerp(C('2E2D36'), C('121116'), t)
    # engraved arc hint in plate (subtle)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            if 6.6 < r < 7.4 and dy > 0 and abs(dx) > 1.0:
                fp[x, y] = lerp(fp[x, y], BR_DK, 0.35)
    save(f, 'medallion.png')


# ================================================================ dial / needle / glass 32x32
def make_dial():
    N = 32
    im, p = new(N, N)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            s = (-dx - dy) / (1.4142 * r) if r > 0 else 0
            if r > 15.8:
                continue
            if r > 15.0:
                p[x, y] = OUTLINE
            elif r >= 13.2:
                p[x, y] = brass_ramp(s)
            elif r >= 12.2:
                p[x, y] = brass_ramp(-s * 0.9)
            else:
                base = lerp(C('45330F'), C('1A1306'), (y + .5) / 32)
                p[x, y] = lerp(base, C('5A4214'), max(0, 1 - r / 12.2) * 0.35)
    # ticks: sweep -120..+120 degrees from "up", clockwise
    for i in range(13):
        a = math.radians(-120 + 20 * i)
        major = (i % 3 == 0)
        r0, r1 = (7.0, 11.2) if major else (9.0, 11.2)
        col = C('FFD470') if major else C('B27A1C')
        steps = int((r1 - r0) / 0.25)
        for k in range(steps + 1):
            r = r0 + k * 0.25
            x = int(math.floor(16 + math.sin(a) * r))
            y = int(math.floor(16 - math.cos(a) * r))
            p[x, y] = col
    save(im, 'dial.png')

    # needle (pivot at texture centre 16,16; points up)
    n, np_ = new(N, N)
    for y in range(3, 21):
        np_[14, y] = OUTLINE
        np_[17, y] = OUTLINE
        np_[15, y] = C('FFD060')
        np_[16, y] = C('D98E10')
    np_[15, 2] = OUTLINE
    np_[16, 2] = OUTLINE
    np_[15, 3] = C('FFF2B8')
    np_[16, 3] = C('FFE08A')
    np_[15, 21] = OUTLINE
    np_[16, 21] = OUTLINE
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            s = (-dx - dy) / (1.4142 * r) if r > 0 else 0
            if r <= 1.3:
                np_[x, y] = BR_DD
            elif r <= 3.2:
                np_[x, y] = brass_ramp(s)
            elif r <= 4.1:
                np_[x, y] = OUTLINE
    save(n, 'needle.png')

    g, gp = new(N, N)
    set_size(g)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            if r < 12.0 and (dx + dy) < -8:
                a = int(min(44, (-8 - (dx + dy)) * 6))
                blend(gp, x, y, (255, 255, 255, a))
            s = (-dx - dy) / (1.4142 * r) if r > 0 else 0
            if 10.6 < r < 11.6 and s > 0.35:
                blend(gp, x, y, (255, 255, 255, 46))
    save(g, 'dial_glass.png')


# ================================================================ socket + ring 32x32
def make_socket():
    N = 32
    R = 15.6
    a_hex = R * math.sqrt(3) / 2
    im, p = new(N, N)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            s = (-dx - dy) / (1.4142 * r) if r > 0 else 0
            m = max(abs(dx), (abs(dx) + math.sqrt(3) * abs(dy)) / 2)
            if m > a_hex:
                continue
            if m > a_hex - 0.9:
                p[x, y] = OUTLINE
            elif m > a_hex - 2.5:
                p[x, y] = brass_ramp(s)
            elif r > 12.0:
                p[x, y] = lerp(IR_LT, IR_DK, 0.5 + 0.5 * (dx + dy) / 22)
            elif r > 11.0:
                p[x, y] = lerp(BR_DK, BR_DD, 0.5 + 0.5 * (-s))
            else:
                t = 0.5 + 0.5 * (dx + dy) / 22
                p[x, y] = lerp(C('15141A'), C('0B0A0E'), t)
    # tiny bolts at 3 hex corners (top, lower-left, lower-right)
    for (cx, cy) in ((16, 3), (4, 23), (27, 23)):
        p[cx, cy] = BR_HI
        p[cx + 1, cy] = BR_MD
        p[cx, cy + 1] = BR_MD
        p[cx + 1, cy + 1] = BR_DK
    save(im, 'socket.png')

    ring, rp = new(N, N)
    for y in range(N):
        for x in range(N):
            dx, dy = x + .5 - 16, y + .5 - 16
            r = math.hypot(dx, dy)
            if 11.5 <= r <= 13.0:
                ang = math.atan2(dy, dx) % (2 * math.pi)
                ph = (ang / (2 * math.pi) * 12) % 1
                if ph < 0.62:
                    v = 255 if (-dx - dy) > 0 else 205
                    rp[x, y] = (v, v, v, 255)
    save(ring, 'socket_ring.png')


# ================================================================ keycap 24x24, border 6
def make_keycap():
    N = 24
    im, p = new(N, N)
    # silhouette
    for y in range(N):
        for x in range(N):
            if x + y < 2 or (N - 1 - x) + y < 2 or x + (N - 1 - y) < 2 or (N - 1 - x) + (N - 1 - y) < 2:
                continue
            p[x, y] = OUTLINE
    # skirt (rows 17..22)
    skirt = {17: BR_MD, 18: BR_DK, 19: BR_DK, 20: BR_DD, 21: BR_DD, 22: BR_DD}
    for y, c in skirt.items():
        for x in range(1, N - 1):
            if p[x, y][3]:
                p[x, y] = c
    # top face rows 1..16
    for y in range(1, 17):
        for x in range(1, N - 1):
            if p[x, y][3] == 0:
                continue
            top, left, bot, right = y == 1, x == 1, y == 16, x == N - 2
            if top or left:
                c = BR_LT
            elif bot or right:
                c = BR_DK
            elif y == 2 or x == 2 or y == 15 or x == N - 3:
                c = IR_DD
            else:
                c = lerp(IR_HI, IR_MD, (y - 3) / 12.0)
            p[x, y] = c
    save(im, 'keycap.png')


# ================================================================ jar (back + glass) 32x32
def make_jar():
    N = 32
    im, p = new(N, N)
    set_size(im)

    def body(x, y):
        # rounded rect x 4..27, y 9..29, radius 4
        if not (4 <= x <= 27 and 9 <= y <= 29):
            return False
        cx = min(max(x, 8), 23)
        cy = min(max(y, 13), 25)
        return math.hypot(x - cx, y - cy) <= 4.4

    inside = [[body(x, y) for x in range(N)] for y in range(N)]
    for y in range(N):
        for x in range(N):
            if not inside[y][x]:
                continue
            edge = not (inside[y - 1][x] and inside[y + 1][x] and inside[y][x - 1] and inside[y][x + 1])
            if edge:
                p[x, y] = C('0A1514', 230)
            else:
                a = 34 + int(30 * (y - 9) / 20)
                p[x, y] = C('8FE6D6', a)
    # inner rim of glass
    for y in range(N):
        for x in range(N):
            if inside[y][x] and p[x, y][3] < 200:
                if not (inside[y - 1][x] and inside[y + 1][x] and inside[y][x - 1] and inside[y][x + 1]):
                    continue
                n8 = [inside[y + dy][x + dx] for dx in (-1, 0, 1) for dy in (-1, 0, 1)]
                if not all(n8):
                    p[x, y] = C('B8F5E8', 120)
    # lid (x 8..23, y 1..6)
    lid = [BR_HI, BR_LT, BR_MD, BR_MD, BR_DK, BR_DD]
    for i, c in enumerate(lid):
        for x in range(8, 24):
            p[x, 1 + i] = c
    for y in range(0, 8):
        pass
    for x in range(8, 24):
        p[x, 0] = OUTLINE
        p[x, 7] = OUTLINE
    for y in range(0, 8):
        p[7, y] = OUTLINE
        p[24, y] = OUTLINE
    for x in (11, 15, 19):  # knurling
        for y in range(2, 6):
            p[x, y] = lerp(p[x, y], BR_DD, 0.45)
    # collar (x 9..22, y 8)
    for x in range(6, 26):
        p[x, 8] = BR_DK if x > 15 else BR_MD
    for x in range(5, 27):
        p[x, 9] = BR_DD if p[x, 9][3] == 0 else p[x, 9]
    # base plate (y 29..31)
    for x in range(5, 27):
        p[x, 29] = BR_MD
        p[x, 30] = BR_DK
        p[x, 31] = OUTLINE
    for x in range(5, 27):
        pass
    save(im, 'jar.png')

    g, gp = new(N, N)
    set_size(g)
    for y in range(13, 26):
        gp[7, y] = (255, 255, 255, 96)
        gp[8, y] = (255, 255, 255, 44)
    gp[7, 12] = (255, 255, 255, 60)
    gp[7, 26] = (255, 255, 255, 50)
    for y in range(14, 24):
        gp[25, y] = (255, 255, 255, 30)
    for x in range(10, 22):
        gp[x, 11] = (255, 255, 255, 26)
    for (bx, by) in ((21, 14), (23, 20), (20, 24)):
        gp[bx, by] = (255, 255, 255, 80)
    save(g, 'jar_glass.png')


# ================================================================ glyph_tile.png 32x32 (tileable, white, tint + low alpha in code)
def make_glyph():
    N = 32
    im, p = new(N, N)
    W = (255, 255, 255, 255)

    def line(x0, y0, x1, y1):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps + 1):
            t = i / steps if steps else 0
            x = int(round(x0 + (x1 - x0) * t)) % N
            y = int(round(y0 + (y1 - y0) * t)) % N
            p[x, y] = W

    # outer diamond
    line(16, 6, 26, 16); line(26, 16, 16, 26); line(16, 26, 6, 16); line(6, 16, 16, 6)
    # inner diamond
    line(16, 12, 20, 16); line(20, 16, 16, 20); line(16, 20, 12, 16); line(12, 16, 16, 12)
    # centre dot
    p[15, 15] = p[16, 15] = p[15, 16] = p[16, 16] = W
    # connecting lines to the tile edge (continue into neighbours)
    line(16, 0, 16, 6); line(16, 26, 16, 31)
    line(0, 16, 6, 16); line(26, 16, 31, 16)
    # diagonal dashes + corner dots
    for (x, y) in ((8, 8), (23, 8), (8, 23), (23, 23)):
        p[x, y] = p[x + 1, y] = p[x, y + 1] = p[x + 1, y + 1] = W
    for (x, y) in ((0, 0), (31, 0), (0, 31), (31, 31)):
        p[x, y] = W
    p[1, 0] = p[0, 1] = p[30, 0] = p[31, 1] = p[1, 31] = p[0, 30] = p[30, 31] = p[31, 30] = W
    save(im, 'glyph_tile.png')


if __name__ == '__main__':
    make_frame()
    make_plaque()
    make_counter_plate()
    make_tube()
    make_medallion()
    make_dial()
    make_socket()
    make_keycap()
    make_jar()
    make_glyph()
    print(sorted(os.listdir(OUT)))
