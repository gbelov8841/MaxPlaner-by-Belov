"""Emit small previews of this test app's screenshots for connector-only visual review.

Full resolution originals remain in planner-ui-evidence. No device/app data or
screenshots outside this test output directory are read.
"""
import base64
import io
from pathlib import Path
from PIL import Image

for path in sorted(Path('ui-screenshots').glob('*.png')):
    if not path.stem.endswith(('-home', '-day')):
        continue
    image = Image.open(path).convert('RGB')
    image.thumbnail((480, 1100))
    buffer = io.BytesIO()
    image.save(buffer, 'WEBP', quality=85)
    print('PLANNER_SCREENSHOT ' + path.stem + ' ' + base64.b64encode(buffer.getvalue()).decode())
