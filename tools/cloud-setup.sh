#!/usr/bin/env bash
set -euo pipefail

# Use the already isolated cloud checkout. No extra worktree is needed.
toolchain_root=/workspace/toolchains
jdk_dir="$toolchain_root/jdk-21.0.13+9"
if [[ ! -x "$jdk_dir/bin/javac" ]]; then
  mkdir -p "$toolchain_root"
  archive=$(mktemp /tmp/spell-power-jdk.XXXXXX.tar.gz)
  trap 'rm -f "$archive"' EXIT
  curl --fail --location --retry 2 \
    'https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/OpenJDK21U-jdk_x64_linux_hotspot_21.0.13_9.tar.gz' \
    --output "$archive"
  printf '%s  %s\n' 'd7f5d5cf865a2902b70ff549b97586a7d3f234e016f041ab447ea740c7d66d1c' "$archive" | sha256sum --check
  tar -xzf "$archive" -C "$toolchain_root"
fi

# Java clients need explicit proxy selection. Copy host/port only, never credentials.
python3 - <<'PY'
import os, pathlib, shlex, urllib.parse
proxy = urllib.parse.urlsplit(os.environ.get('HTTPS_PROXY') or os.environ.get('https_proxy', ''))
options = []
trust = pathlib.Path('/etc/ssl/certs/java/cacerts')
if trust.exists():
    options.append('-Djavax.net.ssl.trustStore=' + str(trust))
if proxy.hostname:
    for protocol in ('https', 'http'):
        options.extend([f'-D{protocol}.proxyHost={proxy.hostname}', f'-D{protocol}.proxyPort={proxy.port or 80}'])
    options.append('-Dhttp.nonProxyHosts=localhost|127.*')
lines = [
    "export JAVA_HOME='/workspace/toolchains/jdk-21.0.13+9'",
    'export PATH="$JAVA_HOME/bin:$PATH"',
    "export GRADLE_USER_HOME='/workspace/.gradle'",
    'export JAVA_TOOL_OPTIONS=' + shlex.quote(' '.join(options)),
]
pathlib.Path('/workspace/cloud-java-env.sh').write_text('\n'.join(lines) + '\n')
PY
source /workspace/cloud-java-env.sh
"$JAVA_HOME/bin/javac" -version
