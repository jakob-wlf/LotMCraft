import os
import sys
import struct
import zlib
import zipfile
import concurrent.futures
import io
import math
import json
import re
from nbtlib.tag import Compound, List, LongArray, IntArray

try:
    import zopfli
except ImportError:
    print("Error: The 'zopflipy' library is missing from this environment path.")
    print("Please run: python3 -m pip install --user zopflipy")
    sys.exit(1)

def minify_json(json_bytes):
    try:
        data = json.loads(json_bytes.decode('utf-8'))
        return json.dumps(data, separators=(',', ':')).encode('utf-8')
    except Exception:
        return json_bytes

def minify_toml(toml_bytes):
    try:
        lines = toml_bytes.decode('utf-8').splitlines()
        clean_lines = []
        for line in lines:
            line = line.strip()
            if not line or line.startswith('#'):
                continue
            if '#' in line:
                if not any(q in line.split('#')[0] for q in ['"', "'"]):
                    line = line.split('#')[0].strip()
            clean_lines.append(line)
        return '\n'.join(clean_lines).encode('utf-8')
    except Exception:
        return toml_bytes

def repack_block_states(block_states, palette):
    """Unpacks old data arrays and tightly packs them into optimal bit-widths."""
    old_data = block_states.get('data')
    if not old_data:
        return False

    palette_size = len(palette)
    if palette_size <= 1:
        del block_states['data']
        return True

    new_bits = max(4, math.ceil(math.log2(palette_size)))
    blocks_per_long = 64 // new_bits
    expected_longs_count = math.ceil(4096 / blocks_per_long)

    old_longs_count = len(old_data)
    old_blocks_per_long = 4096 // old_longs_count if old_longs_count > 0 else 1
    old_bits = 64 // old_blocks_per_long if old_blocks_per_long > 0 else 4

    indices = []
    for long_val in old_data:
        unsigned_long = long_val if long_val >= 0 else (long_val + (1 << 64))
        for _ in range(old_blocks_per_long):
            if len(indices) < 4096:
                indices.append(unsigned_long & ((1 << old_bits) - 1))
                unsigned_long >>= old_bits

    while len(indices) < 4096:
        indices.append(0)

    new_longs = []
    current_long = 0
    bit_offset = 0

    for index in indices:
        if index >= palette_size:
            index = 0

        current_long |= (index & ((1 << new_bits) - 1)) << bit_offset
        bit_offset += new_bits

        if bit_offset + new_bits > 64:
            signed_long = current_long if current_long < (1 << 63) else (current_long - (1 << 64))
            new_longs.append(signed_long)
            current_long = 0
            bit_offset = 0

    if bit_offset > 0:
        signed_long = current_long if current_long < (1 << 63) else (current_long - (1 << 64))
        new_longs.append(signed_long)

    while len(new_longs) < expected_longs_count:
        new_longs.append(0)

    block_states['data'] = LongArray(new_longs[:expected_longs_count])
    return True

def extreme_palette_compaction(root):
    """Strips default block state property strings and compresses array layouts."""
    sections = root.get('sections')
    if not sections:
        return False

    chunk_modified = False
    for section in sections:
        block_states = section.get('block_states')
        if not block_states:
            continue

        palette = block_states.get('palette')
        if not palette:
            continue

        for block in palette:
            if 'Properties' in block:
                properties = block['Properties']
                defaults_to_drop = ['waterlogged', 'powered', 'lit', 'snowy', 'disarmed']
                for prop in defaults_to_drop:
                    if prop in properties and str(properties[prop]).lower() == 'false':
                        del properties[prop]
                        chunk_modified = True
                if len(properties) == 0:
                    del block['Properties']

        if repack_block_states(block_states, palette):
            chunk_modified = True

    return chunk_modified

