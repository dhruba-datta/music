#!/usr/bin/env bash
# Preview or merge the latest official Echo Music release into this branch.
#   scripts/official-update.sh          show what is new, change nothing
#   scripts/official-update.sh --apply  merge it locally; `git push` then releases it
set -euo pipefail

official="https://github.com/EchoMusicApp/Echo-Music"
tag=$(git ls-remote --tags --refs --sort=-v:refname "$official" 'v*' | head -n 1 | sed 's#.*refs/tags/##')
git fetch --quiet "$official" "refs/tags/$tag:refs/tags/official-$tag"

if git merge-base --is-ancestor "official-$tag" HEAD; then
  echo "Already up to date with official $tag."
  exit 0
fi

echo "Official $tag has these changes you don't have:"
git log --oneline --no-merges "HEAD..official-$tag"

if [ "${1:-}" != "--apply" ]; then
  echo
  echo "Nothing changed. Run with --apply to merge them."
  exit 0
fi

if ! git merge --no-edit "official-$tag"; then
  # The mod deliberately has no Firebase config, so that one conflict always resolves the same way.
  git rm --quiet --ignore-unmatch app/google-services.json
  if [ -n "$(git diff --name-only --diff-filter=U)" ]; then
    echo
    echo "Fix these conflicts, then run 'git commit':"
    git diff --name-only --diff-filter=U
    exit 1
  fi
  git commit --no-edit
fi
git rm --quiet --ignore-unmatch app/google-services.json
git diff --cached --quiet || git commit --quiet -m "chore: keep official Firebase config out of the mod"

echo
echo "Merged official $tag. Run 'git push' to build it and send it to your phone."
