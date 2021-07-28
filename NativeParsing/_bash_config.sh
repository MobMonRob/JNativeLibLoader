#!/bin/bash


# Needed Boilerplate to use this bash config in a script:
# scriptPath="$(realpath -s "${BASH_SOURCE[0]}")"
# source "./_bash_config.sh"
# run() {
# # <Your code>
# }
# run_bash run $@


# PUBLIC
# Last command
##############################
run_bash() {
	exportIsolation "$@"
}


# PUBLIC
# Execute within run
##############################
setSuccessToken() {
	cd "$unlinkedOwnDirPath"

	mkdir -p "$currentTmp"
	local -r succesToken="$currentTmp/successToken"
	echo "$(date --rfc-3339=seconds)" > "$succesToken"

	cd "$scriptDir"
}


# Usage: if [[ "$(isSuccessTokenSet)" == "false" ]]; then
isSuccessTokenSet() {
	cd "$unlinkedOwnDirPath"

	local -r succesToken="$currentTmp/successToken"
	if [[ -e "$succesToken" ]]; then
		echo "true"
	else
		echo "false"
	fi

	cd "$scriptDir"
}


# To use this function in a project, remove "readonly" declarations of variables in _project_config.sh to avoid errors.
# A project is a directory which contains _bash_config.sh, _platform_config.sh and _project_config.sh.
# Invoked other projects won't be affected.
changePlatformTo() {
	newPlatform="$1"

	if [[ "$currentPlatform" != "$newPlatform" ]]; then
		setCurrentPlatform "$newPlatform"
		source "$unlinkedOwnDirPath/_project_config.sh"
	fi
}


clearCurrentPlatform() {
	cd "$unlinkedOwnDirPath"

	rm -rdf "$currentTarget"
	mkdir -p "$currentTarget"

	rm -rdf "$currentTmp"
	mkdir -p "$currentTmp"

	cd "$scriptDir"
}


# PRIVATE
# Executed within run
##############################
exportIsolation() {
	# this implements export isolation.
	# exports within the withinExportIsolation function won't be
	# exported to this outer environment.
	# exports will only passed to invoked scripts.
	withinExportIsolation "$@" &
	my_pid=$!
	wait $my_pid
}


withinExportIsolation() {
	loadConfig
	loggedRunner "$@"
}


loadConfig() {
	source "$unlinkedOwnDirPath/_platform_config.sh"
	source "$unlinkedOwnDirPath/_project_config.sh"
}


loggedRunner() {
	if [[ -z ${level+x} ]]; then
		level="1"
		export level
	fi

	echo "---started($level): $relativeScriptPath"

	((level+=1))
	export level

	local -r the_run="$@"
	$the_run

	((level-=1))

	echo "--finished($level): $relativeScriptPath"
}


# PRIVATE
# Used while sourcing
##############################
setupVariables() {
	local -r ownFullPath="$(realpath -s "${BASH_SOURCE[0]}")"
	local -r unlinkedOwnFullPath="$(readlink -f "$ownFullPath")"
	readonly unlinkedOwnDirPath="$(dirname "$unlinkedOwnFullPath")"
}


setRelativeScriptPath() {
	if [[ -z ${firstInvokedSkriptPath+x} ]]; then
		#"CurrentPlatform is unset"
		firstInvokedSkriptPath="$scriptDir"
		# export is needed to pass variable to invoked skripts
		export firstInvokedSkriptPath
		echo "Info: set and exported firstInvokedSkriptPath=$firstInvokedSkriptPath"
	fi

	local -r scriptName="$(basename "$scriptPath")"

	if [[ "$firstInvokedSkriptPath" == "$scriptDir" ]]; then
		local -r relativeFullPath="./$scriptName"
	else
		local -r relativeDirPath="$(realpath --relative-to="$firstInvokedSkriptPath" "$scriptDir")"
		local -r relativeFullPath="./$relativeDirPath/$scriptName"
	fi

	readonly relativeScriptPath="$relativeFullPath"
}


setupErrorHandling() {
	#add -x for debugging
	set -Eeuo pipefail errexit
	trap on_err ERR INT TERM
}


on_err() {
	errorCode="$?"
	magicNum="22"

	if [[ "$errorCode" -ne "$magicNum" ]] ;then
		echo -e "\nError occurred in: $relativeScriptPath" >&2
	fi

	exit "$magicNum"
}


# PRIVATE
# Executed while sourcing
##############################
whileSourcing() {
	setupVariables
	setRelativeScriptPath
	setupErrorHandling
}

whileSourcing

