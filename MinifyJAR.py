import os
import sys
import struct
import zlib
import zipfile
import concurrent.futures
import io
import math
import json
import gzip
from nbtlib.tag import Compound, List, LongArray, IntArray

try:
    import zopfli
except ImportError:
    print("Error: The 'zopflipy' library is missing.")
    sys.exit(1)

def zopfli_compress(data, format=zopfli.ZOPFLI_FORMAT_ZLIB, iterations=5):
    c = zopfli.ZopfliCompressor(format, iterations=iterations)
    return c.compress(data) + c.flush()

def recursive_sort_nbt(tag):
    if isinstance(tag, Compound):
        return Compound({k: recursive_sort_nbt(tag[k]) for k in sorted(tag.keys())})
    elif isinstance(tag, List):
        return List([recursive_sort_nbt(v) for v in tag])
    return tag

def minify_json(json_bytes):
    try:
        d = json.loads(json_bytes.decode('utf-8'))
        return json.dumps(d, separators=(',', ':')).encode('utf-8')
    except Exception: return json_bytes

def minify_toml(toml_bytes):
    try:
        lines = toml_bytes.decode('utf-8').splitlines()
        clean = []
        for l in lines:
            l = l.strip()
            if not l or l.startswith('#'): continue
            if '#' in l and not any(q in l.split('#')[0] for q in ['"', "'"]): l = l.split('#')[0].strip()
            clean.append(l)
        return '\n'.join(clean).encode('utf-8')
    except Exception: return toml_bytes

def repack_block_states(block_states, palette):
    d = block_states.get('data')
    if not d:
        if len(palette) > 1:
            first = palette[0]
            palette.clear()
            palette.append(first)
        return True
    bpl_old = 4096 // len(d) if len(d) > 0 else 1
    bits_old = 64 // bpl_old if bpl_old > 0 else 4
    indices = []
    for val in d:
        u = val if val >= 0 else (val + (1 << 64))
        for _ in range(bpl_old):
            if len(indices) < 4096:
                indices.append(u & ((1 << bits_old) - 1))
                u >>= bits_old
    while len(indices) < 4096: indices.append(0)
    used = set(indices)
    def key_fn(idx):
        if idx >= len(palette): return ("", "")
        e = palette[idx]
        p = e.get('Properties', {})
        return (str(e.get('Name', '')), str(sorted(p.items())) if p else "")
    sorted_idx = sorted(list(used), key=key_fn)
    new_pal = [palette[i] for i in sorted_idx if i < len(palette)]
    mapping = {old: i for i, old in enumerate(sorted_idx)}
    palette.clear()
    palette.extend(new_pal)
    if len(palette) <= 1:
        if 'data' in block_states: del block_states['data']
        return True
    new_indices = [mapping.get(i, 0) for i in indices]
    bits = max(4, math.ceil(math.log2(len(palette))))
    bpl = 64 // bits
    exp = math.ceil(4096 / bpl)
    new_longs, curr, off = [], 0, 0
    for idx in new_indices:
        curr |= (idx & ((1 << bits) - 1)) << off
        off += bits
        if off + bits > 64:
            new_longs.append(curr if curr < (1 << 63) else (curr - (1 << 64)))
            curr, off = 0, 0
    if off > 0: new_longs.append(curr if curr < (1 << 63) else (curr - (1 << 64)))
    while len(new_longs) < exp: new_longs.append(0)
    block_states['data'] = LongArray(new_longs[:exp])
    return True

def extreme_palette_compaction(root):
    sections = root.get('sections')
    if not sections: return False
    mod = False
    for s in sections:
        bs = s.get('block_states')
        if not bs: continue
        pal = bs.get('palette')
        if not pal: continue
        for b in pal:
            if 'Properties' in b:
                p = b['Properties']
                for prop in ['waterlogged','powered','lit','snowy','disarmed','persistent','distance']:
                    v = str(p.get(prop, '')).lower()
                    if v == 'false' or (prop == 'distance' and v == '7') or (prop == 'persistent' and v == 'true'):
                        del p[prop]
                        mod = True
                if not p: del b['Properties']
        if repack_block_states(bs, pal): mod = True
    return mod

