"""Build the static folder used by the existing Netlify project."""
from pathlib import Path
import shutil

root = Path(__file__).resolve().parent.parent
source = root / "src/main/resources/static"
target = root / "netlify-frontend"
for file in source.rglob("*"):
    if file.is_file() and file.name != ".DS_Store":
        destination = target / file.relative_to(source)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(file, destination)
shutil.copy2(target / "login.html", target / "index.html")
shutil.copy2(root / "deploy/netlify/_redirects", target / "_redirects")
print(target)
