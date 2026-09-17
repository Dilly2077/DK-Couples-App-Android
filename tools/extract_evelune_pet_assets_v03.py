#!/usr/bin/env python3
"""Extract the verified Evelune v0.3 runtime asset archive into Android resources.

Expected archive path:
  design/pets/artifacts/evelune_pet_assets_v03_runtime.zip

Expected SHA-256:
  9459fbe279812513a1072e72f4dce24cb73aedadecf7e02fb4dfefe5375f4309
"""

from __future__ import annotations

import hashlib
import shutil
import sys
import zipfile
from pathlib import Path

EXPECTED_SHA256 = "9459fbe279812513a1072e72f4dce24cb73aedadecf7e02fb4dfefe5375f4309"
ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / "design" / "pets" / "artifacts" / "evelune_pet_assets_v03_runtime.zip"
DEST = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi" / "pets_v03"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> int:
    if not ARCHIVE.exists():
        print(f"Missing archive: {ARCHIVE}", file=sys.stderr)
        return 2

    actual = sha256(ARCHIVE)
    if actual != EXPECTED_SHA256:
        print("Evelune asset archive SHA-256 mismatch.", file=sys.stderr)
        print(f"Expected: {EXPECTED_SHA256}", file=sys.stderr)
        print(f"Actual:   {actual}", file=sys.stderr)
        return 3

    if DEST.exists():
        shutil.rmtree(DEST)
    DEST.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(ARCHIVE, "r") as archive:
        members = [m for m in archive.infolist() if not m.is_dir()]
        for member in members:
            target = DEST / Path(member.filename).name
            with archive.open(member, "r") as source, target.open("wb") as output:
                shutil.copyfileobj(source, output)

    print(f"Extracted {len(members)} Evelune v0.3 assets to {DEST}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
