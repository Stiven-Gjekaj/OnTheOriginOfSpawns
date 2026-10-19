# Make the draft gene .bbmodel files for the zombie.
# Usage: make_zombie_models.py <repository root>
# Each model has the vanilla zombie geometry and one cube set for each gene layer.
# The vanilla texture is a reference with no data. Blockbench asks for the file.
import base64, json, os, sys, uuid

ROOT = sys.argv[1]
TEX_DIR = "src/main/resources/assets/originofspawns/textures/entity/zombie"
OUT_DIR = "models/entity/zombie"

# The layers render in this order. The inflate keeps each layer over the one below.
INFLATE = {
    "zombie_water_resistance": 0.25,
    "zombie_fire_resistance": 0.3,
    "zombie_arrow_resistance": 0.35,
}

# Name, pivot, rotation, from, to, box UV offset, mirror.
BONES = [
    ("head", [0, 24, 0], [0, 0, 0], [-4, 24, -4], [4, 32, 4], [0, 0], False),
    ("body", [0, 24, 0], [0, 0, 0], [-4, 12, -2], [4, 24, 2], [16, 16], False),
    ("right_arm", [-5, 22, 0], [90, 0, 0], [-8, 12, -2], [-4, 24, 2], [40, 16], False),
    ("left_arm", [5, 22, 0], [90, 0, 0], [4, 12, -2], [8, 24, 2], [40, 16], True),
    ("right_leg", [-1.9, 12, 0], [0, 0, 0], [-3.9, 0, -2], [0.1, 12, 2], [0, 16], False),
    ("left_leg", [1.9, 12, 0], [0, 0, 0], [-0.1, 0, -2], [3.9, 12, 2], [0, 16], True),
]


def uid(*parts):
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "originofspawns/" + "/".join(parts)))


def box_faces(frm, to, offset, mirror, tex):
    w, h, d = (round(to[i] - frm[i]) for i in range(3))
    u, v = offset
    f = {
        "north": [u + d, v + d, u + d + w, v + d + h],
        "east": [u, v + d, u + d, v + d + h],
        "south": [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        "west": [u + d + w, v + d, u + 2 * d + w, v + d + h],
        "up": [u + d + w, v + d, u + d, v],
        "down": [u + d + 2 * w, v, u + d + w, v + d],
    }
    if mirror:
        f = {k: [r[2], r[1], r[0], r[3]] for k, r in f.items()}
        f["east"], f["west"] = f["west"], f["east"]
    return {k: {"uv": r, "texture": tex} for k, r in f.items()}


def texture(name, index, source):
    return {
        "name": name, "path": "", "folder": "", "namespace": "", "id": str(index),
        "group": "", "scope": 0, "width": 64, "height": 64, "uv_width": 64,
        "uv_height": 64, "particle": False, "use_as_default": False,
        "layers_enabled": False, "sync_to_project": "", "file_format": "png",
        "render_mode": "default", "render_sides": "auto", "wrap_mode": "limited",
        "pbr_channel": "color", "fps": 7, "force_translucent": False,
        "frame_time": 1, "frame_order_type": "loop", "frame_order": "",
        "frame_interpolate": False, "visible": True, "internal": bool(source),
        "saved": True, "uuid": uid(name, "texture"), "source": source,
        "relative_path": "../../../" + TEX_DIR + "/" + name + ".png" if source else "",
    }


def model(name, layers):
    textures = [texture("vanilla_zombie_reference", 0, "")]
    for i, layer in enumerate(layers, 1):
        with open(os.path.join(ROOT, TEX_DIR, layer + ".png"), "rb") as fh:
            data = "data:image/png;base64," + base64.b64encode(fh.read()).decode()
        textures.append(texture(layer, i, data))
    elements, groups, outliner = [], [], []
    for bone, pivot, rot, frm, to, offset, mirror in BONES:
        children = []
        for i, layer in enumerate([None] + layers):
            cname = bone if layer is None else bone + "_" + layer.replace("zombie_", "")
            e = {
                "name": cname, "box_uv": True, "render_order": "default",
                "locked": False, "export": True, "scope": 0,
                "allow_mirror_modeling": True, "from": frm, "to": to,
                "autouv": 0, "color": i, "origin": pivot, "uv_offset": offset,
                "faces": box_faces(frm, to, offset, mirror, i),
                "type": "cube", "uuid": uid(name, cname),
            }
            if mirror:
                e["mirror_uv"] = True
            if layer is not None:
                e["inflate"] = INFLATE[layer]
            elements.append(e)
            children.append(e["uuid"])
        g = uid(name, bone, "group")
        groups.append({
            "name": bone, "uuid": g, "export": True, "locked": False, "scope": 0,
            "selected": False, "visibility": True, "origin": pivot,
            "rotation": rot, "color": 0, "children": [], "reset": False,
            "shade": True, "mirror_uv": False, "autouv": 0, "isOpen": False,
        })
        outliner.append({"uuid": g, "isOpen": False, "children": children})
    return {
        "meta": {"format_version": "5.0", "model_format": "free", "box_uv": True},
        "name": name, "model_identifier": "", "visible_box": [1, 1, 0],
        "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": 64, "height": 64},
        "elements": elements, "groups": groups, "outliner": outliner,
        "textures": textures,
    }


MODELS = {
    "zombie_fire_resistance": ["zombie_fire_resistance"],
    "zombie_water_resistance": ["zombie_water_resistance"],
    "zombie_arrow_resistance": ["zombie_arrow_resistance"],
    "zombie_combined_preview": [
        "zombie_water_resistance", "zombie_fire_resistance", "zombie_arrow_resistance",
    ],
}

os.makedirs(os.path.join(ROOT, OUT_DIR), exist_ok=True)
for name, layers in MODELS.items():
    path = os.path.join(ROOT, OUT_DIR, name + ".bbmodel")
    with open(path, "w") as fh:
        json.dump(model(name, layers), fh, indent="\t")
        fh.write("\n")
    print(path)
