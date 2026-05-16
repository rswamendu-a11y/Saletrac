import zipfile
import re

found_cert = False
with zipfile.ZipFile('app/build/outputs/apk/debug/app-debug.apk', 'r') as z:
    for f in z.namelist():
        if f == 'META-INF/CERT.RSA' or f == 'META-INF/BNDLTOOL.RSA':
            found_cert = True
print("Has V1 cert file:", found_cert)
