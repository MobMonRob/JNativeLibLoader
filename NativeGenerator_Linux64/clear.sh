#!/bin/bash

cd "$(dirname "$BASH_SOURCE")"

#remove untracked AND ignored files and folders
git clean -d -f -X &> /dev/null

#remove ignored tracked files and folders
git ls-files -i --exclude-standard --directory -z| xargs -0 rm -r &> /dev/null

echo "NativeGenerator_Linux64 cleared"

