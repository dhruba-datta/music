#!/usr/bin/env python3
"""Rename the visible Echo Music brand to Music at build time.

This runs on the build's working tree and is never committed, so official
updates to strings and translations cannot conflict with the rename.
"""
import re
import subprocess
import sys
from pathlib import Path

NEW_NAME = "Music"

# Links, handles and code identifiers must keep their original spelling.
BEFORE = r"(?<![\w/.@-])"
AFTER = r"(?![\w-]|\.\w)"
FULL_NAME = re.compile(rf"{BEFORE}(?:echo ?music)s?{AFTER}", re.IGNORECASE)
SHORT_NAME = re.compile(rf"{BEFORE}Echo{AFTER}")

ELEMENT = re.compile(r"(<(string|item)\b[^>]*>)(.*?)(</\2>)", re.DOTALL)
NAME = re.compile(r'\bname="([^"]+)"')

# Crediting the original author is kept on purpose.
KEEP_STRINGS = {"developer_name", "wrapped_special_thanks"}

# Only user-facing literals. The Discord, ListenBrainz and AI playlist names are left out because
# they identify the client or match data that is already saved.
KOTLIN_FILES = [
    "app/src/main/kotlin/com/music/echo/MainActivity.kt",
    "app/src/main/kotlin/com/music/echo/ui/menu/QueueMenu.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/WelcomeDialog.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/settings/AboutScreen.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/settings/AppIconSettingsScreen.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/settings/DiscordExperimental.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/settings/DiscordSettings.kt",
    "app/src/main/kotlin/com/music/echo/ui/screens/settings/SearchableSettings.kt",
    "app/src/main/kotlin/com/music/echo/utils/CrashHandler.kt",
]
KOTLIN_LITERAL = re.compile(r'"(?:[^"\\\n]|\\.)*"')


def rename(text):
    return SHORT_NAME.sub(NEW_NAME, FULL_NAME.sub(NEW_NAME, text))


def rename_element(match):
    opening, _, body, closing = match.groups()
    name = NAME.search(opening)
    if name and name.group(1) in KEEP_STRINGS:
        return match.group(0)
    return opening + rename(body) + closing


def rewrite(path, transform):
    original = path.read_text(encoding="utf-8")
    updated = transform(original)
    if updated == original:
        return False
    path.write_text(updated, encoding="utf-8")
    return True


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    tracked = subprocess.check_output(["git", "-C", str(root), "ls-files", "*.xml"], text=True)
    resources = [
        root / line
        for line in tracked.splitlines()
        if re.search(r"/res/values(-[^/]+)?/[^/]+\.xml$", line)
    ]
    changed = sum(rewrite(p, lambda t: ELEMENT.sub(rename_element, t)) for p in resources)
    changed += sum(
        rewrite(
            root / file,
            lambda t: KOTLIN_LITERAL.sub(lambda m: rename(m.group(0)), t),
        )
        for file in KOTLIN_FILES
        if (root / file).exists()
    )
    print(f"Rebranded {changed} files")


if __name__ == "__main__":
    main()