def squeeze_chunk_to_bones(decompressed_bytes):
    """Deletes complete empty void layers and wipes structural overhead elements."""
    try:
        stream = io.BytesIO(decompressed_bytes)
        tag_type = stream.read(1)
        if tag_type != b'\x0a':
            return decompressed_bytes

        stream.read(2)
        root = Compound.parse(stream, byteorder='big')

        # Run properties/palette compaction
        extreme_palette_compaction(root)

        sections = root.get('sections')

        # ADVANCED: Completely delete entirely empty air/void sections from the NBT array tree
        if sections:
            retained_sections = List()
            for section in sections:
                block_states = section.get('block_states', {})
                palette = block_states.get('palette', [])

                is_pure_void = True
                for block in palette:
                    if block.get('Name') != 'minecraft:air':
                        is_pure_void = False
                        break

                # Keep the layer only if it contains physical custom structures/blocks
                if not is_pure_void:
                    # Wipe secondary layer overhead parameters safely
                    sub_tags_to_wipe = ['BlockLight', 'SkyLight', 'PostProcessing', 'block_ticks', 'fluid_ticks', 'biomes', 'Biomes']
                    for sub_tag in sub_tags_to_wipe:
                        if sub_tag in section: del section[sub_tag]

                    # Drop index array references on uniform single-block fill layers
                    if len(palette) == 1 and 'data' in block_states:
                        del block_states['data']

                    retained_sections.append(section)

            root['sections'] = retained_sections

        # If the whole chunk contains nothing but void sections, drop it safely
        if not root.get('sections') or len(root['sections']) == 0:
            return None

        # Wipe map file tracking layers globally (Biomes dropped completely)
        bulky_tags = [
            'structures', 'BlockLight', 'SkyLight', 'Heightmaps',
            'block_ticks', 'fluid_ticks', 'entities', 'block_entities',
            'PostProcessing', 'CarvingMasks', 'Lights', 'Biomes', 'biomes'
        ]
        for tag in bulky_tags:
            if tag in root: del root[tag]

        # Order keys alphabetically to optimize dictionary matching profiles for LZ77 passes
        sorted_root = Compound({k: root[k] for k in sorted(root.keys())})

        out_stream = io.BytesIO()
        out_stream.write(b'\x0a\x00\x00')
        sorted_root.write(out_stream, byteorder='big')
        return out_stream.getvalue()
    except Exception:
        return decompressed_bytes

def process_mca_to_max(mca_bytes):
    mca_data = bytearray(mca_bytes)
    if len(mca_data) < 8192: return mca_data

    new_locations = [0] * 1024
    new_timestamps = [0] * 1024
    chunk_data_buffer = bytearray()

    for i in range(1024):
        ts_offset = 4096 + (i * 4)
        new_timestamps[i] = struct.unpack('>I', mca_data[ts_offset:ts_offset+4])[0]

    for i in range(1024):
        offset_bytes = mca_data[i*4 : i*4 + 4]
        offset = int.from_bytes(offset_bytes[:3], byteorder='big') * 4096
        if offset == 0 or int.from_bytes(offset_bytes[3:], byteorder='big') == 0: continue

        chunk_header = mca_data[offset : offset + 5]
        length = int.from_bytes(chunk_header[:4], byteorder='big')
        compression_type = chunk_header[4]
        if compression_type not in (1, 2): continue

        compressed_payload = mca_data[offset + 5 : offset + 4 + length]

        try:
            if compression_type == 2:
                decompressed = zlib.decompress(compressed_payload)
            else:
                import gzip
                decompressed = gzip.decompress(compressed_payload)

            stripped_decompressed = squeeze_chunk_to_bones(decompressed)
            if stripped_decompressed is None: continue

            # ADVANCED: Use Zopfli to squeeze individual chunk components down before sector quantization
            # This causes massive file savings by moving chunks below the 4KB boundary lines
            compressor = zopfli.ZopfliCompressor(zopfli.ZOPFLI_FORMAT_DEFLATE, iterations=5)
            new_compressed = compressor.compress(stripped_decompressed) + compressor.flush()

            new_length = len(new_compressed) + 1
            chunk_packet = struct.pack('>I', new_length) + bytes([compression_type]) + new_compressed
        except Exception:
            chunk_packet = struct.pack('>I', length) + bytes([compression_type]) + compressed_payload

        current_offset = len(chunk_data_buffer) + 8192
        new_sector_offset = current_offset // 4096
        new_sector_count = (len(chunk_packet) + 4095) // 4096

        new_locations[i] = (new_sector_offset << 8) | (new_sector_count & 0xFF)
        padding = (new_sector_count * 4096) - len(chunk_packet)
        chunk_data_buffer.extend(chunk_packet + (b'\x00' * padding))

    final_mca = bytearray(8192 + len(chunk_data_buffer))
    for i in range(1024):
        final_mca[i*4 : i*4 + 4] = struct.pack('>I', new_locations[i])
        final_mca[4096 + (i*4) : 4100 + (i*4)] = struct.pack('>I', new_timestamps[i])
    final_mca[8192:] = chunk_data_buffer
    return final_mca