def squeeze_chunk_to_bones(decomp_bytes):
    try:
        s = io.BytesIO(decomp_bytes)
        if s.read(1) != b'\x0a': return decomp_bytes
        s.read(2)
        root = Compound.parse(s, byteorder='big')
        extreme_palette_compaction(root)
        sections = root.get('sections')
        if sections:
            retained = List()
            airs = ['minecraft:air', 'minecraft:void_air', 'minecraft:cave_air']
            for sec in sections:
                bs = sec.get('block_states', {})
                pal = bs.get('palette', [])
                if not all(b.get('Name') in airs for b in pal):
                    for tag in ['BlockLight', 'SkyLight', 'PostProcessing', 'block_ticks', 'fluid_ticks', 'biomes', 'Biomes', 'LightPopulated']:
                        if tag in sec: del sec[tag]
                    if len(pal) == 1 and 'data' in bs: del bs['data']
                    retained.append(sec)
            root['sections'] = retained
        if not root.get('sections'): return None
        for tag in ['structures', 'BlockLight', 'SkyLight', 'Heightmaps', 'block_ticks', 'fluid_ticks', 'entities', 'block_entities', 'PostProcessing', 'CarvingMasks', 'Lights', 'Biomes', 'biomes', 'InhabitedTime', 'LastUpdate', 'Status', 'isLightOn', 'TerrainPopulated', 'LightPopulated', 'HasLightData', 'V', 'v']:
            if tag in root: del root[tag]
        sorted_root = recursive_sort_nbt(root)
        out = io.BytesIO()
        out.write(b'\x0a\x00\x00')
        sorted_root.write(out, byteorder='big')
        return out.getvalue()
    except Exception: return decomp_bytes

def process_mca_to_max(mca_bytes):
    if len(mca_bytes) < 8192: return mca_bytes
    new_locs, buffer = [0] * 1024, bytearray()
    for i in range(1024):
        off = int.from_bytes(mca_bytes[i*4:i*4+3], 'big') * 4096
        if off == 0 or mca_bytes[i*4+3] == 0: continue
        hdr = mca_bytes[off:off+5]
        ln = int.from_bytes(hdr[:4], 'big')
        ctype = hdr[4]
        if ctype not in (1, 2): continue
        payload = mca_bytes[off+5:off+4+ln]
        try:
            decomp = zlib.decompress(payload) if ctype == 2 else gzip.decompress(payload)
            stripped = squeeze_chunk_to_bones(decomp)
            if stripped is None: continue
            new_comp = zopfli_compress(stripped, iterations=5)
            pkt = struct.pack('>I', len(new_comp) + 1) + b'\x02' + new_comp
        except Exception: pkt = struct.pack('>I', ln) + bytes([ctype]) + payload
        curr_off = len(buffer) + 8192
        sec_off, sec_cnt = curr_off // 4096, (len(pkt) + 4095) // 4096
        new_locs[i] = (sec_off << 8) | (sec_cnt & 0xFF)
        buffer.extend(pkt + (b'\x00' * ((sec_cnt * 4096) - len(pkt))))
    res = bytearray(8192 + len(buffer))
    for i in range(1024): res[i*4:i*4+4] = struct.pack('>I', new_locs[i])
    res[8192:] = buffer
    return res

def parallel_asset_worker(name, data):
    ln = name.lower()
    if ln.endswith('.mca'): return name, process_mca_to_max(data)
    if ln.endswith(('.json', '.mcmeta')): return name, minify_json(data)
    if ln.endswith('.toml'): return name, minify_toml(data)
    return name, data

def optimize_and_create_compacted_jar(path):
    if not os.path.exists(path): sys.exit(1)
    out = os.path.join(os.path.dirname(path), f"{os.path.splitext(os.path.basename(path))[0]}_compacted{os.path.splitext(path)[1]}")
    orig_sz = os.path.getsize(path) / (1024*1024)
    tasks = []
    with zipfile.ZipFile(path, 'r') as old:
        for item in old.infolist(): tasks.append((item.filename, old.read(item.filename)))
    final = []
    with concurrent.futures.ProcessPoolExecutor() as ex:
        futs = [ex.submit(parallel_asset_worker, n, d) for n, d in tasks]
        for idx, f in enumerate(concurrent.futures.as_completed(futs)): final.append(f.result())
    try:
        with zipfile.ZipFile(out, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as new:
            for n, d in final: new.writestr(n, d)
        print(f"Done! {orig_sz:.2f}MB -> {os.path.getsize(out)/(1024*1024):.2f}MB")
    except Exception as e:
        if os.path.exists(out): os.remove(out)
        print(f"Failed: {e}"); sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2: sys.exit(1)
    optimize_and_create_compacted_jar(sys.argv[1])
