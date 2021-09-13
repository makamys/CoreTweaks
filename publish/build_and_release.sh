if [ ! -s changelog.md ]; then
    echo "Changelog is empty, refusing to publish."
    exit 3
fi

if [[ $(git diff --cached --stat) != '' ]]
then
	echo "There are staged uncommitted changes, refusing to publish."
	exit 2
fi

if [ "$#" -lt 2 ] || [ "$#" -gt 3 ]
then
	echo "Usage: $0 GITHUB_TOKEN CURSEFORGE_TOKEN [MODRINTH_TOKEN]"
	exit 1
fi

GITHUB_TOKEN=$1
CURSEFORGE_TOKEN=$2
MODRINTH_TOKEN=$3

# build the release
./gradlew cleanBuildAll

# release
py prepare_publish.py
./gradlew githubRelease -PgithubToken=$GITHUB_TOKEN
py update_updatejson.py
./curseforge_all.sh -PcurseToken=$CURSEFORGE_TOKEN
/dev/null > changelog.md

if [ -n "$MODRINTH_KEY" ]
then
	./modrinth_all.sh -PmodrinthToken=$MODRINTH_TOKEN
fi