def parallel_asset_worker(filename, file_bytes):
    crc_checksum = zlib.crc32(file_bytes) & 0xffffffff
    if filename.endswith('/') or len(file_bytes) == 0:
        return filename, file_bytes, len(file_bytes), crc_checksum, False

    lower_name = filename.lower()

    if lower_name.endswith('.mca'):
        optimized_mca_bytes = process_mca_to_max(file_bytes)
        # Brute-force the finalized overall MCA containment system
        compressor = zopfli.ZopfliCompressor(zopfli.ZOPFLI_FORMAT_DEFLATE, iterations=25)
        raw_deflate_bytes = compressor.compress(optimized_mca_bytes) + compressor.flush()
        mca_crc = zlib.crc32(optimized_mca_bytes) & 0xffffffff
        return filename, raw_deflate_bytes, len(optimized_mca_bytes), mca_crc, True

    elif lower_name.endswith('.json') or lower_name.endswith('.mcmeta'):
        payload = minify_json(file_bytes)
    elif lower_name.endswith('.toml'):
        payload = minify_toml(file_bytes)
    elif lower_name.endswith('.png'):
        compressor = zlib.compressobj(level=9, method=zlib.DEFLATED, wbits=-15, strategy=zlib.Z_HUFFMAN_ONLY)
        raw_deflate_bytes = compressor.compress(file_bytes) + compressor.flush()
        return filename, raw_deflate_bytes, len(file_bytes), crc_checksum, True
    else:
        payload = file_bytes

    compressor = zlib.compressobj(level=9, method=zlib.DEFLATED, wbits=-15)
    fast_deflate_bytes = compressor.compress(payload) + compressor.flush()
    payload_crc = zlib.crc32(payload) & 0xffffffff
    return filename, fast_deflate_bytes, len(payload), payload_crc, True

def optimize_and_create_compacted_jar(target_jar_path):
    if not os.path.exists(target_jar_path):
        print(f"Error: Target file '{target_jar_path}' not found.")
        sys.exit(1)

    base_dir, full_filename = os.path.split(target_jar_path)
    filename, ext = os.path.splitext(full_filename)
    output_jar_path = os.path.join(base_dir, f"{filename}_compacted{ext}")
    orig_sz = os.path.getsize(target_jar_path) / (1024 * 1024)
    tasks = []

    with zipfile.ZipFile(target_jar_path, 'r') as old_jar:
        for item in old_jar.infolist():
            tasks.append((item.filename, old_jar.read(item.filename)))

    print(f"Analyzing and optimizing {len(tasks)} NeoForge package resource channels...")
    final_packaged_manifest = []

    with concurrent.futures.ProcessPoolExecutor() as process_executor:
        futures = [process_executor.submit(parallel_asset_worker, name, data) for name, data in tasks]
        total_assets = len(futures)
        for idx, future in enumerate(concurrent.futures.as_completed(futures)):
            name, data_bytes, original_len, crc_checksum, was_compressed = future.result()
            final_packaged_manifest.append((name, data_bytes, original_len, crc_checksum, was_compressed))
            if (idx + 1) % 500 == 0 or idx + 1 == total_assets:
                print(f"[{idx+1}/{total_assets}] Compressed and minified mod assets...")

    print(f"Assembling optimized NeoForge-ready archive at: {output_jar_path}\n" + "-"*50)
    try:
        with zipfile.ZipFile(output_jar_path, 'w', zipfile.ZIP_STORED) as new_jar:
            for name, data_bytes, original_len, crc_checksum, was_compressed in final_packaged_manifest:
                zinfo = zipfile.ZipInfo(name)
                if was_compressed:
                    zinfo.compress_type = zipfile.ZIP_DEFLATED
                    zinfo.compress_size = len(data_bytes)
                else:
                    zinfo.compress_type = zipfile.ZIP_STORED
                    zinfo.compress_size = original_len
                zinfo.file_size = original_len
                zinfo.CRC = crc_checksum
                new_jar.writestr(zinfo, data_bytes)

        new_sz = os.path.getsize(output_jar_path) / (1024 * 1024)
        print("\n" + "="*50)
        print(f"Successfully generated ultimate NeoForge-compacted JAR file!")
        print(f"Original JAR Size:   {orig_sz:.2f} MB")
        print(f"Compacted JAR Size:  {new_sz:.2f} MB (Saved {(orig_sz - new_sz):.2f} MB)")
        print("="*50)
    except Exception as e:
        if os.path.exists(output_jar_path):
            os.remove(output_jar_path)
        print(f"Packaging failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python compress_jar.py <path_to_jar_file>")
        sys.exit(1)
    optimize_and_create_compacted_jar(sys.argv[1])

