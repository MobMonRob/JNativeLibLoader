#!/bin/bash

scriptPath="$(realpath -s "${BASH_SOURCE[0]}")"
scriptDir="$(dirname "$scriptPath")"
cd "$scriptDir"

source "./_bash_config.sh"

run() {
	ksc -t java --java-package de.dhbw.rahmlab.nativelibloader.impl.dependencies ./microsoft_pe.ksy

	ksc -t java --java-package de.dhbw.rahmlab.nativelibloader.impl.dependencies ./elf.ksy

	local -r packageDir="de/dhbw/rahmlab/nativelibloader/impl/dependencies"
	local -r targetDir="../NativeLibLoader/src/main/java/$packageDir"

	cp -L -l -f "$packageDir/MicrosoftPe.java" "$targetDir"
	cp -L -l -f "$packageDir/Elf.java" "$targetDir"

	rm -rdf ./de
}

run_bash run $@

