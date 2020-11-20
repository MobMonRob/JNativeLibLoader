#!/bin/bash

cd $(dirname "$(readlink -f "${BASH_SOURCE[0]}")")

#remove untracked AND ignored files and folders
git clean -d -ff -X &> /dev/null

#remove ignored tracked files and folders
git ls-files -i --exclude-standard --directory -z| xargs -0 rm -r &> /dev/null

echo "JNativeLibLoader cleared"
