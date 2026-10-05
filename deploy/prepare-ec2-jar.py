"""Prepare the tested JAR for EC2 without bundling local credentials."""
from pathlib import Path
import zipfile

root = Path(__file__).resolve().parent.parent
source = root / "target/Smartspend-backend-0.0.1-SNAPSHOT.jar"
target = source.with_name("Smartspend-backend-0.0.1-SNAPSHOT-ec2.jar")
overrides = {
    "spring.datasource.password": "${DB_PASSWORD:}",
    "spring.mail.username": "${MAIL_USERNAME:}",
    "spring.mail.password": "${MAIL_PASSWORD:}",
}
with zipfile.ZipFile(source) as original, zipfile.ZipFile(target, "w", zipfile.ZIP_DEFLATED) as release:
    for entry in original.infolist():
        contents = original.read(entry.filename)
        if entry.filename == "BOOT-INF/classes/application.properties":
            lines = []
            for line in contents.decode().splitlines():
                key = line.split("=", 1)[0].strip()
                lines.append(key + "=" + overrides[key] if key in overrides else line)
            contents = ("\n".join(lines) + "\n").encode()
        release.writestr(entry, contents)
print(target)
