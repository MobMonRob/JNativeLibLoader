#!/bin/bash

scriptPath="$(realpath -s "${BASH_SOURCE[0]}")"
scriptDir="$(dirname "$scriptPath")"
cd "$scriptDir"

run() {
	ksc -t java --java-package de.dhbw.rahmlab.nativelibloader.impl.dependencies ./microsoft_pe.ksy

	local -r packageDir="de/dhbw/rahmlab/nativelibloader/impl/dependencies"
	local -r targetDir="../NativeLibLoader/src/main/java/$packageDir"

	cp -L -l -f "$packageDir/MicrosoftPe.java" "$targetDir"

	rm -rdf ./de
}

run $@
