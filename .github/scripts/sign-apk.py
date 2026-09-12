"""Sign distributable APKs with the owner's pinned key; never fall back to debug signing."""
import base64
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

bundle = os.environ.get('PRIMEPLANER_SIGNING_BUNDLE')
if not bundle:
    raise SystemExit('Set repository secret PRIMEPLANER_SIGNING_BUNDLE before distributing APKs.')
settings = json.loads(bundle)
expected = Path('config/signing-certificate.sha256').read_text().strip().lower()
sdk = Path(os.environ.get('ANDROID_HOME') or os.environ['ANDROID_SDK_ROOT'])
candidates = list((sdk / 'build-tools').glob('*/apksigner'))
if not candidates:
    raise SystemExit('Android build-tools/apksigner is required.')
signer = str(sorted(candidates, key=lambda p: tuple(int(n) for n in re.findall(r'\d+', p.parent.name)))[-1])
output = Path('distribution/PrimePlaner-0.2.1.apk')
output.parent.mkdir(exist_ok=True)
with tempfile.TemporaryDirectory() as directory:
    key = Path(directory) / 'signing.p12'
    key.write_bytes(base64.b64decode(settings['keystore_base64'], validate=True))
    key.chmod(0o600)
    env = os.environ.copy()
    env['PRIME_KEY_PASSWORD'] = settings['password']
    # Refuse another key before creating an apparently installable deliverable.
    certificate = subprocess.check_output(['keytool', '-exportcert', '-keystore', str(key),
        '-storepass:env', 'PRIME_KEY_PASSWORD', '-alias', settings['alias']], env=env, stderr=subprocess.DEVNULL)
    if hashlib.sha256(certificate).hexdigest() != expected:
        raise SystemExit('Signing certificate differs from the pinned owner certificate.')
    subprocess.run([signer, 'sign', '--ks', str(key), '--ks-key-alias', settings['alias'],
        '--ks-pass', 'env:PRIME_KEY_PASSWORD', '--key-pass', 'env:PRIME_KEY_PASSWORD',
        '--out', str(output), 'app/build/outputs/apk/debug/app-debug.apk'], env=env, check=True)
    result = subprocess.check_output([signer, 'verify', '--verbose', '--print-certs', str(output)], text=True)
    if expected not in result.lower():
        output.unlink(missing_ok=True)
        raise SystemExit('Signed APK certificate verification failed.')
    print(result)
checksum = hashlib.sha256(output.read_bytes()).hexdigest()
output.with_suffix('.apk.sha256').write_text(checksum + '  ' + output.name + '\n')
print('Verified distributable APK:', output)
